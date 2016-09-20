/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Kuti
 */
public class DateUtils {

    public static Date getDate(String sDate) {
        Date date = new Date();
        if (sDate != null) {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
            try {
                date = df.parse(sDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    public static Date getDate(String sDate, String format) {
        Date date = new Date();
        if (sDate != null) {
            DateFormat df = new SimpleDateFormat(format);
            try {
                date = df.parse(sDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    public static String getDate(Date date, String format) {
        if ((date == null) || (StringUtils.isEmpty(format))) {
            return "";
        }
        DateFormat df = new SimpleDateFormat(format);
        String sDate = "";
        sDate = df.format(date);

        return sDate;
    }

    public static String getExpiredDate(Date date) {
        if (date == null) {
            return "";
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(2) + 1;
        int year = calendar.get(1);
        String expDate = Integer.toString(year).substring(2) + NumberUtils.getNumberInFormat(Integer.valueOf(month), "00");

        return expDate;
    }

    public static boolean checkDateFormat(String inDate, String format) {
        if (inDate == null) {
            return false;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        if (inDate.trim().length() != dateFormat.toPattern().length()) {
            return false;
        }
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
