package ru.skinnyweb.server.servlets;

import java.util.ArrayList;
import java.util.List;

public class FormListDesc {

  public static class Column {
    private FormListDesc parent;
    private String name;
    private String title;
    private String width;

    public String getName() {
      return name;
    }

    public Column setName(String name) {
      this.name = name;
      return this;
    }

    public String getTitle() {
      return title;
    }

    public Column setTitle(String title) {
      this.title = title;
      return this;
    }

    public String getWidth() {
      return width;
    }

    public Column setWidth(String width) {
      this.width = width;
      return this;
    }

    public FormListDesc build() {
      if (title == null || title.length() == 0) {
        for (TypeDesc.Field field : parent.type.getFields()) {
          if (field.getName().equals(name)) {
            title = field.getTitle();
            break;
          }
        }
        if (title == null || title.length() == 0) {
          throw new RuntimeException("field " + name + " not found");
        }
      }
      return parent;
    }
  }

  private final List<Column> columns = new ArrayList<Column>();
  private final TypeDesc type;

  public FormListDesc(TypeDesc type) {
    this.type = type;
  }

  public Column newColumn() {
    Column column = new Column();
    column.parent = this;
    columns.add(column);
    return column;
  }

  public List<Column> getColumns() {
    return columns;
  }

  public TypeDesc getType() {
    return type;
  }
}
