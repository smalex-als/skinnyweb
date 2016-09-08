package ru.skinnyweb.client.activities;

import elemental.dom.Element;
import elemental.dom.NodeList;
import elemental.html.TextAreaElement;

public class FormTextArea implements FormControl {
  private TextAreaElement input; 

  public FormTextArea(Element el) {
    NodeList nodes = el.getElementsByTagName("textarea");
    this.input = nodes.getLength() > 0 ? (TextAreaElement) nodes.item(0) : null;
  }

  @Override
  public String getName() {
    return input.getName();
  }

  @Override
  public Object getValue() {
    return input.getValue();
  }
}
