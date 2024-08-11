package com.example.whatapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.example.whatapp.Common;
import com.example.whatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import yuku.ambilwarna.AmbilWarnaDialog;

import java.util.UUID;

public class SettingActivity extends AppCompatActivity {
    private TextView about;
    private CardView changeTheme;
    private MaterialCardView imageProgressBar;
    private MaterialButton logout;
    private DatabaseReference myRef;
    private TextView name;
    private ImageView profilePic;
    private MaterialCardView profilePicVisibility;
    FirebaseStorage storage;
    StorageReference storageReference;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setContentView(R.layout.activity_setting);
        this.initializeVariable();
        this.listeners();
    }

    public void initializeVariable() {
        this.setTitle("Setting");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = this.findViewById(R.id.name);
        about = this.findViewById(R.id.about);
        profilePic = this.findViewById(R.id.profile_pic);
        imageProgressBar = this.findViewById(R.id.image_progress_bar);
        profilePicVisibility = this.findViewById(R.id.profile_pic_visiblity);
        changeTheme = this.findViewById(R.id.change_theme);
        logout = this.findViewById(R.id.logout);
        myRef = FirebaseDatabase.getInstance().getReference();
        testing();
        name.setText(Common.getInstance().userName);
        about.setText(Common.getInstance().userAbout);

        Common.getInstance().setTheme(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("");
    }

    public void listeners() {
        profilePic.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction("android.intent.action.GET_CONTENT");
                SettingActivity.this.startActivityForResult(Intent.createChooser(intent, "Select Picture"), 200);
            }
        });
        this.changeTheme.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                int n = ContextCompat.getColor(SettingActivity.this, (int)2131034146);
                new AmbilWarnaDialog(SettingActivity.this, n, new AmbilWarnaDialog.OnAmbilWarnaListener(){

                    @Override
                    public void onCancel(AmbilWarnaDialog ambilWarnaDialog) {
                    }

                    @Override
                    public void onOk(AmbilWarnaDialog ambilWarnaDialog, int n) {
                        SharedPreferences.Editor editor = SettingActivity.this.getSharedPreferences("theme", 0).edit();
                        editor.putString("color", n + "");
                        editor.apply();
                        Common.getInstance().themeColor = n;
                        Common.getInstance().setTheme(SettingActivity.this);
                    }
                }).show();
            }

        });
        this.logout.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SettingActivity.this.startActivity(new Intent((Context)SettingActivity.this, LoginActivity.class));
                SettingActivity.this.finish();
            }
        });
    }

    public void onActivityResult(int n, int n2, Intent intent) {
        super.onActivityResult(n, n2, intent);
        if (n2 == -1 && n == 200) {
            Uri uri = intent.getData();
            profilePicVisibility.setVisibility(View.GONE);
            imageProgressBar.setVisibility(View.VISIBLE);
            uploadImage(uri);
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            default: {
                return super.onOptionsItemSelected(menuItem);
            }
            case 16908332:
        }
        this.onBackPressed();
        return true;
    }

    protected void onResume() {
        super.onResume();
        Common.getInstance().setTheme((AppCompatActivity)this);
    }

    public void testing() {
        String string2;
        AppCompatDelegate.setDefaultNightMode((int)1);
        SharedPreferences sharedPreferences = this.getSharedPreferences("UserData", 0);
        Common.getInstance().userName = sharedPreferences.getString("name", "");
        Common.getInstance().userAbout = sharedPreferences.getString("about", "");
        Common.getInstance().profilePic = sharedPreferences.getString("profilePic", "");
        Common.getInstance().senderMobileNumber = string2 = this.getSharedPreferences("ChatData", 0).getString("senderNumber", "");
        if (!Common.getInstance().profilePic.equals((Object)"")) {
            Picasso.get().load(Common.getInstance().profilePic).into(this.profilePic);
        }
    }

    public void uploadImage(Uri uri) {
//        StorageReference storageReference;
        FirebaseStorage firebaseStorage;
        this.storage = firebaseStorage = FirebaseStorage.getInstance();
        this.storageReference = firebaseStorage.getReference();
        if (uri != null) {
            storageReference.child("images/" + UUID.randomUUID().toString()).putFile(uri).addOnSuccessListener((OnSuccessListener)new OnSuccessListener<UploadTask.TaskSnapshot>(){

                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i((String)"image", (String)"Uploaded");
                    taskSnapshot.getUploadSessionUri();
                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener((OnCompleteListener)new OnCompleteListener<Uri>(){

                        public void onComplete(Task<Uri> task) {
                            task.getResult();
                            SettingActivity.this.profilePicVisibility.setVisibility(0);
                            SettingActivity.this.imageProgressBar.setVisibility(8);
                            SettingActivity.this.myRef.child("profiles").child(Common.getInstance().senderMobileNumber).child("profilePic").setValue((Object)((Uri)task.getResult()).toString());
                            SharedPreferences.Editor editor = SettingActivity.this.getSharedPreferences("UserData", 0).edit();
                            editor.putString("profilePic", ((Uri)task.getResult()).toString());
                            editor.apply();
                            Common.getInstance().profilePic = ((Uri)task.getResult()).toString();
                            SettingActivity.this.initializeVariable();
                        }
                    });
                }

            }).addOnFailureListener(new OnFailureListener(){

                public void onFailure(Exception exception) {
                    Log.i((String)"image", (String)"Upload Failed");
                    SettingActivity.this.profilePicVisibility.setVisibility(8);
                    SettingActivity.this.imageProgressBar.setVisibility(0);
                    Toast.makeText((Context)SettingActivity.this, (CharSequence)exception.toString(), (int)0).show();
                }
            });
        }
    }

}