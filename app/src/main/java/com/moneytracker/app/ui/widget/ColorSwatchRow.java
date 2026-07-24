package com.moneytracker.app.ui.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;

import com.moneytracker.app.R;

/**
 * Single-select row of color swatches (a {@link LinearLayout} filled with plain
 * {@link View}s given an oval {@link GradientDrawable} background), shared by the
 * category and account creation dialogs so a user-chosen accent color always comes
 * from the same small preset palette.
 */
public class ColorSwatchRow {

    public static final int[] DEFAULT_COLORS = {
            0xFF1A1A1A, 0xFF2F80ED, 0xFF9B51E0, 0xFFF2994A, 0xFF219653, 0xFFEB5757, 0xFF2D9CDB
    };

    private final Context context;
    private final LinearLayout container;
    private final int[] colors;
    private int selectedColor;

    public ColorSwatchRow(Context context, LinearLayout container, int[] colors, int initialSelectedColor) {
        this.context = context;
        this.container = container;
        this.colors = colors;
        this.selectedColor = initialSelectedColor;
        build();
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    private void build() {
        float density = context.getResources().getDisplayMetrics().density;
        int sizePx = (int) (36 * density);
        int marginPx = (int) (8 * density);
        int strokePx = (int) (2.5f * density);

        container.removeAllViews();
        for (int color : colors) {
            View swatch = new View(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMarginEnd(marginPx);
            swatch.setLayoutParams(params);
            swatch.setTag(color);
            applySwatchDrawable(swatch, color, strokePx);
            swatch.setOnClickListener(v -> {
                selectedColor = color;
                refreshSelection(strokePx);
            });
            container.addView(swatch);
        }
    }

    private void applySwatchDrawable(View swatch, int color, int strokePx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        if (color == selectedColor) {
            drawable.setStroke(strokePx, context.getColor(R.color.income_green));
        }
        swatch.setBackground(drawable);
    }

    private void refreshSelection(int strokePx) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View swatch = container.getChildAt(i);
            applySwatchDrawable(swatch, (int) swatch.getTag(), strokePx);
        }
    }
}
