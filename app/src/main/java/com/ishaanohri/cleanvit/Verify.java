package com.ishaanohri.cleanvit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.concurrent.TimeUnit;

public class Verify extends AppCompatActivity {

    private String verificationID;
    private FirebaseAuth firebaseAuth;
    private AVLoadingIndicatorView progressBar;
    private PinView userCodeEditText;
    private MaterialButton verifyButton;
    private int minute, second;
    private TextView resend, userNumber, editMobNoTextView;
    private DatabaseReference databaseReference;
    public static String logIn = "false";
    private ImageView stepImageView, grpImageView, fieldImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressBar = findViewById(R.id.progressBar);
        userCodeEditText = findViewById(R.id.userCodeEditText);
        verifyButton = findViewById(R.id.verifyButton);
        userNumber = findViewById(R.id.userNumber);
        resend = findViewById(R.id.resend);
        stepImageView = findViewById(R.id.stepImageView);
        grpImageView = findViewById(R.id.grpImageView);
        fieldImageView = findViewById(R.id.fieldImageView);
        editMobNoTextView = findViewById(R.id.editMobNoTextView);

        userNumber.setEnabled(false);
        userNumber.setText(MobileNumber.mobileNumber);

        sendVerificationCode(MobileNumber.mobileNumber);

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(resend.getText().toString().trim().equals("Resend OTP"))
                {
                    sendVerificationCode(MobileNumber.mobileNumber);
                    new CountDownTimer(120000,1000)
                    {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            minute = (int)millisUntilFinished/60000;
                            second = (int)(millisUntilFinished/1000)%60;

                            resend.setText("Resend OTP in " + String.format("%02d",minute) + ":" + String.format("%02d",second));
                        }

                        @Override
                        public void onFinish() {
                            resend.setText("Resend OTP");
                        }
                    }.start();
                }

            }
        });

        new CountDownTimer(120000,1000)
        {

            @Override
            public void onTick(long millisUntilFinished) {
                minute = (int)millisUntilFinished/60000;
                second = (int)(millisUntilFinished/1000)%60;

                resend.setText("Resend OTP in " + String.format("%02d",minute) + ":" + String.format("%02d",second));
            }

            @Override
            public void onFinish() {
                resend.setText("Resend OTP");
            }
        }.start();

        editMobNoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Verify.this,MobileNumber.class);
                startActivity(intent);
                Animatoo.animateSlideDown(Verify.this);
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userCodeEditText.getText().toString().trim().length() < 6) {
                    userCodeEditText.setError("Please enter proper OTP");
                    userCodeEditText.requestFocus();
                } else {
                    hideUI();
                    verifyCode(userCodeEditText.getText().toString().trim());
                }
            }
        });

    }

    private void hideUI()
    {
        float alpha = 0.2f;
        progressBar.setVisibility(View.VISIBLE);
        stepImageView.setAlpha(alpha);
        grpImageView.setAlpha(alpha);
        fieldImageView.setAlpha(alpha);
        userNumber.setAlpha(alpha);
        editMobNoTextView.setAlpha(alpha);
        editMobNoTextView.setEnabled(false);
        userCodeEditText.setAlpha(alpha);
        userCodeEditText.setEnabled(false);
        verifyButton.setAlpha(alpha);
        verifyButton.setEnabled(false);
        resend.setAlpha(alpha);
        resend.setEnabled(false);
    }

    private void showUI()
    {
        float alpha = 1.0f;
        progressBar.setVisibility(View.INVISIBLE);
        stepImageView.setAlpha(alpha);
        grpImageView.setAlpha(alpha);
        fieldImageView.setAlpha(alpha);
        userNumber.setAlpha(alpha);
        editMobNoTextView.setAlpha(alpha);
        editMobNoTextView.setEnabled(true);
        userCodeEditText.setAlpha(alpha);
        userCodeEditText.setEnabled(true);
        verifyButton.setAlpha(alpha);
        verifyButton.setEnabled(true);
        resend.setAlpha(alpha);
        resend.setEnabled(true);
    }

    private void sendVerificationCode(String mobileNumber)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNumber,
                30,
                TimeUnit.SECONDS,
                this,
                callback
        );
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks
            callback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationID = s;

        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String verificationCode = phoneAuthCredential.getSmsCode();

            if(verificationCode != null)
            {
                hideUI();
                userCodeEditText.setText(verificationCode);
                verifyCode(verificationCode);
            }

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            showUI();
            Toast.makeText(Verify.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String code)
    {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID,code);
            signInWithCredential(credential);
        }catch (Exception e)
        {

        }

    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            logIn = "true";
                            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
                            Gson gson = new Gson();
                            String json = gson.toJson(logIn);
                            sharedPreferences.edit().putString("logIn",json).apply();
                            json = gson.toJson(LogIn.name);
                            sharedPreferences.edit().putString("name",json).apply();
                            json = gson.toJson(LogIn.regNo);
                            sharedPreferences.edit().putString("regNo",json).apply();
                            json = gson.toJson(MobileNumber.mobileNumber);
                            sharedPreferences.edit().putString("mobNo",json).apply();

                            Log.i("INFO",json);

                            databaseReference.child(LogIn.regNo).child("Mobile Number").setValue(MobileNumber.mobileNumber);
                            databaseReference.child(LogIn.regNo).child("Name").setValue(LogIn.name);
                            Intent intent = new Intent(Verify.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            Animatoo.animateSlideUp(Verify.this);
                        }
                        else
                        {
                            Toast.makeText(Verify.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            showUI();
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
