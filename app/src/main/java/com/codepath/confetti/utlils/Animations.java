package com.codepath.confetti.utlils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Util class for UI animations
 */
public class Animations {

    public static final String TAG = "Animations";
    public static final long DURATION = 350;

    public static void fadeIn(View view) {
        Log.d(TAG, "fade in");

        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(1.0f)
                .setListener(null);
    }

    public static void fadeOut(View view) {
        Log.d(TAG, "fade out");

        view.animate()
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Slides view off screen downward
     * @param view
     */
    public static void slideDown(View view, int offset) {
        Log.d(TAG, "slide down");
        // Prepare the View for the animation
        view.setVisibility(View.VISIBLE);

        // Start the animation
        view.animate()
                .setDuration(DURATION)
                .translationY(view.getHeight() + offset)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Slides view back onto screen upward
     * @param view
     */
    public static void reverseSlideDown(View view) {
        Log.d(TAG, "reverse slide down");
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(DURATION)
                .translationY(0)
                .setListener(null);
    }

    /**
     * Slides view off screen upward
     * @param view
     */
    public static void slideUp(View view, int offset) {
        Log.d(TAG, "slide up");
        // Prepare the View for the animation
        view.setVisibility(View.VISIBLE);

        // Start the animation
        view.animate()
                .setDuration(DURATION)
                .translationY(-(view.getHeight() + offset))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Slides view back onto screen downward
     * @param view
     */
    public static void reverseSlideUp(View view) {
        Log.d(TAG, "reverse slide up");
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(DURATION)
                .translationY(0)
                .setListener(null);
    }

}
