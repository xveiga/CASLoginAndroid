package com.example.caslogin.data.utils.httpclient;

import com.example.caslogin.data.utils.exceptions.URLEncodingException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class HttpUtils {

    public static byte[] charsToBytes(char[] c, Charset encoding) {
        // Encode characters to a new ByteBuffer (CharBuffer.wrap does not copy the array)
        ByteBuffer buf = encoding.encode(CharBuffer.wrap(c));
        // Copy the contents to a byte array (similar to memcpy in c)
        byte[] b = Arrays.copyOfRange(buf.array(), buf.position(), buf.limit());
        Arrays.fill(buf.array(), (byte) 0); // Overwrite buffer array to protect passwords
        // Remember to overwrite char array if not needed after this function!
        return b;
    }

    public static String encodeParameters(String parameterName[], String parameterValue[], Charset encoding) throws UnsupportedEncodingException, URLEncodingException {
        StringBuilder result = null;
        if (parameterName.length == parameterValue.length) {
            result = new StringBuilder();
            for (int i = 0; i < parameterName.length; i++) {
                String param = parameterName[i] + "=" + URLEncoder.encode(parameterValue[i], encoding.name());
                if (result.length() == 0) {
                    result.append(param);
                } else {
                    result.append("&" + param);
                }
            }
        } else {
            throw new URLEncodingException(
                    "Encoding Error: Arrays parameterName and parameterValue are not of the same length.");
        }

        return result.toString();
    }
}
