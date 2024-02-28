package com.example.transformingparking;

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

import com.google.zxing.Result;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class ScanQRActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private CompoundBarcodeView barcodeView;

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
                Constants c = new Constants();
                String serverURL = c.SERVER_URL;
                int status = c.OPEN_COMMAND;
                String secret = c.SERVER_SECRET;
                sendGetRequest(
                        serverURL +
                                "?parking_id=" + parkingId +
                                "&status=" + status +
                                "&secret=" + secret);
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

    private void sendGetRequest(String url) {
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
                if (response.isSuccessful()) {
                    int httpCode = response.code();
                    String responseData = response.body().string();
                    Log.d(TAG, "Http Code: " + httpCode);
                    Log.d(TAG, "Response: " + responseData);
                    if (httpCode == 200 && responseData.equals("Data saved successfully")) {
                        Intent paymentIntent = new Intent(ScanQRActivity.this, CheckoutActivity.class);
                        startActivity(paymentIntent);
                    } else if (httpCode == 404 && responseData.equals("Parking id not found")) {
                        Toast.makeText(ScanQRActivity.this, "Invalid QR code", Toast.LENGTH_LONG).show();
                        startScanning();
                    }
                } else {
                    Log.e(TAG, "Failed to get response: " + response.code());
                }
            }
        });
    }
}
