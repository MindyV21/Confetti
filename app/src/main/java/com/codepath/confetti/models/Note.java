package com.codepath.confetti.models;

public class Note {

    private String name;

    public Note() {
        name = "";
    };

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
