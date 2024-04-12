package com.example.transformingparking.ParkingActivities.RatingAdapter;

import static com.google.firebase.appcheck.internal.util.Logger.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.transformingparking.AccountActivities.ProfileActivity;
import com.example.transformingparking.R;
import com.example.transformingparking.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.ViewHolder> {

    private final List<RatingReviewItem> items;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RatingsAdapter(List<RatingReviewItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RatingReviewItem item = items.get(position);
        holder.ratingBar.setRating(item.getRating());
        holder.reviewText.setText(item.getReview());

        db.collection("users").document(item.getUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String userName = documentSnapshot.getString("name");
                    Log.d(TAG, "onComplete: " + userName);
                    holder.userBtn.setText(userName);
                } else {
                    holder.userBtn.setText("User not found");
                }
            }
        });
        holder.userBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userId", item.getUserId());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView reviewText;
        Button userBtn;

        ViewHolder(View view) {
            super(view);
            ratingBar = view.findViewById(R.id.ratingBarItem);
            reviewText = view.findViewById(R.id.reviewTextItem);
            userBtn = view.findViewById(R.id.userProfileBtn);
        }
    }
}
