package com.example.transformingparking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddParkingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    MarkerOptions markerOptions;
    Marker marker;
    ImageView parkingPic;
    Button back_btn2;
    SupportMapFragment mapFragment;
    AutocompleteSupportFragment autocompleteFragment;
    NumberPicker priceField;
    LatLng parkingCoordinates;
    Uri selectedImageUri;
    String additionalInfo;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_location);

        back_btn2 = findViewById(R.id.back_btn2);
        back_btn2.setOnClickListener(v -> finish());

        try {
            Places.initialize(getApplicationContext(), "AIzaSyARE9BF99v3T3zM_GET5KaWXx0R84sezn0");
        } catch (Exception e) {
            // TODO
            // Handle initialization errors
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        assert mapFragment != null;
        mapFragment.getMapAsync((OnMapReadyCallback) this);

        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setOnPlaceSelectedListener(new
                                                                PlaceSelectionListener() {
                                                                    @Override
                                                                    public void onPlaceSelected(@NonNull Place place) {
                                                                        // Handle the selected place
                                                                        LatLng latLng = place.getLatLng();
                                                                        // Move the camera to the selected place
                                                                        assert latLng != null;
                                                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                                                    }

                                                                    @Override
                                                                    public void onError(@NonNull Status status) {
                                                                        // TODO: Handle the error.
                                                                        Log.i("AddParking", "An error occurred: " + status);
                                                                    }
                                                                });

        Button nextBtn = findViewById(R.id.next_btn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.set_parking_details);

                parkingPic = findViewById(R.id.pic);

                parkingPic.setOnClickListener(k -> {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    galleryIntent.putExtra("crop", "true");
//                    galleryIntent.putExtra("aspectX", 16); // Set desired aspect ratio X
//                    galleryIntent.putExtra("aspectY", 9);  // Set desired aspect ratio Y
//                    galleryIntent.putExtra("outputX", 1024); // Optional: Set output image width
//                    galleryIntent.putExtra("outputY", 576);  // Optional: Set output image height
//                    galleryIntent.putExtra("scale", "true");
//                    galleryIntent.putExtra("return-data", "true");
                    startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
                });

                priceField = findViewById(R.id.price);
                priceField.setMaxValue(500);
                priceField.setMinValue(80);

                Button submitBtn = findViewById(R.id.submit);
                submitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextInputEditText additionalInfoField = findViewById(R.id.additional_info);
                        additionalInfo = additionalInfoField.getText().toString();

                        FirebaseUser user = auth.getCurrentUser();

                        assert user != null;

                        Map<String, Object> parking_space = new HashMap<>();
                        parking_space.put("latlng", parkingCoordinates);
                        parking_space.put("additional_info", additionalInfo);
                        parking_space.put("price", priceField.getValue());
                        parking_space.put("status", true);
                        parking_space.put("user_id", user.getUid());

                        database.collection("parking_spaces")
                                .add(parking_space)
                                .addOnSuccessListener(documentReference -> {
                                    uploadProfilePictureToStorage(documentReference.getId(), selectedImageUri);
                                    Log.d("Adding Parking Space", "Parking added with ID: " + documentReference.getId());
                                });

                        Intent intent = new Intent(AddParkingActivity.this, MapActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                selectedImageUri = data.getData();
                ImageView imageView = findViewById(R.id.pic);
                Picasso.get().load(selectedImageUri).into(imageView);
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        markerOptions = new MarkerOptions();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                // Add a marker at the clicked location
                if (marker == null) {
                    marker = map.addMarker(markerOptions.position(latLng));
                } else {
                    marker.setPosition(latLng);
                }
                parkingCoordinates = latLng;
            }
        });
    }

    private void uploadProfilePictureToStorage(String parkingId, Uri profilePictureUri) {
        StorageReference storageRef = storage.getReference().child("parking_pics").child(parkingId);

        storageRef.putFile(profilePictureUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // TODO
                    // Profile picture uploaded successfully
                    // You can handle further logic here if needed
                })
                .addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    // TODO
                });
    }

}