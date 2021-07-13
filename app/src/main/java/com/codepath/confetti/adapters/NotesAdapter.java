package com.codepath.confetti.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.confetti.R;
import com.codepath.confetti.models.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    public static final String TAG = "NotesAdapter";

    private Context context;
    private List<Note> notes;

    public NotesAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.ViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // connect vars to layout
        }

        public void bind(Note note) {
            // bind vars
        }
    }
}
