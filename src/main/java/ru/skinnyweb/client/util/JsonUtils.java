package ru.skinnyweb.client.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

public class JsonUtils {
  public static JsonObject mapToJsonObject(Map<String, Object> in) {
    final JsonObject object = Json.createObject();
    final Set<String> keys = in.keySet();
    for (String key : keys) {
      final Object inValue = in.get(key);
      if (inValue instanceof String) {
        object.put(key, (String) inValue);
        continue;
      }
      if (inValue instanceof Double) {
        object.put(key, (Double) inValue);
        continue;
      }
      if (inValue instanceof Map) {
        object.put(key, (JsonValue) mapToJsonObject((Map<String, Object>) inValue));
        continue;
      }
      if (inValue instanceof Boolean) {
        object.put(key, (Boolean) inValue);
        continue;
      }
      if (inValue instanceof List) {
        object.put(key, convertArray((List) inValue));
      }
    }
    return object;
  }

  private static JsonArray convertArray(List jsonArray) {
    final JsonArray outArray = Json.createArray();
    for (int i = 0; i < jsonArray.size(); i++) {
      final Object inValue = jsonArray.get(i);
      if (inValue instanceof String) {
        outArray.set(i, (String) inValue);
        continue;
      }
      if (inValue instanceof Double) {
        outArray.set(i, (Double) inValue);
        continue;
      }
      if (inValue instanceof Map) {
        outArray.set(i, (JsonValue) mapToJsonObject((Map<String, Object>) inValue));
        continue;
      }
      if (inValue instanceof Boolean) {
        outArray.set(i, (Boolean) inValue);
        continue;
      }
      if (inValue instanceof List) {
        outArray.set(i, convertArray((List) inValue));
      }
    }
    return outArray;
  }
}
