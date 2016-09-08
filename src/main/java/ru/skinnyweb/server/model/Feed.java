package ru.skinnyweb.server.model;

import ru.skinnyweb.server.api.Id;
import ru.skinnyweb.server.api.IdFactory;

public class Feed extends BaseModel {
  private String name;
  private String href;
  private boolean listShowDescription;

  public Feed() {
    super(IdFactory.createId("Feed", 0L));
  }

  public Feed(Id key) {
    super(key);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public boolean isListShowDescription() {
    return listShowDescription;
  }

  public void setListShowDescription(boolean listShowDescription) {
    this.listShowDescription = listShowDescription;
  }
}
