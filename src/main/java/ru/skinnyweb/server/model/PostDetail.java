package ru.skinnyweb.server.model;

import ru.skinnyweb.server.api.Id;
import ru.skinnyweb.server.api.IdFactory;

public class PostDetail extends BaseModel {
  private String content;

  public PostDetail(Id key) {
    super(key);
  }

  public PostDetail(long id) {
    super(IdFactory.createId("PostDetail", id));
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
