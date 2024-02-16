package com.example.transformingparking;

public class ParkingRequest {
    private String parking_id;
    private int status;
    private int hours;
    private int minutes;

    public String getParking_id() {
        return parking_id;
    }

    public int getStatus() {
        return status;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    ParkingRequest(String parking_id, int status, int hours, int minutes) {
        this.parking_id = parking_id;
        this.status = status;
        this.hours = hours;
        this.minutes = minutes;
    }
}
