package com.example.transformingparking;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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

public class MyFirebaseService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "NTFCHNLID";
    private DatabaseReference receivedRef;
    private DatabaseReference sentRef;
    private ChildEventListener childEventListener;
    private FirebaseUser currentUser;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    RequestStatusConstants c = new RequestStatusConstants();
    NotificationChannel channel;
    NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        currentUser = auth.getCurrentUser();
        // Initialize Firebase Realtime Database reference
        receivedRef = FirebaseDatabase.getInstance().getReference(currentUser.getUid()).child("received_requests");
        sentRef = FirebaseDatabase.getInstance().getReference(currentUser.getUid()).child("sent_requests");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(NOTIFICATION_ID, createNotification());
        startListeningForRequests();
        startListeningForAnswers();
        return START_STICKY;
    }

    private void startListeningForRequests() {
        // Create a ChildEventListener to listen for changes in the "calls" node
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Handle new call requests
                if (c.AWAITING_REQUEST == dataSnapshot.child("status").getValue(Long.class)) {
//                    Intent responseRequestIntent = new Intent(MyFirebaseService.this, RespondRequestActivity.class);
                    String userId = dataSnapshot.getKey();
                    db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @SuppressLint("ScheduleExactAlarm")
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String name = (String) document.get("name");
//                                    responseRequestIntent.putExtra("name", name);
//                                    responseRequestIntent.putExtra("hours", dataSnapshot.child("hours").getValue(Long.class));
//                                    responseRequestIntent.putExtra("minutes", dataSnapshot.child("minutes").getValue(Long.class));
//                                    responseRequestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(responseRequestIntent);

                                    Intent intent = new Intent(MyFirebaseService.this, MyBroadcastReceiver.class);
                                    intent.putExtra("userId", userId);
                                    intent.putExtra("parkingId", dataSnapshot.child("parking_id").getValue(String.class));
                                    intent.putExtra("name", name);
                                    intent.putExtra("hours", dataSnapshot.child("hours").getValue(Long.class));
                                    intent.putExtra("minutes", dataSnapshot.child("minutes").getValue(Long.class));
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                                    try {
                                        pendingIntent.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        throw new RuntimeException(e);
                                    }
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
        receivedRef.addChildEventListener(childEventListener);
    }

    private void startListeningForAnswers() {
        // Create a ChildEventListener to listen for changes in the "calls" node
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (c.ACCEPTED_REQUEST == snapshot.child("status").getValue(Long.class)) {
                    Intent intent = new Intent(MyFirebaseService.this, MyTicket.class);
                    startActivity(intent);
                    Toast.makeText(MyFirebaseService.this, "Request Accepted!", Toast.LENGTH_LONG).show();
                }
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
        receivedRef.addChildEventListener(childEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Detach the ChildEventListener when the service is destroyed
        if (receivedRef != null && childEventListener != null) {
            receivedRef.removeEventListener(childEventListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Method to create the notification
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, RespondRequestActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Your service is running in the foreground")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp);

        return builder.build();
    }
}