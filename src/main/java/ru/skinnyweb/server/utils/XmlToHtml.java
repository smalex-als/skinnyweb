package ru.skinnyweb.server.utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlToHtml {
  public static String xmlToTable(String xml) {
    StringBuilder sb = new StringBuilder();
    Document doc = Utils.newDocumentFromString(xml);
    Element[] elements = Utils.getElements(doc, doc.getDocumentElement());
    
    if (elements.length != 0) {
      List<String> columns = new ArrayList<String>();
      Element el = elements[0];
      sb.append("<table id='orders'>");
      sb.append("<tr>");
      for (Element child : Utils.getElements(doc, el)) {
        sb.append("<th>");
        sb.append(child.getTagName().toLowerCase());
        sb.append("</th>");
        columns.add(child.getTagName());
      }
      sb.append("</tr>");
      for (Element item : elements) {
        sb.append("<tr>");
        for (Element child : Utils.getElements(doc, item)) {
          sb.append("<td>");
          if (!printChildren(doc, child, sb)) {
            sb.append(Utils.elementContentToString(child));
          }
          sb.append("</td>");
        }
        sb.append("</tr>");
      }
      sb.append("</table>");
    }
    return sb.toString();
  }

  private static boolean printChildren(Document doc, Element parent, 
      StringBuilder sb) {
    Element[] elements = Utils.getElements(doc, parent);
    if (elements.length == 0) {
      return false;
    }
    if (elements.length == 1) {
      Element el = elements[0];
      if (!printChildren(doc, el, sb)) {
        sb.append(Utils.elementContentToString(el));
      }
    } else {
      for (Element el : elements) {
        sb.append(el.getTagName());
        if (!printChildren(doc, el, sb)) {
          sb.append(": ");
          sb.append(Utils.elementContentToString(el));
        }
        sb.append("<br>");
      }
    }
    return true;
  }
}
