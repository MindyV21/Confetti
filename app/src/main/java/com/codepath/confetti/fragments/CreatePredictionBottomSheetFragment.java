package com.codepath.confetti.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentCreatePredictionBottomSheetBinding;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.codepath.confetti.utlils.Firebase;
import com.codepath.confetti.models.PinView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment to create a new prediction for a specific note
 */
public class CreatePredictionBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "CreatePredictionBottomSheetFragment";
    private FragmentCreatePredictionBottomSheetBinding binding;

    private Bitmap takenImage;
    private Prediction newPrediction;

    private TextView tvCancel;
    private PinView ssivCreatePrediction;
    private TabLayout tabLayoutCreatePrediction;
    private EditText etText;
    private TextView tvCreate;
    private ProgressBar pbLoading;

    // the fragment initialization parameters
    private static final String NOTE = "note";
    private Note note;

    public CreatePredictionBottomSheetFragment() {}

    /**
     * Sets note predictions to be inflated
     * @param note note
     * @return new instance of fragment for a specified note
     */
    public static CreatePredictionBottomSheetFragment newInstance(Note note) {
        CreatePredictionBottomSheetFragment frag = new CreatePredictionBottomSheetFragment();
        Bundle args = new Bundle();
        args.putParcelable(NOTE, Parcels.wrap(note));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
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
        binding = FragmentCreatePredictionBottomSheetBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newPrediction = new Prediction();

        tvCancel = binding.tvCancel;
        ssivCreatePrediction = binding.ssivCreatePrediction;
        tabLayoutCreatePrediction = binding.tabLayoutCreatePrediction;
        etText = binding.etText;
        tvCreate = binding.tvCreate;
        pbLoading = binding.pbLoading;

        ssivCreatePrediction.setZoomEnabled(false);

        // loading in image file
//        takenImage = BitmapFactory.decodeFile(note.getImageFile().getAbsolutePath());
//        ssivCreatePrediction.setImage(ImageSource.bitmap(takenImage));
        onUploadPhoto();

        // to exit creating a prediction
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if loading
                if (isLoading()) {
                    Toast.makeText(getContext(), "Loading! Have some confetti while you wait!", Toast.LENGTH_SHORT).show();
                    return;
                }

                dismiss();
            }
        });

        // tab selection changes - hide/show option to name keyword
        tabLayoutCreatePrediction.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "tab selected " + tab.getText().toString());
                if (tab.getText().toString().equals(getString(R.string.example))) {
                    etText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getText().toString().equals(getString(R.string.example))) {
                    etText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // set up gesture detector for canvas (image)
        initImage();

        // creates a new prediction !
        tvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick btn create prediction !");
                String label = tabLayoutCreatePrediction.getSelectedTabPosition() == 0 ?
                        getString(R.string.topic) :
                        getString(R.string.example);
                Boolean firstPrediction = false;

                // check if loading
                if (isLoading()) {
                    Toast.makeText(getContext(), "Loading! Have some confetti while you wait!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check if pin is placed
                if (ssivCreatePrediction.getPin(newPrediction) == null) {
                    Toast.makeText(getContext(), "Place a pin!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // check keyword is filled out if a topic
                if (tabLayoutCreatePrediction.getSelectedTabPosition() == 0 && etText.getText().toString().trim().equals("")) {
                    Toast.makeText(getContext(), "Define a keyword!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // set newPrediction info
                newPrediction.label = label;
                newPrediction.text = etText.getText().toString().trim();
                newPrediction.xMin = (int) ssivCreatePrediction.getPin(newPrediction).x - getContext().getResources().getInteger(R.integer.xMinOffset);
                newPrediction.yMax = (int) ssivCreatePrediction.getPin(newPrediction).y - getContext().getResources().getInteger(R.integer.yMaxOffset);

                // place prediction in note
                if (note.getPredictions() == null) {
                    note.setPredictions(new ArrayList<>());
                    firstPrediction = true;
                }
                note.getPredictions().add(newPrediction);

                // update canvas
                CreatePredictionListener listener = (CreatePredictionListener) getActivity();
                listener.addPinToImage(firstPrediction);

                // update note database in firebase
                pbLoading.setVisibility(View.VISIBLE);
                Firebase.updateNotePredictions(getContext(), note, "upload", pbLoading);

                // reset view
                etText.setText("");
                ssivCreatePrediction.removeAllPins();
                newPrediction = new Prediction();
            }
        });
    }

    /**
     * Stops click functionality when something is loading
     * @return
     */
    private boolean isLoading() {
        return pbLoading.getVisibility() == View.VISIBLE;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);

                BottomSheetBehavior bottomSheetBehavior = ((BottomSheetDialog) dialogInterface).getBehavior();
                bottomSheetBehavior.setDraggable(false);
            }
        });
        return  dialog;
    }

    /**
     * Sets the bottom sheet height to fit the screen
     * @param bottomSheetDialog
     */
    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight();
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Gets the window height
     * @return
     */
    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /**
     * Sets up gesture detection for canvas
     */
    private void initImage() {
        // handle touch events
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (ssivCreatePrediction.isReady()) {
                    ssivCreatePrediction.removeAllPins();

                    PointF tappedCoordinate = ssivCreatePrediction
                            .viewToSourceCoord(
                                    new PointF(e.getX() + getContext().getResources().getInteger(R.integer.xMinOffset),
                                            e.getY() + getContext().getResources().getInteger(R.integer.yMaxOffset)));
                    Log.d(TAG, "tapped coords x: " + tappedCoordinate.x + " y: " + tappedCoordinate.y);

                    // set pin
                    ssivCreatePrediction.setPin(new PointF(tappedCoordinate.x, tappedCoordinate.y), newPrediction);
                }
                return true;
            }
        });

        ssivCreatePrediction.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
    }

    /**
     * Interface for fragment and activity communication
     */
    public interface CreatePredictionListener {
        // when a new prediction is created
        public void addPinToImage(Boolean firstPrediction);
    }

    // TODO: TBD upload photo testing stuff
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
                ssivCreatePrediction.setImage(ImageSource.bitmap(takenImage));
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }
        }
    }
}
