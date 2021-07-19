package com.codepath.confetti.models;

import android.content.Context;
import android.view.View;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.Map;

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
    public static void populateChipsDeletable() {

    }
}
