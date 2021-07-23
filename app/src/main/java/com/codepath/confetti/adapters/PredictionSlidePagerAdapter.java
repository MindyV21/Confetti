package com.codepath.confetti.adapters;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.codepath.confetti.fragments.PredictionSlidePageFragment;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;

import java.util.ArrayList;
import java.util.List;

public class PredictionSlidePagerAdapter extends FragmentStateAdapter {

    private Context context;
    private List<Prediction> predictions;

    public PredictionSlidePagerAdapter(FragmentActivity fa, List<Prediction> predictions) {
        super(fa);
        if (predictions == null) {
            this.predictions = new ArrayList<>();
        } else {
            this.predictions = predictions;
        }
    }

    @Override
    public Fragment createFragment(int position) {
        return new PredictionSlidePageFragment(predictions.get(position));
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }
}
