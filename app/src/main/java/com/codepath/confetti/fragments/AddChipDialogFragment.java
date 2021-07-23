package com.codepath.confetti.fragments;

import android.content.Context;
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

import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentAddChipBinding;
import com.codepath.confetti.databinding.FragmentNoteImagesBinding;

public class AddChipDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "AddChipDialogFragment";
    private FragmentAddChipBinding binding;
    private AddChipDialogListener listener;

    private EditText etChipName;
    private Button btnAddChip;

    public AddChipDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static AddChipDialogFragment newInstance(String title) {
        AddChipDialogFragment frag = new AddChipDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
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
        // Get field from view
        etChipName = binding.etChipName;
        btnAddChip = binding.btnAddChip;

        btnAddChip.setOnClickListener(this);

        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title");
        getDialog().setTitle(title);
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

        AddChipDialogListener listener = (AddChipDialogListener) getActivity();
        listener.onFinishAddChipDialog(etChipName.getText().toString().trim());
        getDialog().dismiss();
    }

    // Defines the listener interface
    public interface AddChipDialogListener {
        public void onFinishAddChipDialog(String inputText);
    }
}
