package ru.skinnyweb.server.api;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

public class Id implements Comparable<Id> {
  static final long NOT_ASSIGNED = 0L;
  private String namespace = "";
  private String kind;
  private long id;
  private String name;
  private Id parentKey;

  Id() {
  }

  public boolean isComplete() {
    return id != NOT_ASSIGNED || name != null;
  }

  public String getNamespace() {
    return namespace;
  }

  void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getKind() {
    return kind;
  }

  void setKind(String kind) {
    this.kind = kind;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  public Id getParent() {
    return parentKey;
  }

  void setParent(Id parentKey) {
    this.parentKey = parentKey;
  }

  @Override
  public boolean equals(Object object) {
    return equals(object, true);
  }

  boolean equals(Object object, boolean considerNotAssigned) {
    if (object instanceof Id) {
      Id key = (Id) object;
      if (this == key) {
        return true;
      }
      if (!namespace.equals(key.namespace)) {
        return false;
      }
      if (considerNotAssigned && name == null && id == NOT_ASSIGNED && key.id == NOT_ASSIGNED) {
        return false;
      }
      if (id != key.id ||
          !kind.equals(key.kind) ||
          !Objects.equals(name, key.name)) {
        return false;
      }
      if (parentKey != key.parentKey && (parentKey == null ||
          !parentKey.equals(key.parentKey, considerNotAssigned))) {
        return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public int compareTo(Id other) {

    if (this == other) {
      return 0;
    }

    Iterator<Id> thisPath = getPathIterator(this);
    Iterator<Id> otherPath = getPathIterator(other);

    while (thisPath.hasNext()) {
      Id thisKey = thisPath.next();
      if (otherPath.hasNext()) {
        Id otherKey = otherPath.next();
        int result = compareToInternal(thisKey, otherKey);
        if (result != 0) {
          return result;
        }
      } else {
        return 1;
      }
    }
    return otherPath.hasNext() ? -1 : 0;
  }

  private static Iterator<Id> getPathIterator(Id key) {
    LinkedList<Id> stack = new LinkedList<Id>();
    do {
      stack.addFirst(key);
      key = key.getParent();
    } while(key != null);
    return stack.iterator();
  }

  private static int compareToInternal(Id thisKey, Id otherKey) {

    if (thisKey == otherKey) {
      return 0;
    }

    int result = thisKey.getNamespace().compareTo(otherKey.getNamespace());
    if (result != 0) {
      return result;
    }

    result = thisKey.getKind().compareTo(otherKey.getKind());
    if (result != 0) {
      return result;
    }

    if (!thisKey.isComplete() && !otherKey.isComplete()) {
      return compareToWithIdentityHash(thisKey, otherKey);
    }

    if (thisKey.getId() != NOT_ASSIGNED) {
      if (otherKey.getId() == NOT_ASSIGNED) {
        return -1;
      }
      return Long.compare(thisKey.getId(), otherKey.getId());
    }

    if (otherKey.getId() != NOT_ASSIGNED) {
      return 1;
    }

    return thisKey.getName().compareTo(otherKey.getName());
  }

  /**
   * Helper method to compare 2 {@code Key} objects using their identity hash
   * codes.
   */
  static int compareToWithIdentityHash(Id k1, Id k2) {
    return Integer.compare(System.identityHashCode(k1), System.identityHashCode(k2));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + ((kind == null) ? 0 : kind.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parentKey == null) ? 0 : parentKey.hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    appendToString(buffer);
    return buffer.toString();
  }

  private void appendToString(StringBuffer buffer) {
    if (parentKey != null) {
      parentKey.appendToString(buffer);
      buffer.append("/");
    } else {
      if (namespace != null) {
        if (namespace.length() > 0) {
          buffer.append("!");
          buffer.append(namespace);
          buffer.append(":");
        }
      }
    }
    buffer.append(kind);
    buffer.append("(");
    if (name != null) {
      buffer.append("\"" + name + "\"");
    } else if (id == NOT_ASSIGNED) {
      buffer.append("no-id-yet");
    } else {
      buffer.append(String.valueOf(id));
    }
    buffer.append(")");
  }
}
