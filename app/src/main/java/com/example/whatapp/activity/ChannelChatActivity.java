package com.example.whatapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.opengl.Visibility;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.MessageSecurity;
import com.example.whatapp.R;
import com.example.whatapp.adapter.MessageRecyclerAdapter;
import com.example.whatapp.model.*;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

public class ChannelChatActivity extends AppCompatActivity {

    private ImageView attachFile;
    private ArrayList<String> chatMembers;
    private EditText getMessage;
    private Channel channelModel;
    private DatabaseReference db;
    private MessageRecyclerAdapter messageRecyclerAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MaterialCardView sendMessage;
    private String myNumber;
    private TextView messageBlock;
    private MaterialCardView sendMessageBox;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_chat);
        initializeVariable();
        listeners();
        inflateMessage();
        setUserOnline();
    }

    public ChannelChatActivity() {
        channelModel = Common.getInstance().channelModel;
    }
    public void inflateMessage() {
        final ArrayList arrayList = new ArrayList();
        db.child("channels").child(Common.getInstance().channelModel.getUid()).child("messages").addValueEventListener(new ValueEventListener(){

            public void onCancelled(DatabaseError databaseError) {
            }

            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayList.clear();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    Message message = ((DataSnapshot)iterator.next()).getValue(Message.class);
//                    MessageSecurity ms = new MessageSecurity();
//                    message.setMessage(ms.decrypt(message.getMessage()));
                    arrayList.add(message);
                }
                recyclerView.smoothScrollToPosition(messageRecyclerAdapter.getItemCount());
                messageRecyclerAdapter.notifyDataSetChanged();
            }
        });

        messageRecyclerAdapter = new MessageRecyclerAdapter(this, (ArrayList<Message>)arrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(messageRecyclerAdapter);
    }

    public void initializeVariable() {
        setTitle(Common.getInstance().channelModel.getChannelName());
        getMessage = findViewById(R.id.write_message_channel);
        sendMessage = findViewById(R.id.right_send_image_channel);
        recyclerView = findViewById(R.id.message_recycler_view_channel);
        attachFile = findViewById(R.id.attach_file_channel);
        progressBar = findViewById(R.id.progress_bar_chat_channel);
        sendMessageBox = findViewById(R.id.write_message_cardview_channel);
        messageBlock = findViewById(R.id.message_blocked);

        db = FirebaseDatabase.getInstance().getReference();
        chatMembers = new ArrayList<>();
        myNumber = getSharedPreferences("UserData", MODE_PRIVATE).getString("MobileNumber", "");

        String myNumber = Common.getInstance().senderMobileNumber;
        if (Common.getInstance().channelModel.getMessageAccess() != null &&
                !Common.getInstance().channelModel.getMessageAccess().contains(myNumber)) {

            attachFile.setVisibility(View.GONE);
//            sendMessageBox.setVisibility(View.GONE);
            messageBlock.setText("You don't have permission to send message");
            getMessage.setVisibility(View.GONE);
            sendMessage.setVisibility(View.GONE);
            messageBlock.setVisibility(View.VISIBLE);
        }

        channelModel = Common.getInstance().channelModel;
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void listeners() {
        sendMessage.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                String message = getMessage.getText().toString().trim();
                if (message.length() == 0) {
                    return;
                }
                sendMessage(message, false);
                getMessage.setText("");
            }
        });
        attachFile.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction("android.intent.action.GET_CONTENT");
                startActivityForResult(Intent.createChooser((Intent)intent, (CharSequence)"Select Picture"), 200);
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

    public void sendMessage(String message, Boolean isImage) {
        String uid = String.valueOf(System.currentTimeMillis());
        MessageSecurity ms = new MessageSecurity();
        message = ms.encrypt(message);

        db.child("channels").child(Common.getInstance().channelModel.getUid())
                .child("messages").child(uid)
                .setValue(new Message(message, myNumber, isImage, uid));

        for (Member member : channelModel.getMembers()) {
            if (member.getNumber().equals(Common.getInstance().senderMobileNumber)) continue;
            updateUserMessage(member.getNumber());
        }
    }

    public void setUserOffline() {
        if (channelModel == null)
            return;

//        channelModel.getMembersData().put(Common.getInstance().senderMobileNumber,
//                new UserChatData("", 0));
//        db.child("channels").child(channelModel.getUid()).setValue(channelModel);

        db.child("channels").child(channelModel.getUid()).child("membersData")
                .child(Common.getInstance().senderMobileNumber)
                .setValue(new UserChatData("", 0));
    }

    public void setUserOnline() {
//        channelModel.getMembersData().put(Common.getInstance().senderMobileNumber,
//                new UserChatData(channelModel.getUid(), 0));
//        db.child("channels").child(channelModel.getUid()).setValue(channelModel);

        db.child("channels").child(channelModel.getUid()).
                child("membersData").child(Common.getInstance().senderMobileNumber).
                setValue(new UserChatData(channelModel.getUid(), MODE_PRIVATE));
    }

    public void updateUserMessage(final String string2) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("groups").child(channelModel.getUid()).child("membersData");
        databaseReference.child(string2).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){

            public void onComplete(Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("Error", task.getException().getMessage());
                    return;
                }
                if ((task.getResult()).getValue(UserChatData.class) == null) {
                    databaseReference.child(string2).setValue(new UserChatData("", 1));
                    return;
                }
                UserChatData userChatData = (task.getResult()).getValue(UserChatData.class);
                if (userChatData.getActiveWith().equals(channelModel.getUid())) {
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
                                    sendMessage((task.getResult()).toString(), true);
                                }
                            });
                        }

                    }).addOnFailureListener(new OnFailureListener(){

                        public void onFailure(Exception exception) {
                            Log.i("image", "Upload Failed");
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ChannelChatActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu2) {
        getMenuInflater().inflate(R.menu.channel_setting, menu2);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.channel_setting: {
                startActivity(new Intent(this, ChannelSettingActivity.class));
//                Toast.makeText(ChannelChatActivity.this, "channel setting clicked" , Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.leave_channel: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Warning");
                dialog.setMessage("Do you really want to leave this Channel");

                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        channelModel.getMessageAccess().remove(myNumber);
                        for (int i = 0; i < channelModel.getMembers().size(); i++)
                            if (channelModel.getMembers().get(i).getNumber().equals(myNumber))
                                channelModel.getMembers().remove(i);

                        db.child("channelsReferences").child(myNumber).child(channelModel.getUid()).removeValue();

                        if (channelModel.getMessageAccess().size() == 0 && channelModel.getMembers().size() > 0)
                            channelModel.getMessageAccess()
                                    .add(channelModel.getMembers().get(0).getNumber());

                        db.child("channels").child(channelModel.getUid()).setValue(channelModel);
                        startActivity(new Intent(ChannelChatActivity.this, MainActivity.class));
                        finish();
                    }
                });

                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                dialog.show();
                return true;
            }

            case R.id.delete_channel: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Warning");
                dialog.setMessage("do you really want to delete this channel");

                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Member member: channelModel.getMembers())
                            db.child("channelsReferences").child(member.getNumber()).child(channelModel.getUid())
                                    .removeValue();

//                db.child("channels").child(channelModel.getUid()).child("membersData").removeValue();
                        db.child("channels").child(channelModel.getUid()).removeValue();

                        Toast.makeText(ChannelChatActivity.this, channelModel.getChannelName()
                        + " is deleted successfully", Toast.LENGTH_SHORT);

                        Common.getInstance().channelModel = channelModel = null;

                        startActivity(new Intent(ChannelChatActivity.this, MainActivity.class));
                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                dialog.show();
                return true;
            }

            case android.R.id.home: {
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem m = menu.findItem(R.id.delete_channel);
        if (!channelModel.getMessageAccess().contains(myNumber))
            m.setVisible(false);
        return true;
    }
}