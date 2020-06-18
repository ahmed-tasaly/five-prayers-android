package com.bouzidi.prayertimes.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UiUtils {

    private static final String GREGORIAN_MONTH_YEAR_FORMAT = "MMMM yyyy";
    private static final String GREGORIAN_READABLE_FORMAT = "EEE dd MMMM, yyyy";
    private static final String TIME_ZONE_READABLE_FORMAT = "ZZZZZ";

    public static String formatTimeForTimer(long time) {
        long seconds = time / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return "- " + String.format(Locale.getDefault(), "%1$02d", Math.abs(hours)) + ":" + String.format(Locale.getDefault(), "%1$02d", Math.abs(minutes % 60)) + ":" + String.format(Locale.getDefault(), "%1$02d", Math.abs(seconds % 60));
    }

    public static String formatShortDate(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(GREGORIAN_MONTH_YEAR_FORMAT, Locale.getDefault());
        return localDate.format(formatter);
    }

    public static String formatReadableGregorianDate(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(GREGORIAN_READABLE_FORMAT, Locale.getDefault());
        return localDate.format(formatter);
    }

    public static String formatReadableTimezone(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_ZONE_READABLE_FORMAT, Locale.getDefault());
        return localDate.format(formatter);
    }

    public static String formatHijriDate(int day, String monthName, int year) {
        return String.format(Locale.getDefault(), "%1$02d", day) + " " + monthName + ", " + year;
    }

    public static String formatShortHijriDate(int day, String monthName) {
        return String.format(Locale.getDefault(), "%1$02d", day) + " " + monthName;
    }
}
