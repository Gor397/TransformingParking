package com.example.transformingparking;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyTicket extends AppCompatActivity {
    private ImageView qrCodeImageView;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DatabaseReference sentRef;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ticket);

        currentUser = auth.getCurrentUser();

        qrCodeImageView = findViewById(R.id.qrCodeImageView);

        // Call method to generate QR code
        String user_id = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        generateQRCode(user_id);

        sentRef = FirebaseDatabase.getInstance().getReference(currentUser.getUid()).child("sent_requests");
        sentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get the snapshot of the data
                DataSnapshot dataSnapshot = task.getResult();

                // Check if there is any data
                if (dataSnapshot.exists()) {
                    // Iterate through all children
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String parkingId = childSnapshot.child("parking_id").getValue(String.class);

                        db.collection("parking_spaces").document(parkingId).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Object latlng = documentSnapshot.get("latlng");
                                    }
                                });
                    }
                } else {
                    // TODO No data exists at the specified location
                }
            } else {
                // TODO Failed to read value
            }
        });
    }

    private void generateQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white));
                }
            }
            qrCodeImageView.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}