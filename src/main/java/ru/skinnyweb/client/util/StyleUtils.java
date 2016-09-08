package ru.skinnyweb.client.util;

import elemental.dom.Element;

/**
 * Created by smalex on 19/03/15.
 */
public class StyleUtils {
  public static void toggleClass(Element el, String className, boolean addOrDelete) {
    if (addOrDelete) {
      addClassName(el, className);
    } else {
      removeClassName(el, className);
    }
  }

  public static void buttonEnable(Element el, boolean enabled) {
    if (enabled) {
      el.removeAttribute("disabled");
    } else {
      el.setAttribute("disabled", "disabled");
    }
  }

  public static void showElement(Element el, String display) {
    if (!StringUtils.hasText(display)) {
      el.getStyle().removeProperty("display");
    } else {
      el.getStyle().setDisplay(display);
    }
  }

  public boolean isElementShown(Element el) {
    return !el.getStyle().getDisplay().equals("none");
  }
  
  public static boolean addClassName(Element el, String className) {
    assert (className != null) : "Unexpectedly null class name";

    className = className.trim();
    assert (className.length() != 0) : "Unexpectedly empty class name";

    // Get the current style string.
    String oldClassName = el.getClassName();
    int idx = indexOfName(oldClassName, className);

    // Only add the style if it's not already present.
    if (idx == -1) {
      if (oldClassName.length() > 0) {
        el.setClassName(oldClassName + " " + className);
      } else {
        el.setClassName(className);
      }
      return true;
    }
    return false;
  }

  public static boolean hasClassName(Element el, String className) {
    assert (className != null) : "Unexpectedly null class name";

    className = className.trim();
    assert (className.length() != 0) : "Unexpectedly empty class name";

    return indexOfName(el.getClassName(), className) != -1;
  }

  public static final boolean removeClassName(Element el, String className) {
    assert (className != null) : "Unexpectedly null class name";

    className = className.trim();
    assert (className.length() != 0) : "Unexpectedly empty class name";

    // Get the current style string.
    String oldStyle = el.getClassName();
    int idx = indexOfName(oldStyle, className);

    // Don't try to remove the style if it's not there.
    if (idx != -1) {
      // Get the leading and trailing parts, without the removed name.
      String begin = oldStyle.substring(0, idx).trim();
      String end = oldStyle.substring(idx + className.length()).trim();

      // Some contortions to make sure we don't leave extra spaces.
      String newClassName;
      if (begin.length() == 0) {
        newClassName = end;
      } else if (end.length() == 0) {
        newClassName = begin;
      } else {
        newClassName = begin + " " + end;
      }

      el.setClassName(newClassName);
      return true;
    }
    return false;
  }

  /**
   * Returns the index of the first occurrence of name in a space-separated list of names,
   * or -1 if not found.
   *
   * @param nameList list of space delimited names
   * @param name a non-empty string.  Should be already trimmed.
   */
  static int indexOfName(String nameList, String name) {
    int idx = nameList.indexOf(name);

    // Calculate matching index.
    while (idx != -1) {
      if (idx == 0 || nameList.charAt(idx - 1) == ' ') {
        int last = idx + name.length();
        int lastPos = nameList.length();
        if ((last == lastPos)
            || ((last < lastPos) && (nameList.charAt(last) == ' '))) {
          break;
        }
      }
      idx = nameList.indexOf(name, idx + 1);
    }

    return idx;
  }

}
