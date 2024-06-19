package com.example.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public static String formatDateString(String dateStr) throws ParseException {
        Date date = inputDateFormat.parse(dateStr);
        return outputDateFormat.format(date);
    }

    public static Date parseDateString(String dateStr) throws ParseException {
        return inputDateFormat.parse(dateStr);
    }
}
