package com.example.transformingparking.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.transformingparking.BookingActivity;
import com.example.transformingparking.MapActivity;
import com.example.transformingparking.R;
import com.example.transformingparking.SignInActivity;
import com.example.transformingparking.databinding.FragmentHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.Executor;

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private FragmentHomeBinding binding;

    private GoogleMap mMap;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    FirebaseUser user;

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

        Button signOutBtn = root.findViewById(R.id.sign_out_btn);
        signOutBtn.setOnClickListener(v -> signOut());

        return root;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng dilijan = new LatLng(40.7406, 44.8626);
        LatLng dilijan2 = new LatLng(40.7406, 44.8628);
        mMap.addMarker(new MarkerOptions().position(dilijan).title("Marker in Dilijan"));
        mMap.addMarker(new MarkerOptions().position(dilijan2).title("Marker in Dilijan"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dilijan, 17));

        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent bookingViewIntent = new Intent(getActivity(), BookingActivity.class);

//        bookingViewIntent.putExtra("Owner_name", "Vartishax");
        startActivity(bookingViewIntent);
//        finish();

        return false;
    }

    private void signOut() {
        // Sign out of Firebase
        auth.signOut();

        // Sign out of Google (optional)
        GoogleSignIn.getClient(getActivity(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                .addOnCompleteListener((Executor) this, task -> {
                    // Update UI or start login activity
                    Toast.makeText(getActivity(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), SignInActivity.class));
                    getActivity().finish();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}