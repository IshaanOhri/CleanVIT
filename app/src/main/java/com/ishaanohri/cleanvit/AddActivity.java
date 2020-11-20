package com.ishaanohri.cleanvit;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;

public class AddActivity extends AppCompatActivity {

    private ExtendedFloatingActionButton addButton;
    private TextInputEditText landmarkEditText, remarksEditText;
    private final int LOC_CODE = 100, IMAGE_CODE = 101;
    private ImageView imageView;
    private DatabaseReference databaseReference, temp;
    private StorageReference storageReference;
    private String landmark, dateTime, latitude, longitude, remarks, status = "Pending";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Uri imageUri;
    private String imageName;
    private LinearLayout linearLayout;
    private AVLoadingIndicatorView progressBar;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && requestCode == LOC_CODE)
            {
                getFirstLocation();
            }
        }
    }

    public void getFirstLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Task location = fusedLocationProviderClient.getLastLocation();
        location.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    assignLocation((Location)task.getResult());
                }
            }
        });
    }

    private void assignLocation(Location location)
    {
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Log.i("INFO","AddActivity created");
        Log.i("INFO",LogIn.name);
        Log.i("INFO",LogIn.regNo);

        addButton = findViewById(R.id.addButton);
        landmarkEditText = findViewById(R.id.landmarkEditText);
        remarksEditText = findViewById(R.id.remarksEditText);
        imageView = findViewById(R.id.imageView);
        linearLayout = findViewById(R.id.linearLayout);
        progressBar = findViewById(R.id.progressBar);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                assignLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("INFO","ImageView Clicked");

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_CODE);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("INFO","Add button clicked");

                if((landmarkEditText.getText().toString()).equals(""))
                {
                    landmarkEditText.setError("Please enter suitable landmark");
                    landmarkEditText.requestFocus();
                }
                else if(imageView.getDrawable().getConstantState() == getResources().getDrawable(R.mipmap.no_image).getConstantState())
                {
                    Toast.makeText(AddActivity.this,"Please choose image",Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.i("INFO","Getting data from views");

                    landmark = landmarkEditText.getText().toString().trim();
                    remarks = remarksEditText.getText().toString().trim();

                    if(remarks.equals(""))
                    {
                        remarks = "No remarks available";
                    }

                    Log.i("INFO","Data from views recvd");

                    temp = databaseReference.child(LogIn.regNo).push();

                    Log.i("INFO",temp.toString());

                    uploadFile();
                }
            }
        });

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOC_CODE);
        }
        else
        {
            getFirstLocation();
        }
    }

    private void hideUI()
    {
        float alpha = 0.2f;
        progressBar.setVisibility(View.VISIBLE);
        linearLayout.setAlpha(alpha);
        landmarkEditText.setEnabled(false);
        remarksEditText.setEnabled(false);
        addButton.setAlpha(alpha);
        addButton.setEnabled(false);
        imageView.setEnabled(false);
    }

    private String getCurrentDateTime()
    {
        Calendar calendar = Calendar.getInstance();
        String currentDateTime = DateFormat.getDateTimeInstance().format(calendar.getTime());

        return currentDateTime;
    }

    private void uploadFile()
    {
        if(imageUri != null)
        {
            byte[] bytes = null;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);
                bytes = stream.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageName = LogIn.regNo + System.currentTimeMillis() + "." + getFileExtension(imageUri);

            StorageReference storageReference1 = storageReference.child(imageName);

            UploadTask uploadTask = storageReference1.putBytes(bytes);

            uploadTask
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            dateTime = getCurrentDateTime();
                            getFirstLocation();

                            temp.child("ImageURL").setValue(imageName);
                            temp.child("Key").setValue(temp.getKey());
                            temp.child("Landmark").setValue(landmark);
                            temp.child("Remarks").setValue(remarks);
                            temp.child("DateTime").setValue(dateTime);
                            temp.child("Status").setValue(status);
                            temp.child("Latitude").setValue(latitude);
                            temp.child("Longitude").setValue(longitude);

                            Toast.makeText(AddActivity.this, "Complaint Added", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(AddActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            Animatoo.animateSlideDown(AddActivity.this);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(AddActivity.this, "Error occurred. Try Again", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            hideUI();
                        }
                    });
        }
        else
        {
            Toast.makeText(AddActivity.this,"No File Chosen",Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if(requestCode == IMAGE_CODE)
            {
                imageUri = data.getData();
                Log.i("INFO",data.getData().toString());
                Glide
                        .with(this)
                        .load(imageUri)
                        .into(imageView);
            }
        }catch (Exception e){
            Toast.makeText(this, "Error getting image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {

            Intent intent = new Intent(AddActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Animatoo.animateSlideDown(AddActivity.this);

    }
}
