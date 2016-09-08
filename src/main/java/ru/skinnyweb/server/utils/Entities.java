package ru.skinnyweb.server.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

public class Entities {
  public static Long getPropertyLong(Entity entity, String field, Long defaultValue) {
    Object rev = entity.getProperty(field);
    if (rev instanceof Number) {
      return ((Number) rev).longValue();
    }
    return defaultValue;
  }

  public static Double getPropertyDouble(Entity entity, String field, Double defaultValue) {
    Object rev = entity.getProperty(field);
    if (rev instanceof Number) {
      return ((Number) rev).doubleValue();
    }
    return defaultValue;
  }

  public static Boolean getPropertyBoolean(Entity entity, String field, Boolean defaultValue) {
    Object value = entity.getProperty(field);
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    return defaultValue;
  }

  public static Boolean getPropertyBoolean(Entity entity, String field) {
    return getPropertyBoolean(entity, field, Boolean.FALSE);
  }

  public static Date getPropertyDate(Entity entity, String field) {
    Object value = entity.getProperty(field);
    if (value instanceof Date) {
      return (Date) value;
    }
    return null;
  }

  public static String getPropertyString(Entity entity, String field, String defaultValue) {
    Object value = entity.getProperty(field);
    if (value instanceof String) {
      return (String) value;
    }
    if (value instanceof Text) {
      return ((Text) value).getValue();
    }
    if (value instanceof Boolean) {
      return value.toString();
    }
    return defaultValue;
  }

  public static Long getReferenceId(Entity entity, String propertyName) {
    return getPropertyLong(entity, propertyName, null);
  }

  public static List<Long> getReferenceIds(Entity entity, String propertyName) {
    List<Long> referenceIds = (List<Long>) entity.getProperty(propertyName);
    if (referenceIds == null) {
      return new ArrayList<Long>();
    }
    return referenceIds;
  }
}
