package com.codepath.confetti;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.codepath.confetti.databinding.ActivityNoteDetailsBinding;
import com.codepath.confetti.fragments.NoteImagesFragment;
import com.codepath.confetti.models.Note;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.parceler.Parcels;

public class NoteDetailsActivity extends AppCompatActivity {

    public static final String TAG = "NoteDetailsActivity";

    private Note note;

    private Toolbar toolbar;

    private ImageView ivTag;
    private Chip chipAdd;
    private ChipGroup chipGroup;

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
        getSupportFragmentManager().beginTransaction().add(R.id.flNoteImages, new NoteImagesFragment(note)).commit();
    }
}