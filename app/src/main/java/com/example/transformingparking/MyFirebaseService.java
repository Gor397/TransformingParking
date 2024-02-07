package com.example.transformingparking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MyFirebaseService extends Service {

    private DatabaseReference callsRef;
    private ChildEventListener childEventListener;
    private FirebaseUser currentUser;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    ReuestStatusConstants c = new ReuestStatusConstants();

    @Override
    public void onCreate() {
        super.onCreate();
        currentUser = auth.getCurrentUser();
        // Initialize Firebase Realtime Database reference
        callsRef = FirebaseDatabase.getInstance().getReference(currentUser.getUid()).child("received_requests");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startListeningForCalls();
        return START_STICKY;
    }

    private void startListeningForCalls() {
        // Create a ChildEventListener to listen for changes in the "calls" node
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Handle new call requests
                if (c.AWAITING_REQUEST == dataSnapshot.child("status").getValue(Long.class)) {
                    Intent responseRequestIntent = new Intent(MyFirebaseService.this, RespondRequestActivity.class);
                    String userId = dataSnapshot.getKey();
                    db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // TODO set name and last name in the layout
                                    String name = (String) document.get("name");
                                    String lastname = (String) document.get("lastname");
                                    String ownerName = name + " " + lastname;
                                    responseRequestIntent.putExtra("name", ownerName);
                                    responseRequestIntent.putExtra("hours", dataSnapshot.child("hours").getValue(Long.class));
                                    responseRequestIntent.putExtra("minutes", dataSnapshot.child("minutes").getValue(Long.class));
                                    responseRequestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(responseRequestIntent);
                                }
                            } else {
                                // Error getting document
                                // TODO Handle the error here
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

            // Implement other ChildEventListener methods as needed
        };

        // Attach the ChildEventListener to the "calls" node
        callsRef.addChildEventListener(childEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Detach the ChildEventListener when the service is destroyed
        if (callsRef != null && childEventListener != null) {
            callsRef.removeEventListener(childEventListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public interface UserCallback {
        void onSuccess(String userName);
        void onFailure(String errorMessage);
    }
}