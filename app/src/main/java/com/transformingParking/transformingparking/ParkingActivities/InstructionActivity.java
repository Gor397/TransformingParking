package com.transformingParking.transformingparking.ParkingActivities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.transformingParking.transformingparking.R;

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