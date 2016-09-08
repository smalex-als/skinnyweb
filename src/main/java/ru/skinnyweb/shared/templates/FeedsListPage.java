package ru.skinnyweb.shared.templates;

import java.util.Map;

import com.googlecode.jatl.client.HtmlWriter;

public class FeedsListPage extends StandardPage {

  @Override
  public HtmlWriter renderBody(final Map<String, Object> map) {
    return new HtmlWriter() {
      @Override
      protected void build() {
        indent(indentOff);
        div().classAttr("page");

        div().classAttr("mainButtons");
        write(buttonImage("buttonNew", "fa-plus"));
        text(" ");
        write(buttonImage("buttonRefresh", "fa-refresh"));
        end(); // mainButtons

        div().classAttr("feedsContainer");
        write(printFeeds(this, map));
        end();

        end(); // div page
      }};
  }

  private HtmlWriter printFeeds(HtmlWriter html, final Map<String, Object> map) {
    return new HtmlWriter() {
      @Override
      protected void build() {
        for (Map<String, Object> feed : getList(map, "feeds")) {
          div().classAttr("feed");
          attr("data-id", getString(feed, "id"));

          div();
          a().href(getString(feed, "href")).classAttr("link");
          span().classAttr("name").text(getString(feed, "name")).end();
          end();  // a
          
          span().classAttr("badge").text(String.valueOf(getNumber(feed, "count"))).end();

          end(); 

          end();
        }
    }};
  }

  private void customButton(HtmlWriter html, String name, String title) {
    html.div().classAttr("pure-button " + name);
    html.text(title);
    html.end();
  }
}
