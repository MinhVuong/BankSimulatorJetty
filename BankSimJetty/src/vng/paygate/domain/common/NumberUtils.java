/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.common;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Pattern;
import vng.paygate.exception.TechniqueException;

/**
 *
 * @author Kuti
 */
public class NumberUtils {

    private static final Logger log = LoggerFactory.getLogger(NumberUtils.class);

    public static String getNumberInFormat(Integer autoNumber, String format) {
        if ((autoNumber == null) || (StringUtils.isEmpty(format))) {
            return "";
        }
        DecimalFormat nf = new DecimalFormat(format);
        return nf.format(autoNumber);
    }

    public static String getNumberInFormat(Object autoNumber, String format)
            throws TechniqueException {
        if ((autoNumber == null) || (StringUtils.isEmpty(format))) {
            return "";
        }
        try {
            DecimalFormat nf = new DecimalFormat(format);
            return nf.format(autoNumber);
        } catch (IllegalArgumentException ex) {
            throw new TechniqueException("Can't parse " + autoNumber, ex);
        }
    }

    public static boolean isInteger(String number) {
        if (StringUtils.isBlank(number)) {
            return false;
        }
        try {
            Long localLong = Long.valueOf(Long.parseLong(number));
        } catch (NumberFormatException ex) {
            log.error(null, ex);
            return false;
        }
        return true;
    }

    public static boolean isIPFormat(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        String regexp = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        Pattern pattern = Pattern.compile(regexp);

        Matcher matcher = pattern.matcher(ip);

        return matcher.matches();
    }

    public static String intToIP(int i) {
        return (i >> 24 & 0xFF) + "." + (i >> 16 & 0xFF) + "." + (i >> 5 & 0xFF) + "." + (i & 0xFF);
    }

    public static Integer ipToInt(String addr) {
        String[] addrArray = addr.split("\\.");
        int num = 0;
        for (int i = 0; i < addrArray.length; i++) {
            int power = 3 - i;
            num = (int) (num + Integer.parseInt(addrArray[i]) % 256 * Math.pow(256.0D, power));
        }
        return Integer.valueOf(num);
    }

    public static boolean checkAccountName(String accountName) {
        if ((StringUtils.isBlank(accountName)) || (accountName.length() > 32) || (accountName.length() < 4)) {
            return false;
        }
        if ((StringUtils.startsWith(accountName, ".")) || (StringUtils.startsWith(accountName, "_"))
                || (StringUtils.endsWith(accountName, ".")) || (StringUtils.endsWith(accountName, "_"))
                || (StringUtils.contains(accountName, "__")) || (StringUtils.contains(accountName, ".."))
                || (StringUtils.contains(accountName, "_.")) || (StringUtils.contains(accountName, "._"))) {
            return false;
        }
        if ((StringUtils.contains(accountName, ".")) || (StringUtils.contains(accountName, "_"))) {
            String accountTemp = StringUtils.remove(accountName, ".");
            accountTemp = StringUtils.remove(accountTemp, "_");
            if (StringUtils.isNumeric(accountTemp)) {
                return true;
            }
        }
        accountName = StringUtils.remove(accountName, ".");
        accountName = StringUtils.remove(accountName, "_");
        if ((!StringUtils.isAlphanumeric(accountName)) || (!StringUtils.isAsciiPrintable(accountName))) {
            return false;
        }
        return true;
    }

    public static boolean isPhoneNumber(String phoneNumber) {
        boolean isValid = false;

        String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";
        CharSequence inputStr = phoneNumber;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}
