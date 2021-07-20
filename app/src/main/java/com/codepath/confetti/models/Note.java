package com.codepath.confetti.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Note {

    public static final String TAG = "NoteModel";

    public String name;
//    public String url;
    public List<Prediction> predictions;
    private File imageFile;
    private String id;

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

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
}
