package com.transformingParking.transformingparking.ui.home;

import static android.content.ContentValues.TAG;

import static com.transformingParking.transformingparking.Constants.FREE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.transformingParking.transformingparking.ParkingActivities.BookingActivity;
import com.transformingParking.transformingparking.R;
import com.transformingParking.transformingparking.ParkingActivities.ScanQRActivity;
import com.transformingParking.transformingparking.databinding.FragmentHomeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private FragmentHomeBinding binding;

    private GoogleMap mMap;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseDatabase realtimeDB = FirebaseDatabase.getInstance();
    private DatabaseReference availableParkingSpotsRef = realtimeDB.getReference("available_parking_spots");
    private FirebaseUser user;
    private List<Marker> markerList = new ArrayList<>();
    private ImageButton scanQrBtn;
    private FloatingActionButton getMyLocationBtn;
    private Marker current_location_marker;
    private FusedLocationProviderClient mFusedLocationClient;
    private Map<String, double[]> markersJSON = new HashMap<>();

    public void addToMarkerList(Marker marker) {
        this.markerList.add(marker);
    }

    public void removeFromMarkerList(Marker marker) {
        this.markerList.remove(marker);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        user = auth.getCurrentUser();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        scanQrBtn = root.findViewById(R.id.scan_qr_btn);
        scanQrBtn.setOnClickListener(v -> {
            startQRCodeScanner();
        });

        getMyLocationBtn = root.findViewById(R.id.get_my_location_btn);
        getMyLocationBtn.setOnClickListener(v -> {
            getMyLocation();
        });

        return root;
    }

    private void startQRCodeScanner() {
        Intent intent = new Intent(requireActivity(), ScanQRActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Initialize FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Create a new LocationRequest object
        LocationRequest locationRequest = new LocationRequest();

        // Set the request parameters
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Get the last known location and update the map
        getMyLocation();

        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if it is not granted
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Request location updates
        mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions().position(currentLocation);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_sign));
                    markerOptions.anchor(0.5f, 0.5f);
                    if (current_location_marker != null) {
                        current_location_marker.remove();
                    }
                    current_location_marker = mMap.addMarker(markerOptions);
                }
            }
        }, Looper.getMainLooper());

        db.collection("parking_spaces")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange snapshot : snapshots.getDocumentChanges()) {
                            switch (snapshot.getType()) {
                                case ADDED:
                                case MODIFIED:
                                    DocumentSnapshot addedDoc = snapshot.getDocument();
                                    String id = addedDoc.getId();
                                    int status = Integer.parseInt(String.valueOf(addedDoc.get("status")));
                                    Log.d(TAG, "onEvent: " + status);

                                    if (status == FREE) {
                                        Map<String, Double> latlng = (Map<String, Double>) addedDoc.get("latlng");
                                        assert latlng != null;
                                        Double latitude = latlng.get("latitude");
                                        Double longitude = latlng.get("longitude");

                                        LatLng location = new LatLng(latitude, longitude);
                                        MarkerOptions markerOptions = new MarkerOptions().position(location);

                                        Marker marker = mMap.addMarker(markerOptions);
                                        Objects.requireNonNull(marker).setTag(id);
                                        addToMarkerList(marker);
                                    } else {
                                        final int size = markerList.size();
                                        for (int i = 0; i < size; i++) {
                                            Marker marker = markerList.get(i);
                                            if (marker.getTag().equals(id)) {
                                                marker.remove();
                                                removeFromMarkerList(marker);
                                                break;
                                            }
                                        }
                                    }
                                    break;

                                case REMOVED:
                                    DocumentSnapshot removedDoc = snapshot.getDocument();
                                    String removed_id = removedDoc.getId();

                                    final int size = markerList.size();
                                    for (int i = 0; i < size; i++) {
                                        Marker marker = markerList.get(i);
                                        if (marker.getTag().equals(removed_id)) {
                                            marker.remove();
                                            removeFromMarkerList(marker);
                                            break;
                                        }
                                    }
                            }
                        }
                    }
                });

        mMap.setOnMarkerClickListener(this);
    }

    private void getMyLocation() {
        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if it is not granted
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions().position(currentLocation);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_sign));
                        markerOptions.anchor(0.5f, 0.5f);
                        if (current_location_marker != null) {
                            current_location_marker.remove();
                        }
                        current_location_marker = mMap.addMarker(markerOptions);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                    }
                });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent bookingViewIntent = new Intent(getActivity(), BookingActivity.class);
        bookingViewIntent.putExtra("markerId", (String) marker.getTag());
        startActivity(bookingViewIntent);

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}