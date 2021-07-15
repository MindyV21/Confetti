package com.codepath.confetti.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Note {

    public static final String TAG = "NoteModel";

    public List<String> text;
    // TODO: add tags map?

    public Note() {};

    public void setText(JSONObject jsonObject) throws JSONException {
        text = new ArrayList<>();
        JSONArray prediction = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("prediction");

        for (int i = 0; i < prediction.length(); i++) {
            text.add(prediction.getJSONObject(i).getString("ocr_text"));
            Log.d(TAG, prediction.getJSONObject(i).getString("ocr_text"));
        }
    }
}
