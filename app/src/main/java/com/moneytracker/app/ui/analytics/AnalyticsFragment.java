package com.moneytracker.app.ui.analytics;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.moneytracker.app.R;
import com.moneytracker.app.data.dao.TransactionDao;
import com.moneytracker.app.data.entity.Account;
import com.moneytracker.app.databinding.FragmentAnalyticsBinding;
import com.moneytracker.app.ui.widget.ColorSwatchRow;
import com.moneytracker.app.ui.widget.SegmentedToggle;
import com.moneytracker.app.util.ContrastColor;
import com.moneytracker.app.util.CurrencyFormatter;
import com.moneytracker.app.util.DisplayNameResolver;
import com.moneytracker.app.util.SettingsPrefs;
import com.moneytracker.app.util.ViewAnimations;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private AnalyticsViewModel viewModel;
    private SegmentedToggle periodToggle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);
        ViewAnimations.fadeInContent(view);

        setupChart();

        periodToggle = new SegmentedToggle(binding.periodContainer, binding.periodIndicator,
                new android.widget.TextView[]{binding.periodDay, binding.periodWeek, binding.periodMonth, binding.periodYear},
                requireContext().getColor(R.color.on_accent_text), requireContext().getColor(R.color.text_secondary));
        AnalyticsViewModel.Period[] periods = AnalyticsViewModel.Period.values();
        periodToggle.setOnSegmentSelectedListener(index -> viewModel.setPeriod(periods[index]));

        viewModel.getPeriod().observe(getViewLifecycleOwner(), period -> periodToggle.setSelectedIndex(period.ordinal()));
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), value ->
                binding.textTotalIncome.setText(CurrencyFormatter.format(value != null ? value : 0, null)));
        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), value ->
                binding.textTotalExpense.setText(CurrencyFormatter.format(value != null ? value : 0, null)));
        viewModel.getCategoryTotals().observe(getViewLifecycleOwner(), this::renderChart);

        viewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> renderAccountFilter(accounts != null ? accounts : new ArrayList<>()));
        viewModel.getSelectedAccountId().observe(getViewLifecycleOwner(), id -> updateAccountFilterSelection());
    }

    /** Tag attached to each account filter chip; {@code color} is null for the neutral "All" chip. */
    private static class ChipData {
        final Long accountId;
        final Integer color;

        ChipData(Long accountId, Integer color) {
            this.accountId = accountId;
            this.color = color;
        }
    }

    private void renderAccountFilter(List<Account> accounts) {
        binding.accountFilterRow.removeAllViews();
        if (accounts.size() < 2) {
            // Nothing to filter by with zero or one account.
            return;
        }

        binding.accountFilterRow.addView(createFilterChip(getString(R.string.analytics_filter_all), new ChipData(null, null)));
        for (Account account : accounts) {
            String name = DisplayNameResolver.resolve(requireContext(), account.getName());
            int color = account.getColor() != 0 ? account.getColor() : ColorSwatchRow.DEFAULT_COLORS[0];
            binding.accountFilterRow.addView(createFilterChip(name, new ChipData(account.getId(), color)));
        }
        updateAccountFilterSelection();
    }

    private View createFilterChip(String label, ChipData data) {
        TextView chip = new TextView(requireContext());
        chip.setText(label);
        chip.setTag(data);
        chip.setTextSize(12f);
        int paddingH = (int) (16 * getResources().getDisplayMetrics().density);
        int paddingV = (int) (8 * getResources().getDisplayMetrics().density);
        chip.setPadding(paddingH, paddingV, paddingH, paddingV);
        int marginEnd = (int) (8 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(marginEnd);
        chip.setLayoutParams(params);
        chip.setOnClickListener(v -> viewModel.setSelectedAccountId(data.accountId));
        return chip;
    }

    private void updateAccountFilterSelection() {
        Long selected = viewModel.getSelectedAccountId().getValue();
        for (int i = 0; i < binding.accountFilterRow.getChildCount(); i++) {
            View child = binding.accountFilterRow.getChildAt(i);
            if (!(child instanceof TextView)) {
                continue;
            }
            TextView chip = (TextView) child;
            ChipData data = (ChipData) chip.getTag();
            boolean isSelected = selected == null ? data.accountId == null : selected.equals(data.accountId);
            styleChip(chip, data, isSelected);
        }
    }

    private void styleChip(TextView chip, ChipData data, boolean isSelected) {
        if (data.color == null) {
            // Neutral "All" chip.
            chip.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_pill_white);
            chip.setTextColor(requireContext().getColor(isSelected ? R.color.on_accent_text : R.color.text_primary));
            return;
        }
        int strokePx = (int) (1.5f * getResources().getDisplayMetrics().density);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        if (isSelected) {
            drawable.setColor(data.color);
            chip.setTextColor(ContrastColor.forBackground(data.color));
        } else {
            drawable.setColor(requireContext().getColor(R.color.surface));
            drawable.setStroke(strokePx, data.color);
            chip.setTextColor(requireContext().getColor(R.color.text_primary));
        }
        chip.setBackground(drawable);
    }

    private void setupChart() {
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleRadius(58f);
        binding.pieChart.setTransparentCircleRadius(62f);
        binding.pieChart.setHoleColor(requireContext().getColor(R.color.surface));
        binding.pieChart.setTransparentCircleColor(requireContext().getColor(R.color.surface));
        // The new palette is pastel-leaning, so dark slice labels read better than white.
        binding.pieChart.setEntryLabelColor(requireContext().getColor(R.color.on_accent_text));
        binding.pieChart.setEntryLabelTextSize(11f);
        Legend legend = binding.pieChart.getLegend();
        legend.setTextColor(requireContext().getColor(R.color.text_primary));
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
    }

    private void renderChart(List<TransactionDao.CategoryTotal> totals) {
        boolean empty = totals == null || totals.isEmpty();
        binding.textNoData.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.pieChart.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);
        if (empty) {
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (TransactionDao.CategoryTotal total : totals) {
            String label = DisplayNameResolver.resolve(requireContext(), total.categoryName);
            entries.add(new PieEntry((float) total.total, label));
            colors.add(total.color != 0 ? total.color : requireContext().getColor(R.color.text_tertiary));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        List<Integer> valueColors = new ArrayList<>();
        for (Integer color : colors) {
            valueColors.add(ContrastColor.forBackground(color));
        }
        dataSet.setValueTextColors(valueColors);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        if (SettingsPrefs.isAnimationsEnabled(requireContext())) {
            binding.pieChart.animateY(900, Easing.EaseOutCirc);
        }
        binding.pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
