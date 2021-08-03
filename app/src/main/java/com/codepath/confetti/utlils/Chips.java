package com.codepath.confetti.utlils;

import android.content.Context;
import android.util.Log;
import android.view.View;
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

/**
 * Util class to generate chips with differing properties in a given chipGroup
 */
public class Chips {

    /**
     * Sets the chip text color based on if selected
     * @param newChip
     */
    public static void setChipText(Context context, Chip newChip) {
        Log.d("WHY", "" + newChip.isChecked());
        if (newChip.isChecked()) {
            // chip is now selected !
            newChip.setTextColor(context.getColor(R.color.white));
        } else {
            // chip is not selected !
            newChip.setTextColor(context.getColor(R.color.shrine_pink_900));
        }
    }

    /**
     * Sets the default settings for chips
     * @param newChip
     */
    public static void setChipAppearance(Context context, Chip newChip) {
        // general
        newChip.setChipBackgroundColorResource(R.color.chip_bg_tint);
        newChip.setChipStrokeWidth(UtilsGeneral.convertDpToFloat(2.0f));
        newChip.setChipStrokeColorResource(R.color.chip_stroke_tint);
        newChip.setChipCornerRadius(UtilsGeneral.convertDpToFloat(5.0f));
        newChip.setTextColor(context.getColor(R.color.shrine_pink_100));

        // no checked icon == ugly
        newChip.setCheckedIconVisible(false);
    }

    /**
     * Populate chips just for show, specifically for each note item in notes list and in each hscroll
     * for a note details page
     * @param context
     * @param chipGroup
     * @param chipNames
     */
    public static void populateChips(Context context, ChipGroup chipGroup, List<String> chipNames) {
        for (String chipName : chipNames) {
            Chip newChip = new Chip(context);
            newChip.setText(chipName);
            newChip.setEnabled(false);

            setChipAppearance(context, newChip);
            newChip.setChipStrokeColorResource(R.color.grey);
            newChip.setChipStrokeWidth(UtilsGeneral.convertDpToFloat(1.5f));
            newChip.setTextColor(context.getColor(R.color.grey));

            chipGroup.addView(newChip);
        }
    }

    /**
     * Populate chips that are checkable, specifically for choosing chips to filter notes list
     * @param context
     * @param chipGroup
     * @param allChips
     */
    public static void populateChipsSelectable(Context context, ChipGroup chipGroup, Map<String, Boolean> allChips) {
        for (Map.Entry<String, Boolean> chip : allChips.entrySet()) {
            Chip newChip = new Chip(context);
            newChip.setText(chip.getKey());
            newChip.setCheckable(true);
            setChipAppearance(context, newChip);

            // checks if chip has been previously selected
            if (chip.getValue()) {
                newChip.setChecked(true);
            }

            // set chip text depending on selected or not
            setChipText(context, newChip);

            // listener to update things when ship is selected / unselected
            newChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // update chip text
                    setChipText(context, newChip);

                    // update chips checked state in the treemap
                    allChips.put(chip.getKey(), !chip.getValue());
                }
            });

            chipGroup.addView(newChip);
        }
    }

    /**
     * Populate chips that are deletable, specifically in hscroll of notes list showing which chips to
     * use for filtering
     * @param context
     * @param chipGroup
     * @param checkedChipIds list of (selected for filtering) checkedChipIds
     * @param checkedChipIdsSet set of (selected for filtering) checkedChipIds to make sure no duplicates are added
     * @param allChipsGroup chipGroup of all chips associated with a user
     * @param adapter notes list adapter
     * @param searchView searchView to query for notes
     * @param allNotes list of all notes
     * @param currentNotes list of current pool of notes based on filtering and querying
     * @param allChips
     */
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
            newChip.setCloseIconTintResource(R.color.white);
            newChip.setCheckedIconVisible(false);
            setChipAppearance(context, newChip);

            // to get list of them back
            newChip.setCheckable(true);
            newChip.setChecked(true);

            setChipText(context, newChip);

            // listener to re-filter notes list when item is closed
            newChip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // reset views
                    currentNotes.clear();
                    adapter.setNotesFull(currentNotes);

                    // set chip to unselected IF IT IS STILL IN ALLCHIPS
                    if (allChips.containsKey(selectedChip.getText().toString())) {
                        allChips.put(selectedChip.getText().toString(), false);
                    }

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
