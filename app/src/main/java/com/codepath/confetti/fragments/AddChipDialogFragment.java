package com.codepath.confetti.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codepath.confetti.databinding.FragmentAddChipBinding;

/**
 * Dialog fragment to create a new chip for a specific note
 */
public class AddChipDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "AddChipDialogFragment";
    private FragmentAddChipBinding binding;

    private EditText etChipName;
    private Button btnAddChip;

    public AddChipDialogFragment() {
        // Empty constructor is required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddChipBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etChipName = binding.etChipName;
        btnAddChip = binding.btnAddChip;

        btnAddChip.setOnClickListener(this);

        // Show soft keyboard automatically and request focus to field
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "add chip button clicked !");

        // check if chipName is valid
        if (etChipName.getText() == null || etChipName.getText().toString().trim().equals("")) {
            Toast.makeText(getContext(), "Enter a tag!", Toast.LENGTH_SHORT).show();
            return;
        }

        // updates note details view with new chip
        AddChipDialogListener listener = (AddChipDialogListener) getActivity();
        listener.onFinishAddChipDialog(etChipName.getText().toString().trim());
        getDialog().dismiss();
    }

    /**
     * Interface for fragment and activity communication
     */
    public interface AddChipDialogListener {
        // updates note details view
        public void onFinishAddChipDialog(String inputText);
    }
}
