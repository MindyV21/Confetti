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
import com.google.firebase.auth.FirebaseUser;
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

/**
 * Util class for firebase calls
 */
public class Firebase {

    public static final String TAG = "Firebase";

    /**
     * Uploads a note image file to firebase storage, then onSuccess the note data will be uploaded
     * to firebase notes database
     * @param context
     * @param pbLoading
     * @param note
     * @param id nanonets id for note
     * @param photoFile
     */
    public static void uploadImage(Context context, ProgressBar pbLoading, Note note, String id, File photoFile) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child(FirebaseAuth.getInstance().getUid() + "/" + id);

        Uri file = Uri.fromFile(photoFile);
        UploadTask uploadTask = fileRef.putFile(file);
        // onFailure and onSuccess failures
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful image uploads
                Log.e(TAG, "OnFailure upload photo to firebase storage");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "OnSuccess upload photo to firebase storage");
                uploadNote(context, pbLoading, note, id, photoFile);
            }
        });
    }

    /**
     * Uploads note data to firebase notes database
     * @param context
     * @param pbLoading
     * @param note
     * @param id nanonets id for note
     * @param photoFile
     */
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
                } else {
                    // note failed to upload to firebase
                    Log.i(TAG, "onFailure to upload note to firebase");
                    pbLoading.setVisibility(View.INVISIBLE);
                    Toast.makeText(context, "Note upload failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Calling by UploadBottomSheetFragment to upload note and photo file data, used over uploadImage() for
     * more readable method names
     * @param context
     * @param pbLoading
     * @param note
     * @param id nanonets id for note
     * @param photoFile
     */
    public static void uploadNoteInfo(Context context, ProgressBar pbLoading, Note note, String id, File photoFile) {
        uploadImage(context, pbLoading, note, id, photoFile);
    }

    /**
     * Gets the note ids associated with each chip, and updates / re-filters notes list
     * @param checkedChipIdsSet set of chip ids in allChipsGroup that are selected
     * @param allChipsGroup chip group of all chips associated with a user
     * @param adapter adapter for notes list
     * @param searchView searchView with query input
     * @param allNotes list of all notes associated with a user
     * @param currentNotes list of current notes based on query and filter
     * @param chippedNoteIds empty set to stop duplicate notes being retrieved
     */
    public static void getChippedNotes(Set<Integer> checkedChipIdsSet, ChipGroup allChipsGroup,
                                       NotesAdapter adapter, SearchView searchView,
                                       Map<String, Note> allNotes, List<Note> currentNotes, Set<String> chippedNoteIds) {
        // loop through all selected chips
        for (Integer id : checkedChipIdsSet) {
            Chip chip = allChipsGroup.findViewById(id);
            Log.d(TAG, chip.getText().toString());

            // retrieve note ids associated with a specific chip
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
                            // check if note file is already added to notes list
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

    /**
     * Delete a note from firebase notes database
     * @param context
     * @param note
     */
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
                    deletePhotoFile(context, note);
                } else {
                    // note failed to delete from firebase
                    Log.i(TAG, "onFailure to delete note from firebase");
                    Toast.makeText(context, "Note deletion failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Used when deleting a note, deletes note id references within the firebase chips database
     * @param context
     * @param note
     */
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
                    } else {
                        // note failed to delete from firebase
                        Log.i(TAG, "onFailure to delete note's chip references from firebase");
                        Toast.makeText(context, "Note deletion failed! Try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Delete a note's photo file in firebase storage
     * @param context
     * @param note
     */
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
                    deleteNoteChips(context, note);
                } else {
                    // image file failed to retrieve from firebase
                    Log.i(TAG, "onFailure to delete note image from firebase");
                    Toast.makeText(context, "Note deletion failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Retrieves a note's photo file from firebase storage
     * @param note
     */
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
                        note.setPhotoLoaded(true);
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

    /**
     * Used when deleting a chip from a note, deletes the note id in the firebase chips database,
     * then updates the chip list in the firebase notes database
     * @param context
     * @param note
     * @param chipName
     */
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

    /**
     * Used when adding a chip to a note, adds the note id in the firebase chips database,
     * then updates the chip list in the firebase notes database
     * @param context
     * @param note
     * @param chipName
     */
    public static void addChipRef(Context context, Note note, String chipName) {
        // Continue with delete operation
        FirebaseDatabase.getInstance().getReference("Chips")
                .child(FirebaseAuth.getInstance().getUid())
                .child(chipName)
                .child(note.getId())
                .setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    // note ref add to firebase
                    Log.i(TAG, "onSuccess to add note ref in chip database from firebase");
                    updateNoteChips(context, note);
                } else {
                    // note ref failed to add to firebase
                    Log.i(TAG, "onFailure to add note ref in chip database from firebase");
                    Toast.makeText(context, "Chip add failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Updates the chips for a note in the firebase notes database
     * @param context
     * @param note
     */
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
                    // update chips
                    Log.i(TAG, "onSuccess to update chips in note database from firebase");
                } else {
                    // update chips failed
                    Log.i(TAG, "onFailure to update chips in note database from firebase");
                    Toast.makeText(context, "Chip update failed! Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Updates the predictions for a note in the firebase notes database
     * @param context
     * @param note
     * @param toastMessage "upload" or "deletion"
     * @param pbLoading null if not applicable
     */
    public static void updateNotePredictions(Context context, Note note, String toastMessage, ProgressBar pbLoading) {
        // update note's predictions
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Notes")
                .child(FirebaseAuth.getInstance().getUid())
                .child("Files")
                .child(note.getId());
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/predictions", note.getPredictions());
        mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    // update predictions
                    Log.i(TAG, "onSuccess to update predictions in note database from firebase");
                    Toast.makeText(context, "Prediction " + toastMessage + " success!.", Toast.LENGTH_SHORT).show();
                } else {
                    // update predictions failed
                    Log.i(TAG, "onFailure to update predictions in note database from firebase");
                    Toast.makeText(context, "Prediction " + toastMessage + " failed! Try again.", Toast.LENGTH_SHORT).show();
                }

                if (pbLoading != null) {
                    pbLoading.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
