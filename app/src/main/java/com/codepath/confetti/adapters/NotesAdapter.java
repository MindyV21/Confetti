package com.codepath.confetti.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.codepath.confetti.NoteDetailsActivity;
import com.codepath.confetti.R;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.codepath.confetti.utlils.Firebase;

import org.parceler.Parcels;

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

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvNoteName;
        private ImageView ivImage;
        private ImageView ivDelete;

        private SwipeRevealLayout swipeRevealLayout;
        private LinearLayout itemMainLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // connect vars to layout
            tvNoteName = itemView.findViewById(R.id.tvNoteName);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            swipeRevealLayout = itemView.findViewById(R.id.swipeLayout);
            itemMainLayout = itemView.findViewById(R.id.itemMainLayout);

            // listener to delete with swipe option
            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "user swiped on " + tvNoteName.getText().toString());
                    new AlertDialog.Builder(context)
                            .setMessage("Delete note '" + tvNoteName.getText().toString() + "'?")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "deleting note " + tvNoteName.getText().toString());
                                    Note note = notes.get(getAdapterPosition());

                                    Firebase.deleteNote(context, note);

                                    // remove item from notes
                                    notes.remove(getAdapterPosition());
                                    notifyItemRemoved(getAdapterPosition());
                                    for (int i = 0; i < notesFull.size(); i++) {
                                        if (note.getId().equals(notesFull.get(i).getId())) {
                                            notesFull.remove(i);
                                            return;
                                        }
                                    }

                                    // TODO: need to delete references to this deleted note in chips database <- to do this need to create a list within note with a list of its chips
                                    // TODO: need to delete photo file from storage

                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // reverse swiped
                                    viewBinderHelper.closeLayout(String.valueOf(tvNoteName.getText().toString()));
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

            // listener to go to note details
            itemMainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "item main layout clicked! - " + tvNoteName.getText().toString());
                    // get position
                    int position = getAdapterPosition();
                    // make sure valid position
                    if (position != RecyclerView.NO_POSITION) {
                        Note note = notes.get(position);
                        goToNote(note);
                    }
                }
            });

        }

        public void bind(Note note) {
            // bind main layout info
            Log.i(TAG, note.name);
            tvNoteName.setText(note.name);
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_launcher_foreground);
            ivImage.setImageDrawable(drawable);
            // TODO: need to wait for images to load in before showing
//            Bitmap bitmap = BitmapFactory.decodeFile(note.getImageFile().getAbsolutePath());
//            ivImage.setImageBitmap(bitmap);

            // bind swiped layout info
        }

        private void goToNote(Note note) {
            Intent intent = new Intent(context, NoteDetailsActivity.class);
            intent.putExtra(Note.class.getSimpleName(), Parcels.wrap(note));
            context.startActivity(intent);
        }
    }
}
