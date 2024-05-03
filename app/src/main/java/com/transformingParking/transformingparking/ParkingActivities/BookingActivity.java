package com.transformingParking.transformingparking.ParkingActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.transformingParking.transformingparking.ParkingActivities.RatingAdapter.RatingReviewItem;
import com.transformingParking.transformingparking.R;
import com.transformingParking.transformingparking.util.SortingAlgorithms;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.transformingParking.transformingparking.util.Util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BookingActivity extends AppCompatActivity {
    String markerId;
    ImageView imageView;
    TextView priceView;
    TextView descriptionView;
    TextView descriptionTitle;
    Button ownerAccBtn;
    Button directionsBtn;
    CardView ratingsCardView;
    RatingBar ratingBar;
    TextView locationText;
    private TextView aboutTextView, reviewsTextView, ownerTextView;
    private FrameLayout fragmentContainer;

    TextView ratingNumber;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseDatabase realtimeDb = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String ownerId;
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_view);

        priceView = findViewById(R.id.price1);
        imageView = findViewById(R.id.imageView);
        directionsBtn = findViewById(R.id.directions_btn);;;
        ratingBar = findViewById(R.id.ratingBar2);
        ratingNumber = findViewById(R.id.textStarsNumber);
        locationText = findViewById(R.id.locationText);

        Intent intent = getIntent();
        markerId = intent.getStringExtra("markerId");

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("parking_spaces").document(markerId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        int price = Integer.parseInt(Objects.requireNonNull(document.get("price")).toString());
                        StorageReference imageRef = storage.getReference().child("parking_pics").child(markerId);
                        Map<String, Double> latlng = (Map<String, Double>) document.get("latlng");
                        assert latlng != null;
                        Double latitude = latlng.get("latitude");
                        Double longitude = latlng.get("longitude");

                        setOwnerId((String) document.get("user_id"));

                        locationText.setText(Util.getLocationFromCoordinates(getApplicationContext(), latitude, longitude));

                        priceView.setText(MessageFormat.format("{0}{1}", price, getString(R.string.amd_per_hour)));
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Load the image into the ImageView using Glide
                                Glide.with(BookingActivity.this).load(uri).into(imageView);
                                progressDialog.cancel();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // TODO Handle any errors
                                progressDialog.cancel();
                            }
                        });

                        directionsBtn.setOnClickListener(v -> {
                            openDirections(latitude, longitude);
                        });
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

        // Initialize your data source for ratings and reviews here
        List<RatingReviewItem> items = fetchDataAndSetMeanRating();

        aboutTextView = findViewById(R.id.about);
        reviewsTextView = findViewById(R.id.reviews);
        ownerTextView = findViewById(R.id.owner);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        // Set initial fragment
        AboutFragment aboutFragment = new AboutFragment();

        Bundle bundle = new Bundle();
        bundle.putString("markerId", markerId);

        // Set the Bundle as arguments for the fragment
        aboutFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, aboutFragment).commit();
        activateInNavbar(0);

        aboutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutFragment aboutFragment = new AboutFragment();
                aboutFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, aboutFragment).commit();
                activateInNavbar(0);
            }
        });

        reviewsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReviewsFragment reviewsFragment = new ReviewsFragment();
                reviewsFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, reviewsFragment).commit();
                activateInNavbar(1);
            }
        });

        ownerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OwnerFragment ownerFragment = new OwnerFragment();

                Bundle bundle = new Bundle();
                bundle.putString("ownerId", ownerId);

                // Set the Bundle as arguments for the fragment
                ownerFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, ownerFragment).commit();
                activateInNavbar(2);
            }
        });
    }

    private void activateInNavbar(int section) {
        aboutTextView.setTextColor(getColor(R.color.my_grey));
        aboutTextView.setTypeface(null, Typeface.NORMAL);

        reviewsTextView.setTextColor(getColor(R.color.my_grey));
        reviewsTextView.setTypeface(null, Typeface.NORMAL);

        ownerTextView.setTextColor(getColor(R.color.my_grey));
        ownerTextView.setTypeface(null, Typeface.NORMAL);

        switch (section) {
            case 0:
                aboutTextView.setTextColor(getColor(R.color.black));
                aboutTextView.setTypeface(null, Typeface.BOLD);
                break;
            case 1:
                reviewsTextView.setTextColor(getColor(R.color.black));
                reviewsTextView.setTypeface(null, Typeface.BOLD);
                break;
            case 2:
                ownerTextView.setTextColor(getColor(R.color.black));
                ownerTextView.setTypeface(null, Typeface.BOLD);
                break;
        }
    }

    private List<RatingReviewItem> fetchDataAndSetMeanRating() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<RatingReviewItem> items = new ArrayList<>();

        // Assuming a collection of users where each user has a sub-collection of ratings
        db.collection("parking_spaces").document(markerId).collection("ratings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            long rating = document.getLong("stars");
                            String review = document.getString("feedback");
                            String userId = document.getString("userId");

                            Timestamp timestamp = document.getTimestamp("timestamp");

                            items.add(new RatingReviewItem(rating, review, userId, timestamp));
                        }

                        if (items.isEmpty()) {
                            ratingBar.setVisibility(View.GONE);
                            ratingNumber.setVisibility(View.GONE);
                        } else {
                            SortingAlgorithms.sortListBasedOnTimestamp(new SortingAlgorithms.RatingReviewItemListWrapper(items));
                            float total = 0;
                            int ratings_quantity = items.size();
                            int ratings_quantity_for_loop = items.size();
                            for (int i = 0; i < ratings_quantity_for_loop; i++) {
                                RatingReviewItem item = items.get(i);
                                total += item.getRating();
                                if (item.getReview().isEmpty()) {
                                    items.remove(i);
                                    ratings_quantity_for_loop--;
                                }
                            }

                            float mean_rating = total / ratings_quantity;

                            ratingBar.setRating(mean_rating);
                            ratingNumber.setText(String.format("%.1f", mean_rating));
                        }

                    } else {
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });

        return items;
    }

    private void openDirections(double lat, double lng) {
        // Create a Uri from destination location coordinates
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);

        // Create an Intent with the action view and set the data
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        // Set the package to Google Maps
        mapIntent.setPackage("com.google.android.apps.maps");

        // Check if there's any activity to handle the intent
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            // Start Google Maps with directions
            startActivity(mapIntent);
        } else {
            // Google Maps app is not installed, display a toast or alternative action
            Toast.makeText(this, getString(R.string.google_maps_app_is_not_installed), Toast.LENGTH_SHORT).show();
        }
    }
}