package ru.skinnyweb.server.feeds;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import ru.skinnyweb.server.api.RemoteApi;

public class PageContentFetcher {
  private static final Logger log = Logger.getLogger(PageContentFetcher.class.getName());
  private final RemoteApi remoteApi = new RemoteApi();

  public String getBody(String href) {
    try {
      String body = remoteApi.fetchUrl(href);
      if (body != null) {
        Document doc = Jsoup.parse(body);
        return parseArticle(doc);
      }
    } catch (Exception ioe) {
      log.log(Level.SEVERE, "Failed to fetch items", ioe);
    }
    return null;
  }
  
  String parseArticle(Document doc) {
    String[] bodyExpressions = new String[]{
      "div.article-entry",
      "div.jobdetail",
      "p.story-body-text", // nytimes
      "[itemprop*=articleBody]",
      "[itemprop*=description]",
      "[itemprop*=blogPost]",
      "[property*=articleBody]",
      "div[id^=content-body]",
      "#content-wrap", 
      "#articleText", 
      "#articleBody", 
      "#mainBody",
      "div.entry_content",
      "div.mw-body-content",
      "div.posts_list", 
      "div.content", // weworkremotely.com
      "div.article",
      "div.b-topic__body",
      "div.articleContent",
      "div.blog-entry",
      "article"
    };
    Elements bodyParts = null;
    for (String path : bodyExpressions) {
      bodyParts = doc.select(path);
      if (!bodyParts.isEmpty()) {
        log.info("path = " + path);
        break;
      }
    }
    if (bodyParts == null) {
      return null;
    }
      
    StringBuilder sb = new StringBuilder();
    for (Element el : bodyParts) {
      sb.append(getPlainText(el));
    }

    String text= sb.toString();
    text = Pattern.compile("^ +", Pattern.MULTILINE).matcher(text).replaceAll("");

    return text.replaceAll("\n\n+", "\n\n").trim();
  }
  
  /**
   * Format an Element to plain-text
   * @param element the root element to format
   * @return formatted text
   */
  public String getPlainText(Element element) {
      FormattingVisitor formatter = new FormattingVisitor();
      NodeTraversor traversor = new NodeTraversor(formatter);
      traversor.traverse(element); // walk the DOM, and call .head() and .tail() for each node

      return formatter.accum.toString();
  }

  // the formatting rules, implemented in a breadth-first DOM traverse
  private class FormattingVisitor implements NodeVisitor {
    private StringBuilder accum = new StringBuilder(); // holds the accumulated text

    // hit when the node is first seen
    public void head(Node node, int depth) {
      String name = node.nodeName();
      if (node instanceof TextNode) {
        append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
      } else if (name.equals("li")) {
        append("\n * ");
      } else if (name.equals("dt")) {
        append("  ");
      } else if (name.equals("img"))  {
        String src = node.attr("src");
        log.info("found img = " + node);
        append("<img src='" + src + "'/ >");
      } else if (StringUtil.in(name, "ul")) {
        append("\n");
      } else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "tr")) {
        append("\n\n");
      }
    }

    // hit when all of the node's children (if any) have been visited
    public void tail(Node node, int depth) {
      String name = node.nodeName();
      if (StringUtil.in(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5"))
        append("\n\n");
      // else if (name.equals("a"))
      //  append(String.format(" <%s>", node.absUrl("href")));
    }

    // appends text to the string builder with a simple word wrap method
    private void append(String text) {
      accum.append(text);
    }
  }
}
