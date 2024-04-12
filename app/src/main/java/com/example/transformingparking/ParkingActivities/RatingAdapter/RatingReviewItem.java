package com.example.transformingparking.ParkingActivities.RatingAdapter;

public class RatingReviewItem {
    private final float rating;
    private final String review;
    private final String userId;

    public RatingReviewItem(float rating, String review, String userId) {
        this.rating = rating;
        this.review = review;
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
}
