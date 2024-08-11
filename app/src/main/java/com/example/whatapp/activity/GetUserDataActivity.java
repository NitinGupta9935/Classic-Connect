package com.example.whatapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.whatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GetUserDataActivity extends AppCompatActivity {

    private String mobileNumber;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText name;
    private EditText email;
    private EditText about;
    private boolean isDataValid = false;
    private MaterialButton continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user_data);

        linkVariableIDs();

        listeners();
//        getActionBar().hide();
        isNumberSaved();

    }

    public void linkVariableIDs() {
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        about = findViewById(R.id.about);
        continueButton = findViewById(R.id.continue_button);
    }

    public void listeners() {
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isDataValid)
                    return;
                saveUserData();
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isValid();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isValid();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void isValid() {

        if (name.getText().toString().length() > 0 && email.getText().toString().length() > 0){
            isDataValid = true;
            continueButton.setBackgroundColor(Color.parseColor("#5193f2"));
        }
        else {
            continueButton.setBackgroundColor(Color.parseColor("#a9a9a9"));
            isDataValid = false;
        }
    }

    public void saveUserData() {
        Map<String, String> user = new HashMap<>();
        user.put("name", name.getText().toString());
        user.put("email", email.getText().toString());
        user.put("about", about.getText().toString());

        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        String number = sp.getString("MobileNumber", "");

        db.collection("users").document(mobileNumber)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        startActivity(new Intent(GetUserDataActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(GetUserDataActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

//        startActivity(new Intent(GetUserDataActivity.this, MainActivity.class));
//        finish();
    }

    public void isNumberSaved() {
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        mobileNumber = sp.getString("MobileNumber", "");
        Log.d("Number", mobileNumber);
        if (mobileNumber.length() != 10) {
            startActivity(new Intent(GetUserDataActivity.this, LoginActivity.class));
            finish();
        }

        db.collection("users").document(mobileNumber).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("Error", task.getException().toString());
                            return;
                        }

                        String name = "";
                        try {
                            name = task.getResult().get("name").toString();
                        }
                        catch (Exception e) {
                            return;
//                            Toast.makeText(GetUserDataActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
//
//                        Log.d("DATA", task.getResult().get("name").toString());
//
//
                        String email = task.getResult().get("email").toString();
                        String about = task.getResult().get("about").toString();

                        if (name.length() == 0)
                            return;

                        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
                        SharedPreferences.Editor ed = sp.edit();
                        ed.putString("name", name);
                        ed.putString("email", email);
                        ed.putString("about", about);
                        ed.apply();

                        startActivity(new Intent(GetUserDataActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }

}