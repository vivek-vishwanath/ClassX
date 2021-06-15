package com.example.classx.ui.messages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

public class Contact implements Serializable {

    private final String uid;
    private byte[] bytes;
    FirebaseFirestore mFirebaseFirestore;

    public Contact(String uid) {
        this.uid = uid;
        this.mFirebaseFirestore = FirebaseFirestore.getInstance();
    }

    public String getUid() {
        return uid;
    }

    public void setBytes(ImageView imageView) {
        Bitmap bitmap;
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        bitmap = drawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bytes = stream.toByteArray();
    }

    public Bitmap getBytes() {
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }

    @NonNull
    @Override
    public String toString() {
        return uid;
    }
}
