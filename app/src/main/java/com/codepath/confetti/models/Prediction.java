package com.codepath.confetti.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Prediction {

    public String label;
    public int xMin;
    public int yMin;
    public int xMax;
    public int yMax;
    public String text;

    public Prediction() {}

    // dummy data predictions
    public Prediction(int xMin, int yMin, int xMax, int yMax, String text) {
        label = "Topic";
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.text = text;
    }

    public Prediction(JSONObject jsonObject) throws JSONException {
        label = jsonObject.getString("label");
        xMin = jsonObject.getInt("xmin");
        yMin = jsonObject.getInt("ymin");
        xMax = jsonObject.getInt("xmax");
        yMax = jsonObject.getInt("ymax");
        text = jsonObject.getString("ocr_text");
    }
}
