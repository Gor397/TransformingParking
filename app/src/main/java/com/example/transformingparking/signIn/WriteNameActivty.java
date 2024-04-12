package com.example.transformingparking.signIn;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.transformingparking.MainActivity;
import com.example.transformingparking.R;
import com.example.transformingparking.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WriteNameActivty extends AppCompatActivity {
    private TextView name_err_message;
    private EditText fullNameEditText;
    private Button nextBtn;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_write_name_activty);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        fullNameEditText = findViewById(R.id.editTextFullName);
        name_err_message = findViewById(R.id.name_err_message);
        nextBtn = findViewById(R.id.next_btn1);

        nextBtn.setOnClickListener(v -> {
            if (checkUserName()) {
                addNameToDB(fullNameEditText.getText().toString());
            }
        });
    }

    private void addNameToDB(String string) {
        DocumentReference docRef = db.collection("users").document(user.getUid());

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", fullNameEditText.getText().toString());
        Util.setCurrentUserName(fullNameEditText.getText().toString());

        docRef.update(updates).addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(WriteNameActivty.this, MainActivity.class);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            // TODO handle the error
            Log.d(TAG, "Error adding field: " + e.getMessage());
        });
    }

    private boolean checkUserName() {
        name_err_message.setText("");
        String name = fullNameEditText.getText().toString();
        for (char c : name.toCharArray()) {
            if (!Character.isLetter(c) && !Character.isSpaceChar(c)) {
                name_err_message.setText(R.string.this_field_should_contain_only_letters);
                return false;
            }
        }
        return true;
    }

}