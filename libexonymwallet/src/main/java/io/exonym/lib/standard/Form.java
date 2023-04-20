package io.exonym.lib.standard;

import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Form {

    public static final String base64Range = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String sha256AsB64(byte[] bytes) {
        byte[] digest = sha256(bytes);
        return Base64.encodeBase64String(digest);

    }

    public static String sha256AsHex(byte[] bytes) {
        byte[] digest = sha256(bytes);
        BigInteger bigI = new BigInteger(1, digest);
        return String.format("%064x", bigI);

    }

    public static byte[] sha256(byte[] bytes){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            return md.digest();

        } catch (NoSuchAlgorithmException e) {
            return null;

        }
    }

    public static BigInteger toBigInteger(String hex) throws UxException {
        if (WhiteList.isHex(hex)){
            return new BigInteger(hex, 16);
        } else {
            throw new UxException(ErrorMessages.HEXADECIMAL_REQUIRED);

        }
    }

    public static String toHex(byte[] bytes){
        BigInteger bigI = new BigInteger(1, bytes);
        return String.format("%064x", bigI);

    }

    public static String toHex(BigInteger b){
        return String.format("%064x", b);

    }

    public static String toHex(long length, long value){
        return toHex(length, BigInteger.valueOf(value));

    }

    public static String toHex(long length, BigInteger b){
        return String.format("%0"+length+"x", b);

    }

    public static String toTwoCharB64String(int n) throws Exception {
        if (n>4095) {
            throw new Exception("Two characters of base64 can only count from 0 to 4095");

        }
        int a = n/64;
        int b = n%64;
        char aAlpha = base64Range.charAt(a);
        char bAlpha = base64Range.charAt(b);
        return aAlpha + "" + bAlpha;

    }
}
