package com.netglue.ngtmobile.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.netglue.ngtmobile.MainActivity;
import com.netglue.ngtmobile.R;
import com.netglue.ngtmobile.common.Constant;
import com.netglue.ngtmobile.common.RestApi;
import com.netglue.ngtmobile.common.Utils;
import com.netglue.ngtmobile.model.AssetItem;
import com.netglue.ngtmobile.model.AssetPushItem;
import com.netglue.ngtmobile.model.CardTypeItem;
import com.netglue.ngtmobile.model.NotificationItem;
import com.netglue.ngtmobile.model.NotificationToneItem;
import com.netglue.ngtmobile.model.SharedStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by PBG on 2017-10-16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    private static int lastNotificationId = 0;

    NotificationCompat.Builder notificationBuilder;


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        sendNotification(remoteMessage);
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param remoteMessage FCM message received.
     */
    private void sendNotification(RemoteMessage remoteMessage) {

        String channelId = "ngt_channel";

        // title / body
        String title = "";
        String body = "";

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            title = notification.getTitle();
            body = notification.getBody();
        } else if (remoteMessage.getData() != null) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }

//        saveMessage(title, body);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("from_notification", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String notificaiton_sound = "";
        List<NotificationToneItem> mNotificationItemList = SharedStorage.getInstance().notificationToneItemList;
        if(mNotificationItemList != null && mNotificationItemList.size() > 0) {
            for (int index = 0; index < mNotificationItemList.size(); index++) {
                if (mNotificationItemList.get(index).isSelected()) {
                    notificaiton_sound = mNotificationItemList.get(index).getTone_uri();
                    break;
                }
            }
            Log.d(TAG, "this is set notification sound::::" + notificaiton_sound);
            notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_stat_notification)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setSound(Uri.parse(notificaiton_sound));
        } else {
            Log.d(TAG, "this is empty notification");
            notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_stat_notification)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent);
        }

        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(body);

        // Notification ID
        int notiId = ++lastNotificationId;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId,
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(notiId /* ID of notification */, notificationBuilder.build());

        List<AssetPushItem> asset_push = SharedStorage.getInstance().asset_push;
        List<NotificationItem> notificationList = SharedStorage.getInstance().notificationList;
        try {
            JSONObject obj = new JSONObject(remoteMessage.getData());

//            boolean exist = false;
//            if(obj.getString("category").equals("alarm")) {
//                if (asset_push != null) {
//                    for(int index = 0; index < asset_push.size(); index++) {
//                        if(asset_push.get(index).getAssets_id().equals(obj.getString("a_id"))) {
//                            exist = true;
//                            ArrayList<Integer> alarm_id_list = asset_push.get(index).getAlarm_id_list();
//                            alarm_id_list.add(Integer.parseInt(obj.getString("alarm_id")));
//                            asset_push.get(index).setAlarm_id_list(alarm_id_list);
//                            break;
//                        }
//                    }
//                    if(!exist) {
//                        ArrayList<Integer> list = new ArrayList<>();
//                        list.add(obj.getInt("alarm_id"));
//                        AssetPushItem assetPushItem = new AssetPushItem(obj.getString("a_id"), list);
//                        asset_push.add(assetPushItem);
//                    }
//
//                }
//            }

            NotificationItem notificationItem = NotificationItem.parse(obj);
            notificationList.add(notificationItem);
            saveMessage(notificationItem);

        } catch (Throwable t) {
            Log.e(TAG, t.getMessage());
        }

        if (Utils.isAppOnForeground(this)) {
            Intent intent_broadcast = new Intent(Constant.ACTION_PUSH);
            intent_broadcast.putExtra("title", title);
            intent_broadcast.putExtra("body", body);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent_broadcast);
            return;
        }



//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra("from_notification", true);
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        String notificaiton_sound = "";
//        List<NotificationToneItem> mNotificationItemList = SharedStorage.getInstance().notificationToneItemList;
//        if(mNotificationItemList != null && mNotificationItemList.size() > 0) {
//            for (int index = 0; index < mNotificationItemList.size(); index++) {
//                if (mNotificationItemList.get(index).isSelected()) {
//                    notificaiton_sound = mNotificationItemList.get(index).getTone_uri();
//                    break;
//                }
//            }
//            Log.d(TAG, "this is set notification sound");
//            notificationBuilder =
//                    new NotificationCompat.Builder(this, channelId)
//                            .setSmallIcon(R.drawable.ic_stat_notification)
//                            .setAutoCancel(true)
//                            .setContentIntent(pendingIntent)
//                            .setSound(Uri.parse(notificaiton_sound));
//        } else {
//            Log.d(TAG, "this is empty notification");
//            notificationBuilder =
//                    new NotificationCompat.Builder(this, channelId)
//                            .setSmallIcon(R.drawable.ic_stat_notification)
//                            .setAutoCancel(true)
//                            .setContentIntent(pendingIntent);
//        }
//
//        notificationBuilder.setContentTitle(title);
//        notificationBuilder.setContentText(body);
//
//        // Notification ID
//        int notiId = ++lastNotificationId;
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelId, channelId,
//                    NotificationManager.IMPORTANCE_DEFAULT);
//
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        notificationManager.notify(notiId /* ID of notification */, notificationBuilder.build());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d(TAG, "Refreshed token: " + token);
        RestApi.sendRegistrationToServer(token);
    }

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {

    }

    private void saveMessage(NotificationItem notificationItem) {
        SharedPreferences pref = getSharedPreferences(Constant.PREF_ALERT, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = pref.getString(Constant.PREF_ALERT, "");
        Type type = new TypeToken<List<NotificationItem>>(){}.getType();
        List<NotificationItem> notificationItemList = gson.fromJson(json, type);
        if(notificationItemList == null) {
            notificationItemList = new ArrayList();
        }
        notificationItemList.add(notificationItem);

        SharedPreferences.Editor prefsEditor = pref.edit();
        json = gson.toJson(notificationItemList);
        prefsEditor.putString(Constant.PREF_ALERT, json);
        prefsEditor.commit();

    }

//    private void saveMessage(String title, String message) {
//        SharedPreferences pref = getSharedPreferences(Constant.PREF_ALERT, Context.MODE_PRIVATE);
//
//        String json = pref.getString(Constant.PREF_KEY_ALERT, "[]");
//
//        try {
//            JSONArray jarr = new JSONArray(json);
//
//            JSONObject jo = new JSONObject();
//            jo.put("title", title);
//            jo.put("message", message);
//            jo.put("timestamp", (new Date()).getTime());
//
//            jarr.put(jo);
//
//            Log.d(TAG, "Alerts count: " + jarr.length());
//
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putString(Constant.PREF_KEY_ALERT, jarr.toString());
//            editor.commit();
//        } catch (Exception e) {
//            Log.e(TAG, "Exception", e);
//        }
//    }

}