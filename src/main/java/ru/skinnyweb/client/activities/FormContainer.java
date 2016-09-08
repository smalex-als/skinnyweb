package ru.skinnyweb.client.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import elemental.dom.Element;
import ru.skinnyweb.client.util.DomUtils;

public class FormContainer {
  private static final Logger log = Logger.getLogger(FormContainer.class.getName());
  private final List<FormControl> controls = new ArrayList<FormControl>();

  public void decorate(final Element element) {
    for (Element el : DomUtils.getElementsByClassName(element, "form-field-container")) {
      String dataType = el.getAttribute("data-type");
      FormControl formControl = null;
      if ("input-text".equals(dataType)) {
        formControl = new FormInputText(el);
      } else if ("textarea".equals(dataType)) {
        formControl = new FormTextArea(el);
      } else if ("button".equals(dataType)) {
      } else {
        log.info("error = uknown data type " + dataType);
      }
      if (formControl != null) {
        controls.add(formControl);
      }
    }
  }

  public Map<String, Object> updateModel() {
    Map<String, Object> map = new HashMap<String, Object>();
    for (FormControl control : controls) {
      map.put(control.getName(), control.getValue());
    }
    return map;
  }
}
