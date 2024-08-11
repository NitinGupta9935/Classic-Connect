package com.example.whatapp.activity;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.whatapp.R;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jsibbold.zoomage.ZoomageView;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;

public class ImageViewActivity extends AppCompatActivity {

//    private ImageView image;
    private ZoomageView image;
    TextView download;
    private String link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        image = findViewById(R.id.view_image);
//        download = findViewById(R.id.download);

        showImage();

        /*download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReference();
//                StorageReference pathReference = storageReference.child("image/stars.jpg");
//                StorageReference gsReference = storage.getReferenceFromUrl("gs://images/");
//                StorageReference httpsReference = storage.getReferenceFromUrl(link);

                StorageReference islandRef = storageReference.child("images/island.jpg");

                final long ONE_MEGABYTE = 1024 * 1024;
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {

                        Toast.makeText(getApplicationContext(), "downloaded ", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });
            }
        });*/

    }

    public void showImage() {
        SharedPreferences sp = getSharedPreferences("image", MODE_PRIVATE);
        link = sp.getString("link", "");
        if (link == null || link.equals(""))
            onBackPressed();
        else
            Picasso.get().load(link).into(image);
        Log.i("link ", link);
    }
}