package com.example.transformingparking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.transformingparking.databinding.ActivityRespondRequestBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class RespondRequestActivity extends AppCompatActivity {
    private ActivityRespondRequestBinding binding;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseDatabase realtimeDb = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String parkingId;
    String userId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add flags to show the activity when the screen is off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        binding = ActivityRespondRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        binding.videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.money));
//        binding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                // Restart video playback when completed
//                binding.videoView.start();
//            }
//        });
//        binding.videoView.start();

        Intent intent = getIntent();
        long hours = intent.getLongExtra("hours", 0);
        long minutes = intent.getLongExtra("minutes", 0);
        String duration;
        if (hours == 0) {
            duration = minutes + " minutes";
        } else if (minutes == 0) {
            duration = hours + " hours";
        } else {
            duration = hours + " hours " + minutes + " minutes";
        }
        String name = intent.getStringExtra("name");
        parkingId = intent.getStringExtra("parkingId");
        userId = intent.getStringExtra("userId");

        binding.duration.setText(duration);
        binding.name.setText(name);

        binding.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
                finish();
            }
        });
        binding.rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reject();
                finish();
            }
        });
    }

    public void accept() {
        RequestStatusConstants c = new RequestStatusConstants();
        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;
        DatabaseReference myRequests = realtimeDb.getReference(currentUser.getUid());

        myRequests.child("received_requests").child(userId).child("status").setValue(c.ACCEPTED_REQUEST).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference ownerRequests = realtimeDb.getReference(userId);
                        ownerRequests.child("sent_requests").child(currentUser.getUid()).child("status").setValue(c.ACCEPTED_REQUEST).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("status", false);
                                        database.collection("parking_spaces").document(parkingId).update(updates)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(RespondRequestActivity.this, "Request Accepted!", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(RespondRequestActivity.this, "Failed to accept the request!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RespondRequestActivity.this, "Failed to accept the request!", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RespondRequestActivity.this, "Failed to accept the request! ", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void reject() {
        RequestStatusConstants c = new RequestStatusConstants();
        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;
        DatabaseReference myRequests = realtimeDb.getReference(currentUser.getUid());

        myRequests.child("received_requests").child(userId).child("status").setValue(c.REJECTED_REQUEST).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DatabaseReference ownerRequests = realtimeDb.getReference(userId);
                        ownerRequests.child("sent_requests").child(currentUser.getUid()).child("status").setValue(c.REJECTED_REQUEST).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(RespondRequestActivity.this, "Request Rejected!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RespondRequestActivity.this, "Failed to reject the request!", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RespondRequestActivity.this, "Failed to reject the request! ", Toast.LENGTH_LONG).show();
                    }
                });
    }
}