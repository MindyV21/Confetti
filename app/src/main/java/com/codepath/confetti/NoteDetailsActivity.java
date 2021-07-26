package com.codepath.confetti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.codepath.confetti.adapters.PredictionSlidePagerAdapter;
import com.codepath.confetti.databinding.ActivityNoteDetailsBinding;
import com.codepath.confetti.fragments.AddChipDialogFragment;
import com.codepath.confetti.fragments.NoteImagesFragment;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.codepath.confetti.utlils.Chips;
import com.codepath.confetti.utlils.Firebase;
import com.codepath.confetti.utlils.ZoomOutPageTransformer;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.circularreveal.CircularRevealFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class NoteDetailsActivity extends AppCompatActivity
        implements NoteImagesFragment.OnItemSelectedListener, AddChipDialogFragment.AddChipDialogListener, PredictionSlidePagerAdapter.UpdatePredictions {

    public static final String TAG = "NoteDetailsActivity";

    private Note note;

    private Toolbar toolbar;

    private RelativeLayout relLayoutTags;
    private ImageView ivTag;
    private Chip chipAdd;
    private ChipGroup chipGroup;

    private NoteImagesFragment noteImagesFragment;

    private FloatingActionButton fabCreatePrediction;
    private CircularRevealFrameLayout sheetCreatePrediction;
    private TabLayout tabLayoutCreatePrediction;
    private ImageView ivCancel;

    private LinearLayout mBottomSheetLayout;
    private BottomSheetBehavior sheetBehavior;
    private ImageView header_Arrow_Image;

    private ViewPager2 viewPager;
    private PredictionSlidePagerAdapter pagerAdapter;

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
        relLayoutTags = binding.relLayoutTags;
        ivTag = binding.ivTag;
        chipAdd = binding.chipAdd;
        chipGroup = binding.chipGroup;

        // add chips associated with note
        if (note.getChips() != null) {
            for (String chipName : note.getChips()) {
                createNewChip(chipName);
            }
        }

        // add a new chip function
        Drawable drawable = AppCompatResources.getDrawable(NoteDetailsActivity.this, R.drawable.ic_baseline_label_24);
        ivTag.setImageDrawable(drawable);
        chipAdd.setText("Add tag");
        chipAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "create a new tag!");
                showAddChipDialog();
            }
        });

        // attach hscroll of note images
        noteImagesFragment = new NoteImagesFragment(note);
        getSupportFragmentManager().beginTransaction().add(R.id.flNoteImages, noteImagesFragment).commit();

        // set up prediction info bottom sheet
        fabCreatePrediction = binding.fabCreatePrediction;
        sheetCreatePrediction = binding.sheetCreatePrediction;
        ivCancel = binding.ivCancel;
        tabLayoutCreatePrediction = view.findViewById(R.id.tabLayoutCreatePrediction);
        mBottomSheetLayout = view.findViewById(R.id.bottom_sheet_layout);
        sheetBehavior = BottomSheetBehavior.from(mBottomSheetLayout);
        header_Arrow_Image = view.findViewById(R.id.bottom_sheet_arrow);

        initPredictions();
    }

    private void createNewChip(String chipName) {
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

    private void showAddChipDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddChipDialogFragment addChipDialogFragment = AddChipDialogFragment.newInstance("test");
        addChipDialogFragment.show(fragmentManager, "fragment_add_chip");
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
                    index++;
                }

                // remove chip in firebase from note AND chip databases
                Log.d(TAG, "updating note chips in firebase");
                Firebase.deleteChipRef(context, note, chipName);
            }
        });
    }

    // initializes all the prediction features
    private void initPredictions() {
        // to exit creating a prediction
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // exit out of create prediction view
                fabCreatePrediction.setExpanded(false);

                // show chips and prediction views
                relLayoutTags.setVisibility(View.VISIBLE);
                mBottomSheetLayout.setVisibility(View.VISIBLE);
            }
        });

        // to start creating a prediction
        fabCreatePrediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // expand create predictions menu
                fabCreatePrediction.setExpanded(true);

                // hide chip and predictions layout
                relLayoutTags.setVisibility(View.GONE);
                mBottomSheetLayout.setVisibility(View.GONE);
            }
        });

        // tab selection changes
        tabLayoutCreatePrediction.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

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
                // hide fab when sliding, fab shown when sheet collapsed completely
                if (BottomSheetBehavior.STATE_DRAGGING == newState || BottomSheetBehavior.STATE_EXPANDED == newState) {
                    fabCreatePrediction.animate().scaleX(0).scaleY(0).setDuration(300).start();
                } else if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    fabCreatePrediction.animate().scaleX(1).scaleY(1).setDuration(300).start();
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                header_Arrow_Image.setRotation(slideOffset * 180);
            }
        });

        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.pagerPredictions);
        pagerAdapter = new PredictionSlidePagerAdapter(this, note);
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
    public void onPinItemSelected(int index) {
        Log.d(TAG, "scrolling to index " + index);
        viewPager.setCurrentItem(index, true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    // updates activity with new tag and updates notes / chips database
    @Override
    public void onFinishAddChipDialog(String inputText) {
        Log.d(TAG, "adding chip to note");
        // populate in hscroll
        createNewChip(inputText);

        // check for if note currently has no chips
        if (note.getChips() == null) {
            note.setChips(new ArrayList<>());
        }

        // add chip manually to note's chip list
        note.getChips().add(inputText);

        // update notes + chips database
        Firebase.addChipRef(NoteDetailsActivity.this, note, inputText);
    }

    // removes a pin from note image
    @Override
    public void removePinFromImage(Prediction prediction) {
        noteImagesFragment.removePin(prediction);
    }
}