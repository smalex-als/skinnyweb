package ru.skinnyweb.shared.templates;

import java.util.Map;

import com.googlecode.jatl.client.HtmlWriter;
import com.googlecode.jatl.client.Indenter;
import com.googlecode.jatl.client.SimpleIndenter;

public abstract class StandardPage extends BaseTemplates implements PageTemplate {
  public static Indenter indentOff = new SimpleIndenter(null, null, null, null);

  public String renderPage(Map<String, Object> map) {
    return renderPage(map, renderBody(map));
  }

  public String renderPage(final Map<String, Object> map, final HtmlWriter body) {
    final String id = getString(map, "activityName");
    final String title = getString(map, "title");
    HtmlWriter writer = new HtmlWriter() {
      @Override
      protected void build() {
        final boolean jsrender = getBoolean(map, "jsrender");
        indent(indentOff);
        html();
        head();
        link().rel("stylesheet").href("/css/mini.css").end();
        script().type("text/javascript").src("/admin/admin.nocache.js").end();
        meta().httpEquiv("content-type").content("text/html; charset=UTF-8").end();
        meta().name("viewport").content("width=device-width, initial-scale=1");

        title().text(title).end();
        end().text("\n"); // head
        body().id(id).text("\n");

        if (!jsrender) {
          div().id("content");
          write(body);
          end();
        } else {
          // script().raw("window['als_now'] = " + map + ";").end();
        }

        indent(indentOn);
        endAll();
        done();
      }};
    return "<!doctype html>\n" + writer.toString(); 
  }

  protected HtmlWriter buttonImage(final String classAttr, final String classImage) {
    return buttonImage(classAttr, classImage, "", "#");
  }

  protected HtmlWriter buttonImage(final String classAttr, final String classImage, 
      final String title, final String href) {
    return new HtmlWriter() {
      @Override
      protected void build() {
        a().href(href).classAttr("pure-button " + classAttr);
        i().classAttr("fa " + classImage).text("").end();
        if (title.length() > 0) {
          raw("&nbsp;").text(" " + title);
        }
        end();
      }
    };
  }
}
