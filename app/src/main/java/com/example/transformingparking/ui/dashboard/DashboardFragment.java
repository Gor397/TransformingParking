package com.example.transformingparking.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transformingparking.AddParkingActivity;
import com.example.transformingparking.R;
import com.example.transformingparking.databinding.FragmentDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    RecyclerView recyclerView;
    Button addParkingBtn;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        addParkingBtn = root.findViewById(R.id.add_parking_btn);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        addParkingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addParkingIntent = new Intent(getActivity(), AddParkingActivity.class);
                startActivity(addParkingIntent);
            }
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