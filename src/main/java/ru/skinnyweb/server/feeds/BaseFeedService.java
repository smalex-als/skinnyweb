package ru.skinnyweb.server.feeds;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import ru.skinnyweb.server.api.Id;
import ru.skinnyweb.server.api.IdFactory;
import ru.skinnyweb.server.api.QueryService;
import ru.skinnyweb.server.api.RemoteApi;
import ru.skinnyweb.server.model.BaseModel;
import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.model.Post;
import ru.skinnyweb.server.model.PostImport;
import ru.skinnyweb.server.utils.DigestUtils;
import ru.skinnyweb.server.utils.Lists;
import ru.skinnyweb.server.utils.StringUtils;

public class BaseFeedService implements FeedService {
  private static final Logger log = Logger.getLogger(BaseFeedService.class.getName());
  private static final DateTimeFormatter GMT_FORMATTER = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z");
  private final RemoteApi remoteApi = new RemoteApi();
  private final QueryService queryService;

  public BaseFeedService(QueryService queryService) {
    this.queryService = queryService;
  }

  public void putItems(List<Post> entities) {
    List<Id> keys = Lists.newArrayList();
    for (Post post : entities) {
      String href = post.getHref();
      keys.add(IdFactory.createId("PostImport", DigestUtils.md5Hex(href)));
    }
    Map<Id, BaseModel> map = queryService.get(keys);
    List<BaseModel> toSave = Lists.newArrayList();
    for (Post post : entities) {
      String href = post.getHref();
      Id key = IdFactory.createId("PostImport", DigestUtils.md5Hex(href));
      if (!map.containsKey(key)) {
        PostImport importEntity = new PostImport(key);
        toSave.add(importEntity);
        toSave.add(post);
        map.put(key, importEntity);
      }
    }
    queryService.put(toSave);
  }

  @Override
  public List<Post> fetch(Feed feed) throws Exception {
    String href = feed.getHref();
    if (!StringUtils.hasText(href)) {
      log.warning("empty href");
      return Lists.newArrayList();
    }
    final String body = remoteApi.fetchUrl(href);
    if (body == null) {
      log.warning("empty body");
      return Lists.newArrayList();
    }
    log.info("length = " + body.length());
    Document document = Jsoup.parse(body, "", Parser.xmlParser());
    List<Post> posts = pageListProcess(document, feed);
    putItems(posts);
    return posts;
  }

  protected List<Post> pageListProcess(Document parse, Feed feed) {
    long feedId = feed.getId();
    final List<Post> posts = Lists.newArrayList();
    for (Element element : parse.select("item")) {
      final Elements img = element.select("enclosure");
      final String href = element.select("guid").text();
      final String name = element.select("title").text();
      final String date = element.select("pubDate").text();
      final String description = element.select("description").text();
      final String category = element.select("category").text();
      final String src = img.attr("url");
      
      Post post = new Post();
      post.setFeedId(feedId);
      post.setCreated(new Date());
      post.setName(name);
      post.setDescription(description);
      post.setHref(href);
      post.setSrc(src);
      post.setCategory(category);
      post.setDate(GMT_FORMATTER.parseDateTime(date).toDate());
      post.setUnread(true);
      post.setArchived(false);
      posts.add(post);
    }
    return posts;
  }
}
