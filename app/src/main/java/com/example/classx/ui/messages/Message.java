package com.example.classx.ui.messages;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {

    public String text;
    public String senderID;
    public String recipientID;
    public Date time;

    public Message(String text, String senderID, String recipientID) {
        this.text = text;
        this.senderID = senderID;
        this.recipientID = recipientID;
        time = new Date();
    }

    public Message(String text, String senderIDD) {
        this.text = text;
        this.senderID = senderID;
        time = new Date();
    }
}