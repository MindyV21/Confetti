package com.codepath.confetti;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.codepath.confetti.adapters.NotesAdapter;
import com.codepath.confetti.fragments.UploadFragment;
import com.codepath.confetti.models.Note;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Firebase {

    public static final String TAG = "Firebase";

    public static void uploadImage(File photoFile, String id) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child(FirebaseAuth.getInstance().getUid() + "/" + id);

        Uri file = Uri.fromFile(photoFile);
        UploadTask uploadTask = fileRef.putFile(file);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.e(TAG, "OnFailure upload photo to firebase storage");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                Log.i(TAG, "OnSuccess upload photo to firebase storage");
            }
        });
    }

    public static void uploadNote(Context context, Note note, String id, File photoFile) {
        // upload Note object to firebase database
        FirebaseDatabase.getInstance().getReference("Notes")
                .child(FirebaseAuth.getInstance().getUid())
                .child("Files")
                .child(id)
                .setValue(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    // note uploaded to firebase
                    // TODO: even if successful, note may have been empty == no note uploaded, need to wait for nanonets to finish image processing
                    Log.i(TAG, "onSuccess to upload note to firebase");
                    Toast.makeText(context, "Note uploaded successfully!", Toast.LENGTH_SHORT).show();
                    Firebase.uploadImage(photoFile, id);
                } else {
                    // note failed to upload to firebase
                    Log.i(TAG, "onFailure to upload note to firebase");
                    Toast.makeText(context, "Note upload failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void getChippedNotes(List<Integer> checkedChipIds, ChipGroup allChipsGroup,
                                       NotesAdapter adapter, SearchView searchView,
                                       Map<String, Note> allNotes, List<Note> currentNotes, Set<String> chippedNoteIds) {
        for (Integer id : checkedChipIds) {
            Chip chip = allChipsGroup.findViewById(id);
            Log.d(TAG, chip.getText().toString());
            FirebaseDatabase.getInstance().getReference("Chips")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child(chip.getText().toString())
                    .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "onSuccess to get chip in firebase database");

                        // add note file ids that are chipped with this chipName
                        Iterable<DataSnapshot> iterable = task.getResult().getChildren();
                        for (DataSnapshot data : iterable) {
                            Log.d(TAG, "iterating: " + data.getKey());
                            // check if file is in the treeSet
                            if (!chippedNoteIds.contains(data.getKey())) {
                                currentNotes.add(allNotes.get(data.getKey()));
                            }
                            chippedNoteIds.add(data.getKey());
                        }

                        adapter.getFilter().filter(searchView.getQuery());

                        Log.d(TAG, "currentNotes size: " + currentNotes.size());
                        Log.d(TAG, "chippedNoteIds size: " + chippedNoteIds.size());
                    } else {
                        Log.i(TAG, "onFailure to get chip in firebase database");
                    }
                }
            });
        }
    }
}
