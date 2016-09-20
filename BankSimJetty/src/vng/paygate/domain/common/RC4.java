/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vng.paygate.domain.common;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import vng.paygate.exception.TechniqueException;

/**
 *
 * @author CPU01661-local
 */
public class RC4 {

    protected static final String ALGORITHM = "RC4";
    private static final String sCR = new String(new byte[]{13});
    private static final String sNL = new String(new byte[]{10});

    public static SecretKey generateKey(String secretKeyText) {
        SecretKey key = new SecretKeySpec(secretKeyText.getBytes(), "RC4");
        return key;
    }

    public static String getKeyAsString(Key key) {
        byte[] keyBytes = key.getEncoded();

        String sKey = Base64.encodeBase64String(keyBytes);
        return sKey.replace(sCR, "").replace(sNL, "");
    }

    public static String encrypt(String text, SecretKey key)
            throws TechniqueException, UnsupportedEncodingException {
        byte[] cipherText = null;
        try {
            cipherText = encrypt(text.getBytes("UTF8"), key);
        } catch (UnsupportedEncodingException ex) {
            throw new TechniqueException("Encrypt RC fail", ex);
        }
        String encryptedText = Base64.encodeBase64String(cipherText);
        encryptedText = encryptedText.replace(sCR, "").replace(sNL, "");
        return encryptedText;
    }

    public static byte[] encrypt(byte[] text, SecretKey key)
            throws TechniqueException {
        byte[] cipherText = null;
        try {
            Cipher cipher = Cipher.getInstance("RC4");

            cipher.init(1, key);
            cipherText = cipher.doFinal(text);
        } catch (Exception e) {
            throw new TechniqueException("Can't encrypt message using RC4", e);
        }
        return cipherText;
    }

    public static byte[] decrypt(byte[] text, SecretKey key)
            throws TechniqueException {
        byte[] dectyptedText = null;
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(2, key);
            dectyptedText = cipher.doFinal(text);
        } catch (Exception e) {
            e.printStackTrace();
            throw new TechniqueException("Can't decrypt message using RC4", e);
        }
        return dectyptedText;
    }

    public static String decrypt(String text, SecretKey key)
            throws TechniqueException {
        String result;
        byte[] dectyptedText = decrypt(Base64.decodeBase64(text), key);
        try {
            result = new String(dectyptedText, "UTF8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            throw new TechniqueException("Decrypt RC fail", ex);
        }

        return result;
    }

    public static void main(String[] args) {
        try {
            SecretKey key = generateKey("This is 16 bytes");

            String plainText = "9704310000373020=TRUONG THI THU HIEN";
            System.out.println("plainText: " + plainText);

            String encrypted = encrypt(plainText, key);
            System.out.println("encrypted: " + encrypted);
            String decrypted = decrypt(encrypted, key);

            System.out.println("decrypted: " + decrypted);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

}
