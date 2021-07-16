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

import com.codepath.confetti.NanonetsApi;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private List<Note> allNotes;

    private SearchView searchView;
    private ImageView ivSearchToggle;

    private ChipGroup chipGroup;
    protected Set<Chip> chips;

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

        allNotes = new ArrayList<>();

        rvNotes = binding.rvNotes;
        adapter = new NotesAdapter(getContext(), allNotes);
        rvNotes.setAdapter(adapter);
        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));

        searchView = binding.searchView;
        searchView.setQueryHint("Search your notes");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // fetchNotes(query)

                // reset SearchView
                searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setIconified(true);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        chipGroup = binding.chipGroup;

        //dummy chips
        Chip one = new Chip(getContext());
        one.setText("Cat");
        one.setCheckable(true);
        chipGroup.addView(one);

        ivSearchToggle = binding.ivSearchToggle;
        Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_baseline_label_24);
        ivSearchToggle.setImageDrawable(drawable);
        ivSearchToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "tags!");
                TagsBottomSheetFragment tagFragment = new TagsBottomSheetFragment();
                tagFragment.show(getChildFragmentManager(), tagFragment.getTag());
            }
        });

//        dummyData();
    }

    protected void dummyData() {
        allNotes.add(new Note("hello"));
        allNotes.add(new Note("my"));
        allNotes.add(new Note("name"));
        allNotes.add(new Note("is"));
        allNotes.add(new Note("bread"));
        allNotes.add(new Note("!"));

        adapter.notifyDataSetChanged();

    }

    protected void clearNotes() {
        chipGroup.removeAllViews();
        allNotes.clear();
        adapter.notifyDataSetChanged();
    }

    protected void addChip(Chip chip) {
        Log.d(TAG, chip.getText().toString());
        chipGroup.addView(chip);
    }
}