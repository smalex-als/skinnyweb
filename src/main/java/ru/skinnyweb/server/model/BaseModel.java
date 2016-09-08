package ru.skinnyweb.server.model;

import ru.skinnyweb.server.api.Id;

public class BaseModel {
  private Id key;

  protected BaseModel(Id key) {
    this.key = key;
  }

  public String getIdAsString() {
    return String.valueOf(key.getId());
  }

  public long getId() {
    return key.getId();
  }

  public Id getKey() {
    return key;
  }
}
