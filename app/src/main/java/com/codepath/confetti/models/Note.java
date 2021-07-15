package com.codepath.confetti.models;

import org.json.JSONObject;

public class Note {

    private String name;

    public Note() {
        name = "";
    };

    public static Note fromJsonObject(JSONObject jsonObject) {
        Note note = new Note();

        // note properties

        return note;
    }

    public Note(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
