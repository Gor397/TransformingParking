package com.example.transformingparking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name");
        long hours = intent.getLongExtra("hours", 0);
        long minutes = intent.getLongExtra("minutes", 0);

        Intent responseRequestIntent = new Intent(context, RespondRequestActivity.class);
        responseRequestIntent.putExtra("name", name);
        responseRequestIntent.putExtra("hours", hours);
        responseRequestIntent.putExtra("minutes", minutes);
        responseRequestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(responseRequestIntent);
    }
}
