package com.taxi_riderapplication;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.taxi_riderapplication.Utils.UserUtils;

import java.util.concurrent.TimeUnit;

public class OTP_verifyform extends AppCompatActivity {
    private Button mVerifyCodeBtn;
    private PinView otpEdit;
    private String OTP,name,mblnumber;
    private FirebaseAuth firebaseAuth;
    private TextView textView,txtresend;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    DatabaseReference databaseReference;
    Rider_Helperclass rider_helperclass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_verifyform);
        mVerifyCodeBtn = findViewById(R.id.verify_btn);
        otpEdit = findViewById(R.id.otp_text_view);
        textView=findViewById( R.id.passphone );
        txtresend=findViewById( R.id.resentotp );
        txtresend.setEnabled( false );
        mblnumber=getIntent().getStringExtra( "phonenumber" );
        textView.setText( "+91"+mblnumber );
        name=getIntent ().getStringExtra ("customername");

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance ().getReference ("Rider");
        rider_helperclass=new Rider_Helperclass (  );

        OTP = getIntent().getStringExtra("auth");
        mVerifyCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String verification_code = otpEdit.getText().toString();
                if(!verification_code.isEmpty()){
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP , verification_code);
                    signIn(credential);
                }else{
                    Toast.makeText(OTP_verifyform.this, "Please Enter OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
        txtresend.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String country_code = textView.getText().toString();
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(country_code)
                        .setTimeout(60L , TimeUnit.SECONDS)
                        .setActivity(OTP_verifyform.this)
                        .setCallbacks(mCallBacks)
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        } );
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 10000);

            }
        };
    }
    private void signIn(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    sendToMain();
                }else{
                    Toast.makeText(OTP_verifyform.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser !=null){
            sendToMain();
        }
    }

    private void sendToMain(){
        addDatatoFirebase (name,mblnumber);
        FirebaseInstanceId.getInstance ().getInstanceId ()
                .addOnFailureListener (e -> Toast.makeText (OTP_verifyform.this, e.getMessage (), Toast.LENGTH_SHORT).show ( ))
                .addOnSuccessListener (instanceIdResult -> {
                    Log.d ("token",instanceIdResult.getToken () );
                    UserUtils.UpdateToken (OTP_verifyform.this,instanceIdResult.getToken ());
                });

        finish();
    }
    private void addDatatoFirebase(String name, String phone) {
        rider_helperclass.setName (name);
        rider_helperclass.setMobilenumber (phone);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child(rider_helperclass.getMobilenumber ()).setValue(rider_helperclass);
                Toast.makeText(OTP_verifyform.this, "data added", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(OTP_verifyform.this, "Fail to add data " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}