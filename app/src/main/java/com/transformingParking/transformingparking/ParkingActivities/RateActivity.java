package com.transformingParking.transformingparking.ParkingActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.transformingParking.transformingparking.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RateActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String parkingId;

    private RatingBar ratingBar;
    private EditText editTextReview;
    private Button buttonSubmit;
    private Button buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        parkingId = getIntent().getStringExtra("parkingId");

        ratingBar = findViewById(R.id.ratingBar);
        editTextReview = findViewById(R.id.editTextReview);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            buttonSubmit.setEnabled(rating > 0);
        });

        buttonSubmit.setOnClickListener(v -> submitRating());

        buttonCancel = findViewById(R.id.buttonCancel);

        buttonCancel.setOnClickListener(v -> {
            Intent intent = new Intent(RateActivity.this, MapActivity.class); // Replace 'MapActivity' with your actual map activity's class
            startActivity(intent);
        });
    }

    private void submitRating() {
        int stars = (int) ratingBar.getRating();
        String feedback = editTextReview.getText().toString().trim();
        String userId = user.getUid();

        saveRatingInfo(stars, feedback, userId);
    }

    private void saveRatingInfo(int stars, String feedback, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> rating = new HashMap<>();
        rating.put("stars", stars);
        rating.put("feedback", feedback);
        rating.put("userId", userId);
        rating.put("timestamp", FieldValue.serverTimestamp());

        db.collection("parking_spaces").document(parkingId).collection("ratings").add(rating)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                          @Override
                                          public void onSuccess(DocumentReference documentReference) {
                                              Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                                              Toast.makeText(RateActivity.this, "Submitted", Toast.LENGTH_SHORT).show();
                                          }
                                      })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Firestore", "Error adding document", e);
                                Toast.makeText(RateActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        });

        finish();
    }
}