package com.codepath.confetti.models;

import android.content.Context;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Map;

public class Chips {

    // create chips that are selectable
    public static void populateChipsSelectable(Context context, ChipGroup chipGroup, Map<String, Boolean> allChips) {
        for (Map.Entry<String, Boolean> chip : allChips.entrySet()) {
            Chip newChip = new Chip(context);
            newChip.setText(chip.getKey());
            newChip.setCheckable(true);
            chipGroup.addView(newChip);
        }
    }

    // create chips that can be deleted
    public static void populateChipsDeletable(Context context, ChipGroup chipGroup, Map<String, Boolean> allChips) {
        for (Map.Entry<String, Boolean> chip : allChips.entrySet()) {
            Chip newChip = new Chip(context);
            newChip.setText(chip.getKey());
            newChip.setCheckable(true);
            chipGroup.addView(newChip);
        }
    }
}
