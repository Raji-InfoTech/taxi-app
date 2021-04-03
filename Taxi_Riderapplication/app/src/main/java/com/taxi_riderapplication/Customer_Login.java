package com.taxi_riderapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Customer_Login extends AppCompatActivity {

    private Button mSendOTPBtn;
    private TextView processText,countryCodeEdit;
    private EditText  phoneNumberEdit,customer_name;
    private FirebaseAuth auth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private ProgressBar mLoginProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer__login);
        mSendOTPBtn = findViewById(R.id.generate_btn);
        processText = findViewById(R.id.login_form_feedback);
        countryCodeEdit = findViewById(R.id.country_code_text);
        phoneNumberEdit = findViewById(R.id.phone_number_text);
        mLoginProgress = findViewById(R.id.login_progress_bar);
        customer_name=findViewById (R.id.edtname);
        auth = FirebaseAuth.getInstance();
        mSendOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String country_code = countryCodeEdit.getText().toString();
                String phone = phoneNumberEdit.getText().toString();
                SharedPreferences sharedPref = getSharedPreferences("myKey", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString( "value",phone);
                editor.commit();
                String phoneNumber = "+" + country_code + "" + phone;
                mLoginProgress.setVisibility(View.VISIBLE);
                if (!country_code.isEmpty() || !phone.isEmpty()){
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L , TimeUnit.SECONDS)
                            .setActivity(Customer_Login.this)
                            .setCallbacks(mCallBacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }else{
                    mSendOTPBtn.setEnabled(false);
                    processText.setText("Please Enter Country Code and Phone Number");
                    processText.setTextColor( Color.RED);
                    processText.setVisibility(View.VISIBLE);
                }
            }
        });
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                processText.setText(e.getMessage());
                processText.setTextColor( Color.RED);
                processText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                processText.setText("OTP has been Sent");
                mLoginProgress.setVisibility(View.INVISIBLE);
                processText.setVisibility(View.VISIBLE);
                mSendOTPBtn.setEnabled(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent otpIntent = new Intent(Customer_Login.this ,OTP_verifyform.class);
                        otpIntent.putExtra("auth" , s);
                        otpIntent.putExtra( "phonenumber", phoneNumberEdit.getText().toString() );
                        otpIntent.putExtra ("customername",customer_name.getText ().toString ());
                        startActivity(otpIntent);
                    }
                }, 6000);

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user !=null){
            sendToMain();
        }
    }
    private void sendToMain(){
        Intent mainIntent = new Intent(Customer_Login.this ,HomeScreenActivity.class);
        startActivity(mainIntent);
        finish();
    }
    private void signIn(PhoneAuthCredential credential){
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    sendToMain();
                }else{
                    processText.setText(task.getException().getMessage());
                    processText.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}