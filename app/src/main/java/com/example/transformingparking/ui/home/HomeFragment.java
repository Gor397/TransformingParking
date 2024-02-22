package com.example.transformingparking.ui.home;

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
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.transformingparking.BookingActivity;
import com.example.transformingparking.R;
import com.example.transformingparking.databinding.FragmentHomeBinding;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private FragmentHomeBinding binding;

    private GoogleMap mMap;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseDatabase realtimeDB = FirebaseDatabase.getInstance();
    private DatabaseReference availableParkingSpotsRef = realtimeDB.getReference("available_parking_spots");
    private FirebaseUser user;
    private List<Marker> markerList = new ArrayList<>();
    private Button scanQrBtn;
    private Marker current_location_marker;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

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

        return root;
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Initialize FusedLocationProviderClient
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Create a new LocationRequest object
        LocationRequest locationRequest = new LocationRequest();

        // Set the request parameters
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if it is not granted
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Get the last known location and update the map
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        // Create a new marker at the current location
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions().position(currentLocation);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_sign));
                        current_location_marker = mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                    }
                });

        // Request location updates
        mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                // Update the marker on the map
                assert location != null;
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions().position(currentLocation);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_sign));
                current_location_marker.remove();
                current_location_marker = mMap.addMarker(markerOptions);
            }
        }, Looper.getMainLooper());

        LatLng dilijan = new LatLng(40.7406, 44.8626);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dilijan, 17));
        mMap.addMarker(new MarkerOptions().position(dilijan));

        availableParkingSpotsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String id = snapshot.getKey();
                Double latitude = snapshot.child("latitude").getValue(Double.class);
                Double longitude = snapshot.child("longitude").getValue(Double.class);

                LatLng location = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions().position(location).title("Marker " + id);

                Marker marker = googleMap.addMarker(markerOptions);
                Objects.requireNonNull(marker).setTag(id);
                markerList.add(marker);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String id = snapshot.getKey();
                for (Marker marker : markerList) {
                    if (Objects.equals(marker.getTag(), id)) {
                        marker.remove();
                        markerList.remove(marker);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("HOME_FRAGMENT", "Failed to read value.", error.toException());
            }
        });

        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent bookingViewIntent = new Intent(getActivity(), BookingActivity.class);
        bookingViewIntent.putExtra("markerId", (String) marker.getTag());

//        bookingViewIntent.putExtra("Owner_name", "Vartishax");
        startActivity(bookingViewIntent);
//        finish();

        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}