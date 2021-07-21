package com.codepath.confetti;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.codepath.confetti.databinding.ActivityNoteDetailsBinding;
import com.codepath.confetti.models.Note;

import org.parceler.Parcels;

public class NoteDetailsActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private Note note;

    private TextView tvNoteName;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNoteDetailsBinding binding = ActivityNoteDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // unwrap note
        note = (Note) Parcels.unwrap(getIntent().getParcelableExtra(Note.class.getSimpleName()));
        Log.d(TAG, String.format("Showing details for '%s", note.getName()));

        tvNoteName = binding.tvNoteName;
        tvNoteName.setText(note.getName());

        toolbar = binding.toolbar;
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
    }
}