package com.codepath.confetti.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentNotesBinding;
import com.codepath.confetti.databinding.FragmentTagsBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TagsBottomSheetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagsBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "TagsBottomSheetFragment";

    private FragmentTagsBottomSheetBinding binding;
    private NotesFragment parentFragment;
    private ChipGroup chipGroup;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TagsBottomSheetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TagsBottomSheetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagsBottomSheetFragment newInstance(String param1, String param2) {
        TagsBottomSheetFragment fragment = new TagsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTagsBottomSheetBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // clear notes
        parentFragment = ((NotesFragment) TagsBottomSheetFragment.this.getParentFragment());
        parentFragment.clearNotes();

        chipGroup = binding.chipGroup;

        int checkedChipId = chipGroup.getCheckedChipId(); // Returns View.NO_ID if singleSelection = false
        List<Integer> checkedChipIds = chipGroup.getCheckedChipIds(); // Returns a list of the selected chips' IDs, if any

        //dummy chips
        Chip one = new Chip(getContext());
        one.setText("Dog");
        one.setCheckable(true);
        chipGroup.addView(one);

        chipGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // responds to child chip checked/unchecked
            }
        });
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);
        parentFragment.dummyData();
        List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();
        for (Integer id : checkedChipIds) {
            // TODO: CREATE A CHIP CLASS TO MAKE THESE MORE EASILY
            Chip chip = new Chip(getContext());
            chip.setText(((Chip) chipGroup.findViewById(id)).getText());
            chip.setCheckable(true);
            parentFragment.addChip(chip);
        }
    }
}