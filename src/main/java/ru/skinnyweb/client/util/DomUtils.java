package ru.skinnyweb.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.DocumentFragment;
import elemental.dom.Element;
import elemental.dom.NamedNodeMap;
import elemental.dom.Node;
import elemental.dom.NodeList;
import elemental.dom.Text;
import elemental.html.HTMLCollection;

/**
 * Created by smalex on 08/07/15.
 */
public class DomUtils {
  private static final Logger log = Logger.getLogger(DomUtils.class.getName());
  private static final boolean INNER_HTML_NEEDS_SCOPED_ELEMENT = Browser.getWindow().getNavigator()
      .getUserAgent().toLowerCase().contains("msie");
  private static final Element[] EMPTY = new Element[0];

  public static Element[] getElementsByClassName(String className) {
    Document doc = Browser.getDocument();
    final NodeList els = doc.getElementsByClassName(className);
    if (els.getLength() == 0) {
      return EMPTY;
    }
    Element[] copy = new Element[els.getLength()];

    for (int i = 0; i < els.getLength(); i++) {
      Element item = (Element) els.item(i);
      copy[i] = item;
    }
    return copy;
  }

  public static Element[] getElementsByClassName(Element parent, String className) {
    assert parent != null;
    int dotIndex = className.indexOf('.');
    if (dotIndex != -1) {
      String[] names = className.split("\\.");
      for (int i = 0; i < names.length - 1; i++) {
        parent = getElementByClassName(parent, names[i]);
        assert parent != null;
      }
      className = names[names.length - 1];
    }
    final NodeList els = parent.getElementsByClassName(className);
    if (els.getLength() == 0) {
      return EMPTY;
    }
    Element[] copy = new Element[els.getLength()];

    for (int i = 0; i < els.getLength(); i++) {
      Element item = (Element) els.item(i);
      copy[i] = item;
    }
    return copy;
  }

  public static Element getElementByClassName(String className) {
    Element[] els = getElementsByClassName(className);
    return els.length > 0 ? els[0] : null;
  }

  public static Element getElementByClassName(Element parent, String className) {
    Element[] els =  getElementsByClassName(parent, className);
    return els.length > 0 ? els[0] : null;
  }

  public static Node childrenToNode_(Document doc, Element tempDiv) {
    if (tempDiv.getChildElementCount() == 1) {
      return tempDiv.removeChild(tempDiv.getFirstChild());
    } else {
      DocumentFragment fragment = doc.createDocumentFragment();
      while (tempDiv.getFirstChild() != null) {
        fragment.appendChild(tempDiv.getFirstChild());
      }
      return fragment;
    }
  }

  public static Node htmlToDocumentFragment_(Document doc, String htmlString) {
    Element tempDiv = doc.createElement("DIV");
    if (INNER_HTML_NEEDS_SCOPED_ELEMENT) {
      tempDiv.setInnerHTML("<br>" + htmlString);
      tempDiv.removeChild(tempDiv.getFirstChild());
    } else {
      tempDiv.setInnerHTML(htmlString);
    }
    return childrenToNode_(doc, tempDiv);
  }

  public static void removeChildren(Element el) {
    while (el.getFirstChild() != null) {
      el.removeChild(el.getFirstChild());
    }
  }

  public static Element getFirstChild(Element el) {
    HTMLCollection els = el.getChildren();
    for (int i = 0; i < els.getLength(); i++) {
      Node node = els.item(i);
      if (node instanceof Element) {
        return (Element) node;
      }
    }
    return null;
  }

  public static List<Element> getElements(Element el) {
    HTMLCollection els = el.getChildren();
    List<Element> res = new ArrayList<Element>();
    for (int i = 0; i < els.getLength(); i++) {
      Node node = els.item(i);
      if (node instanceof Element) {
        res.add((Element) node);
      }
    }
    return res;
  }

  public static String dump(Element el) {
    StringBuilder sb = new StringBuilder();
    appendElement(sb, el, "");
    return sb.toString();
  }

  private static void appendElement(StringBuilder sb, Element el, String prefix) {
    sb.append(prefix);
    sb.append("<").append(el.getTagName().toLowerCase());
    NamedNodeMap attrs = el.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node attr = attrs.item(i);
      sb.append(" ");
      sb.append(attr.getNodeName()).append("=\"");
      sb.append(attr.getNodeValue()).append("\"");
    }
    sb.append(">");
    HTMLCollection children = el.getChildren();
    if (children.getLength() > 0) {
      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);
        if (node instanceof Element) {
          sb.append("\n");
          appendElement(sb, (Element) node, "  " + prefix);
        } else if (node instanceof Text) {
          sb.append(((Text)node).getTextContent());
        }
      }
      sb.append("\n");
      sb.append(prefix);
    } else {
      sb.append(el.getTextContent());
    }
    sb.append("</").append(el.getTagName().toLowerCase()).append(">");
  }
}
