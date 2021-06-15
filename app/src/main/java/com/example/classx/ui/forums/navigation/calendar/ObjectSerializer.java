package com.example.classx.ui.forums.navigation.calendar;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ObjectSerializer {

    public static String serialize(Event event){
        String serializedObject = "";

        // serialize the object
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(event);
            so.flush();
            serializedObject = new String(Base64.encode(bo.toByteArray(), 0));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return serializedObject;
    }

    public static Event deserialize(String s){
        try {
            byte[] b = Base64.decode(s.getBytes(),0);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (Event) si.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Event> deserialize(ArrayList<String> strings){
        ArrayList<Event> arrayList = new ArrayList<>();
        for(String s : strings){
            arrayList.add(deserialize(s));
        }
        return arrayList;
    }
}