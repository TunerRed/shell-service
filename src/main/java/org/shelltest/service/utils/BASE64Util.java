package org.shelltest.service.utils;

import org.springframework.util.Base64Utils;

import java.io.UnsupportedEncodingException;

public class BASE64Util {
    public static String encode(String origin) {
        return new String(Base64Utils.encode(origin.getBytes()));
    }
    public static String decode(String enc) {
        String origin = null;
        try {
            origin = new String(Base64Utils.decode(enc.getBytes()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return origin;
    }
}
