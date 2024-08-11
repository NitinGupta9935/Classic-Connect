package com.example.whatapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.whatapp.Common;
import com.example.whatapp.R;
import com.example.whatapp.adapter.ChannelSettingRecyclerAdapter;
import com.example.whatapp.model.Channel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class ChannelSettingActivity extends AppCompatActivity {
    private Channel channelModel;
    private RecyclerView recyclerView;
    private ChannelSettingRecyclerAdapter adapter;
    private TextView numberOfParticipants;
    private TextView channelName;
    private ImageView channelImage;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference myRef;
    private ProgressBar imageProgressBar;
    private int IMAGE_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_setting);

        initializeVariable();
        listeners();
    }

    private void listeners() {
        channelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction("android.intent.action.GET_CONTENT");
                ChannelSettingActivity.this.startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST_CODE);
            }
        });
    }

    public void onActivityResult(int n, int n2, Intent intent) {
        super.onActivityResult(n, n2, intent);
        if (n2 == -1 && n == IMAGE_REQUEST_CODE) {
            Uri uri = intent.getData();
            uploadImage(uri);
        }
    }

    public void uploadImage(Uri uri) {
        FirebaseStorage firebaseStorage;
        storage = firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        imageProgressBar.setVisibility(View.VISIBLE);
        channelImage.setVisibility(View.GONE);

        if (uri != null) {
            storageReference.child("images/" + UUID.randomUUID().toString()).putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){

                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i("image", "Uploaded");
                    taskSnapshot.getUploadSessionUri();
                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>(){

                        public void onComplete(Task<Uri> task) {
                            String imageLink = task.getResult().toString();
                            channelModel.setImageLink(imageLink);

                            myRef.child("channels").child(channelModel.getUid())
                                    .child("imageLink").setValue(imageLink);
                            Picasso.get().load(imageLink).into(channelImage);
                            imageProgressBar.setVisibility(View.GONE);
                            channelImage.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener(){
                public void onFailure(Exception exception) {
                    Log.i("image", "Upload Failed");
                    imageProgressBar.setVisibility(View.GONE);
                    channelImage.setVisibility(View.VISIBLE);
                    Toast.makeText(ChannelSettingActivity.this, exception.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initializeVariable() {
        channelModel = Common.getInstance().channelModel;
        numberOfParticipants = findViewById(R.id.number_of_participants);
        channelImage = findViewById(R.id.channel_setting_image);
        myRef = FirebaseDatabase.getInstance().getReference();
        imageProgressBar = findViewById(R.id.image_progress_bar);

        if (channelModel.getImageLink() != null)
            Picasso.get().load(channelModel.getImageLink()).into(channelImage);

        if (channelModel != null)
            numberOfParticipants.setText(channelModel.getMembers().size() + " participants");

        channelName = findViewById(R.id.channel_name);
        channelName.setText(channelModel.getChannelName());

        setTitle("Channel Setting");
        Common.getInstance().setTheme(this);

        adapter = new ChannelSettingRecyclerAdapter(this, channelModel);
        recyclerView = findViewById(R.id.channel_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }

//            default: super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
}