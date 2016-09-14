package ru.skinnyweb.server.api;

import java.util.List;

import ru.skinnyweb.server.utils.Lists;

import java.util.ArrayList;


public class Criteria {
  private String kind;
  public static class Filter {
    private final String field;
    private final String op;
    private final Object value;

    public Filter(String field, String op, Object value) {
      this.field = field;
      this.op = op;
      this.value = value;
    }

    public String getField() {
      return field;
    }

    public String getOp() {
      return op;
    }

    public Object getValue() {
      return value;
    }

    public String toString() {
      return field + " " + op + " " + value;
    }
  }

  private final List<Filter> filters = Lists.newArrayList();
  private final List<String> sorts = Lists.newArrayList();
  private boolean cache;

  public static Criteria newCriteria(String kind) {
    Criteria cr = new Criteria();
    cr.kind = kind;
    return cr;
  }

  public Criteria filter(String field, Object value) {
    return filter(field, "=", value);
  }

  public Criteria filter(String field, String operator, Object value) {
    Filter f = new Filter(field, operator, value);
    filters.add(f);
    return this;
  }

  public Criteria sort(String sort) {
    sorts.add(sort);
    return this;
  }

  public String getKind() {
    return kind;
  }

  public List<Filter> getFilters() {
    return filters;
  }

  public List<String> getSorts() {
    return sorts;
  }

  public boolean isCache() {
    return cache;
  }

  public Criteria cache(boolean cache) {
    this.cache = cache;
    return this;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Criteria{" + kind);
    if (!filters.isEmpty()) {
      sb.append(", ");
      for (Filter filter : filters) {
        sb.append(filter);
        sb.append(" AND ");
      }
      sb.delete(sb.length() - 5, sb.length());
    }
    sb.append("}");
    return sb.toString();
  }
}
