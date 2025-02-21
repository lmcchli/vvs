package com.mobeon.masp.chargingaccountmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class. Contains different functions to convert time strings and date objects.
 * <p/>
 * The UCIP format is yyyyMMdd'T'HH:mm:ssZ
 * Ex: 20081230T12:00:00+0100
 * <p/>
 *
 * @author emahagl
 */
public class DateUtil {
    private static String UCIP_DATEFORMAT_STRING = "yyyyMMdd'T'HH:mm:ssZ";

    /**
     * Returns a Date object form a VVA formatted time string.
     *
     * @param value
     * @return a Date parsed from the string
     * @throws ChargingAccountException
     */
    public static Date getDateFromValue(String value) throws ChargingAccountException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UCIP_DATEFORMAT_STRING);
        try {
            return simpleDateFormat.parse(value);
        } catch (ParseException e) {
            throw new ChargingAccountException(e);
        }
    }

    /**
     * Returns a VVA formatted string from a Date object
     *
     * @param date
     * @return a VVA formatted string
     */
    public static String getStringFromDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UCIP_DATEFORMAT_STRING);
        return simpleDateFormat.format(date);
    }
}
