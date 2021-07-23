package com.codepath.confetti.utlils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.codepath.confetti.adapters.NotesAdapter;
import com.codepath.confetti.models.Note;
import com.codepath.confetti.models.Prediction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Firebase {

    public static final String TAG = "Firebase";

    public static void uploadPredictions(List<Prediction> predictions, String id) {
        for (Prediction prediction : predictions) {
            if (prediction.label.equals("Topic")){
                FirebaseDatabase.getInstance().getReference("Topics")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child(prediction.text)
                        .child(id)
                        .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.i(TAG, "onSuccess to upload predictions to firebase");
                        } else {
                            // note failed to upload to firebase
                            Log.i(TAG, "onFailure to upload predictions to firebase");
                        }
                    }
                });
            }
        }
    }

    public static void uploadImage(Note note, File photoFile, String id) {
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
                uploadPredictions(note.getPredictions(), id);
            }
        });
    }

    public static void uploadNote(Context context, ProgressBar pbLoading, Note note, String id, File photoFile) {
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
                    Log.i(TAG, "onSuccess to upload note to firebase");
                    Toast.makeText(context, "Note uploaded successfully!", Toast.LENGTH_SHORT).show();
                    pbLoading.setVisibility(View.INVISIBLE);
                    // TODO : uncomment, delete predictions one - tired of firebase error
//                    Firebase.uploadImage(note, photoFile, id);
                    uploadPredictions(note.getPredictions(), id);
                } else {
                    // note failed to upload to firebase
                    Log.i(TAG, "onFailure to upload note to firebase");
                    pbLoading.setVisibility(View.INVISIBLE);
                    Toast.makeText(context, "Note upload failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void getChippedNotes(Set<Integer> checkedChipIdsSet, ChipGroup allChipsGroup,
                                       NotesAdapter adapter, SearchView searchView,
                                       Map<String, Note> allNotes, List<Note> currentNotes, Set<String> chippedNoteIds) {
        for (Integer id : checkedChipIdsSet) {
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

    public static void deleteNote(Context context, Note note) {
        // Continue with delete operation
        FirebaseDatabase.getInstance().getReference("Notes")
                .child(FirebaseAuth.getInstance().getUid())
                .child("Files")
                .child(note.getId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    // note delete from firebase
                    Log.i(TAG, "onSuccess to delete note from firebase");
                    deleteNoteChips(context, note);
                } else {
                    // note failed to delete from firebase
                    Log.i(TAG, "onFailure to delete note from firebase");
                    Toast.makeText(context, "Note deletion failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void deleteNoteChips(Context context, Note note) {
        // loop through chips and delete reference to this note
        List<String> chipNames = note.getChips();
        Log.d(TAG, "chip names count - " + chipNames.size());
        for (String chipName : chipNames) {
            // Continue with delete operation
            FirebaseDatabase.getInstance().getReference("Chips")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child(chipName)
                    .child(note.getId())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()){
                        // note delete from firebase
                        Log.i(TAG, "onSuccess to delete note's chip references from firebase");
                        Toast.makeText(context, "Note deleted successfully!", Toast.LENGTH_SHORT).show();
                        // deletePhotoFile(context, note);
                    } else {
                        // note failed to delete from firebase
                        Log.i(TAG, "onFailure to delete note's chip references from firebase");
                        Toast.makeText(context, "Note deletion failed! Try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static void deletePhotoFile(Context context, Note note) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child(FirebaseAuth.getInstance().getUid() + "/" + note.getId());
        Log.d(TAG, fileRef.getPath());

        fileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    // Local temp file has been created
                    Log.i(TAG, "onSuccess to delete note image from firebase");
                    Toast.makeText(context, "Note deleted successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    // image file failed to retrieve from firebase
                    Log.i(TAG, "onFailure to delete note image from firebase");
                    Toast.makeText(context, "Note deletion failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void getImage(Note note) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child(FirebaseAuth.getInstance().getUid() + "/" + note.getId());
        Log.d(TAG, fileRef.getPath());

        try {
            File localFile = File.createTempFile("images", "jpg");

            fileRef.getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<FileDownloadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        // Local temp file has been created
                        Log.i(TAG, "onSuccess to get note image from firebase");
                        note.setImageFile(localFile);
                    } else {
                        // image file failed to retrieve from firebase
                        Log.i(TAG, "onFailure to get note image from firebase");
                    }
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "failed to get note image from firebase storage", e);
        }
    }

    // removes note reference within chip database
    public static void deleteChipRef(Context context, Note note, String chipName) {
        // Continue with delete operation
        FirebaseDatabase.getInstance().getReference("Chips")
                .child(FirebaseAuth.getInstance().getUid())
                .child(chipName)
                .child(note.getId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    // note delete from firebase
                    Log.i(TAG, "onSuccess to delete note ref in chip database from firebase");
                    updateNoteChips(context, note);
                } else {
                    // note failed to delete from firebase
                    Log.i(TAG, "onFailure to delete note ref in chip database from firebase");
                    Toast.makeText(context, "Chip deletion failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static void updateNoteChips(Context context, Note note) {
        // update note's chip list
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Notes")
                .child(FirebaseAuth.getInstance().getUid())
                .child("Files")
                .child(note.getId());
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/chips", note.getChips());
        mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    // note delete from firebase
                    Log.i(TAG, "onSuccess to chip in note database from firebase");
                } else {
                    // note failed to delete from firebase
                    Log.i(TAG, "onFailure to delete chip in note database from firebase");
                    Toast.makeText(context, "Chip deletion failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
