package com.example.classx.ui.forums.navigation.calendar;

import android.icu.text.UFormat;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {

    private String name;
    private String description;
    private Date date;
    private String eventID;
    private String forumID;

    public Event(String name, String description, Date date, String eventID, String forumID) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.eventID = eventID;
        this.forumID = forumID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getForumID() {
        return forumID;
    }

    public void setForumID(String forumID) {
        this.forumID = forumID;
    }

    @NonNull
    @Override
    public String toString() {
        return  "\n" + eventID + "\n\t" + name + "\n\t" + date;
    }
}