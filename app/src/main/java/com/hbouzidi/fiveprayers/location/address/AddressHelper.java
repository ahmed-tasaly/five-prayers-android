package com.hbouzidi.fiveprayers.location.address;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.hbouzidi.fiveprayers.R;
import com.hbouzidi.fiveprayers.exceptions.LocationException;
import com.hbouzidi.fiveprayers.location.osm.NominatimAPIService;
import com.hbouzidi.fiveprayers.location.osm.NominatimReverseGeocodeResponse;
import com.hbouzidi.fiveprayers.preferences.PreferencesHelper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.rxjava3.core.Single;

/**
 * @author Hicham Bouzidi Idrissi
 * Github : https://github.com/Five-Prayers/five-prayers-android
 * licenced under GPLv3 : https://www.gnu.org/licenses/gpl-3.0.en.html
 */
public class AddressHelper {

    private static final int MINIMUM_DISTANCE_FOR_OBSOLESCENCE = 1000; //1KM

    public static Single<Address> getAddressFromLocation(final Location location,
                                                         final Context context) {

        return Single.create(emitter -> {
            boolean locationSetManually = PreferencesHelper.isLocationSetManually(context);
            if (locationSetManually) {
                Address lastKnownAddress = PreferencesHelper.getLastKnownAddress(context);
                emitter.onSuccess(lastKnownAddress);
            } else if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                Address lastKnownAddress = PreferencesHelper.getLastKnownAddress(context);

                if (!isAddressObsolete(lastKnownAddress, latitude, longitude)) {
                    emitter.onSuccess(lastKnownAddress);
                } else {
                    Thread thread = new Thread(() -> {
                        try {
                            Address geocoderAddresses = getGeocoderAddresses(latitude, longitude, context);
                            if (geocoderAddresses != null) {
                                emitter.onSuccess(geocoderAddresses);
                            } else if (getNominatimAddress(latitude, longitude, context) != null) {
                                emitter.onSuccess(Objects.requireNonNull(getNominatimAddress(latitude, longitude, context)));
                            } else if (lastKnownAddress.getLocality() != null) {
                                emitter.onSuccess(lastKnownAddress);
                            } else {
                                Log.e(AddressHelper.class.getName(), "Unable connect to get address");
                                emitter.onError(new LocationException(context.getResources().getString(R.string.enable_to_reverse_geolocalisation)));
                            }
                        } catch (Exception e) {
                            if (lastKnownAddress.getLocality() != null) {
                                Log.i(AddressHelper.class.getName(), "Unable connect to get address from API, return last known", e);
                                emitter.onSuccess(lastKnownAddress);
                            } else {
                                Log.e(AddressHelper.class.getName(), "Unable connect to get address from API", e);
                                emitter.onError(new LocationException(context.getResources().getString(R.string.enable_to_reverse_geolocalisation)));
                            }
                        }
                    });
                    thread.start();
                }
            } else {
                Log.e(AddressHelper.class.getName(), "Location is null");
                emitter.onError(new LocationException(context.getResources().getString(R.string.location_service_unavailable)));
            }
        });
    }

    private static Address getGeocoderAddresses(double latitude, double longitude, Context context) throws IOException {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

        if (addressList != null && addressList.size() > 0) {
            Address address = addressList.get(0);

            if (address.getCountryName() != null && address.getLocality() != null) {
                PreferencesHelper.updateAddressPreferences(context, address);
                return address;
            }
            return null;
        }
        return null;
    }

    private static Address getNominatimAddress(double latitude, double longitude, Context context) throws IOException {
        NominatimAPIService nominatimAPIService = NominatimAPIService.getInstance();

        NominatimReverseGeocodeResponse response = nominatimAPIService.getAddressFromLocation(latitude, longitude);

        if (response != null && response.getAddress() != null && response.getAddress().getCountry() != null && response.getAddress().getLocality() != null) {
            Address address = new Address(Locale.getDefault());
            address.setCountryName(response.getAddress().getCountry());
            address.setCountryCode(response.getAddress().getCountryCode());
            address.setAddressLine(1, response.getAddress().getState());
            address.setLocality(response.getAddress().getLocality());
            address.setPostalCode(response.getAddress().getPostcode());
            address.setLatitude(response.getLat());
            address.setLongitude(response.getLon());

            PreferencesHelper.updateAddressPreferences(context, address);

            return address;
        }

        return null;
    }

    private static boolean isAddressObsolete(Address lastKnownAddress, double latitude, double longitude) {
        if (lastKnownAddress.getLocality() != null) {

            Location LastKnownLocation = new Location("");
            LastKnownLocation.setLatitude(lastKnownAddress.getLatitude());
            LastKnownLocation.setLongitude(lastKnownAddress.getLongitude());

            Location newLocation = new Location("");
            newLocation.setLatitude(latitude);
            newLocation.setLongitude(longitude);

            float distance = LastKnownLocation.distanceTo(newLocation);
            return distance > MINIMUM_DISTANCE_FOR_OBSOLESCENCE;
        }
        return true;
    }
}
