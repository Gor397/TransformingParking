package com.transformingParking.transformingparking.ParkingActivities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.transformingParking.transformingparking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AboutFragment extends Fragment {

    private TextView descriptionTextView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String markerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        descriptionTextView = view.findViewById(R.id.descriptionTextView);

        Bundle args = getArguments();
        assert args != null;
        markerId = args.getString("markerId");

        getDescriptionFromFirestore();

        return view;
    }

    private void getDescriptionFromFirestore() {
        db.collection("parking_spaces").document(markerId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String description = (String) document.get("additional_info");

                        if (description != null && !description.isEmpty()) {
                            descriptionTextView.setText(description);
                        } else {
                            descriptionTextView.setText(R.string.no_description_about_this_garage);
                        }
                    } else {
                        // Document does not exist
                        // TODO Handle the case here
                    }
                } else {
                    // Error getting document
                    // TODO Handle the error here
                }
            }
        });
    }
}
