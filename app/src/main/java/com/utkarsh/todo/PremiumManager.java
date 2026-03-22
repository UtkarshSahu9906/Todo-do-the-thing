package com.utkarsh.todo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles Google Play subscription for the "premium_monthly" product.
 *
 * SETUP: In Google Play Console → Monetize → Subscriptions
 *   Product ID: premium_monthly
 *   Billing period: monthly
 */
public class PremiumManager implements PurchasesUpdatedListener {

    private static final String TAG        = "PremiumManager";
    private static final String PRODUCT_ID = "premium_monthly";    // ← your Play Console product ID
    private static final String PREFS_NAME = "todo_prefs";
    private static final String KEY_PREMIUM = "is_premium";

    public interface PremiumListener {
        void onPremiumStatusChanged(boolean isPremium);
    }

    private final BillingClient   billingClient;
    private final SharedPreferences prefs;
    private ProductDetails         productDetails;
    private PremiumListener        listener;
    private final Activity         activity;

    public PremiumManager(Activity activity) {
        this.activity = activity;
        prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        billingClient = BillingClient.newBuilder(activity)
                .setListener(this)
                .enablePendingPurchases(com.android.billingclient.api.PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .build();
    }

    public void setListener(PremiumListener l) { this.listener = l; }

    public boolean isPremiumCached() {
        return prefs.getBoolean(KEY_PREMIUM, false);
    }

    public void connect() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult result) {
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryExistingPurchases();
                    fetchProductDetails();
                }
            }
            @Override public void onBillingServiceDisconnected() {}
        });
    }

    public void disconnect() {
        if (billingClient.isReady()) billingClient.endConnection();
    }

    /** Show the Play purchase sheet. Call this from your "Go Premium" button. */
    public void launchPurchaseFlow() {
        if (productDetails == null) { connect(); return; }

        List<BillingFlowParams.ProductDetailsParams> params = new ArrayList<>();
        params.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                .build());

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(params)
                .build();

        billingClient.launchBillingFlow(activity, flowParams);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult result, List<Purchase> purchases) {
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase p : purchases) handlePurchase(p);
        } else if (result.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled");
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(ackParams, r -> {});
        }
        boolean active = purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED;
        setPremiumStatus(active);
    }

    private void queryExistingPurchases() {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (result, list) -> {
                    boolean hasSub = false;
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase p : list) {
                            if (p.getProducts().contains(PRODUCT_ID)
                                    && p.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                hasSub = true;
                            }
                        }
                    }
                    setPremiumStatus(hasSub);
                });
    }

    private void fetchProductDetails() {
        List<QueryProductDetailsParams.Product> products = new ArrayList<>();
        products.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build());

        billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(products).build(),
                (result, queryResult) -> {
                    if (queryResult != null && !queryResult.getProductDetailsList().isEmpty())
                        productDetails = queryResult.getProductDetailsList().get(0);
                });
    }

    private void setPremiumStatus(boolean premium) {
        prefs.edit().putBoolean(KEY_PREMIUM, premium).apply();
        activity.runOnUiThread(() -> {
            if (listener != null) listener.onPremiumStatusChanged(premium);
        });
    }
}