package com.example.whatapp.model;

import android.util.ArrayMap;
import com.example.whatapp.Common;

import java.util.*;

public class Channel {
    String channelName;
    ArrayList<Member> members;
    String uid;
    ArrayList<String> messageAccess;
    HashMap<String, UserChatData> membersData;
    String imageLink;

    public Channel() {
    }

    public Channel(String channelName, ArrayList<Member> members, String uid) {
        this.channelName = channelName;
        this.members = members;
        this.uid = uid;
        messageAccess = new ArrayList<>();
        messageAccess.add(Common.getInstance().senderMobileNumber);
        membersData = new HashMap<>();
    }

    public ArrayList<String> getMessageAccess() {
        if (messageAccess == null)
            messageAccess = new ArrayList<>();
        return messageAccess;
    }

    public HashMap<String, UserChatData> getMembersData() {
        if (membersData == null)
            membersData = new HashMap<>();
        return membersData;
    }

    public void putMembersData(String number, UserChatData userChatData) {
        membersData.put(number, userChatData);
//        this.membersData = membersData;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public void setMessageAccess(ArrayList<String> messageAccess) {
        this.messageAccess = messageAccess;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public ArrayList<Member> getMembers() {
        if (members == null)
            members = new ArrayList<>();
        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
