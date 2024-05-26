package com.transformingParking.transformingparking.Notifications;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationModel {
    private String notificationId;
    private String message;
    private String title;
    private String ownerId;
    private String timestamp;
    private String senderId;

    public NotificationModel() {

    }

    public NotificationModel(String title, String message, String ownerId, String senderId) {
        this.title = title;
        this.message = message;
        this.ownerId = ownerId;
        this.timestamp = getCurrentTimestamp();
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    @SuppressLint("SimpleDateFormat")
    private String getCurrentTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date(currentTimeMillis);
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(currentDate);
    }
}