package ru.skinnyweb.shared.templates;

import java.util.Map;

import com.googlecode.jatl.client.HtmlWriter;

public interface PageTemplate {
  String renderPage(Map<String, Object> map);

  HtmlWriter renderBody(final Map<String, Object> map);
}
