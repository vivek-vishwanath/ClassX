package com.example.classx.ui.messages;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ObjectSerializer {

    public static String serialize(Message message){
        String serializedObject = "";

        // serialize the object
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(message);
            so.flush();
            serializedObject = new String(Base64.encode(bo.toByteArray(), 0));
            Log.wtf("(messages\\ObjectSerializer.java:24)", serializedObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serializedObject;
    }

    public static Message deserialize(String s){
        try {
            byte[] b = Base64.decode(s.getBytes(),0);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            return (Message) si.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Message> deserialize(ArrayList<String> strings){
        ArrayList<Message> arrayList = new ArrayList<>();
        for(String s : strings){
            arrayList.add(deserialize(s));
        }
        return arrayList;
    }
}