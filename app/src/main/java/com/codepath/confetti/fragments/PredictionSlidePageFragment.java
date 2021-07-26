package com.codepath.confetti.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.codepath.confetti.databinding.FragmentPredictionSlidePageBinding;
import com.codepath.confetti.models.Prediction;

import org.jetbrains.annotations.NotNull;

public class PredictionSlidePageFragment extends Fragment {

    public static final String TAG = "PredictionSlidePageFragment";
    private FragmentPredictionSlidePageBinding binding;

    private Prediction prediction;

    private CardView cardPrediction;
    private ImageView ivDeletePrediction;
    private TextView tvLabel;
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

        cardPrediction = binding.cardPrediction;
        ivDeletePrediction = binding.ivDeletePrediction;
        tvLabel = binding.tvLabel;
        tvText = binding.tvText;

        tvLabel.setText(prediction.label);
        if (prediction.label.equals("Topic")) {
            tvText.setText(prediction.text);
        }

        // to delete a prediction
        ivDeletePrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "long click");
                new AlertDialog.Builder(getContext())
                        .setMessage("Delete keyword '" + prediction.text + "'?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "deleting prediction " + prediction.text);

                                // update predictions in notes database
                                // redraw pins on canvas <- child listener for predictions updated?
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }
}