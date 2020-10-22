package com.hbouzidi.fiveprayers.notifier;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.VolumeProviderCompat;

import com.hbouzidi.fiveprayers.R;
import com.hbouzidi.fiveprayers.preferences.PreferencesConstants;
import com.hbouzidi.fiveprayers.timings.PrayerEnum;
import com.hbouzidi.fiveprayers.ui.MainActivity;

import static android.content.Context.MODE_PRIVATE;

class PrayerNotification {

    private PrayerNotification() {
    }

    static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = NotifierConstants.ADTHAN_NOTIFICATION_CHANNEL_NAME;
            String description = NotifierConstants.ADTHAN_NOTIFICATION_CHANNEL_DESCRIPTION;
            String id = NotifierConstants.ADTHAN_NOTIFICATION_CHANNEL_ID;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

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
        String prayerCity = intent.getStringExtra("prayerCity");

        String prayerName = context.getResources().getString(
                context.getResources().getIdentifier(prayerKey,
                        "string", context.getPackageName()));

        PendingIntent pendingIntent = getNotificationIntent(context);

        String closeActionTitle = context.getResources().getString(R.string.adthan_notification_close_action_title);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotifierConstants.ADTHAN_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_mosque_24dp)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setContentTitle(context.getString(R.string.adthan_notification_title))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(prayerName + " : " + prayerTiming + " (" + prayerCity + ")"))
                .addAction(R.drawable.ic_notifications_24dp, closeActionTitle, getCloseNotificationActionIntent(notificationId, context))
                .setDeleteIntent(createOnDismissedIntent(context, notificationId))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(notificationId, builder.build());

        createVibration(context);

        setupAdhanCall(context, prayerKey);
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
        intent.setClass(context, NotificationDismissedReceiver.class);
        intent.putExtra("notificationId", notificationId);

        return PendingIntent.getBroadcast(context.getApplicationContext(),
                notificationId, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    private static void setupAdhanCall(Context context, String prayerKey) {
        String adhanCallKeyPart = PreferencesConstants.ADTHAN_CALL_ENABLED_KEY;
        String callPreferenceKey = prayerKey + adhanCallKeyPart;

        final SharedPreferences sharedPreferences = context.getSharedPreferences(PreferencesConstants.ADTHAN_CALLS_SHARED_PREFERENCES, MODE_PRIVATE);
        boolean callEnabled = sharedPreferences.getBoolean(callPreferenceKey, false);

        if (callEnabled) {
            AdhanPlayer.getInstance().playAdhan(context, PrayerEnum.FAJR.toString().equals(prayerKey));
            setMediaSession(context);
        }
    }

    private static void setMediaSession(Context context) {
        MediaSessionCompat mediaSession = new MediaSessionCompat(context, "PrayerNotification");
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
                .build());

        VolumeProviderCompat myVolumeProvider =
                new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE, 100, 50) {
                    @Override
                    public void onAdjustVolume(int direction) {
                        if (direction == -1) {
                            AdhanPlayer.getInstance().stopAdhan();
                        }
                        mediaSession.release();
                    }
                };
        mediaSession.setPlaybackToRemote(myVolumeProvider);
        mediaSession.setActive(true);

        AdhanPlayer.getInstance().setOnCompletionListener(mp -> mediaSession.release());
    }

    private static void createVibration(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = new long[]{0, 1000, 500, 1000, 500, 500, 500};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1),
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build());
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }

    private static PendingIntent getCloseNotificationActionIntent(int notificationId, Context context) {
        Intent intentAction = new Intent(context, NotifierActionReceiver.class);

        intentAction.setAction(NotifierConstants.ADTHAN_NOTIFICATION_CANCEL_ADHAN_ACTION);
        intentAction.putExtra("notificationId", notificationId);
        intentAction.setClass(context, NotifierActionReceiver.class);

        return PendingIntent.getBroadcast(context, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}