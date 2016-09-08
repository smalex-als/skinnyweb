package ru.skinnyweb.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import elemental.client.Browser;

public class NavigationStack {
  private static final Logger log = Logger.getLogger(NavigationStack.class.getName());
  private final List<String> items = new ArrayList<String>();
  private final Map<String, List<String>> childrenMap = new HashMap<String, List<String>>();

  public String getNext() {
    String url = getPathname();
    for (String parent : childrenMap.keySet()) {
      List<String> currentLevel = childrenMap.get(parent);
      for (int i = 0; i < currentLevel.size(); i++) {
        if (currentLevel.get(i).equals(url) && i + 1 < currentLevel.size()) {
          return currentLevel.get(i + 1);
        }
      }
    }
    return null;
  }

  public String getPrev() {
    String url = getPathname();
    for (String parent : childrenMap.keySet()) {
      List<String> currentLevel = childrenMap.get(parent);
      for (int i = currentLevel.size() - 1; i >= 0; i--) {
        if (currentLevel.get(i).equals(url) && i - 1 >= 0) {
          return currentLevel.get(i - 1);
        }
      }
    }
    return null;
  }

  public void pushHref(String href) {
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).length() >= href.length()) {
        items.remove(i);
        i--;
      }
    }
    items.add(href);
  }

  public List<String> getHistory() {
    return items;
  }

  public String getLevelUp() {
    String href = getPathname() ;
    if (href.endsWith("/")) {
      final int lastIndexOf = href.lastIndexOf('/', href.length() - 2);
      if (lastIndexOf >= 0) {
        href = href.substring(0, lastIndexOf + 1);
      }
    }
    String betterHref = findHistoryLevelUp(href);
    return betterHref != null ? betterHref : href;
  }

  public String findHistoryLevelUp(String search) {
    search = cutQuery(search);
    for (int i = items.size() - 1; i >= 0; i--) {
      if (cutQuery(items.get(i)).equals(search)) {
        return items.get(i);
      }
    }
    return null;
  }

  public void addChildrenUrls(List<String> allLinks) {
    String path = getPathname();
    if (allLinks.isEmpty()) {
      childrenMap.remove(path);
    } else {
      childrenMap.put(path, allLinks);
    }
  }

  private static String getPathname() {
    return Browser.getWindow().getDocument().getLocation().getPathname();
  }

  private String cutQuery(String url) {
    int s = url.indexOf('?');
    if (s != -1) {
      return url.substring(0, s);
    }
    return url;
  }
}
