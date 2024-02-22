package com.example.transformingparking.ui.myProfile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transformingparking.AddParkingActivity;
import com.example.transformingparking.R;
import com.example.transformingparking.SettingsActivity;;
import com.example.transformingparking.databinding.FragmentMyProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyProfileFragment extends Fragment {

    private @NonNull FragmentMyProfileBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private RecyclerView recyclerView;
    private Button addParkingBtn;
    private Button settingsBtn;
    private TextView name;
    private TextView phone;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MyProfileViewModel myProfileViewModel = new ViewModelProvider(this).get(MyProfileViewModel.class);

        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        addParkingBtn = root.findViewById(R.id.add_parking_btn);
        recyclerView = root.findViewById(R.id.recyclerView);
        settingsBtn = root.findViewById(R.id.settings_btn);
        name = root.findViewById(R.id.name);
        phone = root.findViewById(R.id.phone);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        addParkingBtn.setOnClickListener(v -> {
            Intent addParkingIntent = new Intent(getActivity(), AddParkingActivity.class);
            startActivity(addParkingIntent);
        });

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(queryDocumentSnapshot -> {
                    name.setText(queryDocumentSnapshot.get("name", String.class));
                    phone.setText(queryDocumentSnapshot.get("phone", String.class));
                });

        db.collection("parking_spaces").whereEqualTo("user_id", user.getUid()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Map<String, Object>> posts = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Map<String, Object> post = document.getData();
                post.put("id", document.getId());
                posts.add(post);
            }

            ParkingAdapter adapter = new ParkingAdapter(posts);
            recyclerView.setAdapter(adapter);
        }).addOnFailureListener(e -> {
            // Handle failure
            Log.e("GET_PARKING", "Error getting parking spaces: " + e.getMessage());
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}