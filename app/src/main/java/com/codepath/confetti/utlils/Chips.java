package com.codepath.confetti.utlils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.SearchView;

import com.codepath.confetti.R;
import com.codepath.confetti.adapters.NotesAdapter;
import com.codepath.confetti.models.Note;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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

    public static void populateChipsDeletable(Context context, ChipGroup chipGroup, List<Integer> checkedChipIds, Set<Integer> checkedChipIdsSet,
                                              ChipGroup allChipsGroup, NotesAdapter adapter, SearchView searchView,
                                              Map<String, Note> allNotes, List<Note> currentNotes, Map<String, Boolean> allChips) {
        for (int i = 0; i <  checkedChipIds.size(); i++) {
            Integer id = checkedChipIds.get(i);

            // set attributes of chip
            Chip selectedChip = allChipsGroup.findViewById(id);
            Chip newChip = new Chip(context);
            newChip.setText(selectedChip.getText().toString());
            newChip.setCloseIconVisible(true);

            // to get list of them back
            newChip.setChecked(true);

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
                    checkedChipIdsSet.remove(id);

                    // check if there are no more chips selected
                    if (checkedChipIdsSet.size() == 0) {
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
                        Firebase.getChippedNotes(checkedChipIdsSet, allChipsGroup, adapter, searchView, allNotes, currentNotes, chippedNoteIds);
                    }
                }
            });

            chipGroup.addView(newChip);
        }
    }
}
