package com.example.transformingparking;

public class ParkingRequest {
    private int status;
    private int hours;
    private int minutes;

    public int getStatus() {
        return status;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    ParkingRequest (int status, int hours, int minutes) {
        this.status = status;
        this.hours = hours;
        this.minutes = minutes;
    }
}
