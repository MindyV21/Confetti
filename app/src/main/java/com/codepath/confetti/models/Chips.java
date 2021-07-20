package com.codepath.confetti.models;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import com.codepath.confetti.Firebase;
import com.codepath.confetti.adapters.NotesAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Chips {

    // create chips that are selectable
    public static void populateChipsSelectable(Context context, ChipGroup chipGroup, Map<String, Boolean> allChips) {
        for (Map.Entry<String, Boolean> chip : allChips.entrySet()) {
            Chip newChip = new Chip(context);
            newChip.setText(chip.getKey());
            newChip.setCheckable(true);

            // checks if chip has been previously selected
            if (chip.getValue()) {
                newChip.setChecked(true);
            }

            // listener to update things when ship is selected / unselected
            newChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // update chips checked state in the treemap
                    allChips.put(chip.getKey(), !chip.getValue());
                }
            });

            chipGroup.addView(newChip);
        }
    }

    // TODO: create chips that can be deleted
    public static void populateChipsDeletable(Context context, ChipGroup chipGroup, List<Integer> checkedChipIds,
                                              ChipGroup allChipsGroup, NotesAdapter adapter, SearchView searchView,
                                              Map<String, Note> allNotes, List<Note> currentNotes, Map<String, Boolean> allChips) {
        for (int i = checkedChipIds.size() - 1; i >= 0; i--) {
            final int index = i;
            Integer id = checkedChipIds.get(i);

            // set attributes of chip
            Chip selectedChip = allChipsGroup.findViewById(id);
            Chip newChip = new Chip(context);
            newChip.setText(selectedChip.getText().toString());
            newChip.setCloseIconVisible(true);

            // listener to refilter notes list when item is closed
            newChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // reset views
                    currentNotes.clear();
                    adapter.setNotesFull(currentNotes);

                    // set chip to unselected
                    allChips.put(selectedChip.getText().toString(), false);
                    // remove chip from scroll view
                    chipGroup.removeView(newChip);
                    // remove id from checkedChipIds
                    checkedChipIds.remove(index);

                    // check if there are no more chips selected
                    if (checkedChipIds.size() == 0) {
                        // reset notes list to default
                        currentNotes.clear();
                        for (Map.Entry<String, Note> note : allNotes.entrySet()) {
                            currentNotes.add(note.getValue());
                        }
                        adapter.getFilter().filter(searchView.getQuery());
                        chipGroup.removeAllViews();
                    } else {
                        // refilter notes list
                        Set<String> chippedNoteIds = new TreeSet<>();
                        Firebase.getChippedNotes(checkedChipIds, allChipsGroup, adapter, searchView, allNotes, currentNotes, chippedNoteIds);
                    }
                }
            });

            chipGroup.addView(newChip);
        }
    }
}
