package com.transformingParking.transformingparking.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Util {
    static StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void setCurrentUserName(String name) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        assert user != null;
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(updateProfileTask -> {
                    if (updateProfileTask.isSuccessful()) {
                        Log.d("Set Display  Name", "Display name set to '" + name + "'");
                    } else {
                        Exception updateProfileException = updateProfileTask.getException();
                        Log.e("Update Profile Failed", Objects.requireNonNull(Objects.requireNonNull(updateProfileException).getMessage()));
                    }
                });
    }

    public static void deleteParking(String parkingId) {
        db.collection("parking_spaces").document(parkingId).delete();
        storageReference.child("parking_spaces").child(parkingId).delete();
    }

    public static String getLocationFromCoordinates(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String locationName = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                String country = address.getCountryName();
                if (city != null && country != null) {
                    locationName = city + "/" + country;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return locationName;
    }
}
