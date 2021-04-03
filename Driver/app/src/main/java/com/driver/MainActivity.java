package com.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;


import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
     Button btnsignin,register;

     FirebaseAuth auth;
     FirebaseDatabase firebaseDatabase;
     DatabaseReference databaseReference;
     RelativeLayout rootlayout;
//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/arkhip_font.ttf")
//               .setFontAttrId(R.attr.fontPath).build());
      setContentView (R.layout.activity_main);
         btnsignin = findViewById(R.id.btnsignin);
         register = findViewById(R.id.btnregister);
         rootlayout = findViewById (R.id.rootLayout);
         auth = FirebaseAuth.getInstance ();

         firebaseDatabase=FirebaseDatabase.getInstance ();

         databaseReference = firebaseDatabase.getReference ("USERS");

         register.setOnClickListener (new View.OnClickListener ( ) {
             @Override
             public void onClick(View v) {
                 showRegisterdialog();
             }
         });


         btnsignin.setOnClickListener (new View.OnClickListener ( ) {
             @Override
             public void onClick(View v) {
                 showLoginDialog();
             }
         });
    }

    private void showLoginDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setTitle ("SIGN IN");
        builder.setMessage ("Please use email to sign in");
        LayoutInflater layoutInflater = LayoutInflater.from (this);
        View view1 = layoutInflater.inflate (R.layout.activity_login,null);


        final MaterialEditText edtemail = view1.findViewById (R.id.edtemail);
        final MaterialEditText edtpass = view1.findViewById (R.id.edtpass);


        builder.setView(view1);

        builder.setPositiveButton ("SIGN IN", new DialogInterface.OnClickListener ( ) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss ( );

                    btnsignin.setEnabled (false);


                if (TextUtils.isEmpty (edtemail.getText ( ).toString ( ))) {
                    Toast.makeText (getApplicationContext (), "PLEASE ENTET EMAIL ADDRESS", Toast.LENGTH_SHORT).show ( );
                    return;
                }

                if (edtpass.getText ( ).toString ( ).length ( ) < 6) {
                    Toast.makeText (getApplicationContext (), "PLEASE ENTET PASSWORD TO SHORT", Toast.LENGTH_SHORT).show ( );
                    return;
                }


               SpotsDialog waitingdialog = new SpotsDialog (MainActivity.this);
                waitingdialog.show ();


                auth.signInWithEmailAndPassword (edtemail.getText ().toString (),edtpass.getText ().toString ())
                        .addOnSuccessListener (new OnSuccessListener<AuthResult> ( ) {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingdialog.dismiss ();
                                startActivity (new Intent ( MainActivity.this, HomeScreen.class ));
                                finish ();
                            }
                        }).addOnFailureListener (new OnFailureListener ( ) {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingdialog.dismiss ();
                        Toast.makeText (getApplicationContext (),"FAILED"+e.getMessage (),Toast.LENGTH_SHORT).show ();

                        btnsignin.setEnabled (true);

                    }
                });

            }
        });
        builder.setNegativeButton ("CANCEL", new DialogInterface.OnClickListener ( ) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss ();
            }
        });
        builder.show ();


    }


    private void showRegisterdialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setTitle ("REGISTER");
        builder.setMessage ("Please use email to register");
        LayoutInflater layoutInflater = LayoutInflater.from (this);
        View view = layoutInflater.inflate (R.layout.activity_registraion,null);


        final MaterialEditText edtemail = view.findViewById (R.id.edtemail);
        final MaterialEditText edtpass = view.findViewById (R.id.edtpass);
        final MaterialEditText edtuser = view.findViewById (R.id.edtname);
        final MaterialEditText edtphone = view.findViewById (R.id.edtphone);


        builder.setView(view);

        builder.setPositiveButton ("REGISTER", new DialogInterface.OnClickListener ( ) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss ();

                 if(TextUtils.isEmpty (edtemail.getText ().toString ()))
                 {
                 Toast.makeText  (getApplicationContext (),"PLEASE ENTET EMAIL ADDRESS",Toast.LENGTH_SHORT).show ();
                     return;
                 }

                if(edtpass.getText ().toString ().length ()< 6)
                {
                    Toast.makeText (getApplicationContext (),"PLEASE ENTET PASSWORD TO SHORT",Toast.LENGTH_SHORT).show ();
                    return;
                }


                if(TextUtils.isEmpty (edtuser.getText ().toString ()))
                {
                    Toast.makeText  (getApplicationContext (),"PLEASE ENTET NAME",Toast.LENGTH_SHORT).show ();
                    return;
                }


                if(TextUtils.isEmpty (edtphone.getText ().toString ()))
                {
                    Toast.makeText (getApplicationContext (),"PLEASE ENTET PHONE NUMBER",Toast.LENGTH_SHORT).show ();
                    return;
                }



                auth.createUserWithEmailAndPassword (edtemail.getText ().toString (),edtpass.getText().toString ())
                        .addOnSuccessListener (new OnSuccessListener<AuthResult> ( ) {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                user s = new user();
                                s.setEmail (edtemail.getText ().toString ());
                                s.setUser (edtuser.getText ().toString ());
                                s.setPhone (edtphone.getText ().toString ());
                                s.setPass (edtpass.getText ().toString ());


                                databaseReference.child (FirebaseAuth.getInstance ().getCurrentUser ().getUid ()).setValue (s).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText (getApplicationContext (),"REGISTER SUCCESSFULLY",Toast.LENGTH_SHORT).show ();
                                        return;
                                    }

                                })
                                .addOnFailureListener (new OnFailureListener ( ) {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText (getApplicationContext (),"FAILED"+e.getMessage (),Toast.LENGTH_SHORT).show ();
                                        return;
                                    }
                                });
                            }
                        }).addOnFailureListener (new OnFailureListener ( ) {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (getApplicationContext (),"FAILED"+e.getMessage (),Toast.LENGTH_SHORT).show ();
                        return;
                    }
                });
            }
        });

        builder.setNegativeButton ("CANCEL", new DialogInterface.OnClickListener ( ) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss ();
            }
        });

        builder.show ();


    }
}