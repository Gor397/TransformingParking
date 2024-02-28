package com.example.transformingparking.ui.home;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.transformingparking.BookingActivity;
import com.example.transformingparking.Constants;
import com.example.transformingparking.HttpsRequest;
import com.example.transformingparking.R;
import com.example.transformingparking.ScanQRActivity;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

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
    private FloatingActionButton getMyLocationBtn;
    private Marker current_location_marker;
    private FusedLocationProviderClient mFusedLocationClient;
    private Constants constants = new Constants();
    private Map<String, double[]> markersJSON = new HashMap<>();

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

        // Get markers from server
        new HttpRequestTask().execute(
                constants.SERVER_URL +
                        "?secret=" + constants.SERVER_SECRET +
                        "&filter_status=" + constants.FREE);

        // Check for updates on server every 5 seconds
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new HttpRequestTask().execute(
                                constants.SERVER_URL +
                                        "?secret=" + constants.SERVER_SECRET +
                                        "&filter_status=" + constants.FREE);
                    }
                });
            }
        }, 0, 10 * 1000);

//        availableParkingSpotsRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String id = snapshot.getKey();
//                Double latitude = snapshot.child("latitude").getValue(Double.class);
//                Double longitude = snapshot.child("longitude").getValue(Double.class);
//
//                LatLng location = new LatLng(latitude, longitude);
//                MarkerOptions markerOptions = new MarkerOptions().position(location).title("Marker " + id);
//
//                Marker marker = googleMap.addMarker(markerOptions);
//                Objects.requireNonNull(marker).setTag(id);
//                markerList.add(marker);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                String id = snapshot.getKey();
//                for (Marker marker : markerList) {
//                    if (Objects.equals(marker.getTag(), id)) {
//                        marker.remove();
//                        markerList.remove(marker);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Failed to read value
//                Log.w("HOME_FRAGMENT", "Failed to read value.", error.toException());
//            }
//        });

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

    private class HttpRequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                // Send GET request
                return HttpsRequest.sendGetRequest(params[0]);
            } catch (IOException e) {
                Log.e(TAG, "Error sending GET request: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the result here
            if (result != null) {
                Log.d(TAG, "Response: " + result);
                Gson gson = new Gson();
                Map<String, ArrayList<Double>> originalMap = gson.fromJson(result, Map.class);
                Map<String, double[]> map = new HashMap<>();
                for (Map.Entry<String, ArrayList<Double>> entry : originalMap.entrySet()) {
                    String key = entry.getKey();
                    ArrayList<Double> arrayList = entry.getValue();
                    double[] doubleArray = arrayList.stream().mapToDouble(Double::doubleValue).toArray();
                    map.put(key, doubleArray);
                }

                Map<String, double[]> addedMarkers = getAddedMarkers(markersJSON, map);
                List<String> removedMarkers = getRemovedMarkers(markersJSON, map);
                for (Map.Entry<String, double[]> entry : addedMarkers.entrySet()) {
                    String id = entry.getKey();
                    double latitude = entry.getValue()[0];
                    double longitude = entry.getValue()[1];
                    LatLng location = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions().position(location).title("Marker " + id);

                    Marker marker = mMap.addMarker(markerOptions);
                    Objects.requireNonNull(marker).setTag(id);
                    markerList.add(marker);
                }

                for (String id : removedMarkers) {
                    for (Marker marker : markerList) {
                        if (marker.getTag() == id) {
                            marker.remove();
                            markerList.remove(marker);
                        }
                    }
                }

                markersJSON = map;
                Log.d(TAG, "onPostExecute: " + map.toString());
            } else {
                Log.e(TAG, "Failed to get response");
            }
        }
    }

    private Map<String, double[]> getAddedMarkers(Map<String, double[]> oldJSON, Map<String, double[]> newJSON) {
        Map<String, double[]> addedEntries = new HashMap<>();

        for (Map.Entry<String, double[]> entry : newJSON.entrySet()) {
            String key = entry.getKey();
            double[] value1 = entry.getValue();
            double[] value2 = oldJSON.get(key);

            if (value2 == null) {
                addedEntries.put(key, value1);
            }
        }

        return addedEntries;
    }

    private List<String> getRemovedMarkers(Map<String, double[]> oldJSON, Map<String, double[]> newJSON) {
        List<String> removedEntries = new ArrayList<>();

        for (Map.Entry<String, double[]> entry : oldJSON.entrySet()) {
            String key = entry.getKey();
            double[] value2 = newJSON.get(key);

            if (value2 == null) {
                removedEntries.add(key);
            }
        }

        return removedEntries;
    }
}