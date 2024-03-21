package com.example.transformingparking;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PayActivity extends AppCompatActivity {
    long milliSeconds;
    long hours;
    long minutes;
    String parkingId;
    TextView time;
    TextView costView;
    Button payBtn;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        time = findViewById(R.id.textViewTime);
        costView = findViewById(R.id.textViewCost);
        payBtn = findViewById(R.id.buttonPay);

        Intent intent = getIntent();
        milliSeconds = intent.getLongExtra("milliSeconds", 60_000 * 10);
        parkingId = intent.getStringExtra("parkingId");

        time.setText(formatTime(milliSeconds));

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("parking_spaces").document(parkingId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        int price = Integer.parseInt(Objects.requireNonNull(document.get("price")).toString());
                        float cost = ((float) price) * (hours + (float) minutes / 60);
                        costView.setText(Math.round(cost) + getString(R.string.dram));
                        progressDialog.cancel();
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

        payBtn.setOnClickListener(v -> {
            DocumentReference docRef = db.collection("parking_spaces").document(parkingId);

            // Create a map to update the desired field
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", Constants.PAID);

            // Perform the update operation
            docRef.update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document field successfully updated!");
                            Toast.makeText(PayActivity.this, "Paid", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });
        });
    }

    private String formatTime(long milliseconds) {
        hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(hours);

        StringBuilder formattedTime = new StringBuilder();

        if (hours > 0) {
            formattedTime.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        }
        if (minutes > 0) {
            formattedTime.append(minutes).append(" minute").append(minutes > 1 ? "s" : "").append(" ");
        }

        if (formattedTime.length() == 0) {
            formattedTime.append("0 minutes");
        }

        return formattedTime.toString().trim();
    }
}