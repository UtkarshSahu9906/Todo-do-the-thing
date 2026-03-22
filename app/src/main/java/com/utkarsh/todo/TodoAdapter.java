package com.utkarsh.todo;

import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    public interface Listener {
        void onToggle(TodoItem item, int position);
        void onDelete(TodoItem item, int position);
    }

    private final List<TodoItem> items = new ArrayList<>();
    private final Listener listener;

    public TodoAdapter(Listener listener) { this.listener = listener; }

    public void setItems(List<TodoItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public List<TodoItem> getItems() { return items; }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        TodoItem item = items.get(position);

        // Title
        h.tvTitle.setText(item.getTitle());
        h.tvMood.setText(item.getMoodText());
        h.tvCategory.setText(item.getCategory());
        h.checkBox.setChecked(item.isDone());

        // Strike-through when done
        if (item.isDone()) {
            h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvTitle.setAlpha(0.45f);
            h.tvMood.setAlpha(0.45f);
        } else {
            h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvTitle.setAlpha(1f);
            h.tvMood.setAlpha(1f);
        }

        // Priority stripe color
        int stripeColor;
        int badgeColor;
        switch (item.getPriority() != null ? item.getPriority() : TodoItem.PRIORITY_LOW) {
            case TodoItem.PRIORITY_HIGH:
                stripeColor = 0xFFE53935;
                badgeColor  = 0xFFFFEBEE;
                break;
            case TodoItem.PRIORITY_MEDIUM:
                stripeColor = 0xFFFB8C00;
                badgeColor  = 0xFFFFF3E0;
                break;
            default:
                stripeColor = 0xFF43A047;
                badgeColor  = 0xFFE8F5E9;
                break;
        }
        h.priorityStripe.setBackgroundColor(stripeColor);
        h.tvCategory.setBackgroundTintList(ColorStateList.valueOf(badgeColor));

        // Category text color
        int catColor;
        switch (item.getCategory() != null ? item.getCategory() : TodoItem.CAT_OTHER) {
            case TodoItem.CAT_WORK:     catColor = 0xFF1565C0; break;
            case TodoItem.CAT_PERSONAL: catColor = 0xFF6A1B9A; break;
            case TodoItem.CAT_HEALTH:   catColor = 0xFF00695C; break;
            default:                    catColor = 0xFF37474F; break;
        }
        h.tvCategory.setTextColor(catColor);

        h.checkBox.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onToggle(item, pos);
        });

        h.btnDelete.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) listener.onDelete(item, pos);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View        priorityStripe;
        TextView    tvTitle, tvMood, tvCategory;
        CheckBox    checkBox;
        ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            priorityStripe = v.findViewById(R.id.priorityStripe);
            tvTitle        = v.findViewById(R.id.tvTitle);
            tvMood         = v.findViewById(R.id.tvMood);
            tvCategory     = v.findViewById(R.id.tvCategory);
            checkBox       = v.findViewById(R.id.checkBox);
            btnDelete      = v.findViewById(R.id.btnDelete);
        }
    }
}
