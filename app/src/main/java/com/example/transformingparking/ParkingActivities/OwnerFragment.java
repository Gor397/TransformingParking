package com.example.transformingparking.ParkingActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.transformingparking.AccountActivities.ProfileActivity;
import com.example.transformingparking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class OwnerFragment extends Fragment {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private String userId;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private ImageView profilePic;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView nameView;
    private TextView phoneNumberView;
    private ImageButton callBtn;
    private ImageButton msgBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_owner, container, false);

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        assert getArguments() != null;
        userId = getArguments().getString("ownerId");
        profilePic = view.findViewById(R.id.imageViewProfilePic);
        nameView = view.findViewById(R.id.textViewUserName);
        phoneNumberView = view.findViewById(R.id.textViewPhoneNumber);
        callBtn = view.findViewById(R.id.buttonCall);
        msgBtn = view.findViewById(R.id.buttonMsg);

        StorageReference imageRef = storageReference.child("profile_pics").child(userId);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(requireContext()).load(uri).into(profilePic);
                progressDialog.cancel();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO Handle the error
                progressDialog.cancel();
            }
        });

        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = (String) document.get("name");
                        nameView.setText(name);
                        String phoneNumber = (String) document.get("phone");
                        phoneNumberView.setText(phoneNumber);
                    }
                } else {
                    // Error getting document
                    // TODO Handle the error here
                }
            }
        });

        callBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumberView.getText().toString()));
            startActivity(intent);
        });

        msgBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phoneNumberView.getText().toString()));
            startActivity(intent);
        });

        return view;
    }
}
