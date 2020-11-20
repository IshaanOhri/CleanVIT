package com.ishaanohri.cleanvit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wang.avi.AVLoadingIndicatorView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView landmarkTextView, dateTimeTextView, statusTextView, remarksTextView;
    private ImageView imageView;
    private Double latitude, longitude;
    private MaterialButton deleteButton;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private LinearLayout linearLayout;
    private AVLoadingIndicatorView progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        landmarkTextView = findViewById(R.id.landmarkTextView);
        remarksTextView = findViewById(R.id.remarksTextView);
        dateTimeTextView = findViewById(R.id.dateTimeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        imageView = findViewById(R.id.imageView);
        deleteButton = findViewById(R.id.deleteButton);
        linearLayout = findViewById(R.id.linearLayout);
        progressBar = findViewById(R.id.progressBar);

        hideUI();

        String landmark = getIntent().getStringExtra("Landmark");
        String remarks = getIntent().getStringExtra("Remarks");
        String dateTime = getIntent().getStringExtra("DateTime");
        String status = getIntent().getStringExtra("Status");
        final String image = getIntent().getStringExtra("Image");
        final String key = getIntent().getStringExtra("Key");

        latitude = Double.parseDouble(getIntent().getStringExtra("Latitude"));
        longitude = Double.parseDouble(getIntent().getStringExtra("Longitude"));

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(MapsActivity.this)
                        .setIcon(R.drawable.del_pic)
                        .setTitle("Delete Complaint")
                        .setMessage("Are you sure you want to delete the complaint? The action cannot be undone.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                databaseReference = FirebaseDatabase.getInstance().getReference().child(LogIn.regNo).child(key);
                                databaseReference.removeValue();
                                storageReference.child(image).delete();
                                storageReference.child(image).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        storageReference.child(uri.toString()).delete();
                                    }
                                });
                                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                                startActivity(intent);
                                Animatoo.animateSlideDown(MapsActivity.this);
                            }
                        })
                        .setNegativeButton("No", null).show();
            }
        });

        landmarkTextView.setText(landmark);
        remarksTextView.setText(remarks);
        dateTimeTextView.setText(dateTime);
        statusTextView.setText(status);

        if(status.equals("Completed"))
        {
            statusTextView.setTextColor(Color.parseColor("#13CF26"));
        }
        else
        {
            statusTextView.setTextColor(Color.parseColor("#FF2626"));
        }

        storageReference = FirebaseStorage.getInstance().getReference().child(image);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(MapsActivity.this)
                        .load(uri)
                        .into(imageView);
                showUI();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showUI();
                Toast.makeText(MapsActivity.this, "Error getting image", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void hideUI()
    {
        Float alpha = 0.2f;
        linearLayout.setAlpha(alpha);
        linearLayout.setEnabled(false);
        deleteButton.setAlpha(alpha);
        deleteButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void showUI()
    {
        Float alpha = 1.0f;
        linearLayout.setAlpha(alpha);
        linearLayout.setEnabled(true);
        deleteButton.setAlpha(alpha);
        deleteButton.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng complaintLocation = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        mMap.addMarker(new MarkerOptions().position(complaintLocation).title("Complaint Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(complaintLocation,18F));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(intent);
        Animatoo.animateSlideDown(MapsActivity.this);
    }
}
