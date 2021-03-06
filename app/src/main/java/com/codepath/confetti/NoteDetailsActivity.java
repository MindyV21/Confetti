package com.codepath.confetti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.codepath.confetti.adapters.PredictionSlidePagerAdapter;
import com.codepath.confetti.databinding.ActivityNoteDetailsBinding;
import com.codepath.confetti.fragments.AddChipDialogFragment;
import com.codepath.confetti.fragments.CreatePredictionBottomSheetFragment;
import com.codepath.confetti.fragments.NoteImagesFragment;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.codepath.confetti.utlils.Animations;
import com.codepath.confetti.utlils.Chips;
import com.codepath.confetti.utlils.UtilsGeneral;
import com.codepath.confetti.utlils.Firebase;
import com.codepath.confetti.utlils.ZoomOutPageTransformer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Activity for a specific note details
 */
public class NoteDetailsActivity extends AppCompatActivity
        implements NoteImagesFragment.OnItemSelectedListener, AddChipDialogFragment.AddChipDialogListener, PredictionSlidePagerAdapter.UpdatePredictions,
        CreatePredictionBottomSheetFragment.CreatePredictionListener {

    public static final String TAG = "NoteDetailsActivity";
    private ActivityNoteDetailsBinding binding;

    // fullscreen
    private boolean mVisible;
    private View mContentView;
    private View mControlsViewHeader;
    private View mControlsPredictions;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private Note note;

    private Toolbar toolbar;
    private TextView tvToolbar;

    // chips
    private RelativeLayout relLayoutTags;
    private ImageView ivTag;
    private Chip chipAdd;
    private ChipGroup chipGroup;

    // note image file
    private FrameLayout flNoteImages;
    private NoteImagesFragment noteImagesFragment;

    // predictions
    private LinearLayout mBottomSheetLayout;
    private BottomSheetBehavior sheetBehavior;
    private ImageView header_Arrow_Image;

    private ViewPager2 viewPager;
    private PredictionSlidePagerAdapter pagerAdapter;

    // loading ui
    private GifImageView nellieConfetti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // unwrap note
        note = (Note) Parcels.unwrap(getIntent().getParcelableExtra(Note.class.getSimpleName()));
        Log.d(TAG, String.format("Showing details for '%s", note.getName()));

        // attach fragment of note images
        flNoteImages = binding.flNoteImages;
        noteImagesFragment = NoteImagesFragment.newInstance(note);
        getSupportFragmentManager().beginTransaction().add(R.id.flNoteImages, noteImagesFragment).commit();

        // init listeners, bind data, etc.
        initToolbar();
        initChips();
        initPredictions(view);
        initFullscreen();
    }

    /**
     * Init chips in horizontal scroll view
     */
    private void initChips() {
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
        chipAdd.setChipIconTintResource(R.color.shrine_pink_900);
        chipAdd.setChecked(true);
        chipAdd.setTextColor(getColor(R.color.shrine_pink_900));
        Chips.setChipAppearance(this, chipAdd);
        chipAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "create a new tag!");
                showAddChipDialog();
            }
        });
    }

    /**
     * Init toolbar
     */
    private void initToolbar() {
        toolbar = binding.toolbar;
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // return to main activity
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });

        tvToolbar = binding.tvToolbar;
        tvToolbar.setText(note.getName());
    }

    /**
     * Init fullscreen
     */
    private void initFullscreen() {
        mVisible = true;
        mContentView = binding.flNoteImages;
        mControlsViewHeader = binding.relLayoutHeader;
        mControlsPredictions = binding.coordinatorLayoutPredictions;
    }

    /**
     * Creates a new chip view to be places in chip group
     * @param chipName
     */
    private void createNewChip(String chipName) {
        Chip newChip = new Chip(NoteDetailsActivity.this);
        newChip.setText(chipName);
        newChip.setTextColor(getColor(R.color.shrine_pink_900));
        newChip.setChecked(true);
        Chips.setChipAppearance(this, newChip);

        // listener for popup
        newChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // alert dialog to delete a chip
                new AlertDialog.Builder(NoteDetailsActivity.this)
                        .setMessage("Delete tag '" + newChip.getText().toString() + "'?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "deleting chip " + newChip.getText().toString());
                                chipGroup.removeView(view);

                                // remove chip from note chips
                                List<String> chipNames = note.getChips();
                                String chipName = ((Chip) view).getText().toString();
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
                                Firebase.deleteChipRef(NoteDetailsActivity.this, note, chipName);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        chipGroup.addView(newChip);
    }

    /**
     * Dialog for adding a new chip to a note
     */
    private void showAddChipDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddChipDialogFragment addChipDialogFragment = new AddChipDialogFragment();
        addChipDialogFragment.show(fragmentManager, "fragment_add_chip");
    }

    /**
     * Init predictions for a note
     * @param view
     */
    private void initPredictions(View view) {
        // loading UI
        nellieConfetti = view.findViewById(R.id.nellieConfetti);
        if (note.getPredictions() == null || note.getPredictions().size() == 0) {
            Animations.fadeIn(nellieConfetti);
        } else {
            Animations.fadeOut(nellieConfetti);
        }

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
        pagerAdapter = new PredictionSlidePagerAdapter(this, note);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
    }

    /**
     * Toggle for hiding / showing UI when going fullscreen
     */
    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    /**
     * Hide UI when entering fullscreen mode setup
     */
    private void hide() {
        mVisible = false;

        // Schedule a runnable to remove UI after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Show UI when entering fullscreen mode setup
     */
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Hide runnable for UI part 1
     */
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Hide runnable for UI part 2
     */
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            Activity activity = NoteDetailsActivity.this;
            if (activity != null
                    && activity.getWindow() != null) {
                activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            }

            Animations.slideUp((View) mControlsViewHeader, 0);
            Animations.slideDown((View) mControlsPredictions, 0);

            // reset note image padding
            flNoteImages.setPadding(0, 0, 0, 0);
        }
    };

    /**
     * Show runnable for UI part 2
     */
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // show status bar
            int flags = View.SYSTEM_UI_FLAG_VISIBLE;

            Activity activity = NoteDetailsActivity.this;
            if (activity != null
                    && activity.getWindow() != null) {
                activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            }

            Animations.reverseSlideUp((View) mControlsViewHeader);
            Animations.reverseSlideDown((View) mControlsPredictions);

            // reset note image padding
            flNoteImages.setPadding((int) UtilsGeneral.convertFloatToDp(10.0f), 0, (int) UtilsGeneral.convertFloatToDp(10.0f), (int) UtilsGeneral.convertFloatToDp(40.0f));
        }
    };

    /**
     * Touch listener for hiding UI
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_details_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_fullscreen) {
            Log.i(TAG, "fullscreen");
            toggle();
            noteImagesFragment.enableFullscreen();

            // to consume menu item
            return true;
        } else if (item.getItemId() == R.id.action_create_prediction) {
            Log.i(TAG, "create prediction");
            // attach fragment of note predictions
            CreatePredictionBottomSheetFragment createPredictionBottomSheetFragment = CreatePredictionBottomSheetFragment.newInstance(note);
            createPredictionBottomSheetFragment.show(getSupportFragmentManager(), createPredictionBottomSheetFragment.getTag());
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Resets viewPager onBackPressed
     */
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

    /**
     * Pin on image file in noteImagesFragment is tapped, scroll to respective location in viewPager
     * @param index
     */
    @Override
    public void onPinItemSelected(int index) {
        Log.d(TAG, "scrolling to index " + index);
        viewPager.setCurrentItem(index, true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * On exiting fullscreen in noteImagesFragment, shows UI
     */
    @Override
    public void onExitFullscreen() {
        toggle();
    }

    /**
     * On creating a new chip in addChipDialogFragment, updates chip group with new chip,
     * and updates firebase notes / chips database
     * @param inputText
     */
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

    /**
     * Upon removing a pin on the image file in noteImagesFragment
     * @param prediction
     */
    @Override
    public void removePinFromImage(Prediction prediction) {
        noteImagesFragment.removePin(prediction);
        if (note.getPredictions().size() == 0) {
            Animations.fadeIn(nellieConfetti);
        }
    }

    /**
     * WHen adding a pin prediction to a note, updates viewPager and adapter
     * @param firstPrediction
     */
    @Override
    public void addPinToImage(Boolean firstPrediction) {
        noteImagesFragment.addPin();
        if (firstPrediction) {
            pagerAdapter.setPredictions(note);
        } else {
            pagerAdapter.notifyItemInserted(note.getPredictions().size() - 1);
        }

        Animations.fadeOut(nellieConfetti);
    }
}