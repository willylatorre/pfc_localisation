/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.pfc_alpha1;

import static com.example.pfc_alpha1.CommonUtilities.SENDER_ID;
import static com.example.pfc_alpha1.CommonUtilities.displayMessage;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	
	
	public static String gcm_type;
	public static String gcm_time;
	public static String gcm_lat;
	public static String gcm_lng;
	

    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        Toast.makeText(this, "GCM Registered.",
                Toast.LENGTH_SHORT).show();
     //   displayMessage(context, getString(R.string.gcm_registered));
        ServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        Toast.makeText(this, "GCM Unregistered.",
                Toast.LENGTH_SHORT).show();
     //   displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
            Toast.makeText(this, "Ignoring unregister callback.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        
        //Retrieving variables
        gcm_type=intent.getStringExtra("type");
    	gcm_time=intent.getStringExtra("time");       
        gcm_lat=intent.getStringExtra("lat");
    	gcm_lng=intent.getStringExtra("lng");
    	
    	// Generating message
        String message = "Evento: "+gcm_type+"!! +info";
    //    displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
   //     displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    //    displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
   //     displayMessage(context, getString(R.string.gcm_recoverable_error,
   //             errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
       
        Intent notificationIntent = new Intent(context, ShareNotification.class);
        //adding parameters
        notificationIntent.putExtra("type", gcm_type);
        notificationIntent.putExtra("time", gcm_time);
        notificationIntent.putExtra("lat", gcm_lat);
        notificationIntent.putExtra("lng", gcm_lng);
        // set intent so it does not start a new activity
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Resources res = context.getResources();
        Notification notification = new Notification.Builder(context)
        							.setContentIntent(pendingNotificationIntent)
        							.setContentTitle("OJO!! Ha pasado algo")
        							.setContentText(message)
        							.setSmallIcon(R.drawable.ic_gcm_noti)
        							.setLargeIcon(BitmapFactory.decodeResource(res,R.drawable.ic_gcm_noti))
        							.setAutoCancel(true)
        							.build();
        
        notificationManager.notify(0, notification);
    }

}
