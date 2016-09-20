/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

/**
 *
 * @author CPU01661-local
 */
public class ChecksumHelper {
    private static final Logger log = LoggerFactory.getLogger(ChecksumHelper.class);
    public static final String ALGORITHM_MD5 = "MD5";
    public static final String ALGORITHM_SHA1 = "SHA-1";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String generateMD5(String message) {
        String checksum = "";
        if (StringUtils.isBlank(message)) {
            log.info("Message to create checksum empty");
            return checksum;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(message.getBytes("UTF8"));
            byte[] s = digest.digest();
            for (int i = 0; i < s.length; i++) {
                checksum = checksum + Integer.toHexString(0xFF & s[i] | 0xFF00).substring(6);
            }
            return checksum;
        } catch (NoSuchAlgorithmException ex) {
            log.error("Can't create checksum", ex);
            return checksum;
        } catch (UnsupportedEncodingException ex1) {
            log.error("Can't create checksum", ex1);
            return checksum;
        } 
    }

    public static String generateChecksum(String algorithm, String message) {
        String checksum = "";
        if (StringUtils.isBlank(message)) {
            log.info("Message to create checksum empty");
            return checksum;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(message.getBytes("UTF-8"));
            byte[] s = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < s.length; i++) {
                String hex = Integer.toHexString(0xff & s[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception ex) {
            log.error("Can't create checksum", ex);
            System.err.println("error checksum: " + ex.getMessage());
            return checksum;
        }
    }

    public static String generateHmacSha1Checksum(String data, String key) {
        String result = "";
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(data.getBytes());

            result = new BASE64Encoder().encode(rawHmac);
        } catch (Exception ex) {
            log.error("Can't create checksum", ex);
        }
        return result;
    }

    public static byte[] generateMD5Ext(String message) {
        String checksum = "";
        if (StringUtils.isBlank(message)) {
            log.info("Message to create checksum empty");
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(message.getBytes("ASCII"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            int j = 0;
            for (int k = 16; j < 8;) {
                keyBytes[(k++)] = keyBytes[(j++)];
            }
            return keyBytes;
        } catch (NoSuchAlgorithmException ex) {
            log.error("Can't create checksum", ex);
        } catch (UnsupportedEncodingException ex1) {
            log.error("Can't create checksum", ex1);
        }
        return null;
    }

    public static void main(String[] s) {
        String key = "a";

        String data = "pmqcADD_CASH";
        System.out.println("SLDFKSD : " + generateMD5(new StringBuilder().append(data).append(key).toString()));
    }
}
