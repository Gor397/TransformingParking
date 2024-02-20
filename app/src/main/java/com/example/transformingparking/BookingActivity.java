package com.example.transformingparking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    ImageView imageView;
    TextView priceView;
    TextView descriptionView;
    Button ownerAccBtn;
    Button directionsBtn;
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
        directionsBtn = findViewById(R.id.directions_btn);

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