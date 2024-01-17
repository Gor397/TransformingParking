package com.example.transformingparking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

public class BookingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_view);

        Button back_btn = findViewById(R.id.back_btn);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent mapIntent = new Intent(BookingActivity.this, MapActivity.class);
//                startActivity(mapIntent);
                finish();
            }
        });

        Button profile_btn = findViewById(R.id.open_profile);

        profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(BookingActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });

        NumberPicker minutes = findViewById(R.id.minutes);
        minutes.setMaxValue(60);
        minutes.setMinValue(20);

        NumberPicker hours = findViewById(R.id.hours);
        hours.setMaxValue(24);
        hours.setMinValue(0);

    }
}