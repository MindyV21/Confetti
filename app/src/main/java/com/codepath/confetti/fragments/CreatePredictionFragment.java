package com.codepath.confetti.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.codepath.confetti.NoteDetailsActivity;
import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentAddChipBinding;
import com.codepath.confetti.databinding.FragmentCreatePredictionBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

public class CreatePredictionFragment extends Fragment {

    public static final String TAG = "CreatePredictionFragment";
    private FragmentCreatePredictionBinding binding;

    private ImageView ivCancel;
    private TabLayout tabLayoutCreatePrediction;

    public CreatePredictionFragment() {}

    public static CreatePredictionFragment newInstance() {
        CreatePredictionFragment frag = new CreatePredictionFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreatePredictionBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivCancel = binding.ivCancel;
        tabLayoutCreatePrediction = binding.tabLayoutCreatePrediction;

        // to exit creating a prediction
        Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_baseline_cancel_24);
        ivCancel.setImageDrawable(drawable);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreatePredictionListener listener = (CreatePredictionListener) getActivity();
                listener.onCancelCreatePrediction();
            }
        });

        // tab selection changes - hide/show option to name keyword
        tabLayoutCreatePrediction.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "tab selected " + tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    // Defines the listener interface
    public interface CreatePredictionListener {
        public void onCancelCreatePrediction();
    }
}
