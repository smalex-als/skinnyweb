package ru.skinnyweb.shared.templates;

import java.util.Map;

import com.googlecode.jatl.client.HtmlWriter;

public class StoryViewPage extends StandardPage {
  public HtmlWriter renderBody(final Map<String, Object> map) {
    return new HtmlWriter() {
      @Override
      protected void build() {
        indent(indentOff);
        Map<String, Object> article = getInnerMap(map, "story");

        div().classAttr("page");

        div().classAttr("mainButtons");
        write(buttonImage("buttonBack", "fa-level-up fa-rotate-270", "Back", getString(article, "back")));
        if (getBoolean(article, "archived")) {
        } else {
          text(" ");
          write(buttonImage("buttonArchive", "fa-archive", "Archive", getString(article, "archive")));
        }
        text(" ");
        boolean isStarred = getBoolean(article, "starred");
        write(buttonImage("buttonUnstar " + (isStarred ? "": "buttonHide"), 
              "fa-star", "Unstar", "#"));
        write(buttonImage("buttonStar " + (isStarred ? "buttonHide" : ""), 
              "fa-star-o", "Star", "#"));
        text(" ");
        write(buttonImage("buttonPrev", "fa-chevron-left", "Prev", "#"));
        text(" ");
        write(buttonImage("buttonNext", "fa-chevron-right", "Next", "#"));
        end(); // mainButtons

        div().classAttr("articleContainer");
        div().classAttr("article");
        attr("data-id", getString(article, "id"));

        h2().text(getString(article, "name")).end();
        div().classAttr("date").text(getString(article, "date")).end();

        div().classAttr("content");
        raw(getString(article, "content")).end();

        div().classAttr("originalLink");
        a().href(getString(article, "originalHref"))
          .target("_blank").text(getString(article, "originalHref")).end();
        end();

        end(); // article

        end(); // articleContainer

        div().classAttr("footer");
        div().classAttr("innerFooter");
        // customButton(this, "buttonPrevPage", "<");
        // text(" ");
        // customButton(this, "buttonNextPage", ">");
        end();
        end();

        div().classAttr("fontTest").style("position: absolute; visibility: hidden; height: auto; width: auto; white-space: nowrap;").end();

        end(); // div page
      }};
  }

  private void customButton(HtmlWriter html, String name, String title) {
    html.div().classAttr("pure-button " + name);
    html.text(title);
    html.end();
  }
}
