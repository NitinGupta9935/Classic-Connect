package com.example.whatapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.whatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.*;

import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class LoginActivity extends AppCompatActivity {

    MaterialButton loginButton;
    private FirebaseAuth mAuth;
    private EditText getNumber;
    private String verificationId;
    private boolean isNumberValid = false;
    private boolean verifyPhoneNumber = true;
    private TextView phoneNumberText;
    private TextView editMobileNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.whatapp.R.layout.activity_login);

        initializeVariable();
//        testing();
        mAuth = FirebaseAuth.getInstance();
//        mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        checkUserLoggedInBefore();

        listeners();

    }

    public void initializeVariable() {
        loginButton = findViewById(com.example.whatapp.R.id.login);
        getNumber = findViewById(com.example.whatapp.R.id.phone_number);
        phoneNumberText = findViewById(com.example.whatapp.R.id.phone_number_text);
        editMobileNumber = findViewById(R.id.reset_mobile_number);
        editMobileNumber.setVisibility(View.GONE);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void listeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNumberValid && getNumber.getText().toString().length() == 10)
                    Toast.makeText(LoginActivity.this, "OTP Sending", Toast.LENGTH_SHORT).show();
                if (isNumberValid && verifyPhoneNumber) {
                    sentOtp();
                }
                else if (!verifyPhoneNumber) {
//                    verifyOtp;
                    if (isNumberValid) {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, getNumber.getText().toString());
                        signInWithPhoneAuthCredential(credential);
                    }
                }
            }
        });

        getNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (verifyPhoneNumber && getNumber.getText().toString().length() == 10) {
                    loginButton.setBackgroundColor(Color.parseColor("#51cc60"));
                    isNumberValid = true;
                }
                else if (verifyPhoneNumber && getNumber.getText().toString().length() == 13
                        && getNumber.getText().toString().contains("+")) {
                    getNumber.setText(getNumber.getText().toString().substring(3));
                }
                else if (!verifyPhoneNumber && getNumber.getText().toString().length() == 6) {
                    loginButton.setBackgroundColor(Color.parseColor("#51cc60"));
                    isNumberValid = true;
                }
                else {
                    loginButton.setBackgroundColor(Color.parseColor("#FFAAAAAA"));
                    isNumberValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editMobileNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMobileNumber.setVisibility(View.GONE);
                startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    public void checkUserLoggedInBefore() {
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    public void sentOtp() {
        SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString("MobileNumber", getNumber.getText().toString());
        ed.apply();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + getNumber.getText().toString())       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    PhoneAuthProvider.ForceResendingToken mResendToken;

    PhoneAuthCredential credential;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential);

            Toast.makeText(LoginActivity.this, "Verification complete", Toast.LENGTH_SHORT).show();
            
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceededy
            }

            Log.d("Send OTP" , getNumber.getText().toString());
            Log.d("Send OTP Error" , e.getMessage());
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            // Show a message and update the UI
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
//            Log.d(TAG, "onCodeSent:" + verificationId);

            Toast.makeText(LoginActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
            Log.d("OTP", "OTP sent Successfully ");
            // Save verification ID and resending token so we can use them later
            LoginActivity.this.verificationId = verificationId;
            mResendToken = token;
            phoneNumberText.setText("Enter OTP Sent to +91" + getNumber.getText().toString());
            getNumber.setHint("Enter OTP");
            getNumber.setText("");
            verifyPhoneNumber = false;
            isNumberValid = false;
            editMobileNumber.setVisibility(View.VISIBLE);
        }
    };


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();

                            startActivity(new Intent(LoginActivity.this, GetUserDataActivity.class));
                            finish();
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(LoginActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
}

    private void testing() {
        getNumber.setText("9115275119");
        if (getNumber.getText().toString().length() == 10) {
            loginButton.setBackgroundColor(Color.parseColor("#51cc60"));
            isNumberValid = true;
        }
    }

}