package ru.skinnyweb.shared.templates;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smalex on 13/04/15.
 */
public class Jsons {
  public static Map<String, Object> convertJsonObject(JsonObject jsonObject) {
    final Map<String, Object> map = new HashMap<String, Object>();
    for (String key : jsonObject.keys()) {
      map.put(key, getValue(jsonObject.get(key)));
    }
    return map;
  }

  public static Object getValue(JsonValue value) {
    switch (value.getType()) {
      case STRING:
        return value.asString();
      case NUMBER:
        return value.asNumber();
      case BOOLEAN:
        return value.asBoolean();
      case OBJECT:
        return convertJsonObject((JsonObject) value);
      case ARRAY:
        final JsonArray array = (JsonArray) value;
        final List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
          list.add(getValue(array.get(i)));
        }
        return list;
      case NULL:
        break;
    }
    return null;
  }
}
