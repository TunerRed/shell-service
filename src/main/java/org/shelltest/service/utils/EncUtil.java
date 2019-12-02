package org.shelltest.service.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Base64Utils;

import java.io.UnsupportedEncodingException;

/**
 * 加密和解密字符串
 * */
public class EncUtil {
    public static String encode(@NotNull String origin) {
        return new String(Base64Utils.encode(origin.getBytes()));
    }
    public static String decode(@NotNull String enc) {
        String origin = null;
        try {
            origin = new String(Base64Utils.decode(enc.getBytes()), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return origin;
    }
}
