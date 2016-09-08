package ru.skinnyweb.client.util;

import com.google.gwt.http.client.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smalex on 17/08/14.
 */
public class StringUtils {
  public static boolean hasLength(String str) {
    return str != null && str.length() > 0;
  }

  public static boolean hasText(String str) {
    return hasLength(str) && str.trim().length() > 0;
  }

  public static String trimToEmpty(String string) {
    return string != null ? string.trim() : "";
  }

  public static String rtrim(String str) {
    int start = str.length() - 1;
    for (int i = start; i >= 0; i--) {
      char c = str.charAt(i);
      if (c != ' ' && c != '\t') {
        return i != start ? str.substring(0, i + 1) : str;
      }
    }
    return "";
  }

  public static String trimToDefault(String value, String defaultValue) {
    String newValue = trimToEmpty(value);
    return newValue.isEmpty() ? defaultValue : newValue;
  }

  public static String notNull(String string) {
    return string != null ? string : "";
  }

  public static String notEmpty(String string, String defVal) {
    return string != null && string.trim().length() > 0 ? string : defVal;
  }

  public static String cutName(String name) {
    String substring = null;
    int start = name.indexOf('"');
    if (start != -1) {
      int end = name.indexOf('"', start + 1);
      if (end != -1) {
        substring = name.substring(start + 1, end);
      }
    }
    return substring;
  }

  public static List<String> splitResponses(String text) {
    List<String> responses = new ArrayList<String>();
    int start = 0;
    while (text.length() > 0) {
      final int i = text.indexOf('|', start);
      if (i < 0) {
        responses.add(text.replace("\\|", "|"));
        break;
      }
      if (i > 0 && text.charAt(i - 1) == '\\') {
        start = i + 1;
      } else {
        responses.add(text.substring(0, i).replace("\\|", "|"));
        text = text.substring(i + 1);
        start = 0;
      }
    }
    return responses;
  }

  public static List<String> splitAndTrim(String tags, String separator) {
    List<String> result = new ArrayList<String>();
    if (tags != null && tags.length() > 0) {
      for (String piece : tags.split(separator)) {
        if (hasText(piece)) {
          result.add(piece.trim());
        }
      }
    }
    return result;
  }

  public static <T> String join(List<T> values, String separator) {
    StringBuilder result = new StringBuilder();
    for (Object value : values) {
      String v = value.toString().trim();
      if (hasLength(v)) {
        if (result.length() > 0) {
          result.append(separator);
        }
        result.append(v);
      }
    }
    return result.toString();
  }

  public static Map<String, String> paramsFromString(String query) {
    Map<String, String> params = new HashMap<String, String>();
    if (query == null) {
      return params;
    }
    for (String piece : query.split("&")) {
      piece = trimToEmpty(piece);
      if (hasLength(piece)) {
        final int index = piece.indexOf('=');
        if (index != -1) {
          String name = piece.substring(0, index);
          String value = piece.substring(index + 1);
          if (hasLength(value)) {
            params.put(name, URL.decodeQueryString(value));
          }
        }
      }
    }
    return params;
  }

  public static String paramsToString(Map<String, String> params) {
    StringBuilder sb = new StringBuilder();
    for (String key : params.keySet()) {
      if (sb.length() > 0) {
        sb.append("&");
      }
      sb.append(key).append("=").append(URL.encodeQueryString(params.get(key)));
    }
    return sb.toString();
  }

  public static List<String> parseTokens(String token) {
    int start = 0;
    int idx;
    List<String> tokens = new ArrayList<String>();
    while ((idx = token.indexOf('/', start)) != -1) {
      tokens.add(token.substring(start, idx));
      start = idx + 1;
    }
    tokens.add(token.substring(start));
    return tokens;
  }

  public static String formatSize(long size) {
    if (size < 1024L) {
      return size + " байт";
    }
    size /= 1024L;
    if (size < 1024L) {
      return size + " КБ";
    }
    size /= 1024L;
    if (size < 1024L) {
      return size + " МБ";
    }
    size /= 1024L;
    return size + " ГБ";
  }
}
