package com.preppyapp.preppy.models;

import java.util.ArrayList;

public class Tags {
    private ArrayList<String> tags = new ArrayList<>();
    private String uid;

    public Tags() {

    }

    public Tags(ArrayList<String> tags, String uid) {
        this.tags = tags;
        this.uid = uid;
    }

    public ArrayList<String> getTags() {
        return this.tags;
    }

    public String getUid() {
        return uid;
    }
}
