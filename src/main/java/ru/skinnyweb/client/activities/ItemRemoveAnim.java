package ru.skinnyweb.client.activities;

import java.util.logging.Logger;

import com.google.gwt.animation.client.Animation;

import elemental.client.Browser;
import elemental.dom.Element;

public class ItemRemoveAnim extends Animation {
  private static final Logger log = Logger.getLogger(ItemRemoveAnim.class.getName());
  private Element element;
  private boolean hide = false;
  private int height;

  public ItemRemoveAnim(Element element) {
    this.element = element;
  }

  @Override
  protected void onUpdate(double v) {
    v *= v;
    if (v < 0.5d) {
      element.getStyle().setOpacity(1 - 2 * v);
    } else {
      double x = 1.0d;
      if (!hide) {
        hide = true;
        height = element.getClientHeight();
        Element parent = element.getParentElement();
        Element newEl = Browser.getDocument().createDivElement();
        parent.replaceChild(newEl, element);
        element = newEl;
      } else {
        x = 2.0 - 2.0 * v;
      }
      element.getStyle().setHeight(height * x, "px");
    }
  }

  @Override
  protected void onComplete() {
    if (element != null) {
      element.getParentElement().removeChild(element);
      element = null;
    }
  }
}
