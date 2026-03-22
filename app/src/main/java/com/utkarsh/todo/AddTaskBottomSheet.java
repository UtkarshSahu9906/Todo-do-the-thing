package com.utkarsh.todo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    public interface OnTaskAddedListener {
        void onTaskAdded(String title, String category, String priority);
    }

    private OnTaskAddedListener taskListener;
    private String selectedPriority = TodoItem.PRIORITY_MEDIUM;
    private String selectedCategory = TodoItem.CAT_PERSONAL;

    public void setOnTaskAddedListener(OnTaskAddedListener l) { this.taskListener = l; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_task, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText  etTitle       = view.findViewById(R.id.etTaskTitle);
        ChipGroup cgPriority    = view.findViewById(R.id.cgPriority);
        ChipGroup cgCategory    = view.findViewById(R.id.cgCategory);
        Button    btnAdd        = view.findViewById(R.id.btnAddTask);

        // Priority chips
        cgPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chipHigh)   selectedPriority = TodoItem.PRIORITY_HIGH;
            else if (id == R.id.chipMedium) selectedPriority = TodoItem.PRIORITY_MEDIUM;
            else                            selectedPriority = TodoItem.PRIORITY_LOW;
        });

        // Category chips
        cgCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chipWork)     selectedCategory = TodoItem.CAT_WORK;
            else if (id == R.id.chipPersonal) selectedCategory = TodoItem.CAT_PERSONAL;
            else if (id == R.id.chipHealth)   selectedCategory = TodoItem.CAT_HEALTH;
            else                              selectedCategory = TodoItem.CAT_OTHER;
        });

        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Come on, type something!");
                return;
            }
            if (taskListener != null) {
                taskListener.onTaskAdded(title, selectedCategory, selectedPriority);
            }
            dismiss();
        });
    }
}
