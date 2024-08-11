package com.example.whatapp.activity;

import android.annotation.SuppressLint;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.MessageSecurity;
import com.example.whatapp.Notification.FcmNotificationsSender;
import com.example.whatapp.model.Message;
import com.example.whatapp.adapter.MessageRecyclerAdapter;
import com.example.whatapp.R;
import com.example.whatapp.model.UserChatData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private EditText getSendMessage;
    private ImageView sendMessageButton;
    public static String senderNumber;
    public static String receiverNumber;
    public static String receiverName;
    private static DatabaseReference myRef;
    private MessageRecyclerAdapter messageRecyclerAdapter;
    private RecyclerView recyclerView;
    private ImageView attachFile;
    private ProgressBar progressBar;
    private UserChatData userChatData;
    private ValueEventListener valueEventListener;
    private BroadcastReceiver broadcastReceiver;
    public static String receiverToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.whatapp.R.layout.activity_chat);

        initializeVariable();
        setTitle(receiverName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listeners();

        inflateMessage();

        setUserOnline();
//        updateOnline();
//        Common.getInstance().setTheme(this);
        getReceiverToken();
    }

    public void initializeVariable() {
        getSendMessage = findViewById(com.example.whatapp.R.id.write_message);
        sendMessageButton = findViewById(com.example.whatapp.R.id.send_message);
        attachFile = findViewById(com.example.whatapp.R.id.attach_file);
        recyclerView = findViewById(com.example.whatapp.R.id.message_recycler_view);
        progressBar = findViewById(R.id.progress_bar_chat);
        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        senderNumber = sp.getString("MobileNumber", "");
//        senderNumber = sp.getString("senderNumber", "");
        sp = getSharedPreferences("ChatData", MODE_PRIVATE);
//        senderNumber = Common.getInstance().senderMobileNumber;
//        Common.getInstance().senderMobileNumber = senderNumber;
        receiverNumber = sp.getString("receiverNumber", "");
        receiverName = sp.getString("receiverName", "");
        myRef = FirebaseDatabase.getInstance().getReference();

        Log.i("Sender Number " , senderNumber);
        Log.i("Receiver Number " , receiverNumber);

        // Broadcast receiver
        IntentFilter broadcasts = new IntentFilter();
        broadcasts.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        broadcasts.addAction("");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // do something
//                Log.i("Broadcast", "network change");
                if (isNetworkConnected())
                    setUserOnline();
                else {
                    myRef.child("messages").child(getHash())
                            .child(Common.getInstance().senderMobileNumber).child("activeWith").setValue("");
                }
            }
        };
        registerReceiver(broadcastReceiver, broadcasts);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void listeners() {
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = getSendMessage.getText().toString();
                if (message.length() == 0 || message.equals("") || message.trim().length() == 0)
                    return;

                sendMessage(message, false, getApplicationContext());
                getSendMessage.setText("");
            }
        });

        attachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                final Dialog dialog = new Dialog(ChatActivity.this);
