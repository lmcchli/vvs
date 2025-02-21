/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class. Contains different functions to retrieve, format and convert time strings.
 * Uses the java.text.SimpleDateFormat class for the formatting of timestrings.
 * <p/>
 * The standard format (vvaformat) is: yyyy-MM-dd HH:mm:ss
 * Ex: 2005-11-10 14:33:32
 *
 * @author ermmaha
 */
public class TimeUtil {
    public static String VVA_DATEFORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    /**
     * Retrieves the current time. If the timezone parameter is present the time for that timezone is used.
     *
     * @param timezone
     * @return time string vvaformated
     */
    public static String getCurrentTime(String timezone) {
        Date time = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(VVA_DATEFORMAT_STRING);
        if (timezone != null) {
            // If timezone is null then time for the default timezone will be returned
            TimeZone zone = TimeZone.getTimeZone(timezone);
            simpleDateFormat.setTimeZone(zone);
        }
        return simpleDateFormat.format(time);
    }

    /**
     * Converts a vvaformated time from one timezone to another.
     *
     * @param vvaTime
     * @param fromTimezone
     * @param toTimezone
     * @return converted timestring
     */
    public static String convertTime(String vvaTime, String fromTimezone, String toTimezone) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(VVA_DATEFORMAT_STRING);
        if (fromTimezone != null) {
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(fromTimezone));
        }
        Date time = simpleDateFormat.parse(vvaTime);

        if (toTimezone != null) {
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(toTimezone));
        }
        return simpleDateFormat.format(time);
    }

    /**
     * Formats a vvatimestring to the format specified with the format parameter
     *
     * @param vvaTime
     * @param format
     * @return formated string
     */
    public static String formatTime(String vvaTime, String format) throws ParseException {
        Date time = parseVvaTime(vvaTime);

        // Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
        // represented by this Date object.
        if (format == null) return String.valueOf(time.getTime());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(time);
    }

    /**
     * Parses a vvaformated timestring into a java.util.Date object
     *
     * @param vvaTime to parse
     * @return new Date object
     */
    public static Date parseVvaTime(String vvaTime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(VVA_DATEFORMAT_STRING);
        return simpleDateFormat.parse(vvaTime);
    }

    /**
     * Converts a java.util.Date object into a vvaTime string.
     *
     * @param date
     * @return vvaTime string
     */
    public static String dateToVvaTime(Date date) {
        return dateToVvaTime(date, null);
    }

    /**
     * Converts a java.util.Date object into a vvaTime string.
     *
     * @param date
     * @param timezone
     * @return vvaTime string
     */
    public static String dateToVvaTime(Date date, String timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(VVA_DATEFORMAT_STRING);
        if (timezone != null) {
            //If timezone is null then time for the default timezone will be returned
            TimeZone zone = TimeZone.getTimeZone(timezone);
            simpleDateFormat.setTimeZone(zone);
        }
        return simpleDateFormat.format(date);
    }
    
    public static Date stringToDate (final String date)
    {
        final DateFormat dateFormat = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        try
        {
                return dateFormat.parse(date);
        }
        catch (ParseException pe)
        {
            pe.printStackTrace();
        }
        return null;
    }  
    

}
