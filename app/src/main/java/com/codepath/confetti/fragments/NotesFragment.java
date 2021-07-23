package com.codepath.confetti.fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.codepath.confetti.MainActivity;
import com.codepath.confetti.utlils.Firebase;
import com.codepath.confetti.R;
import com.codepath.confetti.adapters.NotesAdapter;
import com.codepath.confetti.databinding.FragmentNotesBinding;
import com.codepath.confetti.utlils.Chips;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.utlils.NanonetsApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotesFragment extends Fragment {

    public static final String TAG = "NotesFragment";

    private FragmentNotesBinding binding;
    protected NotesAdapter adapter;
    private RecyclerView rvNotes;
    protected List<Note> currentNotes;
    protected Map<String, Note> allNotes;

    protected SearchView searchView;
    private ImageView ivSearchToggle;

    protected ChipGroup chipGroup;
    protected Map<String, Boolean> allChips;
    protected Set<String> currentChips;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NotesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotesFragment newInstance(String param1, String param2) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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

        currentNotes = new ArrayList<>();
        allNotes = new TreeMap<>();
        allChips = new TreeMap<>();
        currentChips = new TreeSet<>();

        rvNotes = binding.rvNotes;
        adapter = new NotesAdapter(getContext(), currentNotes);
        rvNotes.setAdapter(adapter);
        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView = binding.searchView;
        searchView.setQueryHint("Searching for...");
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

        chipGroup = binding.chipGroup;

        ivSearchToggle = binding.ivSearchToggle;
        Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_baseline_label_24);
        ivSearchToggle.setImageDrawable(drawable);
        ivSearchToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "tags!");
                Log.d(TAG, "NOTES LIST allChips size - " + allChips.size());
                ChipsBottomSheetFragment tagFragment = new ChipsBottomSheetFragment(allChips);
                tagFragment.show(getChildFragmentManager(), tagFragment.getTag());
            }
        });

        // Get a reference to our notes
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refNotes = database.getReference("Notes/" + FirebaseAuth.getInstance().getUid() + "/Files");

        // child added - called once  for each existing child, then again every time a new child added
        refNotes.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Log.i(TAG, "note added" + snapshot.toString());
                // create note from firebase database
                Note note = snapshot.getValue(Note.class);
                note.setId(snapshot.getKey());

                currentNotes.add(note);
                allNotes.put(snapshot.getKey(), note);

                // get image ?

                // update currentNotes taking into account filters and query input
                Log.d(TAG, "" + chipGroup.getChildCount());
                if (chipGroup.getChildCount() == 0) {
                    adapter.setNotesFull(currentNotes);
                    adapter.getFilter().filter(searchView.getQuery().toString().trim());
                } else {
                    // TODO: future listener for when you add a chip to a note on creation
                    List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();
                    Log.d(TAG, "CHECKED CHIP IDS FOR NOTES LIST " + checkedChipIds.size());
                    refreshChips(checkedChipIds, chipGroup, false);
                }
            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {
                Log.i(TAG, "note changed " + snapshot.toString());
                Note note = snapshot.getValue(Note.class);
                note.setId(snapshot.getKey());
                note.setImageFile(allNotes.get(snapshot.getKey()).getImageFile());
                // update allNotes
                allNotes.put(snapshot.getKey(), note);

                // update currentNotes
                // check if note is in current notes and refresh currentNotes
                for (int i = 0; i < currentNotes.size(); i++) {
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
                // update adapter within adapter ??

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
            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.i(TAG, "The notes read failed: " + error.getCode());
            }
        });

        // Attach a listener to read the data at our chips reference ONCE
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
}