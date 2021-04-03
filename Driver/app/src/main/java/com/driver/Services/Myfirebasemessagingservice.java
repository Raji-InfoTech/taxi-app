package com.driver.Services;

import androidx.annotation.NonNull;

import com.driver.Utils.UserUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import java.util.Map;
import java.util.Random;

public class Myfirebasemessagingservice extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken (s);
        if(FirebaseAuth.getInstance ().getCurrentUser () != null)
            UserUtils.UpdateToken(this,s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived (remoteMessage);
        Map<String,String> name = remoteMessage.getData ();
        if (name != null){
            com.driver.Common.showNotification(this,new Random (  ).nextInt (),name.get (com.driver.Common.NOTI_TITLE),name.get (com.driver.Common.NOTI_CONTENT),null);
        }
    }
}
