package com.codepath.confetti.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Parcel
public class Note {

    public static final String TAG = "NoteModel";

    public String name;
    public List<Prediction> predictions;
    public List<String> chips;

    private File imageFile;
    private String id;
    private boolean photoLoaded;

    public Note() {};

    // for dummy data
    public Note(String name) {
        this.name = name;
    }

    public void getPredictions(JSONObject jsonObject) throws JSONException {
        predictions = new ArrayList<>();
        JSONArray prediction = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("prediction");

        for (int i = 0; i < prediction.length(); i++) {
            predictions.add(new Prediction(prediction.getJSONObject(i)));
            Log.d(TAG, predictions.get(i).text);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<Prediction> predictions) {
        this.predictions = predictions;
    }

    public List<String> getChips() {
        return chips;
    }

    public void setChips(List<String> chips) {
        this.chips = chips;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }
    public File getImageFile() {
        return imageFile;
    }

    public boolean isPhotoLoaded() {
        return photoLoaded;
    }

    public void setPhotoLoaded(boolean photoLoaded) {
        this.photoLoaded = photoLoaded;
    }
}
