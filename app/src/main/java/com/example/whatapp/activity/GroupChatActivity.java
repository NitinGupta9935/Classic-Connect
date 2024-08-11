package com.example.whatapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.R;
import com.example.whatapp.adapter.MessageRecyclerAdapter;
import com.example.whatapp.model.Group;
import com.example.whatapp.model.Member;
import com.example.whatapp.model.Message;
import com.example.whatapp.model.UserChatData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class GroupChatActivity extends AppCompatActivity {

    private ImageView attachFile;
    private ArrayList<String> chatMembers;
    private EditText getMessage;
    private Group group;
    private DatabaseReference mDatabase;
    private MessageRecyclerAdapter messageRecyclerAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MaterialCardView sendMessage;
    private String senderMobileNumber;
    FirebaseStorage storage;
    StorageReference storageReference;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_group_chat);
        initializeVariable();
        listeners();
        inflateMessage();
        setUserOnline();
    }

    public GroupChatActivity() {
        group = Common.getInstance().groupModel;
    }

    public void inflateMessage() {
        final ArrayList arrayList = new ArrayList();
        mDatabase.child("groups").child(Common.getInstance().groupModel.getUid())
                .child("messages")
                .addValueEventListener(new ValueEventListener(){

            public void onCancelled(DatabaseError databaseError) {
            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayList.clear();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    Message message = ((DataSnapshot)iterator.next()).getValue(Message.class);
                    arrayList.add(message);
                }
                recyclerView.smoothScrollToPosition(messageRecyclerAdapter.getItemCount());
                messageRecyclerAdapter.notifyDataSetChanged();
            }
        });
        messageRecyclerAdapter = new MessageRecyclerAdapter(this, arrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(this.messageRecyclerAdapter);
    }

    public void initializeVariable() {
        setTitle(Common.getInstance().groupModel.getGroupName());
        getMessage = findViewById(R.id.write_message_group);
        sendMessage = findViewById(R.id.right_send_image_group);
        recyclerView = findViewById(R.id.message_recycler_view_group);
        attachFile = findViewById(R.id.attach_file_group);
        progressBar = findViewById(R.id.progress_bar_chat_group);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        chatMembers = new ArrayList<>();
        senderMobileNumber = getSharedPreferences("UserData", MODE_PRIVATE).getString("MobileNumber", "");
    }

    public void listeners() {
        sendMessage.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                String string2 = getMessage.getText().toString();
                if (string2.length() == 0)
                    return;

                sendMessage(string2, false);
                getMessage.setText("");
            }
        });
        this.attachFile.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction("android.intent.action.GET_CONTENT");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 200);
            }
        });
    }

    public void onActivityResult(int n, int n2, Intent intent) {
        super.onActivityResult(n, n2, intent);
        if (n2 == -1 && n == 200) {
            Uri uri = intent.getData();
            Log.i("Image", uri.toString());
            progressBar.setVisibility(View.VISIBLE);
            uploadImage(uri);
        }
    }

    protected void onPause() {
        super.onPause();
        this.setUserOffline();
    }

    protected void onResume() {
        super.onResume();
        this.setUserOnline();
        Common.getInstance().setTheme(this);
    }

    public void sendMessage(String string2, Boolean bl) {
        String string3 = String.valueOf(System.currentTimeMillis());
        mDatabase.child("groups").child(Common.getInstance().groupModel.getUid())
                .child("messages").child(string3)
                .setValue(new Message(string2, senderMobileNumber, bl, string3));

        for (Member member : group.getMembers()) {
            if (member.getNumber().equals(Common.getInstance().senderMobileNumber))
                continue;
                updateUserMessage(member.getNumber());
        }
    }

    public void setUserOffline() {
        mDatabase.child("groups").child(group.getUid())
                .child("membersData").child(Common.getInstance().senderMobileNumber)
                .setValue(new UserChatData("", 0));
    }

    public void setUserOnline() {
        this.mDatabase.child("groups").child(group.getUid())
                .child("membersData").child(Common.getInstance().senderMobileNumber)
                .setValue(new UserChatData(group.getUid(), 0));
    }

    public void updateUserMessage(final String string2) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(group.getUid())
                .child("membersData");
        databaseReference.child(string2).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){

            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("Error", task.getException().getMessage());
                    return;
                }
                if (task.getResult().getValue(UserChatData.class) == null) {
                    databaseReference.child(string2).setValue(new UserChatData("", 1));
                    return;
                }
                UserChatData userChatData = task.getResult().getValue(UserChatData.class);
                if (userChatData.getActiveWith().equals(group.getUid())) {
                    userChatData.updateDateAndTime();
                    databaseReference.child(string2).setValue(userChatData);
                    return;
                }
                userChatData.setUnseenChat(1 + userChatData.getUnseenChat());
                Log.i("unseenChat", String.valueOf(userChatData.getUnseenChat()));
                userChatData.updateDateAndTime();
                databaseReference.child(string2).setValue(userChatData);
            }
        });
    }

    public void uploadImage(Uri uri) {
        StorageReference storageReference;
        FirebaseStorage firebaseStorage;
        this.storage = firebaseStorage = FirebaseStorage.getInstance();
        this.storageReference = storageReference = firebaseStorage.getReference();
        if (uri != null) {
            storageReference.child("images/" + UUID.randomUUID().toString()).putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){

                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i("image", "Uploaded");
                    taskSnapshot.getUploadSessionUri();
                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>(){

                        public void onComplete(Task<Uri> task) {
                            task.getResult();
                            progressBar.setVisibility(View.GONE);
                            sendMessage(task.getResult().toString(), true);
                        }
                    });
                }

            }).addOnFailureListener(new OnFailureListener(){

                public void onFailure(Exception exception) {
                    Log.i("image", "Upload Failed");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GroupChatActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}

/*
    private DatabaseReference mDatabase;
    private EditText getMessage;
    private MaterialCardView sendMessage;
    private ImageView attachFile;
    private ProgressBar progressBar;
    private ArrayList<String> chatMembers;
    private String senderMobileNumber;
    private RecyclerView recyclerView;
    private MessageRecyclerAdapter messageRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        initializeVariable();
        listeners();
        inflateMessage();
    }

    public void initializeVariable() {
//        setTitle("Group");
        setTitle(Common.getInstance().groupModel.getGroupName());
        getMessage = findViewById(R.id.write_message_group);
        sendMessage = findViewById(R.id.right_send_image_group);
        recyclerView = findViewById(R.id.message_recycler_view_group);
        attachFile = findViewById(R.id.attach_file_group);
        progressBar = findViewById(R.id.progress_bar_chat_group);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        chatMembers = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        senderMobileNumber = sp.getString("MobileNumber", "");
//        Common.getInstance().senderMobileNumber = senderMobileNumber;
    }

    public void listeners() {

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = getMessage.getText().toString();
                if (message.length() == 0)
                    return;

                sendMessage(message, false);

                getMessage.setText("");
            }
        });

        attachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                int SELECT_PICTURE = 200;
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
        });
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
        if(filePath != null) {
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
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
                                    sendMessage(task.getResult().toString(), true);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.i("image", "Upload Failed");
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(GroupChatActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void sendMessage(String message, Boolean isImage) {
        final String currentTimestamp = String.valueOf(System.currentTimeMillis());
        String uid = Common.getInstance().groupModel.getUid();
        mDatabase.child("groups").child(uid)
                .child("messages").child(currentTimestamp)
                .setValue(new Message(message, senderMobileNumber, isImage, uid));
    }

    public void inflateMessage() {
        ArrayList<Message> messages = new ArrayList<>();

        mDatabase.child("groups").child(Common.getInstance().groupModel.getUid())
                .child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            Message message = dataSnapshot.getValue(Message.class);
//                            message.setSender(senderMobileNumber);
//                            Log.i("sender Mobile number", senderMobileNumber);
                            messages.add(message);
//                            Log.i("Message", message.getMessage());
                        }
                        recyclerView.smoothScrollToPosition(messageRecyclerAdapter.getItemCount());
                        messageRecyclerAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
        messageRecyclerAdapter = new MessageRecyclerAdapter(this, messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageRecyclerAdapter);
    }
}*/
