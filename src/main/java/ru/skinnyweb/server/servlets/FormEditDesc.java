package ru.skinnyweb.server.servlets;

import java.util.ArrayList;
import java.util.List;

public class FormEditDesc {
  public enum WidgetType {
    TEXT_BOX,
    TEXT_AREA,
    CHECK_BOX,
    LIST_BOX,
    HIDDEN,
    DATETIME,
    DATE
  }

  public static class Field {
    private FormEditDesc parent;
    private String name;
    private String title;
    private WidgetType type;
    private boolean readOnly;

    public String getName() {
      return name;
    }

    public Field setName(String name) {
      this.name = name;
      return this;
    }

    public String getTitle() {
      return title;
    }

    public Field setTitle(String title) {
      this.title = title;
      return this;
    }

    public WidgetType getType() {
      return type;
    }

    public Field setType(WidgetType type) {
      this.type = type;
      return this;
    }

    public boolean isReadOnly() {
      return readOnly;
    }

    public Field setReadOnly(boolean readOnly) {
      this.readOnly = readOnly;
      return this;
    }

    public FormEditDesc build() {
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

  private final List<Field> fields = new ArrayList<Field>();
  private final TypeDesc type;

  public FormEditDesc(TypeDesc type) {
    this.type = type;
  }

  public Field newField() {
    Field field = new Field();
    field.parent = this;
    field.type = WidgetType.TEXT_BOX;
    fields.add(field);
    return field;
  }

  public TypeDesc getType() {
    return type;
  }

  public List<Field> getFields() {
    return fields;
  }
}
