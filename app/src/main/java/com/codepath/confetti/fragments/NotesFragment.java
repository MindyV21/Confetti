package com.codepath.confetti.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;

import com.codepath.confetti.Firebase;
import com.codepath.confetti.R;
import com.codepath.confetti.adapters.NotesAdapter;
import com.codepath.confetti.databinding.FragmentNotesBinding;
import com.codepath.confetti.models.Note;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
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
    private NotesAdapter adapter;
    private RecyclerView rvNotes;
    protected List<Note> currentNotes;
    protected Map<String, Note> allNotes;

    private SearchView searchView;
    private ImageView ivSearchToggle;

    private ChipGroup chipGroup;
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
        searchView.setQueryHint("Search by name");
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
                ChipsBottomSheetFragment tagFragment = new ChipsBottomSheetFragment();
                tagFragment.show(getChildFragmentManager(), tagFragment.getTag());
            }
        });

        // Get a reference to our notes
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refNotes = database.getReference("Notes/" + FirebaseAuth.getInstance().getUid() + "/Files");

        // Attach a listener to read the data at our notes reference
        refNotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, dataSnapshot.toString());
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                for (DataSnapshot data : iterable) {
                    Log.i(TAG, data.toString());
                    Note note = data.getValue(Note.class);
                    currentNotes.add(note);
                    allNotes.put(data.getKey(), note);
                }
                Log.i(TAG, "currentNotes: " + currentNotes.size());
                Log.i(TAG, "allNotes: " + allNotes.size());
                adapter.getFilter().filter(searchView.getQuery());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.i(TAG, "The note read failed: " + error.getCode());
            }
        });

        // Attach a listener to read the data at our chips reference ONCE
        DatabaseReference refChips = database.getReference("Chips/" + FirebaseAuth.getInstance().getUid());
        refChips.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                // populate allChips
                Log.i(TAG, snapshot.toString());
                Iterable<DataSnapshot> iterable = snapshot.getChildren();
                for (DataSnapshot data : iterable) {
                    Log.i(TAG, "CHIPS " + data.toString());
                    // false because on startup no chips are selected
                    allChips.put(data.getKey(), false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Log.i(TAG, "The chip read failed: " + error.getCode());
            }
        });

        // TODO: Attach a onChildAdded/Changed/Removed listener to read the data at our chips reference
    }

    protected void addChip(Chip chip) {
        Log.d(TAG, chip.getText().toString());
        chipGroup.addView(chip);
    }

    private void hardcodeChips() {
        ArrayList<String> list = new ArrayList<>();
        list.add("522cdc69-e4e5-11eb-93ae-e2f76a726d2b"); // baguette
        list.add("844f05c1-e670-11eb-bff2-d2e268007c8a"); // brioche
        list.add("aa54e6b2-e67f-11eb-81a9-d2e268007c8a"); // croissant
        list.add("d110063c-e67b-11eb-af1e-9a1974bea1bf"); // boule de pain

        Firebase.createChip("french breads", list);
    }
}