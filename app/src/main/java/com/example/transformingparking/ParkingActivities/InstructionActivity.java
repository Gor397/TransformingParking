package com.example.transformingparking.ParkingActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.transformingparking.R;

public class InstructionActivity extends AppCompatActivity {
    private Button okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        okBtn = findViewById(R.id.buttonOk);

        okBtn.setOnClickListener(v -> {
            finish();
        });
    }
}