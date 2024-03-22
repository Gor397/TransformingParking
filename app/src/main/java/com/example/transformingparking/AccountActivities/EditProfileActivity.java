package com.example.transformingparking.AccountActivities;

import static android.content.ContentValues.TAG;
import static com.example.transformingparking.Constants.CROP_IMAGE_REQUEST_CODE;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.example.transformingparking.R;
import com.example.transformingparking.signIn.VerifyCodeActivity;
import com.example.transformingparking.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EditProfileActivity extends AppCompatActivity {
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private ImageView profilePic;
    private EditText nameView;
    private EditText phoneView;
    private TextView nameErrMessageView;
    private TextView phoneErrMessageView;
    private Button saveBtn;
    private Button changePhoneNumberBtn;

    private String current_name_str;
    private String current_phone_str;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePic = findViewById(R.id.imageViewProfilePic);
        nameView = findViewById(R.id.editTextName);
        phoneView = findViewById((R.id.editTextPhone));
        nameErrMessageView = findViewById(R.id.textViewNameErrMessage);
        phoneErrMessageView = findViewById(R.id.textViewPhoneErrMessage);
        saveBtn = findViewById(R.id.buttonSave);
        changePhoneNumberBtn = findViewById(R.id.changePhoneNumberButton);
        progressDialog = new ProgressDialog(EditProfileActivity.this);

        Intent intent = getIntent();
        current_name_str = intent.getStringExtra("name");
        current_phone_str = intent.getStringExtra("phone");

        nameView.setText(current_name_str);
        phoneView.setText(current_phone_str);

        StorageReference imageRef = storageReference.child("profile_pics").child(user.getUid());
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(EditProfileActivity.this).load(uri).into(profilePic);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO Handle the error
            }
        });

        nameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!checkUserName()) {
                    saveBtn.setEnabled(false);
                } else {
                    saveBtn.setEnabled(selectedImageUri != null || !s.toString().equals(current_name_str));
                }
            }
        });

        phoneView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    phoneErrMessageView.setText(R.string.this_field_can_t_be_empty);
                }
                changePhoneNumberBtn.setEnabled(!s.toString().equals(current_phone_str) && !s.toString().isEmpty());
            }
        });

        profilePic.setOnClickListener(k -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, CROP_IMAGE_REQUEST_CODE);
        });

        saveBtn.setOnClickListener(v -> {
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            DocumentReference docRef = database.collection("users").document(user.getUid());
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", nameView.getText().toString());

            docRef.update(updates);
            Util.setUserName(nameView.getText().toString());

            if (selectedImageUri != null) {
                uploadProfilePictureToStorage(user.getUid(), selectedImageUri);
            } else {
                progressDialog.cancel();
                finish();
            }
        });

        changePhoneNumberBtn.setOnClickListener(v -> {
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            sendVerificationCode(phoneView.getText().toString());
        });
    }

    private boolean checkUserName() {
        nameErrMessageView.setText("");

        String userName = nameView.getText().toString();

        if (userName.trim().isEmpty()) {
            nameErrMessageView.setText(R.string.this_field_can_t_be_empty);
            return false;
        }

        // Split the userName into words
        String[] words = userName.trim().split("\\s+");

        // Check each word
        for (String word : words) {
            // Check if the word contains only letters
            if (!word.matches("[a-zA-Z]+")) {
                nameErrMessageView.setText(R.string.this_field_should_contain_only_letters);
                return false;
            }
        }

        // If all checks passed, return true
        return true;
    }

    ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            selectedImageUri = result.getUriContent();
            Bitmap cropped = BitmapFactory.decodeFile(result.getUriFilePath(getApplicationContext(), true));
            ImageView imageView = findViewById(R.id.imageViewProfilePic);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            RequestOptions options = new RequestOptions().override(screenWidth, Target.SIZE_ORIGINAL);

            Glide.with(this).load(cropped).apply(options).into(imageView);
            saveBtn.setEnabled(true);
        }
    });

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CROP_IMAGE_REQUEST_CODE) {
                CropImageOptions cropImageOptions = new CropImageOptions();
                cropImageOptions.imageSourceIncludeGallery = true;
                cropImageOptions.imageSourceIncludeCamera = true;
                cropImageOptions.fixAspectRatio = true;
                cropImageOptions.aspectRatioX = 1;
                cropImageOptions.aspectRatioY = 1;
                cropImageOptions.autoZoomEnabled = false;
                cropImageOptions.cropperLabelText = "Done";
                cropImageOptions.guidelines = CropImageView.Guidelines.ON;
                CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(data.getData(), cropImageOptions);
                cropImage.launch(cropImageContractOptions);
            }
        }
    }

    private void uploadProfilePictureToStorage(String parkingId, Uri profilePictureUri) {
        StorageReference storageRef = storage.getReference().child("profile_pics").child(parkingId);

        storageRef.putFile(profilePictureUri).addOnSuccessListener(taskSnapshot -> {
            progressDialog.cancel();
            finish();
        }).addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            // TODO
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                updatePhoneNumber(phoneAuthCredential);
                progressDialog.cancel();
                finish();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.cancel();
                phoneErrMessageView.setText("");
                if (e instanceof FirebaseAuthInvalidCredentialsException && ((FirebaseAuthInvalidCredentialsException) e).getErrorCode().equals("ERROR_INVALID_PHONE_NUMBER")) {
                    phoneErrMessageView.setText(R.string.invalid_phone_number);
                } else {
                    phoneErrMessageView.setText(e.getMessage());
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                // Move to the next activity to enter the verification code
                Intent intent = new Intent(EditProfileActivity.this, VerifyCodeActivity.class);
                intent.putExtra("verificationId", mVerificationId);
                intent.putExtra("updatePhoneNumber", true);
                startActivity(intent);
                finish();
            }
        });
    }

    private void updatePhoneNumber(PhoneAuthCredential phoneAuthCredential) {
        user.updatePhoneNumber(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.cancel();
                    Log.d(TAG, "Phone number updated.");

                    DocumentReference docRef = database.collection("users").document(user.getUid());
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("phone", phoneView.getText().toString());

                    docRef.update(updates);
                    finish();
                } else {
                    Log.d(TAG, "Failed to update phone number.", task.getException());
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        phoneErrMessageView.setText(getString(R.string.this_number_is_already_used_in_another_account));
                    }
                }
            }
        });
    }
}