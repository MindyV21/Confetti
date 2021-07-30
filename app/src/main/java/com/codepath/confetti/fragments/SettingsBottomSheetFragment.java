package com.codepath.confetti.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentSettingsBottomSheetBinding;
import com.codepath.confetti.databinding.FragmentTagsBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

/**
 * Bottom Sheet Fragment with setting options
 */
public class SettingsBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "SettingsBottomSheetFragment";
    private FragmentSettingsBottomSheetBinding binding;

    private RelativeLayout layoutLogout;

    public SettingsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBottomSheetBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutLogout = binding.layoutLogout;

        layoutLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    /**
     * Logs out the current Firebase user
     */
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Log.i(TAG, "User logging out");
        Toast.makeText(getContext(), "Logging out", Toast.LENGTH_SHORT).show();
        getActivity().finish();
    }
}