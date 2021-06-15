package com.example.classx.ui.forums.navigation.thread;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ObjectSerializer {

    public static String serialize(ForumPost post){
        String serializedObject = "";

        // serialize the object
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(post);
            so.flush();
            serializedObject = new String(Base64.encode(bo.toByteArray(), 0));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return serializedObject;
    }

    public static ForumPost deserialize(String s){
        try {
            byte[] b = Base64.decode(s.getBytes(),0);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (ForumPost) si.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<ForumPost> deserialize(ArrayList<String> strings){
        ArrayList<ForumPost> arrayList = new ArrayList<>();
        for(String s : strings){
            arrayList.add(deserialize(s));
        }
        return arrayList;
    }
}