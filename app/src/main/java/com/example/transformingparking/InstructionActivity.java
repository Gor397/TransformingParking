package com.example.transformingparking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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