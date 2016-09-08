package ru.skinnyweb.shared.templates;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.jatl.client.HtmlWriter;

/**
 * Created by smalex on 09/04/15.
 */
public class RenderContext {
  // private final Map<String, Object> info;
  private final Map<String, Object> map;
  private final HtmlWriter html;
  private final Map<String, RenderTemplate> handlers = new HashMap<String, RenderTemplate>();

  public RenderContext(Map<String, Object> map, HtmlWriter html) {
    this.map = map;
    this.html = html;
    // this.info = BaseTemplates.getInnerMap(map, "global.info");
  }

  public Map<String, Object> getMap() {
    return map;
  }

  // public Map<String, Object> getInfo() {
  //   return info;
  // }

  public void addHandler(String name, RenderTemplate template) {
    handlers.put(name, template);
  }

  public void render(String name, Map<String, Object> entity) {
    render(name, entity, null);
  }

  public void render(String name, Map<String, Object> entity, String mode) {
    final RenderTemplate handler = handlers.get(name);
    if (handler != null) {
      handler.render(this, entity, mode);
    }
  }

  public HtmlWriter getHtml() {
    return html;
  }
}

