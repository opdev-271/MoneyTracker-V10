package com.moneytracker.app.ui.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

/**
 * Drives a sliding-pill segmented control: a colored indicator View that glides
 * under whichever of N equal-width TextViews is selected, instead of swapping
 * flat backgrounds. The caller is the single source of truth for which index
 * is selected (usually a ViewModel's LiveData) — {@link #setSelectedIndex} is
 * how that state is reflected here; taps only ever go out through the listener.
 */
public class SegmentedToggle {

    public interface OnSegmentSelectedListener {
        void onSegmentSelected(int index);
    }

    private final ViewGroup container;
    private final View indicator;
    private final TextView[] labels;
    private final int selectedColor;
    private final int unselectedColor;
    private int currentIndex = -1;
    private OnSegmentSelectedListener listener;

    public SegmentedToggle(ViewGroup container, View indicator, TextView[] labels,
                            int selectedColor, int unselectedColor) {
        this.container = container;
        this.indicator = indicator;
        this.labels = labels;
        this.selectedColor = selectedColor;
        this.unselectedColor = unselectedColor;

        for (int i = 0; i < labels.length; i++) {
            int index = i;
            labels[i].setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSegmentSelected(index);
                }
            });
        }
    }

    public void setOnSegmentSelectedListener(OnSegmentSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedIndex(int index) {
        boolean firstLayout = currentIndex == -1;
        boolean changed = index != currentIndex;
        currentIndex = index;
        applyColors(index);

        if (!changed && !firstLayout) {
            return;
        }
        if (container.getWidth() > 0) {
            positionIndicator(index, !firstLayout);
        } else {
            container.post(() -> positionIndicator(index, false));
        }
    }

    private void positionIndicator(int index, boolean animate) {
        int usableWidth = container.getWidth() - container.getPaddingLeft() - container.getPaddingRight();
        if (usableWidth <= 0 || labels.length == 0) {
            return;
        }
        int segmentWidth = usableWidth / labels.length;
        ViewGroup.LayoutParams params = indicator.getLayoutParams();
        if (params.width != segmentWidth) {
            params.width = segmentWidth;
            indicator.setLayoutParams(params);
        }
        float targetX = segmentWidth * index;
        if (animate) {
            indicator.animate()
                    .translationX(targetX)
                    .setDuration(260)
                    .setInterpolator(new DecelerateInterpolator(1.6f))
                    .start();
        } else {
            indicator.animate().cancel();
            indicator.setTranslationX(targetX);
        }
    }

    private void applyColors(int index) {
        for (int i = 0; i < labels.length; i++) {
            labels[i].setTextColor(i == index ? selectedColor : unselectedColor);
        }
    }
}
