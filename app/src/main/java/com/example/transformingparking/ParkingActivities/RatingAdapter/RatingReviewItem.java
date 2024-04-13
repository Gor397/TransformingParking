package com.example.transformingparking.ParkingActivities.RatingAdapter;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class RatingReviewItem {
    private final float rating;
    private final String review;
    private final LocalDateTime timestamp;
    private final String userId;

    public RatingReviewItem(float rating, String review, String userId, Timestamp timestamp) {
        this.rating = rating;
        this.review = review;
        this.timestamp = timestamp.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.userId = userId;
    }

    public float getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getTimestampString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }
}
