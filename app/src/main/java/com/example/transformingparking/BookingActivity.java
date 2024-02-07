package com.example.transformingparking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;

public class BookingActivity extends AppCompatActivity {
    Button back_btn;
    String markerId;
    String ownerId;
    String[] owner;
    ImageView imageView;
    TextView priceView;
    TextView descriptionView;
    Button ownerAccBtn;
    Button sendRequestBtn;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseDatabase realtimeDb = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_view);

        back_btn = findViewById(R.id.back_btn);
        priceView = findViewById(R.id.price1);
        descriptionView = findViewById(R.id.descriptionTitle);
        imageView = findViewById(R.id.imageView);
        ownerAccBtn = findViewById(R.id.open_profile);
        sendRequestBtn = findViewById(R.id.send_request);

        Intent intent = getIntent();
        if (intent != null) {
            markerId = intent.getStringExtra("markerId");
        }

        db.collection("parking_spaces").document(markerId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long price = (long) document.get("price");
                        String description = (String) document.get("additional_info");
                        StorageReference imageRef = storage.getReference().child("parking_pics").child(markerId);

                        ownerId = (String) document.get("user_id");
                        setOwner();

                        priceView.setText("Price: " + price);
                        descriptionView.setText(description);
                        imageRef.getBytes(1024 * 1024) // Max size of the image in bytes
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        // Convert the byte array to a Bitmap
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                        // Display the Bitmap in the ImageView
                                        imageView.setImageBitmap(bitmap);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // TODO Handle errors
                                    }
                                });
                    } else {
                        // Document does not exist
                        // TODO Handle the case here
                    }
                } else {
                    // Error getting document
                    // TODO Handle the error here
                }
            }
        });

        NumberPicker minutes = findViewById(R.id.minutes);
        minutes.setMaxValue(60);
        minutes.setMinValue(20);

        NumberPicker hours = findViewById(R.id.hours);
        hours.setMaxValue(24);
        hours.setMinValue(0);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent mapIntent = new Intent(BookingActivity.this, MapActivity.class);
//                startActivity(mapIntent);
                finish();
            }
        });

        ownerAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(BookingActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest(hours.getValue(), minutes.getValue());
            }
        });
    }

    private void sendRequest(int hours, int minutes) {
        ReuestStatusConstants c = new ReuestStatusConstants();
        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;
        DatabaseReference myRequests = realtimeDb.getReference(currentUser.getUid());
        myRequests.child("sent_requests").child(ownerId).setValue(new ParkingRequest(c.AWAITING_REQUEST, hours, minutes)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference ownerRequests = realtimeDb.getReference(ownerId);
                        ownerRequests.child("received_requests").child(currentUser.getUid()).setValue(new ParkingRequest(c.AWAITING_REQUEST, hours, minutes)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(BookingActivity.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(BookingActivity.this, "Failed to send the request!", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BookingActivity.this, "Failed to send the request!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    void setOwner() {
        db.collection("users").document(ownerId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = (String) document.get("name");
                        String lastname = (String) document.get("lastname");
                        ownerAccBtn.setText(name + " " + lastname);
                    }
                } else {
                    // Error getting document
                    // TODO Handle the error here
                }
            }
        });
    }
}