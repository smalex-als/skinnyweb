package ru.skinnyweb.shared.templates;

import java.util.List;
import java.util.Map;

import com.googlecode.jatl.client.Html;
import com.googlecode.jatl.client.HtmlWriter;
import com.googlecode.jatl.client.Indenter;
import com.googlecode.jatl.client.SimpleIndenter;
import com.googlecode.jatl.client.io.StringWriter;

/**
 * Created by smalex on 08/07/14.
 */
public class BaseTemplates {
  private static final String[] mounts = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
      "Oct", "Nov", "Dec"};
  protected static Indenter indentOn = new SimpleIndenter("\n", "\t", "\n", "\t");
  protected static Indenter indentSameLine = new SimpleIndenter("\n", "\t", null, null);

  public static String getFormValue(List<Map<String, Object>> params, String name) {
    String value = null;
    if (params != null) {
      for (Map<String, Object> param : params) {
        if (name.equals(param.get("_name"))) {
          value = (String) param.get("_innerText");
        }
      }
    }
    return trimToEmpty(value);
  }

  public static boolean hasLength(Map<String, Object> map, String name) {
    return !getString(map, name).isEmpty();
  }

  public static String getString(Map<String, Object> map, String name) {
    final Object value = getInnerObject(map, name);
    return value instanceof String ? (String) value : "";
  }

  public static Number getNumber(Map<String, Object> map, String name) {
    final Object value = getInnerObject(map, name);
    return value instanceof Number ? (Number) value : null;
  }

  public static int getInteger(Map<String, Object> map, String name) {
    final Object value = getInnerObject(map, name);
    return value instanceof Number ? ((Number) value).intValue() : 0;
  }

  public static boolean getBoolean(Map<String, Object> map, String name) {
    Object value = getInnerObject(map, name);
    return value instanceof Boolean ? (Boolean) value : false;
  }

  public static List<Map<String, Object>> getList(Map<String, Object> map, String name) {
    final Object value = getInnerObject(map, name);
    return value instanceof List ? (List<Map<String, Object>>) value : null;
  }

  public static String trimToEmpty(String string) {
    return string != null ? string.trim() : "";
  }

  public static Map<String, Object> getInnerMap(Map<String, Object> map, String ... names) {
    if (map == null) {
      return null;
    }
    for (String name : names) {
      int idx = name.indexOf('.');
      if (idx >= 0) {
        String[] pieces = name.split("\\.");
        for (String piece : pieces) {
          final Object val = map.get(piece);
          if (val == null) {
            return null;
          }
          if (!(val instanceof Map)) {
            return null;
          }
          map = (Map<String, Object>) val;
        }
      } else {
        final Object val = map.get(name);
        if (val == null) {
          return null;
        }
        if (!(val instanceof Map)) {
          return null;
        }
        map = (Map<String, Object>) val;
      }
    }
    return map;
  }

  public static Object getInnerObject(Map<String, Object> map, String name) {
    if (map == null) {
      return null;
    }
    int idx = name.indexOf('.');
    if (idx < 0) {
      return map.get(name);
    }
    String[] pieces = name.split("\\.");
    for (int i = 0; i < pieces.length - 1; i++) {
      final Object val = map.get(pieces[i]);
      if (val == null) {
        return null;
      }
      if (!(val instanceof Map)) {
        return null;
      }
      map = (Map<String, Object>) val;
    }
    return map.get(pieces[pieces.length - 1]);
  }

  public static String getDate(Map<String, Object> now, Map<String, Object> map, String name) {
    Map<String, Object> dateMap = getInnerMap(map, name);
    if (dateMap == null) {
      return "";
    }
    if (now == null) {
      return "";
    }

    int year = getNumber(dateMap, "year").intValue();
    int day = getNumber(dateMap, "day").intValue();
    int hour = getNumber(dateMap, "hour").intValue();
    int dayOfYear = getNumber(dateMap, "dayOfYear").intValue();
    String minuteOfHour = getString(dateMap, "_minute");

    int nowDayOfYear = getNumber(now, "dayOfYear").intValue();
    if (nowDayOfYear == dayOfYear) {
      return "Today " + hour + ":" + minuteOfHour;
    } else if (nowDayOfYear == dayOfYear + 1) {
      return "Yesterday " + hour + ":" + minuteOfHour;
    }
    return mounts[getNumber(dateMap, "month").intValue() - 1] + " " + day + ", " + year;
  }

  public String toString(final HtmlWriter body) {
    final StringWriter writer = new StringWriter();

    Html html = new Html(writer) {{
      write(body);
    }};
    return writer.getBuffer().toString();
  }
}
