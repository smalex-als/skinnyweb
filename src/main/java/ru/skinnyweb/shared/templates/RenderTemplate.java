package ru.skinnyweb.shared.templates;

import java.util.Map;

/**
 * Created by smalex on 09/04/15.
 */
public interface RenderTemplate {
  void render(RenderContext ctx, Map<String, Object> map, String mode);
}
