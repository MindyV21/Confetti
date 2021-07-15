package com.codepath.confetti.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Note {

    public String photoFileUrl;
    public List<String> text;
    // TODO: add tags map?

    public Note() {};

    public Note(String photoFileUrl) {
        this.photoFileUrl = photoFileUrl;
        text = null;
    }

    public void setText(JSONObject jsonObject) throws JSONException {
        text = new ArrayList<>();
        JSONArray prediction = jsonObject.getJSONArray("result").getJSONObject(0).getJSONArray("prediction");

        for (int i = 0; i < prediction.length(); i++) {
            text.add(prediction.getJSONObject(i).getString("ocr_text"));
        }
    }
}
