package com.example.transformingparking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.transformingparking.databinding.ActivityRespondRequestBinding;

public class RespondRequestActivity extends AppCompatActivity {
    private ActivityRespondRequestBinding binding;

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

        binding.videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.money));
        binding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Restart video playback when completed
                binding.videoView.start();
            }
        });
        binding.videoView.start();

        Intent intent = getIntent();
        long hours = intent.getLongExtra("hours", 0);
        long minutes = intent.getLongExtra("minutes", 0);
        String duration;
        if (hours == 0) {
            duration = minutes + " minutes";
        } else {
            duration = hours + " hours" + minutes + " minutes";
        }
        String name = intent.getStringExtra("name");

        binding.duration.setText(duration);
        binding.name.setText(name);

        binding.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        binding.rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}