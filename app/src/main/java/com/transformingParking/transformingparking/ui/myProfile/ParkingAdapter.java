package com.transformingParking.transformingparking.ui.myProfile;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.transformingParking.transformingparking.Constants;
import com.transformingParking.transformingparking.AccountActivities.ProfileActivity;
import com.transformingParking.transformingparking.ParkingActivities.EditParkingActivity;
import com.transformingParking.transformingparking.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ViewHolder> {

    private List<Map<String, Object>> localDataSet;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    View view;
    Context context;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView priceView;
        private final TextView descriptionView;
        private final ImageView imageView;
        private final TextView statusView;
        private final Button clientProfileBtn;
        private final Button changeStatusBtn;
        private final Button openOrCloseBtn;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            priceView = (TextView) view.findViewById(R.id.price);
            imageView = (ImageView) view.findViewById(R.id.imageView);
            descriptionView = (TextView) view.findViewById(R.id.description);
            statusView = (TextView) view.findViewById(R.id.textViewStatus);
            clientProfileBtn = (Button) view.findViewById(R.id.buttonClientProfile);
            changeStatusBtn = (Button) view.findViewById(R.id.buttonChangeStatus);
            openOrCloseBtn = (Button) view.findViewById(R.id.buttonOpenOrClose);
        }

        public TextView getPriceView() {
            return priceView;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getDescriptionView() {
            return descriptionView;
        }

        public TextView getStatusView() {
            return statusView;
        }

        public Button getClientProfileBtn() {
            return clientProfileBtn;
        }

        public Button getChangeStatusBtn() {
            return changeStatusBtn;
        }

        public Button getOpenOrCloseBtn() {
            return openOrCloseBtn;
        }
    }

    public ParkingAdapter(List<Map<String, Object>> dataSet, Context context) {
        localDataSet = dataSet;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.parking_row_item, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ProgressDialog progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        int status = Integer.parseInt(Objects.requireNonNull(localDataSet.get(position).get("status")).toString());

        DocumentReference docRef = db.collection("parking_spaces").document(localDataSet.get(position).get("id").toString());

        if (status == Constants.BUSY || status == Constants.CLOSE_COMMAND_OWNER || status == Constants.OPEN_COMMAND) {
            viewHolder.getStatusView().setBackground(context.getResources().getDrawable(R.drawable.busy_status_background));

            if (localDataSet.get(position).get("client_id") == null) {
                viewHolder.getStatusView().setText("Entry Closed (busy)");
                viewHolder.getClientProfileBtn().setVisibility(View.INVISIBLE);

                viewHolder.getChangeStatusBtn().setVisibility(View.VISIBLE);
                viewHolder.getChangeStatusBtn().setBackgroundColor(context.getResources().getColor(com.google.android.libraries.places.R.color.quantum_googgreen));
                viewHolder.getChangeStatusBtn().setText("Make Online");
                viewHolder.getChangeStatusBtn().setOnClickListener(v -> {
                    ProgressDialog progressDialog3 = new ProgressDialog(context);
                    progressDialog3.setCancelable(false);
                    progressDialog3.setMessage("Loading...");
                    progressDialog3.show();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", Constants.FREE);
                    localDataSet.get(position).put("status", Constants.FREE);

                    docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document field successfully updated!");
                            notifyDataSetChanged();
                            progressDialog3.cancel();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });
                });

                viewHolder.getOpenOrCloseBtn().setVisibility(View.VISIBLE);
                viewHolder.getOpenOrCloseBtn().setText("Open Entry");
                viewHolder.getOpenOrCloseBtn().setOnClickListener(v -> {
                    ProgressDialog progressDialog2 = new ProgressDialog(context);
                    progressDialog2.setCancelable(false);
                    progressDialog2.setMessage("Opening...");
                    progressDialog2.show();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", Constants.OPEN_COMMAND_OWNER);
                    localDataSet.get(position).put("status", Constants.OPEN_COMMAND_OWNER);

                    docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Document field successfully updated!");
                            notifyDataSetChanged();
                            progressDialog2.cancel();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating document", e);
                        }
                    });
                });
            } else {
                viewHolder.getStatusView().setText("busy");

                viewHolder.getChangeStatusBtn().setVisibility(View.GONE);
                viewHolder.getOpenOrCloseBtn().setVisibility(View.GONE);

                String userId = localDataSet.get(position).get("client_id").toString();
                db.collection("users").document(userId).get().addOnSuccessListener(queryDocumentSnapshot -> {
                    String nameStr = queryDocumentSnapshot.get("name", String.class);
                    viewHolder.getClientProfileBtn().setText(nameStr);
                    viewHolder.getClientProfileBtn().setOnClickListener(v -> {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("userId", userId);
                        context.startActivity(intent);
                    });
                });
            }
        } else if (status == Constants.OPEN_COMMAND_OWNER) {
            viewHolder.getStatusView().setText("Entry Opened (busy)");
            viewHolder.getStatusView().setBackground(context.getResources().getDrawable(R.drawable.busy_status_background));

            viewHolder.getClientProfileBtn().setVisibility(View.INVISIBLE);
            viewHolder.getChangeStatusBtn().setVisibility(View.GONE);

            viewHolder.getOpenOrCloseBtn().setVisibility(View.VISIBLE);
            viewHolder.getOpenOrCloseBtn().setText("Close Entry");
            viewHolder.getOpenOrCloseBtn().setOnClickListener(v -> {
                ProgressDialog progressDialog1 = new ProgressDialog(context);
                progressDialog1.setCancelable(false);
                progressDialog1.setMessage("Closing...");
                progressDialog1.show();
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", Constants.CLOSE_COMMAND_OWNER);
                localDataSet.get(position).put("status", Constants.CLOSE_COMMAND_OWNER);

                docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document field successfully updated!");
                        notifyDataSetChanged();
                        progressDialog1.cancel();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });


            });
        } else if (status == Constants.FREE) {
            viewHolder.getStatusView().setText("free");
            viewHolder.getStatusView().setBackground(context.getResources().getDrawable(R.drawable.free_status_background));

            viewHolder.getClientProfileBtn().setVisibility(View.INVISIBLE);
            viewHolder.getOpenOrCloseBtn().setVisibility(View.GONE);

            viewHolder.getChangeStatusBtn().setVisibility(View.VISIBLE);
            viewHolder.getChangeStatusBtn().setBackgroundColor(context.getResources().getColor(com.google.android.libraries.places.R.color.quantum_googred));
            viewHolder.getChangeStatusBtn().setText("Make Ofline");
            viewHolder.getChangeStatusBtn().setOnClickListener(v -> {
                ProgressDialog progressDialog4 = new ProgressDialog(context);
                progressDialog4.setCancelable(false);
                progressDialog4.setMessage("Loading...");
                progressDialog4.show();
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", Constants.BUSY);
                localDataSet.get(position).put("status", Constants.BUSY);

                docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document field successfully updated!");
                        notifyDataSetChanged();
                        progressDialog4.cancel();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
            });
        } else if (status == Constants.PENDING) {
            viewHolder.getStatusView().setText("Pending");
            viewHolder.getStatusView().setBackground(context.getResources().getDrawable(R.drawable.pending_status_background));

            viewHolder.getClientProfileBtn().setVisibility(View.INVISIBLE);
            viewHolder.getChangeStatusBtn().setVisibility(View.GONE);
            viewHolder.getOpenOrCloseBtn().setVisibility(View.GONE);
        }

        String price = String.format("%s%s", localDataSet.get(position).get("price"), view.getContext().getString(R.string.amd_per_hour));
        viewHolder.getPriceView().setText(price);

        String description = Objects.requireNonNull(localDataSet.get(position).get("additional_info")).toString();
        if (description.isEmpty()) {
            viewHolder.getDescriptionView().setVisibility(View.GONE);
        } else {
            viewHolder.getDescriptionView().setText(description);
        }

        StorageReference imageRef = storageReference.child("parking_pics").child((String) Objects.requireNonNull(localDataSet.get(position).get("id")));

        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load the image into the ImageView using Glide
                Glide.with(view).load(uri).into(viewHolder.getImageView());
                progressDialog.cancel();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO Handle the error
                progressDialog.cancel();
            }
        });

        viewHolder.getImageView().setOnClickListener(v -> {
            startEditParkingActivity(Objects.requireNonNull(localDataSet.get(position).get("id")).toString());
        });

        viewHolder.getDescriptionView().setOnClickListener(v -> {
            startEditParkingActivity(Objects.requireNonNull(localDataSet.get(position).get("id")).toString());
        });

        viewHolder.getPriceView().setOnClickListener(v -> {
            startEditParkingActivity(Objects.requireNonNull(localDataSet.get(position).get("id")).toString());
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    private void startEditParkingActivity(String id) {
        Intent intent = new Intent(context, EditParkingActivity.class);
        intent.putExtra("parkingId", id);
        context.startActivity(intent);
    }
}
