package com.hbouzidi.fiveprayers.notifier;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hbouzidi.fiveprayers.common.PrayerEnum;
import com.hbouzidi.fiveprayers.timings.DayPrayer;
import com.hbouzidi.fiveprayers.utils.TimingUtils;
import com.hbouzidi.fiveprayers.utils.UiUtils;

import java.time.LocalDateTime;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * @author Hicham Bouzidi Idrissi
 * Github : https://github.com/Five-Prayers/five-prayers-android
 * licenced under GPLv3 : https://www.gnu.org/licenses/gpl-3.0.en.html
 */
@Singleton
public class PrayerAlarmScheduler {

    public static final String TAG = "PrayerAlarmScheduler";
    private final Context context;

    @Inject
    public PrayerAlarmScheduler(Context context) {
        this.context = context;
    }

    public void scheduleNextPrayerAlarms(@NonNull DayPrayer dayPrayer) {
        Log.i(TAG, "Start scheduling Alarm for: " + dayPrayer.getDate());

        Map<PrayerEnum, LocalDateTime> timings = dayPrayer.getTimings();

        int index = 0;
        for (PrayerEnum key : timings.keySet()) {
            index++;

            LocalDateTime timing = timings.get(key);

            if (timing != null && LocalDateTime.now().isBefore(timing)) {
                Log.i(TAG, "Scheduling " + key.toString() + " Alarm at : " + TimingUtils.formatTiming(timing));

                AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, NotifierReceiver.class);
                intent.putExtra("prayerKey", key.toString());
                intent.putExtra("prayerTiming", UiUtils.formatTiming(timing));
                intent.putExtra("prayerCity", dayPrayer.getCity());
                intent.putExtra("notificationId", 1000);

                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, index, intent, FLAG_UPDATE_CURRENT);
                alarmMgr.cancel(alarmIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, TimingUtils.getTimeInMilliIgnoringSeconds(timing), alarmIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmMgr.setExact(AlarmManager.RTC_WAKEUP, TimingUtils.getTimeInMilliIgnoringSeconds(timing), alarmIntent);
                } else {
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, TimingUtils.getTimeInMilliIgnoringSeconds(timing), alarmIntent);
                }
            }
        }

        Log.i(TAG, "End scheduling Alarm for: " + dayPrayer.getDate());
    }
}
