package com.transformingParking.transformingparking.ui.notifications;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.transformingParking.transformingparking.Notifications.NotificationDetailsActivity;
import com.transformingParking.transformingparking.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private static List<Map<String, Object>> data;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public NotificationAdapter(List<Map<String, Object>> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        String msg = String.valueOf(data.get(position).get("message"));
        String dt = null;
        try {
            dt = String.valueOf(dateFormat.parse(String.valueOf(data.get(position).get("timestamp"))));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        String notificationId = String.valueOf(data.get(position).get("notificationId"));

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), NotificationDetailsActivity.class);

                intent.putExtra("notificationId", notificationId);

                v.getContext().startActivity(intent);
            }
        };

        holder.bind(msg, dt, onClickListener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMsg;
        private TextView dateTime;
        private View item;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMsg = itemView.findViewById(R.id.textViewMessage);
            dateTime = itemView.findViewById(R.id.textViewDateTime);

            item = itemView;
        }

        void bind(String msg, String dt, View.OnClickListener onClickListener) {
            textViewMsg.setText(msg);
            dateTime.setText(dt);

            item.setOnClickListener(onClickListener);
        }
    }
}
