package com.codepath.confetti.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.confetti.databinding.FragmentTagsBottomSheetBinding;
import com.codepath.confetti.utlils.Chips;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChipsBottomSheetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChipsBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ChipsBottomSheetFragment";

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

    public ChipsBottomSheetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChipsBottomSheetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChipsBottomSheetFragment newInstance(String param1, String param2) {
        ChipsBottomSheetFragment fragment = new ChipsBottomSheetFragment();
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

        // ref to NotesFragment
        parentFragment = ((NotesFragment) ChipsBottomSheetFragment.this.getParentFragment());

        chipGroup = binding.chipGroup;

        // populate fragment with all chips available
        Chips.populateChipsSelectable(getContext(), chipGroup, parentFragment.allChips);
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);

        // load chips into horizontal view
        List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();

        // reset chipGroup
        parentFragment.chipGroup.removeAllViews();

        // if no chips selected
        if (checkedChipIds.size() == 0) {
            // reset notes list
            parentFragment.currentNotes = new ArrayList<>(parentFragment.allNotes.values());
            parentFragment.adapter.setNotesFull(parentFragment.currentNotes);
            parentFragment.adapter.getFilter().filter(parentFragment.searchView.getQuery());
            return;
        }

        parentFragment.refreshChips(checkedChipIds, chipGroup, true);
    }
}