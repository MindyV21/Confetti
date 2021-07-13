package com.codepath.confetti.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.confetti.R;
import com.codepath.confetti.fragments.NoteDetailsFragment;
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
        return notes.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvNoteName;
        private ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // connect vars to layout
            tvNoteName = itemView.findViewById(R.id.tvNoteName);
            ivImage = itemView.findViewById(R.id.ivImage);

            // Attach a click listener to the entire row view
            itemView.setOnClickListener(this);
        }

        public void bind(Note note) {
            // bind var
            tvNoteName.setText(note.getName());
            Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.ic_launcher_foreground);
            ivImage.setImageDrawable(drawable);
        }

        private void goToNote(Note note) {
            AppCompatActivity activity = (AppCompatActivity) context;
            Fragment myFragment = new NoteDetailsFragment(note);
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, myFragment).addToBackStack(null).commit();
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "item clicked!");
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
