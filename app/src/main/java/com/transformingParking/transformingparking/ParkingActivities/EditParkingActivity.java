package com.transformingParking.transformingparking.ParkingActivities;

import static com.transformingParking.transformingparking.Constants.CROP_IMAGE_REQUEST_CODE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.transformingParking.transformingparking.R;
import com.transformingParking.transformingparking.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditParkingActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    TextView descriptionView;
    NumberPicker priceView;
    ImageView picture;
    Button saveBtn;
    Button deleteBtn;

    Uri selectedImageUri;
    String additionalInfoStr;
    String priceStr;
    String[] prices = new String[10];

    public void setAdditionalInfoStr(String additionalInfoStr) {
        this.additionalInfoStr = additionalInfoStr;
    }

    public void setPriceStr(String priceStr) {
        this.priceStr = priceStr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_parking);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        priceView = findViewById(R.id.price);
        descriptionView = findViewById(R.id.additional_info);
        picture = findViewById(R.id.pic);
        saveBtn = findViewById(R.id.saveBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        for (int i = 1; i <= 10; i++) {
            String price = Integer.toString(i * 100);
            prices[i - 1] = price;
        }

        priceView.setMinValue(0);
        priceView.setMaxValue(prices.length - 1);
        priceView.setWrapSelectorWheel(false);
        priceView.setDisplayedValues(prices);

        String parkingId = getIntent().getStringExtra("parkingId");

        assert parkingId != null;
        db.collection("parking_spaces").document(parkingId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                setPriceStr(Objects.requireNonNull(documentSnapshot.get("price")).toString());
                setAdditionalInfoStr(documentSnapshot.getString("additional_info"));

                priceView.setValue(Arrays.binarySearch(prices, priceStr));
                descriptionView.setText(additionalInfoStr);
            }
        });

        StorageReference imageRef = storage.child("parking_pics").child(parkingId);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(picture);
                progressDialog.cancel();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO Handle the error
                progressDialog.cancel();
            }
        });

        picture.setOnClickListener(k -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, CROP_IMAGE_REQUEST_CODE);
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputEditText additionalInfoField = findViewById(R.id.additional_info);
                additionalInfoStr = Objects.requireNonNull(additionalInfoField.getText()).toString()
                        .replaceAll("\\s{2,}", " ")
                        .replaceAll("([\\n\\r]){2,}", "\n");

                FirebaseUser user = auth.getCurrentUser();

                assert user != null;

                Map<String, Object> parking_space = new HashMap<>();
                parking_space.put("additional_info", additionalInfoStr);
                parking_space.put("price", Integer.parseInt(prices[priceView.getValue()]));
//                parking_space.put("status", PENDING);

                db.collection("parking_spaces")
                        .document(parkingId).update(parking_space)
                        .addOnSuccessListener(documentReference -> {
                            uploadProfilePictureToStorage(parkingId, selectedImageUri);
                            Log.d("EditParkingActivity", "Firebase Firestore document updated!");
                            Toast.makeText(EditParkingActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                        });

                finish();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.deleteParking(parkingId);
                Toast.makeText(EditParkingActivity.this, "Parking deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        priceView.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker view, int scrollState) {
                saveBtn.setEnabled(checkForChanges());
            }
        });

        descriptionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveBtn.setEnabled(checkForChanges());
            }
        });
    }

    ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            selectedImageUri = result.getUriContent();
            Bitmap cropped = BitmapFactory.decodeFile(result.getUriFilePath(getApplicationContext(), true));
            ImageView imageView = findViewById(R.id.pic);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            RequestOptions options = new RequestOptions()
                    .override(screenWidth, Target.SIZE_ORIGINAL);

            Glide.with(this).load(cropped).apply(options).into(imageView);

            saveBtn.setEnabled(checkForChanges());
        } else {

        }
    });

    private boolean checkForChanges() {
        return selectedImageUri != null || !priceStr.equals(prices[priceView.getValue()]) || !additionalInfoStr.equals(descriptionView.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CROP_IMAGE_REQUEST_CODE) {
                CropImageOptions cropImageOptions = new CropImageOptions();
                cropImageOptions.imageSourceIncludeGallery = true;
                cropImageOptions.imageSourceIncludeCamera = true;
                cropImageOptions.fixAspectRatio = true;
                cropImageOptions.aspectRatioX = 4;
                cropImageOptions.aspectRatioY = 3;
                cropImageOptions.cropperLabelText = "Done";
                cropImageOptions.autoZoomEnabled = false;
                cropImageOptions.guidelines = CropImageView.Guidelines.ON;
                CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(data.getData(), cropImageOptions);
                cropImage.launch(cropImageContractOptions);
            }
        }
    }

    private void uploadProfilePictureToStorage(String parkingId, Uri profilePictureUri) {
        StorageReference storageRef = storage.child("parking_pics").child(parkingId);

        storageRef.putFile(profilePictureUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // TODO
                    // Profile picture uploaded successfully
                    // You can handle further logic here if needed
                })
                .addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    // TODO
                });
    }
}