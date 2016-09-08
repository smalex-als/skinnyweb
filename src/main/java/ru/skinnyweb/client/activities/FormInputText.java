package ru.skinnyweb.client.activities;

import com.google.gwt.dom.client.InputElement;

import elemental.dom.Element;
import elemental.dom.NodeList;

public class FormInputText implements FormControl {
  private InputElement input; 

  public FormInputText(Element el) {
    NodeList nodes = el.getElementsByTagName("input");
    this.input = nodes.getLength() > 0 ? (InputElement) nodes.item(0) : null;
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
