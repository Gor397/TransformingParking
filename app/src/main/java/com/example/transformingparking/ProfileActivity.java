package com.example.transformingparking;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_PERMISSION = 1;
    private String userId;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private ImageView profilePic;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView nameView;
    private TextView phoneNumberView;
    private Button callBtn;
    private Button msgBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        userId = getIntent().getStringExtra("userId");
        profilePic = findViewById(R.id.imageViewProfilePic);
        nameView = findViewById(R.id.textViewUserName);
        phoneNumberView = findViewById(R.id.textViewPhoneNumber);
        callBtn = findViewById(R.id.buttonCall);
        msgBtn = findViewById(R.id.buttonMsg);

        StorageReference imageRef = storageReference.child("profile_pics").child(userId);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ProfileActivity.this).load(uri).into(profilePic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO Handle the error
            }
        });

        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = (String) document.get("name");
                        nameView.setText(name);
                        String phoneNumber = (String) document.get("phone");
                        phoneNumberView.setText(phoneNumber);
                    }
                } else {
                    // Error getting document
                    // TODO Handle the error here
                }
            }
        });

        callBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumberView.getText().toString()));
            startActivity(intent);
        });

        msgBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phoneNumberView.getText().toString()));
            startActivity(intent);
        });
    }
}
