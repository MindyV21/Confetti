package com.codepath.confetti.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.confetti.utlils.Firebase;
import com.codepath.confetti.utlils.NanonetsApi;
import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentUploadBinding;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
 * A simple {@link Fragment} subclass.
 * Use the {@link UploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadFragment extends Fragment {

    public static final String TAG = "UploadFragment";

    private FragmentUploadBinding binding;
    private EditText etFileName;
    private Button btnUploadGallery;
    private ImageView ivPreview;
    private Button btnSubmit;
    private ProgressBar pbLoading;

    public final static int PICK_PHOTO_CODE = 1046;
    private Bitmap selectedImage;
    File photoFile;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UploadFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UploadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadFragment newInstance(String param1, String param2) {
        UploadFragment fragment = new UploadFragment();
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
        binding = FragmentUploadBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etFileName = binding.etFileName;
        btnUploadGallery = binding.btnUploadGallery;
        ivPreview = binding.ivPreview;
        btnSubmit = binding.btnSubmit;
        pbLoading = binding.pbLoading;

        etFileName.setText("");

        btnUploadGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "photo gallery intent!");
                onUploadPhoto();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // make sure there is a file name
                String fileName = etFileName.getText().toString().trim();
                if (etFileName == null || fileName.equals("")) {
                    Toast.makeText(getContext(), "There is no description!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // check an image is loaded in
                if (photoFile == null || ivPreview.getDrawable() == null) {
                    Toast.makeText(getContext(), "There is no image!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "upload photo to nanonets database for prediction!");
                pbLoading.setVisibility(View.VISIBLE);

                // TODO: delete - for testing
                Note note = new Note(fileName);

                List<Prediction> predictions = new ArrayList<>();
                predictions.add(new Prediction(404, 1, 1000, 3, "bread"));
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

                //NanonetsApi.predictFile(getContext(), fileName, getString(R.string.nanonets_api_key), getString(R.string.nanonets_notes_model_id), photoFile);
            }
        });

    }

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
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                ivPreview.setImageBitmap(takenImage);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }
        }
    }
}