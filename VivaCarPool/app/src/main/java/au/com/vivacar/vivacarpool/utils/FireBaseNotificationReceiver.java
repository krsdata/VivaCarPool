package au.com.vivacar.vivacarpool.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import au.com.vivacar.vivacarpool.config.Config;

/**
 * Created by adarsh on 21/3/17.
 */

public class FireBaseNotificationReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
            // gcm successfully registered
            // now subscribe to `global` topic to receive app wide notifications
            FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

            //displayFirebaseRegId();

        } else{
            // new push notification is received

            String message = intent.getStringExtra("message");

            Toast.makeText(context, "Push notification: " + message, Toast.LENGTH_LONG).show();


        }
    }
}
