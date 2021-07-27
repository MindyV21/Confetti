package com.codepath.confetti.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentCreatePredictionBinding;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.utlils.PinView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static android.app.Activity.RESULT_OK;

public class CreatePredictionFragment extends Fragment {

    public static final String TAG = "CreatePredictionFragment";
    private FragmentCreatePredictionBinding binding;

    private Bitmap takenImage;
    private Note note;

    private ImageView ivCancel;
    private PinView ssivCreatePrediction;
    private TabLayout tabLayoutCreatePrediction;
    private ImageButton ibPin;
    private EditText etText;
    private Button btnCreatePrediction;

    public CreatePredictionFragment() {}

    public static CreatePredictionFragment newInstance(Note note) {
        CreatePredictionFragment frag = new CreatePredictionFragment();
        Bundle args = new Bundle();
        args.putParcelable("note", Parcels.wrap(note));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get back arguments
        note = Parcels.unwrap(getArguments().getParcelable("note"));
        Log.d(TAG, note.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreatePredictionBinding.inflate(getLayoutInflater(), container, false);
        // layout of fragment is stored in a special property called root
        View view = binding.getRoot();
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivCancel = binding.ivCancel;
        ssivCreatePrediction = binding.ssivCreatePrediction;
        tabLayoutCreatePrediction = binding.tabLayoutCreatePrediction;
        ibPin = binding.ibPin;
        etText = binding.etText;
        btnCreatePrediction = binding.btnCreatePrediction;

        // loading in image file
//        takenImage = BitmapFactory.decodeFile(note.getImageFile().getAbsolutePath());
//        ssivCreatePrediction.setImage(ImageSource.bitmap(takenImage));
        onUploadPhoto();

        // to exit creating a prediction
        Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.ic_baseline_cancel_24);
        ivCancel.setImageDrawable(drawable);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreatePredictionListener listener = (CreatePredictionListener) getActivity();
                listener.onCancelCreatePrediction();
            }
        });

        // tab selection changes - hide/show option to name keyword
        tabLayoutCreatePrediction.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "tab selected " + tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    // Defines the listener interface
    public interface CreatePredictionListener {
        public void onCancelCreatePrediction();
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
