package ru.skinnyweb.server.feeds;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ru.skinnyweb.server.api.QueryService;
import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.model.Post;
import ru.skinnyweb.server.utils.Lists;
import ru.skinnyweb.server.utils.StringUtils;

/**
 * Created by smalex on 14/06/15.
 */
public class LentaFeed extends BaseFeedService {
  private static final Logger log = Logger.getLogger(LentaFeed.class.getName());
  private static final DateTimeFormatter GMT_FORMATTER 
    = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z");

  public LentaFeed(QueryService queryService) {
    super(queryService);
  }

  protected List<Post> pageListProcess(Document doc, Feed feed) {
    long feedId = feed.getId();
    final List<Post> posts = Lists.newArrayList();
    for (Element element : doc.select("item")) {
      Post post = new Post();
      post.setFeedId(feedId);
      post.setCreated(new Date());

      final String href = element.select("link").text();
        // .replaceFirst("^http:", "https:");
      if (!StringUtils.hasText(href)) {
        continue;
      }
      final String name = element.select("title").text();
      final String date = element.select("pubDate").text()
            .replace(" GMT", " Z"); // fix for NYT
      String description = html2text(
          element.select("description").text());
      description = description.replaceAll("Read More$", "");
      final Elements img = element.select("enclosure");
      if (!img.isEmpty()) {
        post.setSrc(img.attr("url"));
      }
      StringBuilder sb = new StringBuilder();
      for (Element el : element.select("category")) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        sb.append(el.text());
      }
      final String category = sb.toString();
      log.info("category = " + category);
      post.setName(name);
      post.setDescription(description);
      post.setHref(href);
      post.setCategory(category);
      post.setDate(GMT_FORMATTER.parseDateTime(date).toDate());
      post.setUnread(true);
      post.setArchived(false);
      posts.add(post);
    }
    return posts;
  }

  public static String html2text(String html) {
    html = html.replace("&nbsp;", " ");
    return Jsoup.parse(html).text().trim();
  }
}
