package com.bouzidi.prayertimes.location.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.bouzidi.prayertimes.exceptions.LocationException;
import com.bouzidi.prayertimes.utils.UserPreferencesUtils;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.core.Single;

import static android.content.Context.MODE_PRIVATE;

public class LocationHelper {

    public static Single<Location> getLocation(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences("location", MODE_PRIVATE);

        final double lastKnownLatitude = UserPreferencesUtils.getDouble(sharedPreferences, "last_known_latitude", 0);
        final double lastKnownLongitude = UserPreferencesUtils.getDouble(sharedPreferences, "last_known_longitude", 0);

        GPSTracker gpsTracker = new GPSTracker(context);

        return Single.create(emitter -> {
            if (gpsTracker.canGetLocation()) {
                Location newLocation = gpsTracker.getLocation();

                if (newLocation != null) {
                    emitter.onSuccess(newLocation);

                    Log.i(LocationHelper.class.getName(), "Get location from tracker");
                } else if (lastKnownLatitude > 0 && lastKnownLongitude > 0) {

                    Location lastKnownLocation = getLocation(lastKnownLatitude, lastKnownLongitude);

                    emitter.onSuccess(lastKnownLocation);

                    Log.w(LocationHelper.class.getName(), "Cannot get location from tracker, use last known location");
                }
            } else if (lastKnownLatitude > 0 && lastKnownLongitude > 0) {

                Location lastKnownLocation = getLocation(lastKnownLatitude, lastKnownLongitude);

                emitter.onSuccess(lastKnownLocation);

                Log.w(LocationHelper.class.getName(), "Location tracker not available, use last known location");
            } else {
                gpsTracker.showSettingsAlert();

                emitter.onError(new LocationException("Unable to find location"));

                Log.e(LocationHelper.class.getName(), "Location tracker not available, use last known location");
            }
        });
    }

    @NotNull
    private static Location getLocation(double lastKnownLatitude, double lastKnownLongitude) {
        Location lastKnownLocation = new Location("");
        lastKnownLocation.setLatitude(lastKnownLatitude);
        lastKnownLocation.setLongitude(lastKnownLongitude);
        return lastKnownLocation;
    }
}