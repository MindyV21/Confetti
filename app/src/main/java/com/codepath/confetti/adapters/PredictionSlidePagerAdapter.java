package com.codepath.confetti.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.confetti.R;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.codepath.confetti.utlils.Firebase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Predictions ViewPager
 */
public class PredictionSlidePagerAdapter extends RecyclerView.Adapter<PredictionSlidePagerAdapter.ViewHolder> {

    public static final String TAG = "PredictionSlidePagerAdapter";

    private long baseId = 0;
    private Context context;
    private Note note;
    private List<Prediction> predictions;

    public PredictionSlidePagerAdapter(Context context, Note note) {
        this.context = context;
        this.note = note;
        if (note.predictions == null) {
            this.predictions = new ArrayList<>();
        } else {
            this.predictions = note.predictions;
        }
    }

    public void setPredictions(Note note) {
        this.note = note;
        predictions = note.getPredictions();
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prediction_slide_page, parent, false);
        return new PredictionSlidePagerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Prediction prediction = predictions.get(position);
        holder.bind(prediction);
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    /**
     * Gives an ID different from position when position has been changed. When deleting a page or
     * making order changes call notifyChangeInPosition(1) before notifyDataSetChanged(). Fixes
     * UI / UX.
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return baseId + position;
    }

    /**
     * Notify that the position of a fragment has been changed.
     * Create a new ID for each position to force recreation of the fragment
     * @param n number of items which have been changed
     */
    public void notifyChangeInPosition(int n) {
        // shift the ID returned by getItemId outside the range of all previous fragments
        baseId += getItemCount() + n;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    /**
     * ViewHolder to bind views for a note's prediction
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private CardView cardPrediction;
        private ImageView ivDeletePrediction;
        private TextView tvLabel;
        private TextView tvText;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            cardPrediction = itemView.findViewById(R.id.cardPrediction);
            ivDeletePrediction = itemView.findViewById(R.id.ivDeletePrediction);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvText = itemView.findViewById(R.id.tvText);

            initDeletePrediction();
        }

        /**
         * Sets up logic for when a note prediction is deleted
         */
        private void initDeletePrediction() {
            // listener to delete a prediction
            ivDeletePrediction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "long click");

                    // setting up delete message depending on type of prediction
                    String deleteMessage = "example";
                    if (tvLabel.getText().toString().equals("Topic")) {
                        deleteMessage = "keyword '" + tvText.getText().toString() + "'";
                    }

                    // alert dialog to delete a prediction
                    new AlertDialog.Builder(context)
                            .setMessage("Delete " + deleteMessage + "?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.i(TAG, "deleting prediction " + tvText.getText().toString());
                                    Prediction prediction = predictions.get(getAdapterPosition());
                                    note.predictions.remove(getAdapterPosition());

                                    // update predictions in notes database
                                    Firebase.updateNotePredictions(context, note, "deletion", null);

                                    // update viewpager
                                    notifyChangeInPosition(1);
                                    notifyDataSetChanged();
                                    // update note image canvas
                                    UpdatePredictions listener = (UpdatePredictions) context;
                                    listener.removePinFromImage(prediction);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {}
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }

        public void bind(Prediction prediction) {
            tvLabel.setText(prediction.label);
            if (prediction.label.equals("Topic")) {
                tvText.setText(prediction.text);
            } else {
                tvText.setText("");
            }
        }
    }

    /**
     * Interface for fragment and activity communication
     */
    public interface UpdatePredictions {
        // removes pin from note image
        public void removePinFromImage(Prediction prediction);
    }
}
