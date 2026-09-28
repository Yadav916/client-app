package com.infospica.dicom.util;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Security {

    private static final Random RANDOM = new SecureRandom();

    private static final String KEY_SPEC = "AES";
    private static final String CIPHER_CONFIGURATION = "AES/CBC/PKCS5Padding";

    private Security() {
        super();
    }

    public static boolean hashMatches(String hashedPassword, String passwordToHash) throws Exception {
        if (hashedPassword == null || passwordToHash == null) {
            throw new Exception("Password cannot be empty.");
        }

        return hashedPassword.equals(encodeString(passwordToHash))
                || hashedPassword.equals(encodeStringSHA1(passwordToHash))
                || hashedPassword.equals(incorrectlyEncodeString(passwordToHash));
    }

    public static String encodeString(String strToEncode) throws Exception {
        return encodeString(strToEncode, "SHA-512");
    }

    private static String encodeStringSHA1(String strToEncode) throws Exception {
        return encodeString(strToEncode, "SHA-1");
    }

    private static String encodeString(String strToEncode, String algorithm) throws Exception {
        return hexString(digest(strToEncode.getBytes(StandardCharsets.UTF_8), algorithm));
    }

    private static byte[] digest(byte[] input, String algorithm) throws Exception {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e) {
            LoggerUtility.log(Security.class, "No such algorithm found.", e);
            throw new Exception("No such algorithm found", e);
        }
        return md.digest(input);
    }

    private static String hexString(byte[] block) {
        StringBuilder buf = new StringBuilder();
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        int high;
        int low;
        for (byte aBlock : block) {
            high = ((aBlock & 0xf0) >> 4);
            low = (aBlock & 0x0f);
            buf.append(hexChars[high]);
            buf.append(hexChars[low]);
        }

        return buf.toString();
    }

    private static String incorrectlyEncodeString(String strToEncode) throws Exception {
        return incorrectHexString(digest(strToEncode.getBytes(StandardCharsets.UTF_8), "SHA-1"));
    }

    private static String incorrectHexString(byte[] b) {
        if (b == null || b.length < 1) {
            return "";
        }
        StringBuilder s = new StringBuilder();
        for (byte aB : b) {
            s.append(Integer.toHexString(aB & 0xFF));
        }
        return new String(s);
    }

    public static String getRandomToken() throws Exception {
        byte[] token = new byte[64];
        RANDOM.nextBytes(token);
        return hexString(digest(token, "SHA-512"));
    }

    public static String encrypt(String text, byte[] initVector, byte[] secretKey) throws Exception {
        IvParameterSpec initVectorSpec = new IvParameterSpec(initVector);
        SecretKeySpec secret = new SecretKeySpec(secretKey, KEY_SPEC);
        byte[] encrypted;
        String result;

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_CONFIGURATION);
            cipher.init(Cipher.ENCRYPT_MODE, secret, initVectorSpec);
            encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            result = new String(Base64.getEncoder().encode(encrypted), StandardCharsets.UTF_8);
        }
        catch (GeneralSecurityException e) {
            throw new Exception("Unable to encrypt text", e);
        }

        return result;
    }

    public static String decrypt(String text, byte[] initVector, byte[] secretKey) throws Exception {
        IvParameterSpec initVectorSpec = new IvParameterSpec(initVector);
        SecretKeySpec secret = new SecretKeySpec(secretKey, KEY_SPEC);
        String decrypted;

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_CONFIGURATION);
            cipher.init(Cipher.DECRYPT_MODE, secret, initVectorSpec);
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(text));
            decrypted = new String(original, StandardCharsets.UTF_8);
        }
        catch (GeneralSecurityException e) {
            throw new Exception("Unable to decrypt text", e);
        }

        return decrypted;
    }

    public static byte[] generateNewInitVector() {
        byte[] initVector = new byte[16];
        RANDOM.nextBytes(initVector);
        return initVector;
    }

    public static byte[] generateNewSecretKey() throws Exception {
        // Get the KeyGenerator
        KeyGenerator kgen;
        try {
            kgen = KeyGenerator.getInstance("AES");
        }
        catch (NoSuchAlgorithmException e) {
            throw new Exception("Cannot generate cypher key", e);
        }
        kgen.init(128); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

}
