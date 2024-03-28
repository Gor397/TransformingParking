package com.example.transformingparking.ParkingActivities;

import static com.example.transformingparking.Constants.CROP_IMAGE_REQUEST_CODE;
import static com.example.transformingparking.Constants.PENDING;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AddParkingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    MarkerOptions markerOptions;
    Marker marker;
    ImageView parkingPic;
    Button back_btn2;
    SupportMapFragment mapFragment;
    //    AutocompleteSupportFragment autocompleteFragment;
    NumberPicker priceField;
    LatLng parkingCoordinates;
    Uri selectedImageUri;
    String additionalInfo;
    SearchView searchView;
    Button nextBtn;
    Button submitBtn;
    EditText iDramId;
    EditText iDramName;
    TextView idramNameErrMsg;
    TextView idramIDErrMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_location);

        back_btn2 = findViewById(R.id.back_btn2);
        back_btn2.setOnClickListener(v -> finish());

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        searchView = findViewById(R.id.search_view);
        nextBtn = findViewById(R.id.next_btn);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();

                List<Address> addressList = null;

                Geocoder geocoder = new Geocoder(AddParkingActivity.this);
                try {
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    assert addressList != null;
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    if (marker != null) {
                        marker.remove();
                    } else {
                        nextBtn.setEnabled(true);
                    }
                    marker = map.addMarker(new MarkerOptions().position(latLng));

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                } catch (IndexOutOfBoundsException | AssertionError e) {
                    Toast.makeText(AddParkingActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

//        try {
//            Places.initialize(getApplicationContext(), "AIzaSyARE9BF99v3T3zM_GET5KaWXx0R84sezn0");
//        } catch (Exception e) {
//            // TODO
//            // Handle initialization errors
//        }
//
//        autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//        assert autocompleteFragment != null;
//        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
//        autocompleteFragment.setOnPlaceSelectedListener(new
//                                                                PlaceSelectionListener() {
//                                                                    @Override
//                                                                    public void onPlaceSelected(@NonNull Place place) {
//                                                                        // Handle the selected place
//                                                                        LatLng latLng = place.getLatLng();
//                                                                        // Move the camera to the selected place
//                                                                        assert latLng != null;
//                                                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//                                                                    }
//
//                                                                    @Override
//                                                                    public void onError(@NonNull Status status) {
//                                                                        // TODO: Handle the error.
//                                                                        Log.i("AddParking", "An error occurred: " + status);
//                                                                    }
//                                                                });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.set_parking_details);

                parkingPic = findViewById(R.id.pic);

                parkingPic.setOnClickListener(k -> {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, CROP_IMAGE_REQUEST_CODE);
                });

                String[] prices = new String[10];

                for (int i = 1; i <= 10; i++) {
                    String price = Integer.toString(i * 100);
                    prices[i - 1] = price;
                }

                priceField = findViewById(R.id.price);
                priceField.setMinValue(0);
                priceField.setMaxValue(prices.length - 1);
                priceField.setWrapSelectorWheel(false);
                priceField.setDisplayedValues(prices);

                iDramId = findViewById(R.id.editTextNumberIdramId);
                iDramName = findViewById(R.id.editTextIDramName);

                iDramId.addTextChangedListener(textWatcher);
                iDramId.addTextChangedListener(idramIDWatcher);
                iDramName.addTextChangedListener(textWatcher);
                iDramName.addTextChangedListener(idramNameWatcher);

                idramIDErrMsg = findViewById(R.id.textViewIdramIDErrMsg);
                idramNameErrMsg = findViewById(R.id.textViewIdramNameErrMsg);

                submitBtn = findViewById(R.id.submit);
                submitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextInputEditText additionalInfoField = findViewById(R.id.additional_info);
                        additionalInfo = Objects.requireNonNull(additionalInfoField.getText()).toString()
                                .replaceAll("\\s{2,}", " ")
                                .replaceAll("([\\n\\r]){2,}", "\n");

                        FirebaseUser user = auth.getCurrentUser();

                        assert user != null;

                        Map<String, Object> parking_space = new HashMap<>();
                        parking_space.put("latlng", parkingCoordinates);
                        parking_space.put("additional_info", additionalInfo);
                        parking_space.put("price", Integer.parseInt(prices[priceField.getValue()]));
                        parking_space.put("status", PENDING);
                        parking_space.put("user_id", user.getUid());
                        parking_space.put("idramID", iDramId.getText().toString());
                        parking_space.put("idramName", iDramName.getText().toString());

                        database.collection("parking_spaces")
                                .add(parking_space)
                                .addOnSuccessListener(documentReference -> {
                                    uploadProfilePictureToStorage(documentReference.getId(), selectedImageUri);
                                    Log.d("Adding Parking Space", "Parking added with ID: " + documentReference.getId());
                                });

                        finish();
                    }
                });
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

            // check if the text fields are ok
            String iDramIdString = iDramId.getText().toString();
            String iDramNameString = iDramName.getText().toString();

            boolean isiDramIdValid = iDramIdString.length() == 9;
            boolean isEditText2Valid = iDramNameString.length() > 4;

            submitBtn.setEnabled(isiDramIdValid && isEditText2Valid && selectedImageUri != null);
        } else {

        }
    });

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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        markerOptions = new MarkerOptions();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                // Add a marker at the clicked location
                if (marker == null) {
                    nextBtn.setEnabled(true);
                    marker = map.addMarker(markerOptions.position(latLng));
                } else {
                    marker.setPosition(latLng);
                }
                parkingCoordinates = latLng;
            }
        });
    }

    private void uploadProfilePictureToStorage(String parkingId, Uri profilePictureUri) {
        StorageReference storageRef = storage.getReference().child("parking_pics").child(parkingId);

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

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No need to implement
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Check conditions and enable/disable the button accordingly
            String iDramIdString = iDramId.getText().toString();
            String iDramNameString = iDramName.getText().toString();

            boolean isIdramIdValid = iDramIdString.length() == 9;
            boolean isIdramNameValid = iDramNameString.length() > 4;

            submitBtn.setEnabled(isIdramIdValid && isIdramNameValid && selectedImageUri != null);
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No need to implement
        }
    };

    private TextWatcher idramNameWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String iDramNameString = iDramName.getText().toString();
            boolean isIdramNameValid = iDramNameString.length() > 4;

            if (!iDramNameString.isEmpty()) {
                for (char c : iDramNameString.toCharArray()) {
                    if (!Character.isLetter(c) && !Character.isSpaceChar(c)) {
                        idramNameErrMsg.setText(R.string.this_field_should_contain_only_letters);
                        idramNameErrMsg.setVisibility(View.VISIBLE);
                        break;
                    } else if (!isIdramNameValid) {
                        idramNameErrMsg.setText(R.string.write_your_name_as_it_is_written_in_your_idram_account);
                        idramNameErrMsg.setVisibility(View.VISIBLE);
                    } else {
                        idramNameErrMsg.setVisibility(View.GONE);
                    }
                }
            } else {
                idramNameErrMsg.setText(R.string.this_field_can_t_be_empty);
                idramNameErrMsg.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher idramIDWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String iDramIdString = iDramId.getText().toString();

            boolean isIdramIdValid = iDramIdString.length() == 9;

            if (!isIdramIdValid) {
                idramIDErrMsg.setVisibility(View.VISIBLE);
            } else {
                idramIDErrMsg.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}