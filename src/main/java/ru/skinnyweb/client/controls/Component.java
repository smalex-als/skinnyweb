package ru.skinnyweb.client.controls;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.events.EventRemover;
import ru.skinnyweb.client.util.DomUtils;

/**
 * Created by smalex on 09/07/15.
 */
public class Component {
  private static final Logger log = Logger.getLogger(Component.class.getName());
  private List<EventRemover> handlers;
  private List<Component> children;
  private Element element;
  private Component parent;
  private boolean inDocument;
  private boolean wasDecorated;
  private Object model;
  protected final Document doc = Browser.getDocument();

  public Component getParent() {
    return parent;
  }

  public void setParent(Component parent) {
    // check previous parent == null

    this.parent = parent;

    // reroute events
  }

  public Object getModel() {
    return model;
  }

  public void setModel(Object model) {
    this.model = model;
  }

  public boolean isInDocument() {
    return inDocument;
  }

  public void render(Element parentElement) {
    renderInternal(parentElement, null);
  }

  protected void createDom() {
    element = doc.createElement("DIV");
  }

  public void decorate(Element element) {
    if (inDocument) {
      throw new RuntimeException("Already in document");
    } else if (element != null && canDecorate(element)) {
      wasDecorated = true;

      // Call specific component decorate logic.
      decorateInternal(element);

      enterDocument();
    }
  }

  protected boolean canDecorate(Element element) {
    return true;
  }

  private void renderInternal(Element parentElement, Element beforeNode) {
    if (inDocument) {
      throw new RuntimeException("Already in document");
    }
    if (element == null) {
      createDom();
    }
    if (parentElement != null) {
      parentElement.insertBefore(element, beforeNode);
    } else {
      doc.getBody().appendChild(element);
    }
    if (parent == null || parent.isInDocument()) {
      enterDocument();
    }
  }

  protected void decorateInternal(Element element) {
    setElement(element);
  }

  public void enterDocument() {
    inDocument = true;
    if (children != null) {
      for (Component child : children) {
        if (!child.isInDocument() && child.getElement() != null) {
          child.enterDocument();
        }
      }
    }
  }

  public void exitDocument() {
    log.info("exitDocument");
    // TODO надо ли? 
    cancelAllHandlerRegistrations();
    if (children != null) {
      for (Component child : children) {
        if (child.isInDocument()) {
          child.exitDocument();
        }
      }
    }
    inDocument = false;
  }

  public Element getElement() {
    return element;
  }

  public Element getContentElement() {
    return element;
  }

  public void setElement(Element element) {
    this.element = element;
  }

  protected void addHandlerRegistration(EventRemover eventRemover) {
    if (handlers == null) {
      handlers = new LinkedList<EventRemover>();
    }
    handlers.add(eventRemover);
  }

  public Element getElementByClassNameRequired(String className) {
    Element el = getElementByClassName(className);
    assert el != null;
    return el;
  }

  protected Element[] getElementsByClassName(String className) {
    return DomUtils.getElementsByClassName(getElement(), className);
  }

  public Element getElementByClassName(String className) {
    return DomUtils.getElementByClassName(getElement(), className);
  }

  public void addChild(Component child, boolean render) {
    if (children == null) {
      children = new ArrayList<Component>();
    }
    if (child.isInDocument() && (render || !inDocument)) {
      throw new RuntimeException("Already rendered");
    }

    child.setParent(this);
    children.add(child);
    if (child.isInDocument() && this.isInDocument() 
        && child.getParent() == this) {
      // Changing the position of an existing child, move the DOM node.
      Element contentElement = this.getContentElement();
      contentElement.appendChild(child.getElement());
    } else if (render) {
      if (this.element == null) {
        this.createDom();
      }
      child.renderInternal(this.getContentElement(), null);
    } else if (this.isInDocument() && !child.isInDocument() 
        && child.getElement() != null 
        && child.getElement().getParentNode() != null) {
      child.enterDocument();
    }
  }

  public int getChildCount() {
    return children == null ? 0 : children.size();
  }

  public Component getChildAt(int index) {
    return children == null ? null : children.get(index);
  }

  public void removeChild(Component child) {
    if (children == null) {
      return;
    }
    if (child != null) {
      children.remove(child);

      child.exitDocument();
      Element el = child.getElement();
      if (el != null) {
        el.getParentElement().removeChild(el);
      }
      child.setParent(null);
    }
  }

  public List<Component> removeChildren() {
    if (children != null) {
      while (children.size() > 0) {
        removeChild(children.get(children.size() - 1));
      }
    }
    return null;
  }

  /**
   * Remove all collected oldHandlers, and remove them from the collection
   */
  protected void cancelAllHandlerRegistrations() {
    if (handlers != null) {
      for (EventRemover hr : handlers) {
        hr.remove();
      }
      handlers.clear();
    }
  }
}
