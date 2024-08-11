package com.example.whatapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.R;
import com.example.whatapp.adapter.MessageRecyclerAdapter;
import com.example.whatapp.model.Member;
import com.example.whatapp.model.Message;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class BroadcastChatActivity extends AppCompatActivity {
    private ImageView attachFile;
    private ArrayList<String> chatMembers;
    private EditText getMessage;
    private DatabaseReference mDatabase;
    private MessageRecyclerAdapter messageRecyclerAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MaterialCardView sendMessage;
    private String senderMobileNumber;
    FirebaseStorage storage;
    StorageReference storageReference;

    public String getHash(String string2) {
        String string3;
        String string4;
        if (this.senderMobileNumber.compareTo(string2) > 0) {
            string4 = this.senderMobileNumber;
            string3 = string2;
        } else {
            string4 = string2;
            string3 = this.senderMobileNumber;
        }
        return string4 + "&" + string3;
    }

    public void inflateMessage() {
        final ArrayList messages = new ArrayList();
        this.mDatabase.child("broadcast").child(Common.getInstance().broadCastModel.getUid()).child("messages").addValueEventListener(new ValueEventListener() {

            public void onCancelled(DatabaseError databaseError) {
            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    Message message = (Message) ((DataSnapshot) iterator.next()).getValue(Message.class);
                    message.setSender(BroadcastChatActivity.this.senderMobileNumber);
                    messages.add((Object) message);
                }
                BroadcastChatActivity.this.recyclerView.smoothScrollToPosition(BroadcastChatActivity.this.messageRecyclerAdapter.getItemCount());
                BroadcastChatActivity.this.messageRecyclerAdapter.notifyDataSetChanged();
            }
        });
        this.messageRecyclerAdapter = new MessageRecyclerAdapter(this, messages);
        this.recyclerView.setLayoutManager( new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        this.recyclerView.setAdapter(this.messageRecyclerAdapter);
    }

    public void initializeVariable() {
        setTitle("BroadCast");
        getMessage = findViewById(R.id.write_message_broadcast);
        sendMessage = findViewById(R.id.right_send_image_broadcast);
        recyclerView = findViewById(R.id.message_recycler_view_broadcast);
        progressBar = findViewById(R.id.progress_bar_chat_broadcast);
        attachFile = findViewById(R.id.attach_file_broadcast);
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.chatMembers = new ArrayList();
        this.senderMobileNumber = Common.getInstance().senderMobileNumber;

    }

    public void listners() {
        this.sendMessage.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String string2 = BroadcastChatActivity.this.getMessage.getText().toString();
                if (string2.length() == 0) {
                    return;
                }
                BroadcastChatActivity broadcastChatActivity = BroadcastChatActivity.this;
                broadcastChatActivity.writeMessage(broadcastChatActivity.senderMobileNumber, string2, false);
                for (Member member : Common.getInstance().broadCastModel.getMembers()) {
                    BroadcastChatActivity.this.sendMessage(member.getNumber(), string2, false);
                }
                BroadcastChatActivity.this.getMessage.setText((CharSequence) "");
            }
        });
        this.attachFile.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction("android.intent.action.GET_CONTENT");
                BroadcastChatActivity.this.startActivityForResult(Intent.createChooser((Intent) intent, (CharSequence) "Select Picture"), 200);
            }
        });
    }

    public void onActivityResult(int n, int n2, Intent intent) {
        super.onActivityResult(n, n2, intent);
        if (n2 == -1 && n == 200) {
            Uri uri = intent.getData();
            Log.i((String) "Image", (String) uri.toString());
            this.progressBar.setVisibility(0);
            this.uploadImage(uri);
        }
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(2131427356);
        this.initializeVariable();
        this.listners();
        this.inflateMessage();
    }

    protected void onResume() {
        super.onResume();
        Common.getInstance().setTheme((AppCompatActivity) this);
    }

    public void sendMessage(final String string2, String message, Boolean isImage) {
        String string4 = String.valueOf((long) System.currentTimeMillis());
        this.mDatabase.child("messages").child(this.getHash(string2)).child("chats").child(string4).setValue((Object) new Message(message, this.senderMobileNumber, isImage, string4));
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("messages").child(this.getHash(string2));
        databaseReference.child(string2).child("activeWith").get().addOnCompleteListener((OnCompleteListener) new OnCompleteListener<DataSnapshot>() {

            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    return;
                }
                if (((DataSnapshot) task.getResult()).getValue() == null) {
                    databaseReference.child(string2).child("activeWith").setValue((Object) "");
                    databaseReference.child(string2).child("unseenChat").setValue((Object) 1);
                    return;
                }
                if (!((DataSnapshot) task.getResult()).getValue().toString().equals((Object) BroadcastChatActivity.this.senderMobileNumber)) {
                    databaseReference.child(string2).child("unseenChat").get().addOnCompleteListener((OnCompleteListener) new OnCompleteListener<DataSnapshot>() {

                        public void onComplete(Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                return;
                            }
                            int n = 1 + Integer.parseInt((String) ((DataSnapshot) task.getResult()).getValue().toString());
                            databaseReference.child(string2).child("unseenChat").setValue((Object) n);
                        }
                    });
                }
                Log.i((String) "onlineWith", (String) ((DataSnapshot) task.getResult()).getValue().toString());
            }

        });
    }

    public void uploadImage(Uri uri) {
        StorageReference storageReference;
        FirebaseStorage firebaseStorage;
        this.storage = firebaseStorage = FirebaseStorage.getInstance();
        this.storageReference = storageReference = firebaseStorage.getReference();
        if (uri != null) {
            storageReference.child("images/" + UUID.randomUUID().toString()).putFile(uri).addOnSuccessListener((OnSuccessListener) new OnSuccessListener<UploadTask.TaskSnapshot>() {

                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i((String) "image", (String) "Uploaded");
                    taskSnapshot.getUploadSessionUri();
                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener((OnCompleteListener) new OnCompleteListener<Uri>() {

                        public void onComplete(Task<Uri> task) {
                            task.getResult();
                            BroadcastChatActivity.this.progressBar.setVisibility(8);
                            String string2 = ((Uri) task.getResult()).toString();
                            BroadcastChatActivity.this.writeMessage(BroadcastChatActivity.this.senderMobileNumber, string2, true);
                            for (Member member : Common.getInstance().broadCastModel.getMembers()) {
                                BroadcastChatActivity.this.sendMessage(member.getNumber(), string2, true);
                            }
                        }
                    });
                }

            }).addOnFailureListener(new OnFailureListener() {

                public void onFailure(Exception exception) {
                    Log.i((String) "image", (String) "Upload Failed");
                    BroadcastChatActivity.this.progressBar.setVisibility(8);
                    Toast.makeText((Context) BroadcastChatActivity.this, (CharSequence) exception.toString(), (int) 0).show();
                }
            });
        }
    }

    public void writeMessage(String string2, String string3, boolean bl) {
        String string4 = String.valueOf((long) System.currentTimeMillis());
        this.mDatabase.child("broadcast").child(Common.getInstance().broadCastModel.getUid()).child("messages").child(string4).setValue((Object) new Message(string3, string2, bl, string4));
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
        setContentView(R.layout.activity_broad_cast_chat);
        initializeVariable();
        listners();
        inflateMessage();

    }

    public void listners() {
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = getMessage.getText().toString();
                if (message.length() == 0)
                    return;

                writeMessage(senderMobileNumber, message, false);

                for(Member member: Common.getInstance().broadCastModel.getMembers())
                    sendMessage(senderMobileNumber, member.getNumber(), message, false);

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
//                                    sendMessage(task.getResult().toString(), true);
                                    String message = task.getResult().toString();
                                    writeMessage(senderMobileNumber, message, true);

                                    for(Member member: Common.getInstance().broadCastModel.getMembers())
                                        sendMessage(senderMobileNumber, member.getNumber(), message, true);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.i("image", "Upload Failed");
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(BroadcastChatActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void inflateMessage() {
        ArrayList<Message> messages = new ArrayList<>();

        mDatabase.child("broadcast").child(Common.getInstance().broadCastModel.getUid())
                .child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            Message message = dataSnapshot.getValue(Message.class);
                            message.setSender(senderMobileNumber);
                            Log.i("sender Mobile number", senderMobileNumber);
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

    public void writeMessage(String senderNumber, String message, boolean isImage) {

        final String currentTimestamp = String.valueOf(System.currentTimeMillis());
        mDatabase.child("broadcast").child(Common.getInstance().broadCastModel.getUid())
                .child("messages").child(currentTimestamp)
                .setValue(new Message(message, senderNumber, isImage));
    }

    public void sendMessage(String senderNumber, String receiverNumber, String message, Boolean isImage) {
        String num1, num2;
        if (senderNumber.compareTo(receiverNumber) > 0) {
            num1 = senderNumber;
            num2 = receiverNumber;
        }
        else {
            num1 = receiverNumber;
            num2 = senderNumber;
        }

        final String currentTimestamp = String.valueOf(System.currentTimeMillis());
        mDatabase.child("messages").child(num1 + "&" + num2).child(currentTimestamp)
                .setValue(new Message(message, senderNumber, isImage));
    }

    public void initializeVariable() {
        setTitle("BroadCast");
        getMessage = findViewById(R.id.write_message_broadcast);
        sendMessage = findViewById(R.id.right_send_image_broadcast);
        recyclerView = findViewById(R.id.message_recycler_view_broadcast);
        progressBar = findViewById(R.id.progress_bar_chat_broadcast);
        attachFile = findViewById(R.id.attach_file_broadcast);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        chatMembers = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        senderMobileNumber = sp.getString("MobileNumber", "");

    }

}*/