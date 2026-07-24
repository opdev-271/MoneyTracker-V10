package com.moneytracker.app.util;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/** Small, reusable motion touches shared across screens so the app feels consistently "alive". */
public class ViewAnimations {

    private ViewAnimations() {
    }

    /** Scales a view down on press and springs it back on release, like a physical button. */
    public static void addPressFeedback(View... views) {
        View.OnTouchListener listener = (v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.94f).scaleY(0.94f)
                            .setDuration(120)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f)
                            .setDuration(180)
                            .setInterpolator(new OvershootInterpolator(1.4f))
                            .start();
                    break;
                default:
                    break;
            }
            return false;
        };
        for (View view : views) {
            if (view != null) {
                view.setOnTouchListener(listener);
            }
        }
    }

    /** Fades + lifts a screen's root content in; call once per fragment view creation. */
    public static void fadeInContent(View root) {
        root.setAlpha(0f);
        root.setTranslationY(24f);
        root.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(40)
                .setDuration(260)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Fades + lifts a RecyclerView row in the first time it's bound (position beyond the
     * highest position already animated), staggered slightly by position. Subsequent rebinds
     * during scroll are left untouched so the effect only plays once per item.
     */
    public static void animateItemEntranceOnce(View itemView, int position, int[] highestAnimatedPosition) {
        if (position <= highestAnimatedPosition[0]) {
            itemView.setAlpha(1f);
            itemView.setTranslationY(0f);
            return;
        }
        highestAnimatedPosition[0] = position;
        itemView.setAlpha(0f);
        itemView.setTranslationY(40f);
        itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(Math.min(position, 8) * 35L)
                .setDuration(260)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
