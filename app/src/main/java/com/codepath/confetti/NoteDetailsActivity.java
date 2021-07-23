package com.codepath.confetti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.codepath.confetti.adapters.PredictionSlidePagerAdapter;
import com.codepath.confetti.databinding.ActivityNoteDetailsBinding;
import com.codepath.confetti.fragments.NoteImagesFragment;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.utlils.Chips;
import com.codepath.confetti.utlils.Firebase;
import com.codepath.confetti.utlils.ZoomOutPageTransformer;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.parceler.Parcels;

import java.util.List;

public class NoteDetailsActivity extends AppCompatActivity implements NoteImagesFragment.OnItemSelectedListener{

    public static final String TAG = "NoteDetailsActivity";

    private Note note;

    private Toolbar toolbar;

    private ImageView ivTag;
    private Chip chipAdd;
    private ChipGroup chipGroup;

    private NoteImagesFragment noteImagesFragment;
    private LinearLayout mBottomSheetLayout;
    private BottomSheetBehavior sheetBehavior;
    private ImageView header_Arrow_Image;

    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNoteDetailsBinding binding = ActivityNoteDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // unwrap note
        note = (Note) Parcels.unwrap(getIntent().getParcelableExtra(Note.class.getSimpleName()));
        Log.d(TAG, String.format("Showing details for '%s", note.getName()));

        // set up toolbar
        toolbar = binding.toolbar;
        toolbar.setTitle(note.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // return to main activity
                finish();
            }
        });

        // set up chipping
        ivTag = binding.ivTag;
        chipAdd = binding.chipAdd;
        chipGroup = binding.chipGroup;

        // add chips associated with note
        if (note.getChips() != null) {
            for (String chipName : note.getChips()) {
                Chip newChip = new Chip(NoteDetailsActivity.this);
                newChip.setText(chipName);
                newChip.setChecked(true);

                // listener for popup
                newChip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // display popup
                        displayChipPopupWindow(NoteDetailsActivity.this, view);
                    }
                });

                chipGroup.addView(newChip);
            }
        }

        Drawable drawable = AppCompatResources.getDrawable(NoteDetailsActivity.this, R.drawable.ic_baseline_label_24);
        ivTag.setImageDrawable(drawable);
        chipAdd.setText("Add tag");
        chipAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "create a new tag!");
                // user types in tag name and it is added to chip group
            }
        });

        // attach hscroll of note images
        noteImagesFragment = new NoteImagesFragment(note);
        getSupportFragmentManager().beginTransaction().add(R.id.flNoteImages, noteImagesFragment).commit();

        // set up prediction info bottom sheet
        initPredictions(view);
    }

    // popup window to delete chip from notes
    private void displayChipPopupWindow(Context context, View anchorView) {
        PopupWindow popup = new PopupWindow(context);
        View layout = getLayoutInflater().inflate(R.layout.popup_content_chip, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAsDropDown(anchorView);

        // click listener to delete chip
        TextView tvDeleteChip = layout.findViewById(R.id.tvDeleteChip);
        tvDeleteChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "deleting chip");
                popup.dismiss();
                chipGroup.removeView(anchorView);

                // remove chip from note chips
                List<String> chipNames = note.getChips();
                String chipName = ((Chip) anchorView).getText().toString();
                int index = 0;
                boolean foundChip = false;
                while(index < chipNames.size() && !foundChip) {
                    if (chipName.equals(chipNames.get(index))) {
                        chipNames.remove(index);
                        foundChip = true;
                    }
                }

                // remove chip in firebase from note AND chip databases
                Log.d(TAG, "updating note chips in firebase");
                Firebase.deleteChipRef(context, note, chipName);
            }
        });
    }

    // initializes all the predictions into bottom sheet
    private void initPredictions(View view) {
        mBottomSheetLayout = view.findViewById(R.id.bottom_sheet_layout);
        sheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);
        header_Arrow_Image = view.findViewById(R.id.bottom_sheet_arrow);

        // listener to expand / collapse dialog
        header_Arrow_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

            }
        });

        // listen to bottom sheet behavior
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                header_Arrow_Image.setRotation(slideOffset * 180);
            }
        });

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pagerPredictions);
        // check if there are even predictions
        if (note.getPredictions() != null) {
            pagerAdapter = new PredictionSlidePagerAdapter(this, note.getPredictions());
        }
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
    }

    // for viewPager
    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    // action to take in the activity when pin in fragment is tapped
    @Override
    public void onRssItemSelected(int index) {
        Log.d(TAG, "scrolling to index " + index);
        viewPager.setCurrentItem(index, true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
}