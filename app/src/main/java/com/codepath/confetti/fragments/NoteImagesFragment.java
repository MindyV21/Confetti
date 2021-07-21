package com.codepath.confetti.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.codepath.confetti.R;
import com.codepath.confetti.databinding.FragmentNoteImagesBinding;
import com.codepath.confetti.databinding.FragmentNotesBinding;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.utlils.PinView;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NoteImagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoteImagesFragment extends Fragment {

    public static final String TAG = "NoteImagesFragment";
    private FragmentNoteImagesBinding binding;

    private Note note;

    private PinView ssivNote;

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
//        Bitmap bitmap = BitmapFactory.decodeFile(note.getImageFile().getAbsolutePath());
//        Log.d(TAG, "image height: " + bitmap.getHeight() + "    width: " + bitmap.getWidth());
//
//        ssivNote.setImage(ImageSource.bitmap(bitmap));
//        ssivNote.setPin(new PointF(1602f, 405f));
//        ssivNote.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "CLICK CLICK NOTE IMAGE");
//            }
//        });
    }
}