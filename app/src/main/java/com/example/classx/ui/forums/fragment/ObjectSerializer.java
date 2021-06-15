package com.example.classx.ui.forums.fragment;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ObjectSerializer {

    public static String serialize(Forum forum){
        String serializedObject = "";

        // serialize the object
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(forum);
            so.flush();
            serializedObject = new String(Base64.encode(bo.toByteArray(), 0));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return serializedObject;
    }

    public static Forum deserialize(String s){
        try {
            byte[] b = Base64.decode(s.getBytes(),0);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (Forum) si.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Forum> deserialize(ArrayList<String> strings){
        ArrayList<Forum> arrayList = new ArrayList<>();
        for(String s : strings){
            arrayList.add(deserialize(s));
        }
        return arrayList;
    }
}