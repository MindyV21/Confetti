package com.codepath.confetti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codepath.confetti.databinding.ActivityMainBinding;
import com.codepath.confetti.fragments.NotesFragment;
import com.codepath.confetti.fragments.SettingsBottomSheetFragment;
import com.codepath.confetti.fragments.UploadBottomSheetFragment;
import com.codepath.confetti.models.Note;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

/**
 * Activity for viewing and creating notes
 */
public class MainActivity extends AppCompatActivity implements NotesFragment.NotesFragmentListener {

    private FirebaseAuth mAuth;
    public static final String TAG = "MainActivity";

    private Toolbar toolbar;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private NotesFragment notesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        mAuth = FirebaseAuth.getInstance();
        Log.i(TAG, "" + mAuth.getCurrentUser());

        // set up toolbar
        toolbar = binding.toolbar;
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        // set up notes list
        notesFragment = new NotesFragment();
        fragmentManager.beginTransaction().add(R.id.flContainer, notesFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Log.i(TAG, "settingsss");

            SettingsBottomSheetFragment tagFragment = new SettingsBottomSheetFragment();
            tagFragment.show(getSupportFragmentManager(), tagFragment.getTag());

            // to consume menu item
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called upon completion of uploading a new note to firebase, and
     * dismisses uploadBottomSheetFragment in the NotesFragment
     * @param note the newly created note
     */
    public void dismissCreateNote(Note note) {
        notesFragment.dismissCreateNote(note);
    }

    @Override
    public void goToNote(Note note) {
        Log.d(TAG, "note details of " + note.getName());
        Intent intent = new Intent(MainActivity.this, NoteDetailsActivity.class);
        intent.putExtra(Note.class.getSimpleName(), Parcels.wrap(note));
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
}