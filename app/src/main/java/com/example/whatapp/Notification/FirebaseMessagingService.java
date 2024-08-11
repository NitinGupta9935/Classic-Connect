/*
 * Decompiled with CFR 0.0.
 * 
 * Could not load the following classes:
 *  android.app.Notification
 *  android.app.NotificationChannel
 *  android.app.NotificationManager
 *  android.app.PendingIntent
 *  android.content.ContentResolver
 *  android.content.Context
 *  android.content.Intent
 *  android.content.SharedPreferences
 *  android.content.SharedPreferences$Editor
 *  android.database.Cursor
 *  android.net.Uri
 *  android.os.Build
 *  android.os.Build$VERSION
 *  android.os.Vibrator
 *  android.provider.ContactsContract
 *  android.provider.ContactsContract$CommonDataKinds
 *  android.provider.ContactsContract$CommonDataKinds$Phone
 *  android.util.Log
 *  androidx.core.app.NotificationCompat
 *  androidx.core.app.NotificationCompat$Action
 *  androidx.core.app.NotificationCompat$Action$Builder
 *  androidx.core.app.NotificationCompat$BigTextStyle
 *  androidx.core.app.NotificationCompat$Builder
 *  androidx.core.app.NotificationCompat$MessagingStyle
 *  androidx.core.app.NotificationCompat$MessagingStyle$Message
 *  androidx.core.app.NotificationCompat$Style
 *  androidx.core.app.NotificationManagerCompat
 *  androidx.core.app.RemoteInput
 *  androidx.core.app.RemoteInput$Builder
 *  com.google.android.gms.tasks.Task
 *  com.google.firebase.database.DatabaseReference
 *  com.google.firebase.database.FirebaseDatabase
 *  com.google.firebase.messaging.FirebaseMessagingService
 *  com.google.firebase.messaging.RemoteMessage
 *  java.lang.CharSequence
 *  java.lang.Class
 *  java.lang.Object
 *  java.lang.String
 *  java.lang.System
 *  java.util.Map
 */
package com.example.whatapp.Notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import com.example.whatapp.Common;
import com.example.whatapp.MessageSecurity;
import com.example.whatapp.R;
import com.example.whatapp.activity.ChatActivity;
import com.example.whatapp.model.Message;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Map;

public class FirebaseMessagingService
extends com.google.firebase.messaging.FirebaseMessagingService {
    NotificationManager mNotificationManager;
    private DatabaseReference myRef;
    public static ArrayList<Message> messages;

    public String getUserName(String string) {
        Cursor cursor = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (contactNumber.contains((CharSequence)"-")) {
                contactNumber = contactNumber.replaceAll("-", "");
            }
            if (contactNumber.contains((CharSequence)" ")) {
                contactNumber = contactNumber.replace((CharSequence)" ", (CharSequence)"");
            }
            if (contactNumber.length() > 10) {
                contactNumber = contactNumber.substring(3);
            }
            if (!contactNumber.equals((Object)string)) continue;
            return contactName;
        }
        return string;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String number = this.getSharedPreferences("Online", MODE_PRIVATE).getString("online", "");
        Log.i("notification", "some notification has come");
        Log.i("notification", number);
        Log.i("notification", remoteMessage.getData().get("title"));

        if (number.equals(remoteMessage.getData().get("title")))
            return;

        SharedPreferences sharedPreferences = this.getSharedPreferences("ChatData", MODE_PRIVATE);
        String title = this.getUserName(remoteMessage.getData().get("title"));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("receiverNumber", remoteMessage.getData().get("title"));
        editor.putString("receiverName", title);
        editor.apply();

        sendNotification(remoteMessage.getData().get("body"), title);

    }

    public void sendNotification(String body, String title) {
        sendNotification(body, title, this);
    }

    public static void sendNotification(String body, String title, Context context) {

        String name = context.getSharedPreferences("Online", MODE_PRIVATE).getString("online", "");

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 300, 300, 300};
        v.vibrate(pattern, -1);

        Intent intent = new Intent(context.getApplicationContext(), ChatActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 1, intent, PendingIntent.FLAG_MUTABLE);

        RemoteInput remoteInput = new RemoteInput.Builder("key").setLabel((CharSequence)"Your answer...").build();
        PendingIntent pendingIntent2 = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent resultIntent = new Intent(context.getApplicationContext(), DirectReply.class);
            pendingIntent2 = PendingIntent.getBroadcast(context.getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_MUTABLE);
        }

        NotificationCompat.Action action = new NotificationCompat.Action
                .Builder(R.drawable.single_check, "send some message... ", pendingIntent2)
                .addRemoteInput(remoteInput)
                .build();

        NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat
                .MessagingStyle(name);

        messagingStyle.setConversationTitle((CharSequence)name);

        MessageSecurity ms = new MessageSecurity();
        if (messages == null)
            messages = new ArrayList<>();
        messages.add(new Message(ms.decrypt(body), title, false, System.currentTimeMillis() + ""));

        for (Message m: messages) {
            if (m.getMessage().trim().length() == 0)
                continue;
            NotificationCompat.MessagingStyle.Message notificationMessage =
                    new NotificationCompat.MessagingStyle.Message(
                            m.getMessage(),
                            System.currentTimeMillis(),
                            m.getSender()
                    );
            messagingStyle.addMessage(notificationMessage);
        }

//        messagingStyle.addMessage(new NotificationCompat.MessagingStyle.Message((CharSequence)number, System.currentTimeMillis(), (CharSequence)remoteMessage.getData().get((Object)"body")));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), "CHANNEL_1_ID");
        builder.setSmallIcon(R.drawable.single_check);
        builder.setContentTitle(title)
//                .setStyle(new NotificationCompat.BigTextStyle()
//                        .bigText(body))
                .setStyle(messagingStyle)
                .addAction(action)
                .setColor(Color.BLUE)
                .setPriority(1)
                .setCategory("msg")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context.getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManagerCompat.createNotificationChannel(new NotificationChannel("Your_channel_id", "Channel human readable title", NotificationManager.IMPORTANCE_HIGH));
            builder.setChannelId("Your_channel_id");
        }
        notificationManagerCompat.notify(1, builder.build());

    }

    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("token ", "Refreshed token: " + token);
        this.sendRegistrationToServer(token);
    }

    public void sendRegistrationToServer(String token) {
        DatabaseReference databaseReference;
        this.myRef = databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("notification").child(Common.getInstance().senderMobileNumber).child("token").setValue(token);
    }
}

