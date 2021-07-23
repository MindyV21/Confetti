package com.codepath.confetti.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentPredictionSlidePageBinding;
import com.codepath.confetti.models.Prediction;

import org.jetbrains.annotations.NotNull;

public class PredictionSlidePageFragment extends Fragment {

    public static final String TAG = "PredictionSlidePageFragment";
    private FragmentPredictionSlidePageBinding binding;

    private Prediction prediction;

    private TextView tvText;

    public PredictionSlidePageFragment(Prediction prediction) {
        this.prediction = prediction;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPredictionSlidePageBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvText = binding.tvText;
        tvText.setText(prediction.text);
    }
}