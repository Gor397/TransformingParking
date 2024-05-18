package com.transformingParking.transformingparking.ParkingActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.transformingParking.transformingparking.ParkingActivities.RatingAdapter.RatingReviewItem;
import com.transformingParking.transformingparking.ParkingActivities.RatingAdapter.RatingsAdapter;
import com.transformingParking.transformingparking.R;
import com.transformingParking.transformingparking.util.SortingAlgorithms;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReviewsFragment extends Fragment {

    RecyclerView ratingsRecyclerView;
    RatingsAdapter ratingsAdapter;
    String markerId;
    TextView reviewFragmentTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);

        Bundle args = getArguments();
        assert args != null;
        markerId = args.getString("markerId");

        reviewFragmentTextView = view.findViewById(R.id.reviewFragmentTextView);

        ratingsRecyclerView = view.findViewById(R.id.ratings_recyclerView);
        ratingsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize your data source for ratings and reviews here
        List<RatingReviewItem> items = fetchDataAndSetMeanRating();

        ratingsAdapter = new RatingsAdapter(items, requireContext());
        ratingsRecyclerView.setAdapter(ratingsAdapter);

        return view;
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

                            if (review == null || !review.isEmpty()) {
                                assert timestamp != null;
                                items.add(new RatingReviewItem(rating, review, userId, timestamp));
                            }
                        }

                        if (items.isEmpty()) {
                            reviewFragmentTextView.setText(R.string.there_are_no_reviews_for_this_garage);
                        } else {
                            reviewFragmentTextView.setVisibility(View.GONE);
                            ratingsRecyclerView.setVisibility(View.VISIBLE);
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
                        }

                        ratingsAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });

        return items;
    }
}
