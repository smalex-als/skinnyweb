package ru.skinnyweb.server.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: smalex
 * Date: 3/16/12
 * Time: 7:24 AM
 */
public class StringUtils {
  public static String trimToEmpty(String str) {
    if (str == null) {
      return "";
    }
    return str.trim();
  }

  public static boolean hasLength(String str) {
    return hasLength((CharSequence) str);
  }

  public static boolean hasLength(CharSequence str) {
    return (str != null && str.length() > 0);
  }

  public static boolean hasText(String str) {
    return hasText((CharSequence) str);
  }

  public static boolean hasText(CharSequence str) {
    if (!hasLength(str)) {
      return false;
    }
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  public static String ifEmpty(String value, String defValue) {
    return value != null && value.length() > 0 ? value : defValue;
  }

  public static String encodeUrl(String destinationURL) {
    try {
      return URLEncoder.encode(destinationURL, "utf-8");
    } catch (UnsupportedEncodingException e) {
      // UTF-8 should always be supported
      throw new AssertionError();
    }
  }

  public static Map<String, String> stringToMap(String str) {
    Map<String, String> res = new HashMap<String, String>();
    int start = 0;
    int index;
    while(start < str.length()) {
      index = str.indexOf(',', start);
      if (index < 0) {
        index = str.length();
      }
      String col = str.substring(start, index).trim();
      start = index + 1;
      if (col.length() > 0) {
        index = col.indexOf(':');
        String name = col;
        String value = "";
        if (index >= 0) {
          name = col.substring(0, index).trim();
          value = col.substring(index + 1).trim();
        }
        res.put(name, value);
      }
    }
    return res;
  }
}
