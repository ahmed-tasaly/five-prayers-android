package com.bouzidi.prayertimes.ui.calendar;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bouzidi.prayertimes.location.address.AddressHelper;
import com.bouzidi.prayertimes.location.tracker.LocationHelper;
import com.bouzidi.prayertimes.timings.CalculationMethodEnum;
import com.bouzidi.prayertimes.timings.DayPrayer;
import com.bouzidi.prayertimes.timings.PrayerHelper;

import java.time.LocalDate;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CalendarViewModel extends AndroidViewModel {

    private final LocalDate todayDate;
    private MutableLiveData<List<DayPrayer>> mCalendar;
    private CompositeDisposable compositeDisposable;

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        mCalendar = new MutableLiveData<>();
        todayDate = LocalDate.now();

        setLiveData(application.getApplicationContext());
    }


    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

    LiveData<List<DayPrayer>> getCalendar() {
        return mCalendar;
    }


    private void setLiveData(Context context) {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(
                LocationHelper.getLocation(context)
                        .flatMap(location ->
                                AddressHelper.getAddressFromLocation(location, context)
                        ).flatMap(address ->
                        PrayerHelper.getCalendarByCity(
                                address.getLocality(),
                                address.getCountryName(),
                                todayDate.getMonthValue(),
                                todayDate.getYear(),
                                CalculationMethodEnum.getDefault(),
                                context
                        ))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<DayPrayer>>() {
                            @Override
                            public void onSuccess(List<DayPrayer> calendar) {
                                mCalendar.postValue(calendar);
                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                                //TODO
                                //mErrorMessage.setValue(e.getMessage());
                            }
                        }));
    }
}