package com.codepath.confetti.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.confetti.NoteDetailsActivity;
import com.codepath.confetti.R;
import com.codepath.confetti.adapters.NotesAdapter;
import com.codepath.confetti.databinding.FragmentNotesBinding;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.utlils.Animations;
import com.codepath.confetti.utlils.Chips;
import com.codepath.confetti.utlils.Firebase;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import pl.droidsonroids.gif.GifImageView;

/**
 * Fragment containing the list of notes for a user
 */
public class NotesFragment extends Fragment {

    public static final String TAG = "NotesFragment";
    private FragmentNotesBinding binding;

    // firebase
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();

    // loading UI
    private GifImageView nellieConfetti;

    // notes list
    protected NotesAdapter adapter;
    private RecyclerView rvNotes;
    protected List<Note> currentNotes;
    protected Map<String, Note> allNotes;

    // search and filter
    protected SearchView searchView;
    private ImageView ivChipToggle;

    // chips
    protected ChipGroup chipGroup;
    protected TreeMap<String, Boolean> allChips;
    protected Set<String> currentChips;

    // create note
    private FloatingActionButton fabCreateNote;
    private UploadBottomSheetFragment uploadBottomSheetFragment;

    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nellieConfetti = binding.nellieConfetti;

        currentNotes = new ArrayList<>();
        allNotes = new TreeMap<>();
        allChips = new TreeMap<>();
        currentChips = new TreeSet<>();

        rvNotes = binding.rvNotes;
        adapter = new NotesAdapter(getContext(), currentNotes);
        rvNotes.setAdapter(adapter);
        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView = binding.searchView;
        ivChipToggle = binding.ivChipToggle;
        chipGroup = binding.chipGroup;

        fabCreateNote = binding.fabCreateNote;

