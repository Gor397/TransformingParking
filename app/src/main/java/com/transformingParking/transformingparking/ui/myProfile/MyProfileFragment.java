package com.transformingParking.transformingparking.ui.myProfile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.transformingParking.transformingparking.ParkingActivities.AddParkingActivity;
import com.transformingParking.transformingparking.AccountActivities.EditProfileActivity;
import com.transformingParking.transformingparking.AccountActivities.SettingsActivity;;
import com.transformingParking.transformingparking.databinding.FragmentMyProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyProfileFragment extends Fragment {

    private FragmentMyProfileBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private String nameStr;
    private String phoneNumber;

    private RecyclerView recyclerView;
    private Button addParkingBtn;
    private ImageButton settingsBtn;
    private TextView name;
    private TextView phone;
    private ImageView profilePic;
    private ImageButton editBtn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MyProfileViewModel myProfileViewModel = new ViewModelProvider(this).get(MyProfileViewModel.class);

        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        addParkingBtn = binding.addParkingBtn;
        recyclerView = binding.recyclerView;
        settingsBtn = binding.settingsBtn;
        name = binding.name;
        phone = binding.phone;
        editBtn = binding.editBtn;
        profilePic = binding.profilePic;

        StorageReference imageRef = storageReference.child("profile_pics").child(user.getUid());

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(requireView()).load(uri).into(profilePic);
                progressDialog.cancel();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO Handle the error
                progressDialog.cancel();
            }
        });

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshot -> {
                    nameStr = queryDocumentSnapshot.get("name", String.class);
                    name.setText(nameStr);
                    phoneNumber = queryDocumentSnapshot.get("phone", String.class);
                    phone.setText(phoneNumber);
                });

        db.collection("parking_spaces").whereEqualTo("user_id", user.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Map<String, Object>> parking_spots = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Map<String, Object> parking = document.getData();
                parking.put("id", document.getId());
                parking_spots.add(parking);
            }

            ParkingAdapter adapter = new ParkingAdapter(parking_spots, getContext());
            recyclerView.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            // Handle failure
            Log.e("GET_PARKING", "Error getting parking spaces: " + e.getMessage());
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        addParkingBtn.setOnClickListener(v -> {
            Intent addParkingIntent = new Intent(getActivity(), AddParkingActivity.class);
            startActivity(addParkingIntent);
        });

        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), EditProfileActivity.class);
            intent.putExtra("name", nameStr);
            intent.putExtra("phone", phoneNumber);
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        StorageReference imageRef = storageReference.child("profile_pics").child(user.getUid());
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(requireView()).load(uri).into(profilePic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO Handle the error
            }
        });

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshot -> {
                    nameStr = queryDocumentSnapshot.get("name", String.class);
                    name.setText(nameStr);
                    phoneNumber = queryDocumentSnapshot.get("phone", String.class);
                    phone.setText(phoneNumber);
                });

        db.collection("parking_spaces").whereEqualTo("user_id", user.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Map<String, Object>> parking_spots = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Map<String, Object> parking = document.getData();
                parking.put("id", document.getId());
                parking_spots.add(parking);
            }

            ParkingAdapter adapter = new ParkingAdapter(parking_spots, getContext());
            recyclerView.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            // Handle failure
            Log.e("GET_PARKING", "Error getting parking spaces: " + e.getMessage());
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}