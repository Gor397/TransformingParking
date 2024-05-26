package com.transformingParking.transformingparking.Notifications;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.transformingParking.transformingparking.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class NotificationDetailsActivity extends AppCompatActivity {
    private String notificationId;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String userId;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private ImageView profilePic;
    private TextView nameView;
    private TextView phoneNumberView;
    private ImageButton callBtn;
    private ImageButton msgBtn;
    private TextView msgView;
    private TextView timestampView;
    private TextView costView;
    ProgressDialog progressDialog;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        profilePic = findViewById(R.id.imageViewProfilePic);
        nameView = findViewById(R.id.textViewUserName);
        phoneNumberView = findViewById(R.id.textViewPhoneNumber);
        callBtn = findViewById(R.id.buttonCall);
        msgBtn = findViewById(R.id.buttonMsg);

        msgView = findViewById(R.id.msgView);
        timestampView = findViewById(R.id.timestampView);
        costView = findViewById(R.id.costView);

        Intent intent = getIntent();
        notificationId = intent.getStringExtra("notificationId");

        db.collection("Notifications").document(notificationId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.contains("message") && documentSnapshot.contains("timestamp")) {
                            NotificationModel notification = documentSnapshot.toObject(NotificationModel.class);

                            userId = notification.getSenderId();

                            String msg = notification.getMessage();

                            int startIndex = msg.indexOf("(");
                            int endIndex = msg.lastIndexOf(")");

                            String cost = null;

                            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                                cost = msg.substring(startIndex + 1, endIndex);
                                msg = msg.substring(0, startIndex) + msg.substring(endIndex + 1);
                            }

                            msgView.setText(msg);

                            if (cost != null) {
                                costView.setText(cost);
                            } else {
                                costView.setVisibility(View.GONE);
                            }

                            String dt = null;
                            try {
                                dt = String.valueOf(dateFormat.parse(String.valueOf(notification.getTimestamp())));
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            timestampView.setText(dt);

                            setUpUserProfileView(userId);
                        }
                    }
                });
    }

    private void setUpUserProfileView(String userId) {
        StorageReference imageRef = storageReference.child("profile_pics").child(userId);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(NotificationDetailsActivity.this).load(uri).into(profilePic);
                progressDialog.cancel();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO Handle the error
                progressDialog.cancel();
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