//
//                dialog.setCancelable(true);
//                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                lp.copyFrom(dialog.getWindow().getAttributes());
//                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
////                int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.90);
////                int height = (int)(getResources().getDisplayMetrics().heightPixels * 0.20);
//                dialog.show();
////                dialog.getWindow().setLayout(width, height);
//
//                dialog.setContentView(R.layout.upload_data_layout);
//                ImageView gallery = dialog.findViewById(R.id.upload_image);

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                int SELECT_PICTURE = 200;

                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

            }
        });

    }

    public void setUserOnline() {
//        myRef.child("messages").child(getHash())
//                .child(Common.getInstance().senderMobileNumber)
//                .child("activeWith").setValue(receiverNumber);
//        myRef.child("messages").child(getHash())
//                .child(Common.getInstance().senderMobileNumber)
//                .child("unseenChat").setValue(0);
        userChatData = new UserChatData(receiverNumber, 0);
        myRef.child("messages").child(getHash())
                .child(senderNumber).setValue(userChatData);

        SharedPreferences sp = getSharedPreferences("Online", Context.MODE_PRIVATE);
        SharedPreferences.Editor chatEdit = sp.edit();
        chatEdit.putString("online", receiverNumber);
        chatEdit.apply();
    }

    public void setUserOffline() {
//        myRef.child("messages").child(getHash())
//                .child(Common.getInstance().senderMobileNumber)
//                .child("activeWith").setValue("");

        userChatData = new UserChatData("", 0);
        myRef.child("messages").child(getHash())
                .child(senderNumber).setValue(userChatData);


        SharedPreferences sp = getSharedPreferences("Online", Context.MODE_PRIVATE);
        SharedPreferences.Editor chatEdit = sp.edit();
        chatEdit.putString("online", "");
        chatEdit.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isNetworkConnected())
            setUserOffline();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUserOnline();
        Common.getInstance().setTheme(this);
    }

    public static String getHash() {
        String num1, num2;
        if (senderNumber.compareTo(receiverNumber) > 0) {
            num1 = senderNumber;
            num2 = receiverNumber;
        } else {
            num1 = receiverNumber;
            num2 = senderNumber;
        }

        return num1 + "&" + num2;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void inflateMessage() {
        ArrayList<Message> messages = new ArrayList<>();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Message message = data.getValue(Message.class);
                    messages.add(message);

                    // update that i have seen-ed the message
                    if (message.getSender().equals(receiverNumber) && !userChatData.getActiveWith().equals(""))
                        myRef.child("messages").child(getHash()).child("chats")
                                .child(message.getUid()).child("messageSeen").setValue(true);
//                    Log.d("Data", message.getMessage());
                }

                // set my un-seen message zero
                setUserOnline();
                recyclerView.smoothScrollToPosition(messageRecyclerAdapter.getItemCount());
                messageRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        };
        myRef.child("messages").child(getHash()).child("chats").addValueEventListener(valueEventListener);

        messageRecyclerAdapter = new MessageRecyclerAdapter(this, messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageRecyclerAdapter);
    }

    FirebaseStorage storage;
    StorageReference storageReference;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            int SELECT_PICTURE = 200;
            if (requestCode == SELECT_PICTURE) {
                Uri filePath = data.getData();
                Log.i("Image", filePath.toString());
                progressBar.setVisibility(View.VISIBLE);
                uploadImage(filePath);
                if (null != filePath) {
                }
            }
        }
    }

    public void uploadImage(Uri filePath) {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Bitmap bitmap;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), filePath));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int scaleWidth = bitmap.getWidth() / 4;
        int scaleHeight = bitmap.getHeight() / 4;

        byte[] downSizedImage = getDownsizedImageBytes(bitmap, scaleWidth, scaleHeight, filePath);


        if (filePath != null) {
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
//            ref.putBytes(downSizedImage)
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i("image", "Uploaded");
                            taskSnapshot.getUploadSessionUri();
//                            Log.d("image sessionUri", taskSnapshot.getUploadSessionUri().toString());
                            taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Uri> task) {
                                    task.getResult();
                                    progressBar.setVisibility(View.GONE);
//                                    Log.d("image sessionUri", task.getResult().toString());
                                    sendMessage(task.getResult().toString(), true, getApplicationContext());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.i("image", "Upload Failed");
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ChatActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private byte[] getDownsizedImageBytes(Bitmap bitmap, int scaleWidth, int scaleHeight, Uri filePath) {
        long fileSize = 0L;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        try {
            InputStream inputStream = getContentResolver().openInputStream(filePath);
            if (inputStream != null) {
                byte[] bytes = new byte[1024];
                int read = -1;
                while ((read = inputStream.read(bytes)) >= 0) {
                    fileSize += read;
                    byteBuffer.write(bytes, 0, read);
                }
            }
            inputStream.close();
        } catch (Exception e) {
        }
        long fileSizeInKb = fileSize / 1024;

        if (fileSizeInKb > 500) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        }

        return byteBuffer.toByteArray();
    }

