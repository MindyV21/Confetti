package com.codepath.confetti.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.confetti.databinding.FragmentUploadBottomSheetBinding;
import com.codepath.confetti.utlils.Firebase;
import com.codepath.confetti.R;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.codepath.confetti.utlils.UtilsGeneral;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Fragment where user can upload photo files of their notes
 */
public class UploadBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String TAG = "UploadBottomSheetFragment";

    private FragmentUploadBottomSheetBinding binding;

    // to disable click events when backend running
    private Boolean isLoading;

    // header
    private TextView tvCancel;

    private EditText etFileName;
    private ImageButton btnTakePhoto;
    private ImageButton btnUploadGallery;
    private ImageView ivPreview;
    private TextView tvCreate;
    private ProgressBar pbLoading;

    // photo upload / camera
    public final static int PICK_PHOTO_CODE = 1046;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    private Bitmap selectedImage;
    File photoFile;

    public UploadBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUploadBottomSheetBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isLoading = false;

        tvCancel = binding.tvCancel;
        etFileName = binding.etFileName;
        btnTakePhoto = binding.btnTakePhoto;
        btnUploadGallery = binding.btnUploadGallery;
        ivPreview = binding.ivPreview;
        tvCreate = binding.tvCreate;
        pbLoading = binding.pbLoading;

        etFileName.setText("");

        // dismiss
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if loading
                if (isLoading()) {
                    Toast.makeText(getContext(), "Loading! Have some confetti while you wait!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "cancel creating prediction");
                dismiss();
            }
        });

        // take a photo with phone's camera
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if loading
                if (isLoading()) {
                    Toast.makeText(getContext(), "Loading! Have some confetti while you wait!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "camera intent!");
                onLaunchCamera(view);
            }
        });

        // upload a note photo file from phone's gallery
        btnUploadGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if loading
                if (isLoading()) {
                    Toast.makeText(getContext(), "Loading! Have some confetti while you wait!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "photo gallery intent!");
                onUploadPhoto();
            }
        });

        // submit a note
        tvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if loading
                if (isLoading()) {
                    Toast.makeText(getContext(), "Loading! Have some confetti while you wait!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // make sure there is a file name
                String fileName = etFileName.getText().toString().trim();
                if (etFileName == null || fileName.equals("")) {
                    Toast.makeText(getContext(), "There is no description!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // check an image is loaded in
                Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.confetti);
                if (photoFile == null || ivPreview.getDrawable() == drawable) {
                    Toast.makeText(getContext(), "There is no image!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "upload photo to nanonets database for prediction!");
                pbLoading.setVisibility(View.VISIBLE);

                dummyData(fileName);

                //NanonetsApi.predictFile(getContext(), pbLoading, fileName, getString(R.string.nanonets_api_key), getString(R.string.nanonets_notes_model_id), photoFile);
            }
        });

    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                UtilsGeneral.setupFullHeight(bottomSheetDialog, getContext());

                BottomSheetBehavior bottomSheetBehavior = ((BottomSheetDialog) dialogInterface).getBehavior();
                bottomSheetBehavior.setDraggable(false);
            }
        });
        return  dialog;
    }

    @Override
    public void onDismiss(@NonNull @NotNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    /**
     * Stops click functionality when something is loading
     * @return
     */
    private boolean isLoading() {
        return pbLoading.getVisibility() == View.VISIBLE;
    }

    //TODO: tbd dummy data to not overload firebase storage
    public void dummyData(String fileName) {
        Note note = new Note(fileName);

        List<Prediction> predictions = new ArrayList<>();
        predictions.add(new Prediction(404, 1, 1000, 3, "bread"));
        predictions.add(new Prediction(800, 1, 2000, 3, "toast"));
        note.setPredictions(predictions);

        List<String> chipNames = new ArrayList<>();
        chipNames.add("hole");
        chipNames.add("french breads");
        chipNames.add("only one");
        note.setChips(chipNames);
        // add chips to chip database
        FirebaseDatabase.getInstance().getReference("Chips")
                .child(FirebaseAuth.getInstance().getUid())
                .child("hole")
                .child("testId")
                .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "onSuccess to add dummy note's chip references from firebase");
                } else {
                    Log.d(TAG, "onFailure to add dummy note's chip references from firebase");
                }
            }
        });
        FirebaseDatabase.getInstance().getReference("Chips")
                .child(FirebaseAuth.getInstance().getUid())
                .child("french breads")
                .child("testId")
                .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "onSuccess to add dummy note's chip references from firebase");
                } else {
                    Log.d(TAG, "onFailure to add dummy note's chip references from firebase");
                }
            }
        });
        FirebaseDatabase.getInstance().getReference("Chips")
                .child(FirebaseAuth.getInstance().getUid())
                .child("only one")
                .child("testId")
                .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "onSuccess to add dummy note's chip references from firebase");
                } else {
                    Log.d(TAG, "onFailure to add dummy note's chip references from firebase");
                }
            }
        });

        Firebase.uploadNoteInfo(getContext(), pbLoading, note, "testId", photoFile);
    }

    /**
     * Launches intent to upload photo from gallery
     */
    public void onUploadPhoto(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_PHOTO_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // setting photo from photo in gallery
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
            photoFile = new File(testDir, photoFileName);
            OutputStream os;
            try {
                os = new FileOutputStream(photoFile);
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();

                // testing to see if file actually contains image file
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivPreview.setImageBitmap(takenImage);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }
        } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            // setting photo from taken photo
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                selectedImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivPreview.setImageBitmap(selectedImage);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Launches intent to take photo with camera
     * @param view
     */
    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.confetti", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    /**
     * Returns the File for a photo stored on disk given the fileName
     * @param fileName name of photo file
     * @return photo file
     */
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }
}