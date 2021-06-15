package com.example.classx.ui.forums.fragment;

import java.io.Serializable;
import java.util.ArrayList;

public class Forum implements Serializable {

    private final String name;
    private final String description;
    private final ArrayList<String> users;

    public Forum(String name, String creator, String description) {
        this.name = name;
        this.users = new ArrayList<>();
        this.users.add(creator);
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getUsers() {
        return users;
    }
}