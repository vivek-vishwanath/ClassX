package com.example.classx.ui.forums.navigation.thread;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.classx.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Date;

public class ForumPost implements Serializable {

    private final String uid;
    private final String message;
    private final String group;
    private final Date time;

    public ForumPost(String uid, String message, String forumID, Date time) {
        this.uid = uid;
        this.message = message;
        this.group = forumID;
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return time;
    }

    public String getTime(){
        Log.wtf("(ForumPost.java:48)", getDate().toString().substring(11, 16));
        return getDate().toString().substring(11, 16);
    }

    @NonNull
    @Override
    public String toString() {
        return uid + ": " + message;
    }
}

