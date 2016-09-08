package ru.skinnyweb.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Various XML utilities.
 *
 * @author simonjsmith, ksim
 * @version 1.2 - ksim - March 10th, 2007 - Added functions regarding DOM manipulation
 */

public class Utils {
  private static final transient Logger log = Logger.getLogger(Utils.class.getName());
  private static DocumentBuilderFactory factory = null;
  private static TransformerFactory tf;
  private static ThreadLocal<DocumentBuilder> builderThreadLocal = new ThreadLocal<DocumentBuilder>();
  private static Element[] EMPTY_ELEMENTS = new Element[0];

  static {
    factory = DocumentBuilderFactory.newInstance();
    tf = TransformerFactory.newInstance();
  }

  public static DocumentBuilder getBuilder() {
    DocumentBuilder builder = builderThreadLocal.get();
    if (builder == null) {
      try {
        builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new SimpleSaxErrorHandler(log));
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      }
      builderThreadLocal.set(builder);
    }
    return builder;
  }

  public static Document newEmptyDocument() {
    return getBuilder().newDocument();
  }

  public static Element findElementOrContainer(Document document, Element parent, String element) {
    NodeList nl = parent.getElementsByTagName(element);
    if (nl.getLength() == 0) {
      return null;
    }
    return (Element) nl.item(0);
  }

  public static Element findContainerElseCreate(Document document, Element parent, String child) {
    NodeList nl = parent.getElementsByTagName(child);
    if (nl.getLength() == 0) {
      parent.appendChild(document.createElement(child));
    }
    return (Element) parent.getElementsByTagName(child).item(0);
  }

  public static Element createNewContainer(Document document, Element parent, String childElement) {
    Element child = document.createElement(childElement);
    parent.appendChild(child);
    return child;
  }

  public static Element findElementElseCreateAndSet(Document document, Element parent, String child, String value) {
    Element ret = null;
    NodeList nl = parent.getElementsByTagName(child);
    if (nl.getLength() == 0) {
      parent.appendChild(document.createElement(child));
      ret = (Element) parent.getElementsByTagName(child).item(0);
      ret.appendChild(document.createTextNode(value));
    }
    return ret;
  }

  public static Element findSingleElementByXPath(Element elementToStartSearchFrom, String xpath) {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    try {
      return (Element) xPath.evaluate(xpath, elementToStartSearchFrom, XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      log.log(Level.SEVERE, "xpath failed", e);
    }
    return null;
  }


  public static Element[] findElementsByXPath(Element elementToStartSearchFrom, String xpath) {
    try {
      final XPath xPath = XPathFactory.newInstance().newXPath();
      return toElements((NodeList) xPath.evaluate(xpath, elementToStartSearchFrom, XPathConstants.NODESET));
    } catch (XPathExpressionException e) {
      log.log(Level.SEVERE, "xpath failed", e);
    }
    return EMPTY_ELEMENTS;
  }


  public static Element findElementElseCreateAndSet(Document document,
                                                    Element parent, String child, boolean value) {
    return findElementElseCreateAndSet(document, parent, child, value + "");
  }

  public static Element findElementAndSetElseCreateAndSet(Document document, Element parent, String child,
                                                          String value) {
    NodeList nl = parent.getElementsByTagName(child);
    if (nl.getLength() == 0) {
      parent.appendChild(document.createElement(child));
    }
    Element ret = (Element) parent.getElementsByTagName(child).item(0);
    if (ret.getFirstChild() != null) {
      ret.removeChild(ret.getFirstChild());
    }
    ret.appendChild(document.createTextNode(value));
    return ret;
  }

  public static Element findElementAndSetElseCreateAndSet(Document document, Element parent, String child,
                                                          boolean value) {
    return findElementAndSetElseCreateAndSet(document, parent, child, "" + value);
  }

  public static Element findElementAndSetElseCreateAndSet(Document document,
                                                          Element parent, String child, float value) {
    return findElementAndSetElseCreateAndSet(document, parent, child, "" + value);
  }

  public static Element createNewElementAndSetXml(Document document, Element parent, String childElement,
                                                  String childValue) {
    if (StringUtil.isEmptyOrWhitespace(childValue)) {
      return createNewContainer(document, parent, childElement);
    }
    childValue = StringUtil.unescapeHTML2(childValue);
    if (childValue.indexOf('<') != -1 || childValue.indexOf('&') != -1 || childValue.indexOf('>') != -1) {
      Document tmpDocument = Utils.newDocumentFromString("<?xml version='1.0' ?>\n" +
          "<!DOCTYPE " + childElement + " SYSTEM \"http://localhost/symbols.ent\">\n" +
          "<" + childElement + ">" + childValue + "</" + childElement + ">\n");
      if (tmpDocument == null) {
        return createNewContainer(document, parent, childElement);
      }
      return (Element) parent.appendChild(document.importNode(tmpDocument.getDocumentElement(), true));
    }
    Element child = document.createElement(childElement);
    parent.appendChild(child);
    child.setNodeValue(childValue);
    child.appendChild(document.createTextNode(childValue));
    return child;
  }

  public static Element replaceElementAndSetXml(Document document, Element childToReplace, String childValue) {
    if (StringUtil.isEmptyOrWhitespace(childValue)) {
      return childToReplace;
    }
    childValue = StringUtil.unescapeHTML2(childValue);
    if (childValue.indexOf('<') != -1 || childValue.indexOf('&') != -1 || childValue.indexOf('>') != -1) {
      Document tmpDocument = Utils.newDocumentFromString("<?xml version='1.0' ?>\n" +
          "<!DOCTYPE " + childToReplace.getNodeName() + " SYSTEM \"http://localhost/symbols.ent\">\n" +
          "<" + childToReplace.getNodeName() + ">" + childValue + "</" + childToReplace.getNodeName() + ">\n");
      if (tmpDocument == null) {
        return childToReplace;
      }
      final Node newChild = document.importNode(tmpDocument.getDocumentElement(), true);
      childToReplace.getParentNode().replaceChild(newChild, childToReplace);
      return (Element) newChild;
    }
    childToReplace.setNodeValue(childValue);
    childToReplace.appendChild(document.createTextNode(childValue));
    return childToReplace;
  }

  public static int getNestingLevel(Element element) {
    int level = 0;
    Node parent = element.getParentNode();
    while (parent != null) {
      level++;
      parent = parent.getParentNode();
    }
    return level;
  }

  public static Element createNewElementAndSet(Document document, Element parent, String childElement,
                                               String childValue) {
    if (childValue == null) {
      childValue = "";
    }
    Element child = document.createElement(childElement);
    parent.appendChild(child);
    child.setNodeValue(StringUtil.unescapeHTML(childValue));
    child.appendChild(document.createTextNode(childValue));
    return child;
  }

  public static Element createNewElementAndSetAndAttribute(Document document, Element parent, String childElement,
                                                           String childValue,
                                                           String attributeName, String attributeValue) {
    Element child = createNewElementAndSet(document, parent, childElement, childValue);
    child.setAttribute(attributeName, attributeValue);
    return child;
  }

  public static Element createNewElementAndSet(Document document, Element parent, String childElement,
                                               float childValue) {
    return createNewElementAndSet(document, parent, childElement, String.valueOf(childValue));
  }

  public static Element createNewElementAndSet(Document document, Element parent, String childElement,
                                               int childValue) {
    return createNewElementAndSet(document, parent, childElement, String.valueOf(childValue));
  }

  public static Element createNewElementAndSet(Document document, Element parent, String childElement,
                                               boolean childValue) {
    return createNewElementAndSet(document, parent, childElement, String.valueOf(childValue));
  }

  public static Element createNewElementAndSet(Document document, Element parent, String childElement,
                                               double childValue) {
    return createNewElementAndSet(document, parent, childElement, String.valueOf(childValue));
  }

  public static String getElementStringValue(Document document,
                                             Element parent, String element) {
    NodeList nl = parent.getElementsByTagName(element);
    if (nl.getLength() == 0) {
      return "";
    }

    Node n = nl.item(0).getFirstChild();
    if (n == null) {
      return "";
    }

    return n.getNodeValue();
  }

  public static Element getElement(Document document, Element parent, String element) {
    NodeList nl = parent.getElementsByTagName(element);
    if (nl.getLength() == 0) {
      return null;
    }

    Node n = nl.item(0);
    if (n == null) {
      return null;
    }

    return (Element) n;
  }

  public static boolean getElementBooleanValue(Document document, Element parent, String element) {
    return Boolean.valueOf(getElementStringValue(document, parent, element));
  }

  public static boolean getElementBooleanValue(Document document, Element parent, String element, boolean defValue) {
    final String value = getElementStringValue(document, parent, element);
    if (StringUtils.hasText(value)) {
      return Boolean.valueOf(value);
    }
    return defValue;
  }

  public static float getElementFloatValue(Document document, Element parent, String element) {
    return Float.parseFloat(getElementStringValue(document, parent, element));
  }

  public static void importElements(Document document, Element parent, Element[] children) {
    for (Element child : children) {
      parent.appendChild(document.importNode(child, true));
    }
  }

  public static void importElementsBefore(Document document, Element parent, Element[] children) {
    Node parentNode = parent.getParentNode();
    for (Element child : children) {
      parentNode.insertBefore(document.importNode(child, true), parent);
    }
  }

  public static long getElementLongValue(Document document, Element parent, String string) {
    return Long.parseLong(getElementStringValue(document, parent, string));
  }

  public static long getElementLongValue(Document document, Element parent, String string, long defValue) {
    final String value = getElementStringValue(document, parent, string);
    if (StringUtils.hasText(value)) {
      return Long.parseLong(value);
    }
    return defValue;
  }

  public static int getElementIntValue(Document document, Element parent, String string) {
    return Integer.parseInt(getElementStringValue(document, parent, string));
  }

  public static int getElementIntValue(Document document, Element parent, String string, int defValue) {
    final String value = getElementStringValue(document, parent, string);
    if (StringUtils.hasText(value)) {
      return Integer.parseInt(value);
    }
    return defValue;
  }

  public static Element[] getElements(Document document, Element parent) {
    if (parent == null) {
      return new Element[]{};
    }

    NodeList nl = parent.getChildNodes();
    return toElements(nl);
  }

  private static Element[] toElements(NodeList nl) {
    if (nl.getLength() == 0) {
      return EMPTY_ELEMENTS;
    }
    List<Element> al = new ArrayList<Element>();

    for (int i = 0; i < nl.getLength(); i++) {
      Node n = nl.item(i);

      if (n instanceof Element) {
        al.add((Element) n);
      }
    }
    return al.toArray(new Element[al.size()]);
  }


  public static Element findContainerWithAttributeValueElseCreate(Document document, Element parent, String element,
                                                                  String attributeName, String attributeValue) {

    NodeList nl = parent.getElementsByTagName(element);
    Element e;
    for (int i = 0; i < nl.getLength(); i++) {
      e = (Element) nl.item(i);
      if (e.getAttribute(attributeName).equals(attributeValue)) {
        return e;
      }
    }

    e = document.createElement(element);
    parent.appendChild(e);
    e.setAttribute(attributeName, attributeValue);

    return e;
  }

  public static Element findContainerWithAttributeValueElseCreateAndSet(Document document, Element parent,
                                                                        String element, String value, String attributeName, String attributeValue) {

    Element e = findContainerWithAttributeValueElseCreate(document, parent,
        element, attributeName, attributeValue);
    e.appendChild(document.createTextNode(value));

    return e;
  }

  public static Element findElementElseCreateAndAttribute(Document document, Element parent, String element,
                                                          String attributeName, String attributeValue) {
    NodeList nl = parent.getElementsByTagName(element);
    Element e = null;

    if (nl.getLength() == 0) {
      parent.appendChild(document.createElement(element));
      e = (Element) parent.getElementsByTagName(element).item(0);
      e.setAttribute(attributeName, attributeValue);
    }

    return e;
  }

  public static Element findElementElseCreateAndSetAndAttribute(Document document, Element parent, String element,
                                                                String value, String attributeName, String attributeValue) {

    Element e = findElementElseCreateAndAttribute(document, parent,
        element, attributeName, attributeValue);
    if (e != null)
      e.appendChild(document.createTextNode(value));

    return e;
  }

  public static String elementContentToString(Element elem) {
    if (elem == null) {
      return null;
    }
    try {
      Transformer trans = tf.newTransformer();
      trans.setOutputProperty("omit-xml-declaration", "yes");
      StringWriter sw = new StringWriter();
      trans.transform(new DOMSource(elem), new StreamResult(sw));
      return stripRootTagName(elem.getNodeName(), sw.toString());
    } catch (TransformerException tEx) {
      tEx.printStackTrace();
    }
    return "";
  }

  public static String stripRootTagName(String elementName, String content) {
    final int start = content.indexOf("<" + elementName);
    if (start >= 0 && content.endsWith("</" + elementName + ">")) {
      final int end = content.indexOf('>');
      if (end > 0) {
        return content.substring(end + 1, content.length() - (elementName.length() + 3));
      }
    }
    return content;
  }

  public static String documentToString(Document document) {
    try {
      Transformer trans = tf.newTransformer();
      StringWriter sw = new StringWriter();
      trans.transform(new DOMSource(document), new StreamResult(sw));
      return sw.toString();
    } catch (TransformerException tEx) {
      tEx.printStackTrace();
    }
    return null;
  }

  public static String documentToString(Document document, boolean omitXmlDeclaration) {
    try {
      Transformer trans = tf.newTransformer();
      trans.setOutputProperty("omit-xml-declaration", omitXmlDeclaration ? "yes" : "no");
      StringWriter sw = new StringWriter();
      trans.transform(new DOMSource(document), new StreamResult(sw));
      return sw.toString();
    } catch (TransformerException tEx) {
      tEx.printStackTrace();
    }
    return null;
  }

  public static String documentToStringPretty(Document document) {
    return documentToStringPretty(document, false);
  }

  public static String documentToStringPretty(Document document, boolean omitXmlDeclaration) {
    return documentToStringPretty(document, "/indent.xsl", omitXmlDeclaration);
  }

  public static String documentToStringPretty(Document document, String xsl) {
    return documentToStringPretty(document, xsl, false);
  }

  public static String documentToStringPretty(Document document, String xsl, boolean omitXmlDeclaration) {
    try {
      StreamSource stylesource = new StreamSource(Utils.class.getResourceAsStream(xsl));

      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer trans = tf.newTransformer(stylesource);
      trans.setOutputProperty("omit-xml-declaration", omitXmlDeclaration ? "yes" : "no");

      StringWriter sw = new StringWriter();
      trans.transform(new DOMSource(document), new StreamResult(sw));

      return sw.toString();

    } catch (TransformerException tEx) {
      tEx.printStackTrace();
    }
    return null;
  }

  public static Document newDocumentFromStringWithEntity(String name, String data) {
    return newDocumentFromStringWithEntity(name, data, false);
  }

  public static Document newDocumentFromStringWithEntity(String name, String data,
                                                         boolean doNotCreateRoot) {
    String xmlString = "<?xml version='1.0' ?>\n"
        + "<!DOCTYPE " + name + " SYSTEM \"http://localhost/symbols.ent\">\n";
    if (doNotCreateRoot) {
      xmlString += data;
    } else {
      xmlString += "<" + name + ">" + data + "</" + name + ">\n";
    }
    return newDocumentFromString(xmlString);
  }

  public static Document newDocumentFromString(String xmlString) {
    try {
      return getBuilder().parse(new InputSource(new StringReader(xmlString)));
    } catch (SAXParseException e) {
      catchXmlError(e, xmlString);
    } catch (SAXException e) {
      catchXmlError(e, xmlString);
    } catch (IOException e) {
      catchXmlError(e, xmlString);
    } catch (NullPointerException e) {
      catchXmlError(e, xmlString);
    }
    return null;
  }

  public static Document newDocumentFromStringValidate(String xmlString) throws IOException, SAXException {
    return getBuilder().parse(new InputSource(new StringReader(xmlString)));
  }

  private static void catchXmlError(Exception e, String xmlString) {
    log.warning("Can't parse document because of " + e);
    if (log.isLoggable(Level.FINE)) {
      log.log(Level.FINE, "catch error", e);
    }
    log.getLevel();
    if (xmlString != null) {
      log.fine("\n=== Document start here ===\n"
          + xmlString
          + "\n=== Document end here ===\n");
    }
  }

  public static Document newDocumentFromInputStream(InputStream in) {
    try {
      return getBuilder().parse(new InputSource(in));
    } catch (SAXException e) {
      catchXmlError(e, null);
    } catch (IOException e) {
      catchXmlError(e, null);
    }
    return null;
  }
}
