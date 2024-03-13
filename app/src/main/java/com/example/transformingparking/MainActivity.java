package com.example.transformingparking;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.transformingparking.signIn.SignInActivity;
import com.example.transformingparking.signIn.VerifyCodeActivity;
import com.example.transformingparking.signIn.WriteNameActivty;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                            Map<String, Object> currentUser = new HashMap<>();
                            currentUser.put("phone", user.getPhoneNumber());

                            docRef.set(currentUser);

                            Intent intent = new Intent(MainActivity.this, WriteNameActivty.class);
                            startActivity(intent);
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
}
