package com.example.transformingparking;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {

    private EditText phoneNumberEditText;
    private EditText fullNameEditText;
    private TextView name_err_message;
    private TextView phone_err_message;
    private Button signInButton;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        fullNameEditText = findViewById(R.id.editTextFullName);
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber);
        signInButton = findViewById(R.id.button);
        progressDialog = new ProgressDialog(SignInActivity.this);
        name_err_message = findViewById(R.id.name_err_message);
        phone_err_message = findViewById(R.id.phone_err_message);

        mAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                if (!phoneNumber.isEmpty() && checkUserName()) {
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    sendVerificationCode(phoneNumber);
                } else {
                    Toast.makeText(SignInActivity.this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkUserName() {
        name_err_message.clearComposingText();
        String name = fullNameEditText.getText().toString();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (!Character.isLetter(ch)) {
                name_err_message.setText(R.string.this_field_should_contain_only_letters);
                return false;
            }
        }
        return true;
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressDialog.cancel();
                        Toast.makeText(SignInActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        mVerificationId = verificationId;
                        // Move to the next activity to enter the verification code
                        Intent intent = new Intent(SignInActivity.this, VerifyCodeActivity.class);
                        intent.putExtra("verificationId", mVerificationId);
                        intent.putExtra("fullName", fullNameEditText.getText().toString());
                        startActivity(intent);
                    }
                });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            assert user != null;

                            DocumentReference docRef = db.collection("users").document(user.getUid());

                            Map<String, Object> currentUser = new HashMap<>();
                            currentUser.put("phone", user.getPhoneNumber());
                            currentUser.put("name", fullNameEditText.getText().toString());

                            docRef.set(currentUser);

                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
}
