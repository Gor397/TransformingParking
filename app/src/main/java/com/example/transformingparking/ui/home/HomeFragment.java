package com.example.transformingparking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.transformingparking.BookingActivity;
import com.example.transformingparking.R;
import com.example.transformingparking.databinding.FragmentHomeBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private FragmentHomeBinding binding;

    private GoogleMap mMap;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseDatabase realtimeDB = FirebaseDatabase.getInstance();
    DatabaseReference availableParkingSpotsRef = realtimeDB.getReference("available_parking_spots");
    FirebaseUser user;
    List<Marker> markerList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        user = auth.getCurrentUser();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        // TODO IMPORTANT!!!
        LatLng dilijan = new LatLng(40.7406, 44.8626);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dilijan, 17));

        availableParkingSpotsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String id = snapshot.getKey();
                Double latitude = snapshot.child("latitude").getValue(Double.class);
                Double longitude = snapshot.child("longitude").getValue(Double.class);

                LatLng location = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title("Marker " + id);

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

//        db.collection("parking_spaces")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                if (Boolean.FALSE.equals(document.get("status", Boolean.class))) {
//                                    continue;
//                                }
//                                Map latlng = (Map) document.getData().get("latlng");
//                                String id = document.getId();
//
//                                LatLng location = new LatLng((Double) latlng.get("latitude"), (Double) latlng.get("longitude"));
//                                MarkerOptions markerOptions = new MarkerOptions()
//                                        .position(location)
//                                        .title("Marker " + id);
//
//                                googleMap.addMarker(markerOptions).setTag(id);
//                            }
//                        } else {
//                            Log.d("GettingParkingSpaces", "Error getting documents: ", task.getException());
//                        }
//                    }
//                });

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