package com.ishaanohri.cleanvit;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView noItemTextView;
    public static ArrayList<Complaint> arrayList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private RecyclerViewAdapter recyclerViewAdapter;
    private FloatingActionButton logoutButton, addButton;
    public String name, regNo, mobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        noItemTextView = findViewById(R.id.noItemTextView);

        logoutButton = findViewById(R.id.logout);
        addButton = findViewById(R.id.add);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        name = LogIn.name;
        regNo = LogIn.regNo;
        mobileNumber = MobileNumber.mobileNumber;

        Log.i("INFO","Reached 1");
        databaseReference = FirebaseDatabase.getInstance().getReference().child(LogIn.regNo);
        Log.i("INFO","Reached 2");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                arrayList = new ArrayList<>();

                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    if(dataSnapshot1.getKey().equals("Mobile Number") || dataSnapshot1.getKey().equals("Name"))
                    {

                    }
                    else
                    {
                        Complaint complaint = dataSnapshot1.getValue(Complaint.class);
                        arrayList.add(complaint);

                        if(arrayList.size() != 0)
                        {
                            noItemTextView.setVisibility(View.GONE);
                        }
                    }
                }
                recyclerViewAdapter = new RecyclerViewAdapter(arrayList,MainActivity.this);
                recyclerView.setAdapter(recyclerViewAdapter);

                if(arrayList.size() == 0)
                {
                    noItemTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(arrayList.size() == 0)
        {
            noItemTextView.setVisibility(View.VISIBLE);
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.drawable.logout_alert)
                        .setTitle("Proceed to Logout")
                        .setMessage(String.format("%s, are you sure you want to logout?",LogIn.name))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
                                Gson gson = new Gson();
                                Verify.logIn = "false";
                                String json = gson.toJson(Verify.logIn);
                                sharedPreferences.edit().putString("logIn",json).apply();
                                Intent intent = new Intent(MainActivity.this, LogIn.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                Animatoo.animateSlideDown(MainActivity.this);
                            }
                        })
                        .setNegativeButton("No",null)
                        .show();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
                Animatoo.animateSlideUp(MainActivity.this);
            }
        });
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
