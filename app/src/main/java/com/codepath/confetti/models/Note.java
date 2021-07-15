package com.codepath.confetti.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Note {

    public static final String TAG = "NoteModel";

    public String name;
    public List<Prediction> predictions;

    public Note() {};

    public Note(String name) {
        this.name = name;
    }

    public void setText(JSONObject jsonObject) throws JSONException {
        predictions = new ArrayList<>();
        JSONArray prediction = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("prediction");

        for (int i = 0; i < prediction.length(); i++) {
            predictions.add(new Prediction(prediction.getJSONObject(i)));
            Log.d(TAG, predictions.get(i).text);
        }
    }
}
