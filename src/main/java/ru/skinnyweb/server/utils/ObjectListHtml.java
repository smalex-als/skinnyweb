package ru.skinnyweb.server.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.datastore.Entity;

public class ObjectListHtml {
  private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z");
  private final DateTimeFormatter fmtMD = DateTimeFormat.forPattern("MMM d");
  private final DateTimeFormatter fmtYMD = DateTimeFormat.forPattern("MMM d YYYY");
  private StringBuilder sb = new StringBuilder();
  private List<String> columns = new ArrayList<String>();
  private List<String> widths = new ArrayList<String>();
  private List<String> titles = new ArrayList<String>();
  private final DateTime now = DateTime.now();

  public void header() {
    sb.append("<table id=\"orders\">");
    sb.append("<tr>");
    for (int i = 0; i < columns.size(); i++) {
      sb.append("<th width=\"" + widths.get(i) + "\">");
      sb.append(titles.get(i));
      sb.append("</th>");
    }
    sb.append("</tr>");
  }
  
  public void addColumn(String str) {
    Map<String, String> res = StringUtils.stringToMap(str);
    addColumn(res.get("width"), res.get("name"), res.get("title"));
  }

  public void addColumn(String width, String name, String title) {
    widths.add(width);
    columns.add(name);
    titles.add(title);
  }

  public String footer() {
    sb.append("</table>");
    // sb.append("<script>alert('hello')</script>");
    return sb.toString();
  }

  public void putFields(Entity entity, String ... names) {
    for (String name : names) {
      String value = getEntityValue(entity, name);
      sb.append("<td");
      if (name.equals("id")) {
        sb.append(" data-id=\"" + value + "\"");
      }
      sb.append(" class=\"" + name + "\">");
      sb.append(value);
      sb.append("</td>");
    }
  }
  
  public String getEntityValue(Entity entity, String name) {
    if (name.equals("id")) {
      long id = entity.getKey().getId();
      return String.valueOf(id);
    } else {
      Object value = entity.getProperty(name);
      if (value instanceof String) {
        return ((String) value);
      } else if (value instanceof Long) {
        return String.valueOf((Long) value);
      } else if (value instanceof Date) {
        DateTime dt = new DateTime(((Date) value));

        if (now.getYear() == dt.getYear()) {
          return fmtMD.print(dt);
        } else {
          return fmtYMD.print(dt);
        }
      }
    }
    return "";
  }

  public void beginObject() {
    sb.append("<tr>");
  }

  public void endObject() {
    sb.append("</tr>");
  }
}
