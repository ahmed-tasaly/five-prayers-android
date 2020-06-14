package com.bouzidi.prayertimes.notifier;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bouzidi.prayertimes.MainActivity;
import com.bouzidi.prayertimes.R;

import static android.content.Context.MODE_PRIVATE;

class PrayerNotification {

    private PrayerNotification() {
    }

    static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.adthan_notification_channel_name);
            String description = context.getString(R.string.adthan_notification_channel_description);
            String id = context.getString(R.string.adthan_notification_channel_id);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    static void createNotification(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notificationId", 0);
        String prayerTiming = intent.getStringExtra("prayerTiming");
        String prayerKey = intent.getStringExtra("prayerKey");

        String prayerName = context.getResources().getString(
                context.getResources().getIdentifier(prayerKey,
                        "string", context.getPackageName()));

        PendingIntent pendingIntent = getNotificationIntent(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ADHAN_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_mosque_24dp)
                .setContentTitle("Prayer Time !")
                .setAutoCancel(true)
                .setDeleteIntent(createOnDismissedIntent(context, notificationId))
                .setContentIntent(pendingIntent)
                .setContentText(prayerName + " : " + prayerTiming);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        setupAdhanCall(context, prayerKey);

        notificationManager.notify(notificationId, builder.build());
    }

    private static PendingIntent getNotificationIntent(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
    }

    private static PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("notificationId", notificationId);

        return PendingIntent.getBroadcast(context.getApplicationContext(),
                notificationId, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    private static void setupAdhanCall(Context context, String prayerKey) {
        String adhanCallsPreferences = context.getResources().getString(R.string.adthan_calls_shared_preferences);
        String adhanCallKeyPart = context.getResources().getString(R.string.adthan_call_enabled_key);
        String callPreferenceKey = prayerKey + adhanCallKeyPart;

        final SharedPreferences sharedPreferences = context.getSharedPreferences(adhanCallsPreferences, MODE_PRIVATE);
        boolean callEnabled = sharedPreferences.getBoolean(callPreferenceKey, true);

        if (callEnabled) {
            AdhanPlayer.getInstance(context).playAdhan();
        }
    }
}