        initSearchBar();
        initNotesList();
        initChips();
        initCreateNote();
    }

    /**
     * listener to read data at chips reference
     */
    private void initChips() {
        DatabaseReference refChips = database.getReference("Chips/" + FirebaseAuth.getInstance().getUid());
        refChips.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Log.i(TAG, "chip added " + snapshot.toString());
                allChips.put(snapshot.getKey(), false);
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Log.i(TAG, "chip changed " + snapshot.toString());
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                Log.i(TAG, "chip removed " + snapshot.toString());
                String chipName = snapshot.getKey();

                // remove chip from list of all chips
                allChips.remove(chipName);
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.i(TAG, "The chip read failed: " + error.getCode());
            }
        });
    }

    /**
     * listener to read data at notes reference
     */
    private void initNotesList() {
        DatabaseReference refNotes = database.getReference("Notes/" + FirebaseAuth.getInstance().getUid() + "/Files");
        refNotes.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Log.i(TAG, "note added" + snapshot.toString());
                Animations.fadeOut(nellieConfetti);

                // create note from firebase database
                Note note = snapshot.getValue(Note.class);
                note.setId(snapshot.getKey());

//                // get image
//                Firebase.getImage(note);

                // update notes list
                currentNotes.add(note);
                allNotes.put(snapshot.getKey(), note);

                // update currentNotes taking into account filters and query input
                Log.d(TAG, "" + chipGroup.getChildCount());
                if (chipGroup.getChildCount() == 0) {
                    adapter.setNotesFull(currentNotes);
                    adapter.getFilter().filter(searchView.getQuery().toString().trim());
                } else {
                    List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();
                    Log.d(TAG, "CHECKED CHIP IDS FOR NOTES LIST " + checkedChipIds.size());
                    refreshChips(checkedChipIds, chipGroup, false);
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Log.i(TAG, "note changed " + snapshot.toString());

                // replace note in allNotes
                Note note = snapshot.getValue(Note.class);
                note.setId(snapshot.getKey());
                note.setImageFile(allNotes.get(snapshot.getKey()).getImageFile());
                allNotes.put(snapshot.getKey(), note);

                // update currentNotes
                for (int i = 0; i < currentNotes.size(); i++) {
                    // check if note is in current notes and refresh currentNotes
                    if (note.getId().equals(currentNotes.get(i).getId())) {
                        currentNotes.set(i, note);
                        if (chipGroup.getChildCount() == 0) {
                            Log.d(TAG, "refresh notes list");
                            adapter.setNotesFull(currentNotes);
                            adapter.getFilter().filter(searchView.getQuery().toString().trim());
                        } else {
                            Log.d(TAG, "refresh notes list with chips");
                            List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();
                            refreshChips(checkedChipIds, chipGroup, false);
                        }
                        return;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {
                Log.i(TAG, "note removed " + snapshot.toString());
                String id = snapshot.getKey();

                // check if note is in current notes and remove
                for (int i = 0; i < currentNotes.size(); i++) {
                    if (id.equals(currentNotes.get(i).getId())) {
                        currentNotes.remove(i);
                        return;
                    }
                }

                // remove note from allNotes
                allNotes.remove(id);
                adapter.setNotesFull(currentNotes);

                // loading ui
                if (allNotes.isEmpty()) {
                    Animations.fadeIn(nellieConfetti);
                }
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.i(TAG, "The notes read failed: " + error.getCode());
            }
        });
    }

    /**
     * set up searching features
     */
    private void initSearchBar() {
        // set up searchView for query with adapter
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });

        // set up toggle to open chips bottom sheet fragment
        Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_baseline_label_24);
        ivChipToggle.setImageDrawable(drawable);
        ivChipToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "tags!");
                Log.d(TAG, "NOTES LIST allChips size - " + allChips.size());
                ChipsBottomSheetFragment tagFragment = ChipsBottomSheetFragment.newInstance(allChips);
                tagFragment.show(getChildFragmentManager(), tagFragment.getTag());
            }
        });
    }

    /**
     * set up create note function
     */
    private void initCreateNote() {
        // set up create note fab
        fabCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "create note !");
                uploadBottomSheetFragment = new UploadBottomSheetFragment();
                uploadBottomSheetFragment.show(getChildFragmentManager(), uploadBottomSheetFragment.getTag());
            }
        });
    }

    /**
     * Called by nested fragment to update notes list with query input and new chips selected
     * @param checkedChipIds chips selected in chip bottom sheet fragment
     * @param allChipsGroup chip group in chip bottom sheet fragment
     * @param resetChips true to reset hscroll of chips, false otherwise
     */
    protected void refreshChips(List<Integer> checkedChipIds, ChipGroup allChipsGroup, Boolean resetChips) {
        Log.i(TAG, "updating chips and currentNotes like based on chips selected");

        // copy over ids for retrieving corresponding chip files - use list only to populate scroll view
        Set<Integer> checkedChipIdsSet = new TreeSet<>();
        for (Integer i : checkedChipIds) {
            checkedChipIdsSet.add(i);
        }

        // change currentNotes to contain notes with these selected chips
        currentNotes.clear();
        adapter.setNotesFull(currentNotes);
        Set<String> chippedNotes = new TreeSet<>();
        Firebase.getChippedNotes(checkedChipIdsSet, allChipsGroup, adapter, searchView, allNotes, currentNotes, chippedNotes);

        if (resetChips) {
            // populate parent fragment chip group with all chips selected
            Chips.populateChipsDeletable(getContext(), chipGroup, checkedChipIds, checkedChipIdsSet, allChipsGroup, adapter, searchView, allNotes, currentNotes, allChips);
        }
    }

    /**
     * Called upon completion of uploading a new note to firebase,
     * dismisses uploadBottomSheetFragment, and start intent for the new note's details page
     * @param note the newly created note
     */
    public void dismissCreateNote(Note note) {
        uploadBottomSheetFragment.dismiss();

        // got to new note details activity after a delay
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNote(note);
            }
        }, 1000);
    }

    /**
     * Intent to go to NoteDetailsActivity for clicked note
     * @param note
     */
    private void goToNote(Note note) {
        Intent intent = new Intent(getContext(), NoteDetailsActivity.class);
        intent.putExtra(Note.class.getSimpleName(), Parcels.wrap(note));
        getContext().startActivity(intent);
    }
}