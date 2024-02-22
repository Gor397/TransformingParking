package com.example.transformingparking.ui.myProfile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.transformingparking.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ViewHolder> {

    private List<Map<String, Object>> localDataSet;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    View view;
    Context context;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final TextView descriptionTextView;
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = (TextView) view.findViewById(R.id.price);
            imageView = (ImageView) view.findViewById(R.id.imageView);
            descriptionTextView = (TextView) view.findViewById(R.id.description);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getDescriptionTextView() {
            return descriptionTextView;
        }
    }

    public ParkingAdapter(List<Map<String, Object>> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.parking_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView()
                .setText(Objects.requireNonNull(localDataSet.get(position).get("price")).toString() + " dram per hour");
        viewHolder.getDescriptionTextView().setText(Objects.requireNonNull(localDataSet.get(position).get("additional_info")).toString());
        StorageReference imageRef = storageReference.child("parking_pics").child((String) Objects.requireNonNull(localDataSet.get(position).get("id")));

        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the image into the ImageView using Glide
                Glide.with(view).load(uri).into(viewHolder.getImageView());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO Handle any errors
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
