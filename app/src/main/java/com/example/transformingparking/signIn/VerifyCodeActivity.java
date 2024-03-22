package com.example.transformingparking.signIn;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
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

import com.example.transformingparking.MainActivity;
import com.example.transformingparking.R;
import com.example.transformingparking.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText verificationCodeEditText;
    private Button verifyCodeButton;
    private TextView err_message;
    private ProgressDialog progressDialog;

    private String mVerificationId;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        verificationCodeEditText = findViewById(R.id.editTextVerificationCode);
        verifyCodeButton = findViewById(R.id.buttonVerifyCode);
        err_message = findViewById(R.id.err_message);
        progressDialog = new ProgressDialog(VerifyCodeActivity.this);

        mVerificationId = getIntent().getStringExtra("verificationId");

        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verificationCode = verificationCodeEditText.getText().toString().trim();
                if (!verificationCode.isEmpty()) {
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    verifyPhoneNumberWithCode(mVerificationId, verificationCode);
                } else {
                    Toast.makeText(VerifyCodeActivity.this, "Please enter the verification code", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUserFCMToken(FirebaseUser firebaseUser) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();

                        Map<String, Object> userMetadata = new HashMap<>();
                        userMetadata.put("FCMToken", fcmToken);

                        db.collection("users").document(firebaseUser.getUid())
                                .set(userMetadata, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Save Metadata", "User metadata saved successfully");
                                })
                                .addOnFailureListener(e -> Log.e("Save Metadata", "Error saving user metadata", e));
                    } else {
                        Log.e("FCM Token", "Failed to get FCM token", task.getException());
                    }
                });
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        if (getIntent().getBooleanExtra("updatePhoneNumber", false)) {
            FirebaseUser user = mAuth.getCurrentUser();
            assert user != null;
            user.updatePhoneNumber(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.cancel();
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Phone number updated.");

                        DocumentReference docRef = db.collection("users").document(user.getUid());
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("phone", user.getPhoneNumber());

                        docRef.update(updates);

                        finish();
                    } else {
                        Log.d(TAG, "Failed to update phone number.", task.getException());
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            err_message.setText(getString(R.string.this_number_is_already_used_in_another_account));
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        err_message.setText(R.string.invalid_code);
                    }
                }
            });
        } else if (getIntent().getBooleanExtra("deleteAccount", false)) {
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    db.collection("parking_spaces").whereEqualTo("user_id", user.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            StorageReference parkingPicRef = storageRef.child("parking_pics").child(document.getId());
                            parkingPicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: deleted parking picture");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // TODO
                                    Log.d(TAG, "onFailure: Failed to delete parking picture" + e.getMessage());
                                }
                            });

                            document.getReference().delete();
                        }
                    });

                    StorageReference profilePicRef = storageRef.child("profile_pics").child(user.getUid());
                    profilePicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: deleted profile picture");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // TODO
                            Log.d(TAG, "onFailure: Failed to delete profile picture " + e.getMessage());
                        }
                    });

                    db.collection("users").document(user.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "onComplete: Deleted user from Firestore Database");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // TODO
                            Log.d(TAG, "onFailure: Failed to remove user from Firestore Database " + e.getMessage());
                        }
                    });

                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User account deleted.");
                                Intent intent = new Intent(VerifyCodeActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Failed to delete FirebaseUser " + e.getMessage());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO
                    Log.d(TAG, "onFailure: Failed to delete account" + e.getMessage());
                }
            });
        } else {
            mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();

                        assert user != null;

                        DocumentReference docRef = db.collection("users").document(user.getUid());
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists() && document.contains("name")) {
                                        Util.setUserName(document.getString("name"));
                                        setUserFCMToken(user);
                                        Intent intent = new Intent(VerifyCodeActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Map<String, Object> currentUser = new HashMap<>();
                                        currentUser.put("phone", user.getPhoneNumber());

                                        docRef.set(currentUser);

                                        Intent intent = new Intent(VerifyCodeActivity.this, WriteNameActivty.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    // TODO handle the error
                                    Log.d(TAG, "Failed to get document.", task.getException());
                                }
                            }
                        });
                    } else {
                        // Sign in failed
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            progressDialog.cancel();
                            err_message.setText(R.string.invalid_verification_code);
                            Toast.makeText(VerifyCodeActivity.this, "Invalid Verification Code", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.cancel();
                            err_message.setText(MessageFormat.format("{0}{1}", getString(R.string.authentication_failed), Objects.requireNonNull(task.getException()).getMessage()));
                            Toast.makeText(VerifyCodeActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
}
