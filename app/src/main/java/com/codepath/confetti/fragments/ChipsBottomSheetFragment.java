package com.codepath.confetti.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codepath.confetti.databinding.FragmentTagsBottomSheetBinding;
import com.codepath.confetti.utlils.Chips;
import com.codepath.confetti.utlils.UtilsGeneral;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Bottom sheet fragment to show list of all available chips to user
 */
public class ChipsBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ChipsBottomSheetFragment";

    private FragmentTagsBottomSheetBinding binding;
    private NotesFragment parentFragment;
    private ChipGroup chipGroup;

    // the fragment initialization parameters
    private static final String ALL_CHIPS = "allChips";
    protected Map<String, Boolean> allChips;

    public ChipsBottomSheetFragment() {
        // Required empty public constructor
    }

    /**
     * Sets the list of all the chips associated with the current user
     * @param allChips map of all chips
     * @return new instance of fragment for all chips
     */
    public static ChipsBottomSheetFragment newInstance(TreeMap<String, Boolean> allChips) {
        ChipsBottomSheetFragment fragment = new ChipsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ALL_CHIPS, allChips);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // get back arguments
            allChips = (TreeMap<String, Boolean>) getArguments().getSerializable(ALL_CHIPS);
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
        Chips.populateChipsSelectable(getContext(), chipGroup, allChips);
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

        // refresh notes list and chips in parent fragment
        parentFragment.refreshChips(checkedChipIds, chipGroup, true);
    }


    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                UtilsGeneral.setupFullHeight(bottomSheetDialog, getContext());

                BottomSheetBehavior bottomSheetBehavior = ((BottomSheetDialog) dialogInterface).getBehavior();
                bottomSheetBehavior.setDraggable(false);
            }
        });
        return  dialog;
    }
}