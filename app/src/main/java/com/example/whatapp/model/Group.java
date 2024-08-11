package com.example.whatapp.model;

import java.util.ArrayList;

public class Group {
    String groupName;
    ArrayList<Member> members;
    String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Group() {
    }

    public Group(String groupName, ArrayList<Member> members, String uid) {
        this.groupName = groupName;
        this.members = members;
        this.uid = uid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<Member> getMembers() {
        if (members == null) {
            members = new ArrayList<>();
        }
        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }
}
