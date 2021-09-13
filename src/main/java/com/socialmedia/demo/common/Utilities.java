package com.socialmedia.demo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public final class Utilities {
    private Utilities() {
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static String encryptWithAES(String password, String encodedKey) throws Exception {
        Key key = new SecretKeySpec(encodedKey.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);

        byte[] encVal = c.doFinal(password.getBytes());

        return new String(Base64.encodeBase64(encVal));
    }

    public static String decryptWithAES(String password, String encodedKey) throws Exception {
        Key key = new SecretKeySpec(encodedKey.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);

        byte[] decordedValue = Base64.decodeBase64(password);
        byte[] decValue = c.doFinal(decordedValue);

        return new String(decValue);
    }
}
