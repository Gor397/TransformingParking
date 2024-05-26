package com.transformingParking.transformingparking.ParkingActivities;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.transformingParking.transformingparking.Constants;
import com.transformingParking.transformingparking.Notifications.NotificationHelper;
import com.transformingParking.transformingparking.Notifications.NotificationModel;
import com.transformingParking.transformingparking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class ScanQRActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private CompoundBarcodeView barcodeView;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qractivity);

        barcodeView = findViewById(R.id.camera_preview);
        requestCameraPermission();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startScanning() {
        barcodeView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // Handle barcode result
                String parkingId = result.getText();
                Log.d("QRCodeScanner", "QR Code text: " + parkingId);
                String serverURL = Constants.SERVER_URL;
                int status = Constants.OPEN_COMMAND;
                String secret = Constants.SERVER_SECRET;
                sendGetRequest(
                        serverURL +
                                "?parking_id=" + parkingId +
                                "&client_id=" + user.getUid() +
                                "&status=" + status +
                                "&secret=" + secret,
                        parkingId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    private void sendGetRequest(String url, String parkingId) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "Error sending GET request: " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                int httpCode = response.code();
                String responseData = response.body().string();
                Log.d(TAG, "Http Code: " + httpCode);
                Log.d(TAG, "Response: " + responseData);
                if (httpCode == 201 && responseData.equals("Data saved successfully")) {
                    db.collection("parking_spaces").document(parkingId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String ownerId = (String) document.get("user_id");
                                    NotificationModel notificationModel = new NotificationModel("You've got a new client", user.getDisplayName() + " rented your garage", ownerId, user.getUid());
                                    NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
                                    notificationHelper.makeNotification(notificationModel);
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

                    Intent instructionsActivity = new Intent(ScanQRActivity.this, InstructionActivity.class);
                    startActivity(instructionsActivity);
                    finish();
                } else if (httpCode == 200) {
                    long milliSeconds = (long) Double.parseDouble(responseData);
                    Intent intent = new Intent(ScanQRActivity.this, PayActivity.class);
                    intent.putExtra("milliSeconds", milliSeconds);
                    intent.putExtra("parkingId", parkingId);
                    startActivity(intent);
                    finish();
                } else if (httpCode == 503 && responseData.equals("Parking is busy")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ScanQRActivity.this, "Parking is busy", Toast.LENGTH_SHORT).show();
                            startScanning();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ScanQRActivity.this, "Invalid QR code", Toast.LENGTH_SHORT).show();
                            startScanning();
                        }
                    });
                }
            }
        });
    }
}
