package com.codepath.confetti.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentNoteImagesBinding;
import com.codepath.confetti.databinding.FragmentNotesBinding;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.codepath.confetti.utlils.PinView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NoteImagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoteImagesFragment extends Fragment {

    public static final String TAG = "NoteImagesFragment";
    private FragmentNoteImagesBinding binding;

    private Note note;

    private Bitmap takenImage;
    private PinView ssivNote;
    private ImageView ivExitFullscreen;

    private OnItemSelectedListener listener;

    public NoteImagesFragment(Note note) {
        this.note = note;
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NoteImagesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NoteImagesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NoteImagesFragment newInstance(String param1, String param2) {
        NoteImagesFragment fragment = new NoteImagesFragment();
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

        // fullscreen stuff
        ivExitFullscreen = binding.ivExitFullscreen;
        ivExitFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "exit fullscreen !");

                // re place pins
                createPins();

                ivExitFullscreen.setVisibility(View.GONE);
                listener.onExitFullscreen();
            }
        });

//        // testing to see if file actually contains image file
//        takenImage = BitmapFactory.decodeFile(note.getImageFile().getAbsolutePath());
//        ssivNote.setImage(ImageSource.bitmap(takenImage));
//        if (note.predictions != null) {
//            createPins();
//        }


        onUploadPhoto();
    }

    public void enableFullscreen() {
        ivExitFullscreen.setVisibility(View.VISIBLE);

        // clear canvas
        ssivNote.removeAllPins();
    }

    // Define the events that the fragment will use to communicate
    public interface OnItemSelectedListener {
        // This can be any number of events to be sent to the activity
        public void onPinItemSelected(int index);
        public void onExitFullscreen();
    }

    // Store the listener (activity) that will have events fired once the fragment is attached
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

    public void removePin(Prediction prediction) {
        Log.d(TAG, "removing prediction pin");
        ssivNote.removePin(prediction);
    }

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
                if (ssivNote.isReady() && note.predictions != null) {
                    PointF tappedCoordinate = new PointF(e.getX(), e.getY());
                    Log.d(TAG, "tapped coords x: " + tappedCoordinate.x + " y: " + tappedCoordinate.y);

                    // range to hit pin
                    int blockWidth = 75;
                    int blockHeight = 75;

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