package com.example.transformingparking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Objects;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent responseRequestIntent = new Intent(context, RespondRequestActivity.class);
        responseRequestIntent.putExtras(Objects.requireNonNull(intent.getExtras()));
        responseRequestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(responseRequestIntent);
    }
}
