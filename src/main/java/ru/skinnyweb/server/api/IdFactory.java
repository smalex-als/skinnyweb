package ru.skinnyweb.server.api;

public class IdFactory {
  public static Id createId(String kind, long id) {
    Id res = new Id();
    res.setKind(kind);
    res.setId(id);
    return res;
  }

  public static Id createId(String kind, String name) {
    Id res = new Id();
    res.setKind(kind);
    res.setName(name);
    return res;
  }
}
