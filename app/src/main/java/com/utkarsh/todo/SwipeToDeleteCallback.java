package com.utkarsh.todo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    public interface Listener {
        void onSwipeDelete(int position);
    }

    private final Listener listener;
    private final Paint    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Drawable iconDelete;

    public SwipeToDeleteCallback(Context ctx, Listener listener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.listener = listener;
        paint.setColor(0xFFE53935);
        Drawable d = ContextCompat.getDrawable(ctx, R.drawable.ic_delete);
        iconDelete = (d != null) ? d.mutate() : null;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView rv,
                          @NonNull RecyclerView.ViewHolder a,
                          @NonNull RecyclerView.ViewHolder b) { return false; }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
        listener.onSwipeDelete(vh.getAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                            @NonNull RecyclerView.ViewHolder vh,
                            float dX, float dY, int actionState, boolean isActive) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
            super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
            return;
        }
        float top    = vh.itemView.getTop();
        float bottom = vh.itemView.getBottom();
        float left   = vh.itemView.getLeft();
        float right  = vh.itemView.getRight();
        int   sz     = 64;
        int   margin = (int) ((bottom - top - sz) / 2f);

        if (dX > 0) {
            c.drawRoundRect(new RectF(left, top, left + dX, bottom), 14, 14, paint);
            if (iconDelete != null) {
                iconDelete.setTint(Color.WHITE);
                iconDelete.setBounds((int) left + margin, (int) top + margin,
                        (int) left + margin + sz, (int) top + margin + sz);
                iconDelete.draw(c);
            }
        } else if (dX < 0) {
            c.drawRoundRect(new RectF(right + dX, top, right, bottom), 14, 14, paint);
            if (iconDelete != null) {
                iconDelete.setTint(Color.WHITE);
                iconDelete.setBounds((int) right - margin - sz, (int) top + margin,
                        (int) right - margin, (int) top + margin + sz);
                iconDelete.draw(c);
            }
        }
        super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
    }
}
