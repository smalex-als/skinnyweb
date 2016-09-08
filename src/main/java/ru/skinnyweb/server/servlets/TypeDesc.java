package ru.skinnyweb.server.servlets;

import java.util.ArrayList;
import java.util.List;

public class TypeDesc {
  enum FieldType {
    KEY, LONG, DOUBLE, STRING, TEXT, BOOL, DATE, REFERENCE
  }

  public static class Field {
    private String name;
    private String title;
    private FieldType type;
    private TypeDesc parent;

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

    public Field setType(FieldType type) {
      this.type = type;
      return this;
    }

    public Field setParent(TypeDesc parent) {
      this.parent = parent;
      return this;
    }

    public TypeDesc build() {
      return parent;
    }
  }

  private String kind;
  private List<Field> fields = new ArrayList<Field>();

  public Field newField() {
    Field field = new Field();
    field.setType(FieldType.STRING);
    field.setParent(this);
    fields.add(field);
    return field;
  }

  public String getKind() {
    return kind;
  }

  public TypeDesc setKind(String kind) {
    this.kind = kind;
    return this;
  }

  public List<Field> getFields() {
    return fields;
  }
}
