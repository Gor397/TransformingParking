package com.transformingParking.transformingparking;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.transformingParking.transformingparking.ParkingActivities.MapActivity;
import com.transformingParking.transformingparking.signIn.SignInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseUser user;

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private List<String> permissionsRequired = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize permissions required based on Android version
        initializePermissions();

        if (!checkPermissions()) {
            // Request permissions if not granted
            requestPermissions();
        }

        user = auth.getCurrentUser();

        if (user != null) {
            DocumentReference docRef = database.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.contains("name")) {
                            Intent intent = new Intent(MainActivity.this, MapActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
                            startActivity(signInIntent);
                            finish();
                        }
                    } else {
                        // TODO handle the error
                        Log.d(TAG, "Failed to get document.", task.getException());
                    }
                }
            });
        } else {
            Intent signInIntent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(signInIntent);
            finish();
        }
    }

    private void initializePermissions() {
        // Add POST_NOTIFICATIONS permission conditionally for Android 13 (API level 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsRequired.add(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private boolean checkPermissions() {
        // Check if the necessary permissions are granted
        for (String permission : permissionsRequired) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        // Request necessary permissions
        ActivityCompat.requestPermissions(this, permissionsRequired.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // Permissions granted, you can proceed with sending notifications
            } else {
                // Permissions not granted, handle accordingly (e.g., inform the user)
            }
        }
    }
}
