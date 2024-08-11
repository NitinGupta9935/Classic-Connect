package com.example.whatapp.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    String date;
    boolean isImage;
    String message;
    private boolean messageSeen;
    String sender;
    String time;
    private String uid;

    public Message() {
        this.updateDateAndTime();
    }

    public Message(String message, String sender, Boolean isImage, String uid) {
        this.message = message;
        this.sender = sender;
        this.isImage = isImage;
        this.uid = uid;
        this.updateDateAndTime();
    }

    public String getDate() {
        return this.date;
    }

    public String getMessage() {
        return this.message;
    }

    public String getSender() {
        return this.sender;
    }

    public String getTime() {
        return this.time;
    }

    public String getUid() {
        return this.uid;
    }

    public boolean isImage() {
        return this.isImage;
    }

    public boolean isMessageSeen() {
        return this.messageSeen;
    }

    public void setDate(String string2) {
        this.date = string2;
    }

    public void setImage(boolean bl) {
        this.isImage = bl;
    }

    public void setMessage(String string2) {
        this.message = string2;
    }

    public void setMessageSeen(boolean bl) {
        this.messageSeen = bl;
    }

    public void setSender(String string2) {
        this.sender = string2;
    }

    public void setTime(String string2) {
        this.time = string2;
    }

    public void setUid(String string2) {
        this.uid = string2;
    }

    public void updateDateAndTime() {
        String[] arrstring = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()).split(" ");
        this.date = arrstring[0];
        this.time = arrstring[1].substring(0, 5);
    }
}
