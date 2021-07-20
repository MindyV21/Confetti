package com.codepath.confetti.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.codepath.confetti.R;
import com.codepath.confetti.fragments.NoteDetailsFragment;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> implements Filterable {

    public static final String TAG = "NotesAdapter";

    // This object helps you save/restore the open/close state of each view
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    private Context context;
    private List<Note> notes;
    private List<Note> notesFull;

    // notesFull is the current pool of available notes
    // notes is the filtered notes from notesFull
    public NotesAdapter(Context context, List<Note> notesFull) {
        this.context = context;
        notes = new ArrayList<>(notesFull);
        this.notesFull = notesFull;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note_swipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.ViewHolder holder, int position) {
        Note note = notes.get(position);

        viewBinderHelper.setOpenOnlyOne(true);
        // Save/restore the open/close state.
        viewBinderHelper.bind(holder.swipeRevealLayout, String.valueOf(note.getName()));
        viewBinderHelper.closeLayout(String.valueOf(note.getName()));

        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotesFull(List<Note> notesFull) {
        this.notesFull = notesFull;
    }

    public List<Note> getNotesFull() {
        return notesFull;
    }

    // filtering currentNotes in searchView
    @Override
    public Filter getFilter() {
        return notesFilter;
    }

    private Filter notesFilter = new Filter() {
        // returns filtered list of notes to publishResults method
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // auto run on background thread
            List<Note> filteredList = new ArrayList<>();
            Log.i(TAG, "NOTES FULL SIZE: " + notesFull.size());

            // checks if there is searchView input
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(notesFull);
            } else {
                Set<String> filteredSet = new TreeSet<>();
                String filterPattern = constraint.toString().toLowerCase().trim();

                // loop through all of the available (chipped) notes
                for (Note note : notesFull) {
                    if (note.getName().toLowerCase().contains(filterPattern)) {
                        // checks through note names
                        Log.i(TAG, "ADDED " + note.getName());
                        filteredList.add(note);
                        filteredSet.add(note.getName());
                    } else {
                        // filter through predictions list
                        List<Prediction> predictions = note.getPredictions();
                        for (Prediction prediction : predictions) {
                            if (prediction.text.toLowerCase().contains(filterPattern)) {
                                // checks if note has already been added before
                                if (!filteredSet.contains(note.getName())) {
                                    Log.i(TAG, "ADDED (by keyword) " + note.getName());
                                    filteredList.add(note);
                                }
                                filteredSet.add(note.getName());
                            }
                        }
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            Log.i(TAG, "publishing query results");
            notes.clear();
            notes.addAll((List) filterResults.values);
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvNoteName;
        private ImageView ivImage;
        private ImageView ivDelete;
        private SwipeRevealLayout swipeRevealLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // connect vars to layout
            tvNoteName = itemView.findViewById(R.id.tvNoteName);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            swipeRevealLayout = itemView.findViewById(R.id.swipeLayout);

            // Attach a click listener to the entire row view
            itemView.setOnClickListener(this);

            // listener to delete
            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "user swiped on " + tvNoteName.getText().toString());
                }
            });
        }

        public void bind(Note note) {
            // bind main layout info
            Log.i(TAG, note.name);
            tvNoteName.setText(note.name);
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_launcher_foreground);
            ivImage.setImageDrawable(drawable);

            // bind swiped layout info
        }

        private void goToNote(Note note) {
            AppCompatActivity activity = (AppCompatActivity) context;
            Fragment myFragment = new NoteDetailsFragment(note);
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, myFragment).addToBackStack(null).commit();
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "item clicked! - " + tvNoteName);
            // get position
            int position = getAdapterPosition();
            // make sure valid position
            if (position != RecyclerView.NO_POSITION) {
                Note note = notes.get(position);
                goToNote(note);
            }
        }
    }
}
