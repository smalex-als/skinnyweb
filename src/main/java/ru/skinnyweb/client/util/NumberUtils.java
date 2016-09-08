package ru.skinnyweb.client.util;

import java.util.logging.Logger;

/**
 * Created by smalex on 17/08/14.
 */
public class NumberUtils {
  private static final Logger log = Logger.getLogger(NumberUtils.class.getName());

  public static String toString(long value) {
    String res = String.valueOf(value);
    for (int i = 0; i < res.length(); i++) {
      if (!(res.charAt(i) >= '0' && res.charAt(i) <= '9')) {
        return res.substring(0, i);
      }
    }
    return res;
  }

  public static String toString(int value) {
    String res = String.valueOf(value);
    for (int i = 0; i < res.length(); i++) {
      if (!(res.charAt(i) >= '0' && res.charAt(i) <= '9')) {
        return res.substring(0, i);
      }
    }
    return res;
  }

  public static int toInt(String value) {
    if (value != null && value.length() > 0) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        log.info("failed parse value");
      }
    }
    return 0;
  }

  public static long toLong(String value) {
    if (value != null && value.length() > 0) {
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
      }
    }
    return 0L;
  }

  public static long toLong(Long value) {
    return value != null ? value : 0L;
  }
}
