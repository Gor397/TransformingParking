package com.example.transformingparking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

import java.text.DecimalFormat;

public class BookingActivity extends AppCompatActivity {
    private static final int MIN_RENTING_MINUTES = 20;
    Button back_btn;
    String markerId;
    String ownerId;
    String[] owner;
    ImageView imageView;
    TextView priceView;
    TextView descriptionView;
    Button ownerAccBtn;
    Button sendRequestBtn;
    TextView costView;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseDatabase realtimeDb = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_view);

        back_btn = findViewById(R.id.back_btn);
        priceView = findViewById(R.id.price1);
        descriptionView = findViewById(R.id.description1);
        imageView = findViewById(R.id.imageView);
        ownerAccBtn = findViewById(R.id.open_profile);
        sendRequestBtn = findViewById(R.id.send_request);
        costView = findViewById(R.id.cost);

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

                        priceView.setText("Price: " + price + " dram per hour");
                        descriptionView.setText(description);

                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Load the image into the ImageView using Glide
                                Glide.with(BookingActivity.this).load(uri).into(imageView);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle any errors
                            }
                        });

                        DecimalFormat df = new DecimalFormat("#");
                        int cost = (int) (Double.parseDouble(df.format((double) price / 60 * MIN_RENTING_MINUTES)));
                        costView.setText("Total cost: " + cost + " dram");

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
        minutes.setMaxValue(59);
        minutes.setMinValue(MIN_RENTING_MINUTES);

        NumberPicker hours = findViewById(R.id.hours);
        hours.setMaxValue(23);
        hours.setMinValue(0);

        hours.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (newVal != 0) {
                    minutes.setMinValue(0);
                } else {
                    minutes.setMinValue(MIN_RENTING_MINUTES);
                }
                int price = Integer.parseInt(((String) priceView.getText()).replaceAll("\\D", ""));
                DecimalFormat df = new DecimalFormat("#");
                int cost = (int) (Double.parseDouble(df.format((double) price / 60 * minutes.getValue())) + price * newVal);
                costView.setText("Total cost: " + cost + " dram");
            }
        });

        minutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int price = Integer.parseInt(((String) priceView.getText()).replaceAll("\\D", ""));
                DecimalFormat df = new DecimalFormat("#");
                int cost = (int) (Double.parseDouble(df.format((double) price / 60 * newVal)) + price * hours.getValue());
                costView.setText("Total cost: " + cost + " dram");
            }
        });

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
        RequestStatusConstants c = new RequestStatusConstants();
        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;
        DatabaseReference myRequests = realtimeDb.getReference(currentUser.getUid());
        myRequests.child("sent_requests").child(ownerId).setValue(new ParkingRequest(markerId, c.AWAITING_REQUEST, hours, minutes)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference ownerRequests = realtimeDb.getReference(ownerId);
                        ownerRequests.child("received_requests").child(currentUser.getUid()).setValue(new ParkingRequest(markerId, c.AWAITING_REQUEST, hours, minutes)).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                        Toast.makeText(BookingActivity.this, "Failed to send the request! ", Toast.LENGTH_LONG).show();
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
                        ownerAccBtn.setText(name);
                    }
                } else {
                    // Error getting document
                    // TODO Handle the error here
                }
            }
        });
    }
}