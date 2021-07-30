package com.codepath.confetti.utlils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Log;
import android.view.View;

/**
 * Util class for UI animations
 */
public class Animations {

    public static final String TAG = "Animations";

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
                .translationY(0)
                .setListener(null);
    }

}
