package com.taxi_riderapplication.Utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.taxi_riderapplication.Common;
import com.taxi_riderapplication.Tokenmodel;


public class UserUtils {


    public static void UpdateToken(Context context, String token) {

        Tokenmodel model = new Tokenmodel ( token );

        FirebaseDatabase.getInstance ().getReference (Common.TOKEN_REFFERENCE)
                .child (FirebaseAuth.getInstance ().getCurrentUser ().getUid ())
                .setValue (model)
                .addOnFailureListener (new OnFailureListener ( ) {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText (context, e.getMessage (), Toast.LENGTH_SHORT).show ( );
                    }
                }).addOnSuccessListener (new OnSuccessListener<Void> ( ) {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });

    }
}