//    public static void sendMessage(String message, Boolean isImage) {
//        if (this != null) {
//            sendMessage(message, isImage, this);
//            return;
//        }
//
//        sendMessage(message, isImage, );
//    }


    public static void sendMessage(String message, Boolean isImage, Context context) {

        MessageSecurity ms = new MessageSecurity();
        message = ms.encrypt(message);
        Log.i("encrypted message " , message);
//        message = ms.decrypt(message);
//        Log.i("decrypted message ", message);

        final String currentTimestamp = String.valueOf(System.currentTimeMillis());
        myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child("messages").child(getHash()).child("chats").child(currentTimestamp)
                .setValue(new Message(message, senderNumber, isImage, currentTimestamp));

        // update -> is message is seen or not
//        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("messages").child(getHash());
//        dbRef.child(receiverNumber).child("activeWith").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//            @Override
//            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
//                if(!task.isSuccessful())
//                    return;
//
//                if (task.getResult().getValue() == null) {  // 1st time chat
//                    dbRef.child(receiverNumber).child("activeWith").setValue("");
//                    dbRef.child(receiverNumber).child("unseenChat").setValue(1);
//                    return;
//                }
//
//                // if user not active with me
//                if (!task.getResult().getValue().toString().equals(senderNumber)){
//                    dbRef.child(receiverNumber).child("unseenChat").get()
//                            .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
//                                    if (!task.isSuccessful())
//                                        return;
//
//                                    int numberOfUnseenMessage = Integer.parseInt(task.getResult().getValue().toString());
//                                    numberOfUnseenMessage++;
//                                    dbRef.child(receiverNumber).child("unseenChat").setValue(numberOfUnseenMessage);
//                                }
//                            });
//                }
//
//                Log.i("onlineWith", task.getResult().getValue().toString());
//            }
//        });

        // message is seen or not
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("messages").child(getHash());
        dbRef.child(receiverNumber).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DataSnapshot> task) {
                if (!task.isSuccessful())
                    return;

                if (task.getResult().getValue() == null) {  // 1st time chat
                    dbRef.child(receiverNumber).setValue(new UserChatData("", 1));
                    return;
                }

                UserChatData userChatData = task.getResult().getValue(UserChatData.class);
//                 if user not active with me
                userChatData.setUnseenChat(userChatData.getUnseenChat() + 1);
                if (!userChatData.getActiveWith().equals(senderNumber)) {
                    userChatData.updateDateAndTime();
                    dbRef.child(receiverNumber).setValue(userChatData);
                }
                else {
                    userChatData.updateDateAndTime();
                    dbRef.child(receiverNumber).setValue(userChatData);
                }

            }
        });

        if (receiverToken == null)
            return;
//        Log.i("chat activity receive token " , receiverToken);
//        Log.i(" sender number " , receiverNumber);
//        Log.i("message " , message);
        FcmNotificationsSender notificationsSender = new FcmNotificationsSender(receiverToken,
//                senderNumber, message, context,ChatActivity.this);
                senderNumber, message, context, null);
        notificationsSender.SendNotifications();
    }


//    public static void sendMessageFrom(String message, Boolean isImage,
//                                       String senderMobileNumber, String receiverToken, ){
//
////        sendMessage(message,isImage);
//
//    }

    public void getReceiverToken() {
        Log.i("receiverNumber " , receiverNumber);
        this.myRef.child("notification").child(receiverNumber).child("token")
                .addValueEventListener(new ValueEventListener(){

            public void onCancelled(DatabaseError databaseError) {
            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                receiverToken = (String)dataSnapshot.getValue(String.class);

                if (receiverToken != null)
                Log.i((String)"token receiver", (String)ChatActivity.this.receiverToken);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myRef.child("messages").child(getHash()).child("chats").removeEventListener(valueEventListener);
        unregisterReceiver(broadcastReceiver);

//        SharedPreferences chat = getSharedPreferences("ChatData", Context.MODE_PRIVATE);
//        SharedPreferences.Editor chatEdit = chat.edit();
//        chatEdit.putString("receiverNumber", "");
//        chatEdit.apply();
    }

}