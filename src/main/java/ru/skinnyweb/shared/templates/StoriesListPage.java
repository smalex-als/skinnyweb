package ru.skinnyweb.shared.templates;

import java.util.Map;
import java.util.logging.Logger;

import com.googlecode.jatl.client.HtmlWriter;

import ru.skinnyweb.client.util.StringUtils;

public class StoriesListPage extends StandardPage {
  private static final Logger log = Logger.getLogger(StoriesListPage.class.getName());

  public HtmlWriter renderArticles(final Map<String, Object> map) {
    return new HtmlWriter() {
      @Override
      protected void build() {
        indent(indentOff);
        printArticles(this, map);
      }};
  }

  private void customButton(HtmlWriter html, String name, String title) {
    html.div().classAttr("pure-button " + name);
    html.text(title);
    html.end();
  }

  public HtmlWriter renderBody(final Map<String, Object> map) {
    return new HtmlWriter() {
      @Override
      protected void build() {
        indent(indentOff);
        div().classAttr("page");

        div().classAttr("feed feedInfo").attr("data-id", getString(map, "feed.id"));
        text(getString(map, "feed.name"));
        end();

        div().classAttr("folders pure-menu pure-menu-horizontal");
        ul().classAttr("pure-menu-list");
        for (Map<String, Object> folder : getList(map, "folders")) {
          li().classAttr("pure-menu-item" 
              + (getBoolean(folder, "selected") ? " pure-menu-selected" : ""));
          a().classAttr("pure-menu-link").href(getString(folder, "href"));
          text(getString(folder, "name"));
          end();
          end();
        }
        end();
        end();

        div().classAttr("mainButtons");
        write(buttonImage("buttonBack", "fa-level-up fa-rotate-270", "Back", "/"));
        text(" ");
        write(buttonImage("buttonRefresh", "fa-refresh", "Refresh", "#"));
        text(" ");
        write(buttonImage("buttonArchiveAll", "fa-archive", "Archive All", "#"));
        end();

        div().classAttr("articlesContainer");
        printArticles(this, map);
        end();

        end(); // div page
      }};
  }

  private void printArticles(HtmlWriter html, final Map<String, Object> map) {
    for (Map<String, Object> article : getList(map, "articles")) {
      html.div().classAttr("article");
      html.attr("data-id", getString(article, "id"));

      html.div();
      html.span().classAttr("date").text(getString(article, "date")).end();
      html.span().text(" ").end();

      html.a().classAttr("link").href(getString(article, "href"));
      html.span().classAttr("name").text(getString(article, "name")).end();
      html.end();  // a
      html.end(); 

      html.div().classAttr("description").text(getString(article, "description")).end();
      
      String category = getString(article, "category");
      if (StringUtils.hasText(category)) {
        html.div().classAttr("category").text(category).end();
      }

      html.ul();
      if (getBoolean(article, "starred")) {
        html.li().classAttr("pure-button button-small buttonUnpin");
        html.i().classAttr("fa fa-star").text("").end();
        html.end();
      } else {
        html.li().classAttr("pure-button button-small buttonPin");
        html.i().classAttr("fa fa-star-o").text("").end();
        html.end();
      }
      html.li().classAttr("pure-button button-small buttonArchive");
      html.i().classAttr("fa fa-archive").text("").end();
      html.end();

      html.end(); // ul

      html.end();
    }
  }
}
