package com.moneytracker.app.ui.addtransaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.moneytracker.app.R;
import com.moneytracker.app.data.entity.Category;
import com.moneytracker.app.util.CategoryIcons;
import com.moneytracker.app.util.DisplayNameResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces an anchored PopupMenu (unreliable inside a scrolling screen) with a proper
 * list dialog. Also lets the user create a brand new category, right where they
 * noticed one was missing, instead of needing a separate management screen.
 */
public class CategoryPickerDialogFragment extends DialogFragment {

    private static final String ARG_TYPE = "arg_type";

    public static CategoryPickerDialogFragment newInstance(String type) {
        CategoryPickerDialogFragment fragment = new CategoryPickerDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    private AddTransactionViewModel viewModel;
    private LinearLayout listContainer;
    private String type;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(AddTransactionViewModel.class);
        type = requireArguments().getString(ARG_TYPE, Category.TYPE_EXPENSE);

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_picker, null, false);
        listContainer = view.findViewById(R.id.category_list_container);

        viewModel.getCategories().observe(this, categories ->
                renderRows(categories != null ? categories : new ArrayList<>()));

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.label_category)
                .setView(view)
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> dismiss())
                .create();
    }

    private void renderRows(List<Category> categories) {
        if (listContainer == null) {
            return;
        }
        listContainer.removeAllViews();
        Long selectedId = viewModel.getSelectedCategoryId().getValue();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (Category category : categories) {
            View row = inflater.inflate(R.layout.item_category_picker_row, listContainer, false);
            FrameLayout badge = row.findViewById(R.id.badge_icon);
            ImageView icon = row.findViewById(R.id.image_icon);
            TextView name = row.findViewById(R.id.text_name);
            View check = row.findViewById(R.id.image_check);

            icon.setImageResource(CategoryIcons.resolve(category.getIcon()));
            GradientDrawable badgeBg = (GradientDrawable) badge.getBackground().mutate();
            badgeBg.setColor(category.getColor() != 0 ? category.getColor() : requireContext().getColor(R.color.text_tertiary));
            name.setText(DisplayNameResolver.resolve(requireContext(), category.getName()));
            check.setVisibility(selectedId != null && category.getId() == selectedId ? View.VISIBLE : View.GONE);

            row.setOnClickListener(v -> {
                viewModel.setSelectedCategoryId(category.getId());
                dismiss();
            });
            listContainer.addView(row);
        }

        View addRow = inflater.inflate(R.layout.item_category_picker_add_row, listContainer, false);
        addRow.setOnClickListener(v -> {
            AddEditCategoryDialogFragment addDialog = AddEditCategoryDialogFragment.newInstance(type);
            addDialog.setOnCategoryCreatedListener(this::dismiss);
            addDialog.show(getParentFragmentManager(), "add_category");
        });
        listContainer.addView(addRow);
    }
}
