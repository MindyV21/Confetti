package com.codepath.confetti.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentNoteImagesBinding;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.codepath.confetti.models.PinView;
import com.davemorrissey.labs.subscaleview.ImageSource;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment to set up photo file for a note
 */
public class NoteImagesFragment extends Fragment {

    public static final String TAG = "NoteImagesFragment";
    private FragmentNoteImagesBinding binding;
    private OnItemSelectedListener listener;

    private Bitmap takenImage;
    private PinView ssivNote;
    private Boolean isFullscreen;

    // the fragment initialization parameters
    private static final String NOTE = "note";
    private Note note;

    public NoteImagesFragment() {
        // Required empty public constructor
    }

    /**
     * Sets note photo file to be blown up
     * @param note note
     * @return new instance of fragment for a specified note
     */
    public static NoteImagesFragment newInstance(Note note) {
        NoteImagesFragment fragment = new NoteImagesFragment();
        Bundle args = new Bundle();
        args.putParcelable(NOTE, Parcels.wrap(note));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // get back arguments
            note = Parcels.unwrap(getArguments().getParcelable("note"));
            Log.d(TAG, note.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            binding = FragmentNoteImagesBinding.inflate(getLayoutInflater(), container, false);
            // layout of fragment is stored in a special property called root
            View view = binding.getRoot();
            // Inflate the layout for this fragment
            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView", e);
            throw e;
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up image view scrollable
        ssivNote = binding.ssivNote;
        ssivNote.setZoomEnabled(false);

        // fullscreen logic
        isFullscreen = false;

//        // testing to see if file actually contains image file
//        takenImage = BitmapFactory.decodeFile(note.getImageFile().getAbsolutePath());
//        ssivNote.setImage(ImageSource.bitmap(takenImage));
//        if (note.predictions != null) {
//            createPins();
//        }


        onUploadPhoto();
    }

    /**
     * Sets up view for fullscreen function
     */
    public void enableFullscreen() {
        isFullscreen = true;

        // clear canvas
        ssivNote.removeAllPins();
    }

    /**
     * Interface for fragment and activity communication
     */
    public interface OnItemSelectedListener {
        // when a prediction pin is selected
        public void onPinItemSelected(int index);
        // when fullscreen is exited
        public void onExitFullscreen();
    }

    /**
     * Stores the listener (activity) that will have events fired once the fragment is attached
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnItemSelectedListener) {
            listener = (OnItemSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement NoteImagesFragment.OnItemSelectedListener");
        }
    }

    /**
     * Communicator method for parent activity to remove a pin
     * @param prediction the pin to be removed
     */
    public void removePin(Prediction prediction) {
        Log.d(TAG, "removing prediction pin");
        ssivNote.removePin(prediction);
    }

    /**
     * Communicator method for parent activity to add a pin
     */
    public void addPin() {
        Log.d(TAG, "adding prediction pin");
        Prediction prediction = note.getPredictions().get(note.getPredictions().size() - 1);
        ssivNote.setPin(new PointF(prediction.xMin + getContext().getResources().getInteger(R.integer.xMinOffset),
                prediction.yMax + getContext().getResources().getInteger(R.integer.yMaxOffset)), prediction);
    }

    // TODO: delete upload photo stuff
    public Bitmap getImage() {
        return takenImage;
    }
    public final static int PICK_PHOTO_CODE = 1046;
    private Bitmap selectedImage;
    File photoFile;

    public void onUploadPhoto(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_PHOTO_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_PHOTO_CODE && resultCode == RESULT_OK && data != null){
            Uri photoUri = data.getData();
            selectedImage = null;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }catch (FileNotFoundException e){
                e.printStackTrace();
                Log.e(TAG, "File not found");
            } catch (IOException e){
                Log.d(TAG, e.getLocalizedMessage());
            }

            // write bitmap to an image file
            File testDir = getContext().getFilesDir();
            photoFile = new File(testDir, "photo.jpg");
            OutputStream os;
            try {
                os = new FileOutputStream(photoFile);
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();

                // testing to see if file actually contains image file
                takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ssivNote.setImage(ImageSource.bitmap(takenImage));
                if (note.predictions != null) {
                    createPins();
                }
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }
        }
    }

    /**
     * Creates all the prediction pins on the canvas, and sets up gesture detection for pin taps
     */
    public void createPins() {
        // add pins to photo
        for (Prediction prediction : note.predictions) {
            Log.d(TAG, prediction.text);
            ssivNote.setPin(new PointF(prediction.xMin + getContext().getResources().getInteger(R.integer.xMinOffset),
                    prediction.yMax + getContext().getResources().getInteger(R.integer.yMaxOffset)), prediction);
        }

        // handle touch events
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // in fullscreen mode, tap event exits fullscreen
                if (isFullscreen) {
                    Log.d(TAG, "exit fullscreen !");

                    // reset views to when it is not fullscreen
                    listener.onExitFullscreen();
                    createPins();
                    isFullscreen = false;
                    return true;
                }

                if (ssivNote.isReady() && note.predictions != null) {
                    PointF tappedCoordinate = new PointF(e.getX(), e.getY());
                    Log.d(TAG, "tapped coords x: " + tappedCoordinate.x + " y: " + tappedCoordinate.y);

                    // range to hit pin
                    int blockWidth = 75;
                    int blockHeight = 75;

                    // limit to first pin found
                    int tappedPredictionIndex = -1;

                    // check if a pin is tapped
                    int i = 0;
                    while(i < note.getPredictions().size() && tappedPredictionIndex == -1) {
                        Prediction prediction = note.getPredictions().get(i);
                        PointF predictionCoordinate = ssivNote.sourceToViewCoord(ssivNote.getPin(prediction));

                        int predictX = (int) predictionCoordinate.x;
                        int predictY = (int) predictionCoordinate.y;

                        // center coordinate -/+ blockWidth actually sets touchable area to 2x icon size
                        if (tappedCoordinate.x >= predictX - blockWidth && tappedCoordinate.x <= predictX + blockWidth &&
                                tappedCoordinate.y >= predictY - blockHeight && tappedCoordinate.y <= predictY + blockHeight) {

                            Log.d(TAG, "---FOUND COORD " + prediction.text + " prediction coords x: " + predictionCoordinate.x + " y: " + predictionCoordinate.y);
                            tappedPredictionIndex = i;
                        }

                        i++;
                    }

                    // if pin is tapped, open bottom sheet and scroll to that item
                    if (tappedPredictionIndex != -1) {
                        Log.d(TAG, "scroll to prediction " + note.getPredictions().get(tappedPredictionIndex));
                        listener.onPinItemSelected(tappedPredictionIndex);
                    }

                }
                return true;
            }
        });

        ssivNote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
    }
}