package com.example.whatapp.model;

import java.util.ArrayList;

public class BroadCast {
    private String senderMobileNumber;
    private String uid;
    private ArrayList<Member> members;

    public BroadCast(String senderMobileNumber, String uid, ArrayList<Member> members) {
        this.senderMobileNumber = senderMobileNumber;
        this.uid = uid;
        this.members = members;
    }

    public BroadCast() {
    }

    public String getSenderMobileNumber() {
        return senderMobileNumber;
    }

    public void setSenderMobileNumber(String senderMobileNumber) {
        this.senderMobileNumber = senderMobileNumber;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }
}
