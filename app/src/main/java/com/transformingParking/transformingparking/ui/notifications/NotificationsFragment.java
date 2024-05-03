package com.transformingParking.transformingparking.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.transformingParking.transformingparking.R;
import com.transformingParking.transformingparking.databinding.FragmentNotificationsBinding;
import com.transformingParking.transformingparking.util.SortingAlgorithms;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerViewNotifications;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fetchNotifications();

        return root;
    }

    private void fetchNotifications() {
        CollectionReference notificationsRef = firestore.collection("Notifications");
        Query query = notificationsRef.whereEqualTo("ownerId", userId);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Map<String, Object>> notifications = new ArrayList<>();
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Map<String, Object> notification = documentSnapshot.getData();
                notifications.add(notification);
            }

            if (notifications.isEmpty()) {
                final TextView textView = binding.textNotifications;
                textView.setVisibility(View.VISIBLE);
                textView.setText(R.string.you_don_t_have_any_notifications_yet);
            } else {
                SortingAlgorithms.sortListBasedOnTimestamp(new SortingAlgorithms.MapListWrapper(notifications));
                adapter = new NotificationAdapter(notifications);
                recyclerView.setAdapter(adapter);
            }
        }).addOnFailureListener(e -> {
            // Handle failure, e.g., display error message to user or log error
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}