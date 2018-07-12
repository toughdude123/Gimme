package com.kalabhedia.gimme;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;


public class MessageRecieverService extends FirebaseMessagingService {
    private static final int REQUEST_CODE = 1;
    DataBaseHelper db;
    private static String id = "";

    public static String getId() {
        return id;
    }

    public MessageRecieverService() {
        super();
    }

    public static boolean isAppSentToBackground(final Context context) {

        try {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            // The first in the list of RunningTasks is always the foreground
            // task.
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            String foregroundTaskPackageName = foregroundTaskInfo.topActivity
                    .getPackageName();// get the top fore ground activity
            PackageManager pm = context.getPackageManager();
            PackageInfo foregroundAppPackageInfo = pm.getPackageInfo(
                    foregroundTaskPackageName, 0);

            String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo
                    .loadLabel(pm).toString();

            if (!foregroundTaskAppName.equals("Gimme")) {
                return true;
            }
        } catch (Exception e) {
            Log.e("isAppSentToBackground", "" + e);
        }
        return false;
    }

    /**
     * @param title
     * @param msg
     * @param phoneNumber
     * @param timeStamp
     * @param reason
     */
    private void showNotifications(String title, String msg, String phoneNumber, String timeStamp, String reason) {
        Intent i = new Intent(this, MainActivity.class);
        int uniqueId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        id += uniqueId + " ";
        String moneyString = msg.split(" ")[0];
        db = new DataBaseHelper(this);
        db.getWritableDatabase();
        Boolean result = db.insertData(timeStamp, phoneNumber, reason, moneyString, "0", "1");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE,
                i, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intent = new Intent(this, NotificationBroadCastReceiver.class);
        intent.putExtra("Button clicked", "accept");
        intent.putExtra("notificationID", uniqueId);
        intent.putExtra("TimeStamp", timeStamp);
        PendingIntent accept = PendingIntent.getBroadcast(this, uniqueId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        intent.putExtra("Button clicked", "declined");
        PendingIntent decline = PendingIntent.getBroadcast(this, uniqueId + 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);



        String CHANNEL_ID = "channel_money_request";// The id of the channel.
        CharSequence name = getString(R.string.channel_name);// The user-visible name of the channel.

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.accept, "Previous", pendingIntent).build();
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mNotificationManager.createNotificationChannel(mChannel);
            Notification notification1 = new Notification.Builder(this, CHANNEL_ID)
                    .setContentText(msg)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.decline, "Accept", accept)
                    .addAction(R.drawable.accept, "Decline", decline)
                    .setSmallIcon(R.drawable.notif_icon)
                    .setGroup("Gimme")
                    .setAutoCancel(true)
                    .build();
            mNotificationManager.notify(uniqueId, notification1);
        } else {
            Notification notification = new Notification.Builder(this)
                    .setContentText(msg)
                    .setContentTitle(title)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.notif_icon)
                    .setAutoCancel(true)
                    .addAction(R.drawable.accept, "Accept", accept)
                    .addAction(R.drawable.decline, "Decline", decline)
                    .setGroup("Gimme")
                    .build();
            mNotificationManager.notify(uniqueId, notification);

            if (!MessageRecieverService.isAppSentToBackground(getApplicationContext())) {
                NotificationManagerCompat.from(getApplicationContext()).cancel(uniqueId);
            }


        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.w("onMessageReceived: ", remoteMessage.getData().get("title"));
        if (!isAppSentToBackground(getApplicationContext())) {
            Intent gcm_rec = new Intent("your_action");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(gcm_rec);
        }
        final String title = remoteMessage.getData().get("title");
        String messageReceived = remoteMessage.getData().get("body");
        int location = messageReceived.indexOf("for");
        String reason = "";
        if (location != -1) {
            String temp = "";
            String[] tempArray = messageReceived.substring(location).split(" ");
            for (int i = 1; i < tempArray.length - 1; i++) {
                reason += tempArray[i] + " ";
            }
            temp = tempArray[tempArray.length - 1];
            messageReceived = messageReceived.substring(0, location) + temp;
        }
        String phoneNumber = "";
        String message = "";
        String[] checkingPhoneNumber = messageReceived.split(" ");
        String timeStamp = checkingPhoneNumber[checkingPhoneNumber.length - 1];
        int i;
        for (i = checkingPhoneNumber.length - 2; i > 0; i--) {
            if (checkingPhoneNumber[i].charAt(0) == '+') {
                phoneNumber = checkingPhoneNumber[i] + phoneNumber;
                break;
            } else {
                phoneNumber = checkingPhoneNumber[i] + phoneNumber;
            }
        }


        int j;
        for (j = 0; j < i; j++) {
            message += checkingPhoneNumber[j] + " ";
        }

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("Gimme", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(phoneNumber, null);
        if (name == null) {
            message += " " + phoneNumber;
        } else {
            message += " " + name;
        }
        message = message + " " + reason;
        showNotifications(title, message, phoneNumber, timeStamp, reason);
    }
}

