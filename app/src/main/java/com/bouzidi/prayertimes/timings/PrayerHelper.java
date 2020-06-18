package com.bouzidi.prayertimes.timings;

import android.content.Context;
import android.util.Log;

import com.bouzidi.prayertimes.database.PrayerRegistry;
import com.bouzidi.prayertimes.exceptions.TimingsException;
import com.bouzidi.prayertimes.timings.aladhan.AladhanAPIService;
import com.bouzidi.prayertimes.timings.aladhan.AladhanCalendarResponse;
import com.bouzidi.prayertimes.timings.aladhan.AladhanTodayTimingsResponse;
import com.bouzidi.prayertimes.utils.TimingUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class PrayerHelper {

    public static Single<DayPrayer> getTimingsByCity(final LocalDate localDate, final String city, final String country,
                                                     final CalculationMethodEnum method, final Context context) {

        final PrayerRegistry prayerRegistry = PrayerRegistry.getInstance(context);

        return Single.create(emitter -> {
            Thread thread = new Thread(() -> {

                DayPrayer prayerTimings;

                if (localDate == null || city == null || country == null) {
                    Log.e(PrayerHelper.class.getName(), "Cannot find timings with null attribute");
                    emitter.onError(new TimingsException("Cannot find timings with null attributes"));
                } else {
                    prayerTimings = prayerRegistry.getPrayerTimings(TimingUtils.formatDateForAdhanAPI(localDate), city, method);

                    if (prayerTimings != null) {
                        emitter.onSuccess(prayerTimings);
                    } else {
                        try {
                            AladhanAPIService aladhanAPIService = AladhanAPIService.getInstance();
                            AladhanTodayTimingsResponse timingsByCity = aladhanAPIService.getTimingsByCity(localDate, city, country, method, context);
                            prayerRegistry.savePrayerTiming(city, country, method, timingsByCity.getData());
                            prayerTimings = prayerRegistry.getPrayerTimings(TimingUtils.formatDateForAdhanAPI(localDate), city, method);

                            emitter.onSuccess(prayerTimings);

                        } catch (IOException e) {
                            Log.e(PrayerHelper.class.getName(), "Cannot find from aladhanAPIService");
                            emitter.onError(e);
                        }
                    }
                }
            });
            thread.start();
        });
    }

    public static Single<List<DayPrayer>> getCalendarByCity(final String city, final String country, int month, int year,
                                                            final CalculationMethodEnum method, final Context context) {


        final PrayerRegistry prayerRegistry = PrayerRegistry.getInstance(context);

        return Single.create(emitter -> {
            Thread thread = new Thread(() -> {

                Log.e("prayer helper start", LocalDateTime.now().toString());

                List<DayPrayer> prayerCalendar;

                if (city == null || country == null) {
                    Log.e(PrayerHelper.class.getName(), "Cannot find calendar with null attribute");
                    emitter.onError(new TimingsException("Cannot find calendar with null attributes"));
                } else {
                    prayerCalendar = prayerRegistry.getPrayerCalendar(city, month, year, method);

                    if (prayerCalendar.size() > 0) {
                        emitter.onSuccess(prayerCalendar);
                    } else {
                        try {
                            AladhanAPIService aladhanAPIService = AladhanAPIService.getInstance();
                            AladhanCalendarResponse calendarByCity = aladhanAPIService.getCalendarByCity(city, country, month, year, method, context);
                            prayerRegistry.saveCalendar(city, country, method, calendarByCity);

                            prayerCalendar = prayerRegistry.getPrayerCalendar(city, month, year, method);

                            emitter.onSuccess(prayerCalendar);
                        } catch (IOException e) {
                            Log.e(PrayerHelper.class.getName(), "Cannot find calendar from aladhanAPIService");
                            emitter.onError(e);
                        }
                    }
                }
                Log.e("prayer helper end", LocalDateTime.now().toString());
            });
            thread.start();
        });
    }

    public static Single<DayPrayer> fetchTimingsByCity(final LocalDate localDate, final String city, final String country,
                                                       final CalculationMethodEnum method, final Context context) {

        final PrayerRegistry prayerRegistry = PrayerRegistry.getInstance(context);

        return Single.create(emitter -> {
            Thread thread = new Thread(() -> {

                DayPrayer prayerTimings;

                try {
                    AladhanAPIService aladhanAPIService = AladhanAPIService.getInstance();
                    AladhanTodayTimingsResponse timingsByCity = aladhanAPIService.getTimingsByCity(localDate, city, country, method, context);
                    prayerRegistry.savePrayerTiming(city, country, method, timingsByCity.getData());
                    prayerTimings = prayerRegistry.getPrayerTimings(TimingUtils.formatDateForAdhanAPI(localDate), city, method);

                    emitter.onSuccess(prayerTimings);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        });
    }
}
