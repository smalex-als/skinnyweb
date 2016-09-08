package ru.skinnyweb.server.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.DateTime;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;

/**
 * Created with IntelliJ IDEA.
 * User: smalex
 * Date: 8/28/12
 * Time: 8:05 AM
 */
public class ResultBuilder {
  private static final Logger log = Logger.getLogger(ResultBuilder.class.getName());
  private static final String [] mounts = new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                  "июля", "августа", "сентября", "октября", "ноября", "декабря"};

  public interface PrintEntity {
    void print(ResultBuilder builder, Entity entity, String mode);
  }

  private static class Item {
    private Map<String, Object> map;
    private List<Object> list;

    private Item() {
      this(false);
    }

    public Item(boolean isList) {
      if (isList) {
        list = Lists.newArrayList();
      } else {
        map = Maps.newLinkedHashMap();
      }
    }

    public void remove(String name) {
      if (map != null) {
        map.remove(name);
      } else {
        log.warning("list doesn't support remove operation");
      }
    }

    public void removeLast() {
      if (map != null) {
        log.warning("map doesn't support removeLast operation");
      } else {
        list.remove(list.size() - 1);
      }
    }

    public void put(String name, Object item) {
      if (item == null) {
        return;
      }
      if (map == null) {
//        log.warning("adding to list " + name);
        list.add(item);
        return;
      }
      Object o = map.get(name);
      if (o == null) {
        map.put(name, item);
      } else {
        Collection<Object> col;
        if (o instanceof Collection) {
          col = (Collection<Object>) o;
        } else {
          col = new ArrayList<Object>();
          col.add(o);
          map.put(name, col);
        }
        col.add(item);
      }
    }
  }

  private static class StackValue {
    private Item item;
    private String name;

    private StackValue(Item item, String name) {
      this.item = item;
      this.name = name;
    }

    public Item getItem() {
      return item;
    }

    public String getName() {
      return name;
    }
  }

  private final EntityContext ctx;
  private final Map<String, PrintEntity> handlers = Maps.newHashMap();
  private final Item root;
  private Item currentItem;
  private Item lastItem;
  private List<StackValue> stacks = new LinkedList<StackValue>();
  private Map<String, Object> userData = Maps.newHashMap();

  public static class EntityContext {
  }
  public ResultBuilder() {
    this(new EntityContext());
  }

  public ResultBuilder(EntityContext ctx) {
    this.ctx = ctx;

    root = new Item();
    currentItem = root;
    lastItem = root;
  }

  public void addUserData(String name, Object data) {
    userData.put(name, data);
  }

  public Object getUserData(String name) {
    return userData.get(name);
  }

  public ResultBuilder addHandler(String kind, PrintEntity printEntity) {
    return addHandler(kind, null, printEntity);
  }

  public ResultBuilder addHandler(String kind, String mode, PrintEntity printEntity) {
    handlers.put(kindAndMode(kind, mode), printEntity);
    return this;
  }

  public ResultBuilder put(Entity entity) {
    return put(entity, null);
  }

  public ResultBuilder put(Entity entity, String mode) {
    String kind = entity.getKind();
    String name = kind.substring(0, 1).toLowerCase() + kind.substring(1);
    return put(name, entity, mode);
  }

  public ResultBuilder put(String tagName, Entity entity, String mode) {
    String kind = entity.getKind();
    PrintEntity printEntity = handlers.get(kindAndMode(kind, mode));
    if (printEntity == null) {
      throw new IllegalArgumentException("PrintEntity handler not found for " + entity.getKey() + " mode " + mode);
    }
    Item item = peekCurrentItem();
    begin(tagName);
    printEntity.print(this, entity, mode);
    end();
    if (item != peekCurrentItem()) {
      throw new RuntimeException("begin() end() calls not equals times");
    }
    return this;
  }

  public ResultBuilder putList(String name, Collection<Entity> entities) {
    return putList(name, entities, null);
  }

  public ResultBuilder putList(String name, Collection<Entity> entities, String mode) {
    if (entities.isEmpty()) {
      return this;
    }
    beginList(name);
    for (Entity entity : entities) {
      put(entity, mode);
    }
    end();
    return this;
  }

  public ResultBuilder putListBelow(String name, Collection<Entity> entities, String mode) {
    if (!entities.isEmpty()) {
      beginListBelow(name);
      for (Entity entity : entities) {
        put(entity, mode);
      }
      end();
    }
    return this;
  }

  public ResultBuilder putListBelow(String name, Collection<Entity> entities) {
    return putListBelow(name, entities, null);
  }

  public ResultBuilder putFields(Entity entity, Iterable<String> fields) {
    printFields(entity, fields);
    return this;
  }

  public ResultBuilder put(String name, boolean value) {
    currentItem.put(name, value);
    return this;
  }

  public ResultBuilder put(String name, long value) {
    if (value > (long) Integer.MAX_VALUE || value < (long) Integer.MIN_VALUE) {
      // little hack for soy doesn't support LONG
      currentItem.put(name, (double) value);
    } else {
      currentItem.put(name, (int) value);
    }
    return this;
  }

  public ResultBuilder put(String name, double value) {
    currentItem.put(name, value);
    return this;
  }

  public ResultBuilder put(String name, int value) {
    currentItem.put(name, value);
    return this;
  }

  public ResultBuilder put(String name, String value) {
    if (value == null) {
      return this;
    }

    value = StringUtil.unescapeHTML(value);
    currentItem.put(name, value);
    return this;
  }

  public ResultBuilder put(String name, Date value) {
    currentItem.put(name, dateTimeToMap(value));
    return this;
  }

  public ResultBuilder put(String field, Map<String, Object> value) {
    currentItem.put(field, value);
    return this;
  }

  public ResultBuilder begin(String name) {
    return beginInner(name, currentItem, false);
  }

  public ResultBuilder beginList(String name) {
    return beginInner(name, currentItem, true);
  }

  public ResultBuilder beginListBelow(String name) {
    return beginInner(name, lastItem, true);
  }

  public ResultBuilder beginBelow() {
    pushCurrentItem(currentItem, null);
    currentItem = lastItem;
    return this;
  }

  private ResultBuilder beginInner(String name, Item toItem, boolean isList) {
    pushCurrentItem(currentItem, name);
    Item newItem = new Item(isList);
    toItem.put(name, isList ? newItem.list : newItem.map);
    currentItem = newItem;
    return this;
  }

  public ResultBuilder end() {
    if (currentItem == root) {
      throw new RuntimeException("reached root");
    }
    lastItem = currentItem;
    currentItem = popCurrentItem();
    return this;
  }

  public ResultBuilder remove(String name) {
    currentItem.remove(name);
    return this;
  }

  public ResultBuilder removeLast() {
    currentItem.removeLast();
    return this;
  }

  public Map<String, Object> build() {
    return root.map;
  }

  public EntityContext getContext() {
    return ctx;
  }

  private void pushCurrentItem(Item item, String name) {
    stacks.add(new StackValue(item, name));
  }

  private Item popCurrentItem() {
    StackValue remove = stacks.remove(stacks.size() - 1);
//    String name1 = remove.getName();
//
//    if (!expected.equals(name1)) {
//      throw new RuntimeException("expected name " + expected + " not equals received " + name1);
//    }
    return remove.getItem();
  }

  private Item peekCurrentItem() {
    if (stacks.size() > 0) {
      return stacks.get(stacks.size() - 1).getItem();
    }
    return null;
  }

  public void printFields(Entity entity, Iterable<String> fields) {
    final Map<String, Object> properties = entity.getProperties();
    for (String field : fields) {
      String mode = null;
      final int idx = field.indexOf('|');
      if (idx >= 0) {
        mode = field.substring(idx + 1);
        field = field.substring(0, idx);
      }
      Object value;
      if ("id".equals(field)) {
        value = entity.getKey().getId();
      } else {
        value = properties.get(field);
      }
      if (value instanceof Text) {
        value = ((Text) value).getValue();
      }
      if (value instanceof Date) {
        put(field, dateTimeToMap((Date) value));
      } else {
        if (value instanceof Integer) {
          put(field, (Integer) value);
        } else if (value instanceof Double) {
          put(field, (Double) value);
        } else if (value instanceof Long) {
          put(field, (Long) value);
        } else if (value instanceof Boolean) {
          put(field, (Boolean) value);
        } else if (value instanceof String) {
          String stringValue = (String) value;
          stringValue = StringUtil.unescapeHTML(stringValue);
          put(field, stringValue);
        }
      }
    }
  }

  public static Map<String, Object> dateTimeToMap(Date dt) {
    DateTime dateTime = new DateTime(dt);
    Map<String, Object> element = Maps.newLinkedHashMap();
    element.put("year", dateTime.getYear());
    element.put("month", dateTime.getMonthOfYear());
    element.put("day", dateTime.getDayOfMonth());
    element.put("hour", dateTime.getHourOfDay());
    element.put("_minute", String.format("%02d", dateTime.getMinuteOfHour()));
    element.put("_second", String.format("%02d", dateTime.getSecondOfMinute()));
    element.put("minuteOfHour", dateTime.getMinuteOfHour());
    element.put("secondOfMinute", dateTime.getSecondOfMinute());
    element.put("dayOfWeek", dateTime.getDayOfWeek());
    element.put("dayOfYear", dateTime.getDayOfYear());
    element.put("monthName", mounts[dateTime.getMonthOfYear() - 1]);
    return element;
  }

  private String kindAndMode(String kind, String mode) {
    return kind + (mode != null ? "." + mode : "");
  }
}
