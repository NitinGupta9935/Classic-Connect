package com.example.whatapp.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserChatData {
    String activeWith;
    String date;
    String time;
    int unseenChat;

    public UserChatData() {
    }

    public UserChatData(String string2, int n) {
        this.activeWith = string2;
        this.unseenChat = n;
        this.updateDateAndTime();
    }

    public UserChatData(String string2, int n, String string3, String string4) {
        this.activeWith = string2;
        this.unseenChat = n;
        this.time = string3;
        this.date = string4;
    }

    public String getActiveWith() {
        return this.activeWith;
    }

    public String getDate() {
        return this.date;
    }

    public String getTime() {
        return this.time;
    }

    public int getUnseenChat() {
        return this.unseenChat;
    }

    public void setActiveWith(String string2) {
        this.activeWith = string2;
    }

    public void setDate(String string2) {
        this.date = string2;
    }

    public void setTime(String string2) {
        this.time = string2;
    }

    public void setUnseenChat(int n) {
        this.unseenChat = n;
    }

    public void updateDateAndTime() {
        String[] arrstring = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()).split(" ");
        this.date = arrstring[0];
        this.time = arrstring[1].substring(0, 5);
    }
}
