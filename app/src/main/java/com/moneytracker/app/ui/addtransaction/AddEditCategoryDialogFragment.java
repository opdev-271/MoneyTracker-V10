package com.moneytracker.app.ui.addtransaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneytracker.app.MoneyTrackerApp;
import com.moneytracker.app.R;
import com.moneytracker.app.data.entity.Category;
import com.moneytracker.app.data.repository.CategoryRepository;
import com.moneytracker.app.ui.widget.IconGridAdapter;
import com.moneytracker.app.util.CategoryIcons;

/**
 * Replaces the old single-EditText "add category" prompt with a proper form: name,
 * an icon grid ({@link CategoryIcons}), and a row of accent-color swatches.
 */
public class AddEditCategoryDialogFragment extends DialogFragment {

    private static final String ARG_TYPE = "arg_type";
    private static final int[] SWATCH_COLORS = {
            0xFF1A1A1A, 0xFF2F80ED, 0xFF9B51E0, 0xFFF2994A, 0xFF219653, 0xFFEB5757, 0xFF2D9CDB
    };

    public static AddEditCategoryDialogFragment newInstance(String type) {
        AddEditCategoryDialogFragment fragment = new AddEditCategoryDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnCategoryCreatedListener {
        void onCategoryCreated();
    }

    private EditText editName;
    private IconGridAdapter iconAdapter;
    private LinearLayout colorRow;
    private int selectedColor = SWATCH_COLORS[0];
    private OnCategoryCreatedListener listener;

    public void setOnCategoryCreatedListener(OnCategoryCreatedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String type = requireArguments().getString(ARG_TYPE, Category.TYPE_EXPENSE);

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_category, null, false);
        editName = view.findViewById(R.id.edit_name);

        iconAdapter = new IconGridAdapter(CategoryIcons.allOptions(), CategoryIcons.allOptions().get(0).key,
                requireContext().getColor(android.R.color.white),
                R.drawable.bg_circle_dark, R.drawable.bg_circle_dark_selected);
        RecyclerView recyclerIcons = view.findViewById(R.id.recycler_icons);
        recyclerIcons.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        recyclerIcons.setAdapter(iconAdapter);

        colorRow = view.findViewById(R.id.color_row);
        buildColorSwatches();

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_category_title)
                .setView(view)
                .setPositiveButton(R.string.button_save, null)
                .setNegativeButton(R.string.action_cancel, (d, which) -> dismiss())
                .create();
        dialog.setOnShowListener((DialogInterface.OnShowListener) d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (trySave(type)) {
                        dismiss();
                    }
                }));
        return dialog;
    }

    private void buildColorSwatches() {
        int sizePx = (int) (36 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (8 * getResources().getDisplayMetrics().density);
        int strokePx = (int) (2.5f * getResources().getDisplayMetrics().density);

        for (int color : SWATCH_COLORS) {
            View swatch = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMarginEnd(marginPx);
            swatch.setLayoutParams(params);
            swatch.setTag(color);
            applySwatchDrawable(swatch, color, strokePx);
            swatch.setOnClickListener(v -> {
                selectedColor = color;
                refreshSwatchSelection(strokePx);
            });
            colorRow.addView(swatch);
        }
    }

    private void applySwatchDrawable(View swatch, int color, int strokePx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        if (color == selectedColor) {
            drawable.setStroke(strokePx, requireContext().getColor(R.color.income_green));
        }
        swatch.setBackground(drawable);
    }

    private void refreshSwatchSelection(int strokePx) {
        for (int i = 0; i < colorRow.getChildCount(); i++) {
            View swatch = colorRow.getChildAt(i);
            applySwatchDrawable(swatch, (int) swatch.getTag(), strokePx);
        }
    }

    private boolean trySave(String type) {
        String name = editName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), R.string.category_name_hint, Toast.LENGTH_SHORT).show();
            return false;
        }
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIcon(iconAdapter.getSelectedKey());
        category.setColor(selectedColor);

        AddTransactionViewModel viewModel = new ViewModelProvider(requireActivity()).get(AddTransactionViewModel.class);
        CategoryRepository categoryRepository = MoneyTrackerApp.from(requireContext()).getCategoryRepository();
        categoryRepository.insert(category, newId -> {
            viewModel.setSelectedCategoryId(newId);
            if (listener != null) {
                listener.onCategoryCreated();
            }
        });
        return true;
    }
}
