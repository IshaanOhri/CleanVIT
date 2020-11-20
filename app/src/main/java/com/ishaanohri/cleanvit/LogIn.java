package com.ishaanohri.cleanvit;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class LogIn extends AppCompatActivity {

    private MaterialButton loginButton;
    private TextInputEditText regNoEditText, nameEditText;
    public static String name, regNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        loginButton = findViewById(R.id.logInButton);
        regNoEditText = findViewById(R.id.mobileEditText);
        nameEditText = findViewById(R.id.nameEditText);

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        Gson gson = new Gson();
        Type type = new TypeToken<String>(){}.getType();
        String json = sharedPreferences.getString("logIn","false");
        Verify.logIn = gson.fromJson(json,type);
        json = sharedPreferences.getString("name"," ");
        name = gson.fromJson(json,type);
        json = sharedPreferences.getString("regNo"," ");
        regNo = gson.fromJson(json,type);
        json = sharedPreferences.getString("mobNo"," ");
        MobileNumber.mobileNumber = gson.fromJson(json,type);

        if(Verify.logIn.equals("true") && name.length()!=0 && regNo.length()!=0 && MobileNumber.mobileNumber.length()!=0)
        {
            Intent intent = new Intent(LogIn.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Animatoo.animateFade(LogIn.this);
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nameEditText.getText().toString().trim().equals(""))
                {

                    nameEditText.setError("Please enter name");
                    nameEditText.requestFocus();
                }
                else if(regNoEditText.getText().toString().trim().equals(""))
                {
                    regNoEditText.setError("Please enter registration number");
                    regNoEditText.requestFocus();
                }
                else {

                    regNo = regNoEditText.getText().toString().trim().toUpperCase();
                    name = nameEditText.getText().toString().trim().toUpperCase();
                    Intent intent = new Intent(LogIn.this, MobileNumber.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Animatoo.animateSlideUp(LogIn.this);
                }
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
