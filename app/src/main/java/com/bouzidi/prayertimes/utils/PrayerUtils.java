package com.bouzidi.prayertimes.utils;

import com.bouzidi.prayertimes.timings.PrayerEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;

public class PrayerUtils {

    public static PrayerEnum getNextPrayer(@NonNull Map<PrayerEnum, LocalDateTime> prayers, @NonNull LocalDateTime currentTime) {
        if (TimingUtils.isBetweenTiming(Objects.requireNonNull(prayers.get(PrayerEnum.MAGHRIB)), currentTime, Objects.requireNonNull(prayers.get(PrayerEnum.ICHA)))) {
            return PrayerEnum.ICHA;
        }
        if (TimingUtils.isBetweenTiming(Objects.requireNonNull(prayers.get(PrayerEnum.ASR)), currentTime, Objects.requireNonNull(prayers.get(PrayerEnum.MAGHRIB)))) {
            return PrayerEnum.MAGHRIB;
        }
        if (TimingUtils.isBetweenTiming(Objects.requireNonNull(prayers.get(PrayerEnum.DHOHR)), currentTime, Objects.requireNonNull(prayers.get(PrayerEnum.ASR)))) {
            return PrayerEnum.ASR;
        }
        if (TimingUtils.isBetweenTiming(Objects.requireNonNull(prayers.get(PrayerEnum.FAJR)), currentTime, Objects.requireNonNull(prayers.get(PrayerEnum.DHOHR)))) {
            return PrayerEnum.DHOHR;
        }
        return PrayerEnum.FAJR;
    }

    public static PrayerEnum getPreviousPrayerKey(PrayerEnum key) {
        switch (key) {
            case FAJR:
                return PrayerEnum.ICHA;
            case DHOHR:
                return PrayerEnum.FAJR;
            case ASR:
                return PrayerEnum.DHOHR;
            case MAGHRIB:
                return PrayerEnum.ASR;
            default:
                return PrayerEnum.MAGHRIB;
        }
    }

    private static boolean isIchaAfterMidnight(@NonNull Map<PrayerEnum, String> prayers) {
        return TimingUtils.isBeforeOnSameDay(Objects.requireNonNull(prayers.get(PrayerEnum.ICHA)), Objects.requireNonNull(prayers.get(PrayerEnum.DHOHR)));
    }

    private static boolean isMaghribAfterMidnight(@NonNull Map<PrayerEnum, String> prayers) {
        return TimingUtils.isBeforeOnSameDay(Objects.requireNonNull(prayers.get(PrayerEnum.MAGHRIB)), Objects.requireNonNull(prayers.get(PrayerEnum.DHOHR)));
    }


}
