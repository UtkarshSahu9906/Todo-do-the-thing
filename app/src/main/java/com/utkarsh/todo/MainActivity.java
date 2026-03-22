package com.utkarsh.todo;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements PremiumManager.PremiumListener {

    private static final String[] DELETE_MSGS = {
        "Task yeeted into the void \uD83D\uDD73\uFE0F",
        "Gone. Forever. You're welcome.",
        "Deleted with zero regrets.",
        "Poof! Productivity via deletion."
    };
    private static final String[] DONE_MSGS = {
        "Wait, you actually did it? \uD83D\uDC40",
        "Certified legend behavior \uD83C\uDF96\uFE0F",
        "Your future self says thanks.",
        "The dopamine is a lie. Enjoy it anyway!"
    };

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextView         tvGreeting, tvSubtitle;
    private TextView         tvStatTotal, tvStatDone, tvStatPending;
    private CircleImageView  ivAvatar;
    private ImageView        ivPremiumCrown;
    private RecyclerView     recyclerView;
    private TextView         tvEmpty;
    private FrameLayout      adContainer;
    private LinearLayout     bannerUpgrade;
    private ChipGroup        chipGroupFilter;
    private FloatingActionButton fab;

    // ── Data ──────────────────────────────────────────────────────────────────
    private FirebaseAuth       mAuth;
    private FirebaseUser       currentUser;
    private FirestoreManager   firestoreManager;
    private ListenerRegistration listenerReg;
    private PremiumManager     premiumManager;
    private TodoAdapter        adapter;
    private final List<TodoItem> allTasks     = new ArrayList<>();
    private String             activeFilter   = "All";
    private final Random       random         = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        bindViews();
        setupUser();
        setupRecyclerView();
        setupFilterChips();
        setupFab();
        setupPremium();
        listenToTasks();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void bindViews() {
        tvGreeting      = findViewById(R.id.tvGreeting);
        tvSubtitle      = findViewById(R.id.tvSubtitle);
        tvStatTotal     = findViewById(R.id.tvStatTotal);
        tvStatDone      = findViewById(R.id.tvStatDone);
        tvStatPending   = findViewById(R.id.tvStatPending);
        ivAvatar        = findViewById(R.id.ivAvatar);
        ivPremiumCrown  = findViewById(R.id.ivPremiumCrown);
        recyclerView    = findViewById(R.id.recyclerView);
        tvEmpty         = findViewById(R.id.tvEmpty);
        adContainer     = findViewById(R.id.adContainer);
        bannerUpgrade   = findViewById(R.id.bannerUpgrade);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        fab             = findViewById(R.id.fab);

        // Sign out on long-press avatar
        ivAvatar.setOnLongClickListener(v -> { confirmSignOut(); return true; });

        // Upgrade banner click
        bannerUpgrade.setOnClickListener(v -> showUpgradeDialog());
    }

    private void setupUser() {
        String name = currentUser.getDisplayName();
        String first = (name != null && name.contains(" "))
                ? name.split(" ")[0] : (name != null ? name : "Friend");
        tvGreeting.setText("Hey, " + first + "! \uD83D\uDC4B");
        tvSubtitle.setText("Let's pretend to be productive today");

        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this).load(currentUser.getPhotoUrl()).into(ivAvatar);
        }
    }

    private void setupRecyclerView() {
        adapter = new TodoAdapter(new TodoAdapter.Listener() {
            @Override
            public void onToggle(TodoItem item, int position) {
                item.setDone(!item.isDone());
                firestoreManager.toggleDone(item.getId(), item.isDone());
                adapter.notifyItemChanged(position);
                updateStats();
                if (item.isDone()) toast(DONE_MSGS[random.nextInt(DONE_MSGS.length)]);
            }
            @Override
            public void onDelete(TodoItem item, int position) {
                firestoreManager.deleteTask(item.getId());
                String msg = DELETE_MSGS[random.nextInt(DELETE_MSGS.length)];
                Snackbar.make(recyclerView, msg, Snackbar.LENGTH_SHORT).show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        new ItemTouchHelper(new SwipeToDeleteCallback(this, pos -> {
            TodoItem item = adapter.getItems().get(pos);
            firestoreManager.deleteTask(item.getId());
            String msg = DELETE_MSGS[random.nextInt(DELETE_MSGS.length)];
            Snackbar.make(recyclerView, msg, Snackbar.LENGTH_SHORT).show();
        })).attachToRecyclerView(recyclerView);
    }

    private void setupFilterChips() {
        String[] filters = {"All", "Work", "Personal", "Health", "Other"};
        for (String f : filters) {
            Chip chip = new Chip(this);
            chip.setText(f);
            chip.setCheckable(true);
            chip.setChecked(f.equals("All"));
            chip.setChipBackgroundColor(buildChipColors());
            chip.setTextColor(buildChipTextColors());
            chip.setOnCheckedChangeListener((c, checked) -> {
                if (checked) { activeFilter = f; applyFilter(); }
            });
            chipGroupFilter.addView(chip);
        }
        chipGroupFilter.setSingleSelection(true);
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            AddTaskBottomSheet sheet = new AddTaskBottomSheet();
            sheet.setOnTaskAddedListener((title, category, priority) -> {
                TodoItem item = new TodoItem(title, category, priority, currentUser.getUid());
                firestoreManager.addTask(item, ref -> {
                    toast("Added to " + category + " because vibes \uD83D\uDE0E");
                });
            });
            sheet.show(getSupportFragmentManager(), "AddTask");
        });
    }

    private void setupPremium() {
        firestoreManager = new FirestoreManager(currentUser.getUid());
        premiumManager = new PremiumManager(this);
        premiumManager.setListener(this);
        premiumManager.connect();
        // Apply cached status immediately so UI is right before billing connects
        onPremiumStatusChanged(premiumManager.isPremiumCached());
    }

    // ── Real-time data ─────────────────────────────────────────────────────────

    private void listenToTasks() {
        listenerReg = firestoreManager.listenToTasks((snapshots, e) -> {
            if (e != null || snapshots == null) return;
            allTasks.clear();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                TodoItem item = doc.toObject(TodoItem.class);
                if (item != null) {
                    item.setId(doc.getId());
                    allTasks.add(item);
                }
            }
            applyFilter();
        });
    }

    private void applyFilter() {
        List<TodoItem> filtered = new ArrayList<>();
        for (TodoItem t : allTasks) {
            if ("All".equals(activeFilter) || activeFilter.equals(t.getCategory())) {
                filtered.add(t);
            }
        }
        adapter.setItems(filtered);
        updateStats();
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateStats() {
        int total   = allTasks.size();
        int done    = 0;
        for (TodoItem t : allTasks) if (t.isDone()) done++;
        int pending = total - done;
        tvStatTotal.setText(String.valueOf(total));
        tvStatDone.setText(String.valueOf(done));
        tvStatPending.setText(String.valueOf(pending));
    }

    // ── Premium ────────────────────────────────────────────────────────────────

    @Override
    public void onPremiumStatusChanged(boolean isPremium) {
        ivPremiumCrown.setVisibility(isPremium ? View.VISIBLE : View.GONE);
        bannerUpgrade.setVisibility(isPremium ? View.GONE : View.VISIBLE);
        if (isPremium) {
            adContainer.setVisibility(View.GONE);
            adContainer.removeAllViews();
            firestoreManager.setPremium(true);
        } else {
            loadBannerAd();
        }
    }

    private void loadBannerAd() {
        adContainer.setVisibility(View.VISIBLE);
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        // TEST ID — replace with your real AdMob banner unit ID
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adContainer.removeAllViews();
        adContainer.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void showUpgradeDialog() {
        new AlertDialog.Builder(this)
            .setTitle("\uD83D\uDC51 Go Premium")
            .setMessage("Remove all ads and unlock unlimited tasks for just \u20B979/month.\n\nBecause your sanity deserves better than banner ads.")
            .setPositiveButton("Upgrade Now", (d, w) -> premiumManager.launchPurchaseFlow())
            .setNegativeButton("Maybe Later", null)
            .show();
    }

    // ── Misc ───────────────────────────────────────────────────────────────────

    private void confirmSignOut() {
        new AlertDialog.Builder(this)
            .setTitle("Sign Out?")
            .setMessage("Your tasks are saved online. They'll be here when you come back.")
            .setPositiveButton("Sign Out", (d, w) -> signOut())
            .setNegativeButton("Stay", null)
            .show();
    }

    private void signOut() {
        GoogleSignInClient gsc = GoogleSignIn.getClient(this,
                GoogleSignInOptions.DEFAULT_SIGN_IN);
        mAuth.signOut();
        gsc.signOut().addOnCompleteListener(t -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private ColorStateList buildChipColors() {
        return new ColorStateList(
            new int[][] {
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
            },
            new int[] { 0xFF5E35B1, 0xFFEDE7F6 }
        );
    }

    private ColorStateList buildChipTextColors() {
        return new ColorStateList(
            new int[][] {
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
            },
            new int[] { 0xFFFFFFFF, 0xFF4527A0 }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerReg != null) listenerReg.remove();
        if (premiumManager != null) premiumManager.disconnect();
    }
}
