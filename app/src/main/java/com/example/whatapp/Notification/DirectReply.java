/*
 * Decompiled with CFR 0.0.
 * 
 * Could not load the following classes:
 *  android.content.BroadcastReceiver
 *  android.content.Context
 *  android.content.Intent
 *  android.util.Log
 *  java.lang.String
 */
package com.example.whatapp.Notification;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.whatapp.activity.ChatActivity;
import com.example.whatapp.model.Message;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import static android.content.Context.MODE_PRIVATE;

public class DirectReply
extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        Log.i("remote input ", " some message has some");

        if (remoteInput == null) {
            Log.i("remote input", " remote input is null");
            return;
        }

        CharSequence replyText = remoteInput.getCharSequence("key");
        Log.i("remote input", replyText.toString());

//        Message message = new Message(replyText.toString(), "Me", false, null);
//        FirebaseMessagingService.messages.add(message);
        String message = replyText.toString().trim();

        FirebaseMessagingService.sendNotification(message, "Me", context);

        SharedPreferences sp = context.getSharedPreferences("UserData", MODE_PRIVATE);
        String senderNumber = sp.getString("MobileNumber", "");
        senderNumber = sp.getString("MobileNumber", "");
        sp = context.getSharedPreferences("ChatData", MODE_PRIVATE);
        String receiverNumber = sp.getString("receiverNumber", "");
        String receiverName = sp.getString("receiverName", "");

        ChatActivity.senderNumber = senderNumber;
        ChatActivity.receiverNumber = receiverNumber;
        ChatActivity.receiverName = receiverName;

        if (ChatActivity.receiverToken == null) {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
            myRef.child("notification").child(receiverNumber).child("token")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            String receiverToken = snapshot.getValue(String.class);
                            Log.i("notification get token ", receiverToken);
                            ChatActivity.receiverToken = receiverToken;
                            ChatActivity.sendMessage(message, false, context);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
        }
        else  {
            ChatActivity.sendMessage(message, false, context);
        }

    }
}

