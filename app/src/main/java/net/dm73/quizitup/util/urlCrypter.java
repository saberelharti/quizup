package net.dm73.quizitup.util;


import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public abstract class urlCrypter {

    public static String encode(String data) {

        Mac crypt = null;
        try {
            crypt = Mac.getInstance(Constants.DECRYPTED);
            SecretKeySpec secret_key = new SecretKeySpec(Constants.TOKEN_SECRET.getBytes(Constants.CODE_BASE), Constants.DECRYPTED);
            crypt.init(secret_key);

            return new String(Hex.encodeHex(crypt.doFinal(data.getBytes(Constants.CODE_BASE))));

        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (InvalidKeyException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String decodeURL(String url, String timeStamp) {
        String TimeStamp = (timeStamp == null) ? "" : timeStamp;
        String newURL = url + "?ts=" + TimeStamp;
        newURL = encode(newURL);
        return (url + "?ts=" + TimeStamp + "&sign=" + newURL);
    }

    public static String decodeQuizURL(String url, String timeStamp) {
        String TimeStamp = (timeStamp == null) ? "" : timeStamp;
        String newURL = url + "?type=psycho&ts=" + TimeStamp;
        newURL = encode(newURL);
        return (url + "?type=psycho&ts=" + TimeStamp + "&sign=" + newURL);
    }

    public static String decodeResultURL(String url, String timeStamp) {
        String TimeStamp = (timeStamp == null) ? "" : timeStamp;
        String newURL = url + "&ts=" + TimeStamp;
        newURL = encode(newURL);
        return (url + "&ts=" + TimeStamp + "&sign=" + newURL);
    }

}
