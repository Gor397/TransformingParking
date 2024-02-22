package com.example.transformingparking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText verificationCodeEditText;
    private Button verifyCodeButton;
    private TextView err_message;

    private String mVerificationId;
    private String fullName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        verificationCodeEditText = findViewById(R.id.editTextVerificationCode);
        verifyCodeButton = findViewById(R.id.buttonVerifyCode);
        err_message = findViewById(R.id.err_message);

        mAuth = FirebaseAuth.getInstance();

        mVerificationId = getIntent().getStringExtra("verificationId");
        fullName = getIntent().getStringExtra("fullName");

        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verificationCode = verificationCodeEditText.getText().toString().trim();
                if (!verificationCode.isEmpty()) {
                    verifyPhoneNumberWithCode(mVerificationId, verificationCode);
                } else {
                    Toast.makeText(VerifyCodeActivity.this, "Please enter the verification code", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();

                            assert user != null;

                            DocumentReference docRef = db.collection("users").document(user.getUid());

                            Map<String, Object> currentUser = new HashMap<>();
                            currentUser.put("phone", user.getPhoneNumber());
                            currentUser.put("name", fullName);

                            docRef.set(currentUser);

                            Intent intent = new Intent(VerifyCodeActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Sign in failed
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                err_message.setText("Invalid Verification Code");
                                Toast.makeText(VerifyCodeActivity.this, "Invalid Verification Code", Toast.LENGTH_SHORT).show();
                            } else {
                                err_message.setText("Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage());
                                Toast.makeText(VerifyCodeActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
