package com.example.transformingparking.util;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class Util {
    static StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void setUserName(String name) {
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
}
