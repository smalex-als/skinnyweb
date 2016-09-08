package ru.skinnyweb.server.servlets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.skinnyweb.client.util.NumberUtils;
import ru.skinnyweb.server.api.QueryService;
import ru.skinnyweb.server.feeds.FeedService;
import ru.skinnyweb.server.feeds.LentaFeed;
import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.model.Post;

public class FeedImportServlet extends BaseServlet {
  private static final Logger log = Logger.getLogger(FeedImportServlet.class.getName());
  private QueryService queryService;

  public FeedImportServlet(QueryService queryService) {
    this.queryService = queryService;
  }

  @Override
  protected void innerGet(HttpServletRequest req, HttpServletResponse resp, String json) throws Exception {
    long feedId = NumberUtils.toLong(req.getParameter("feedId"));
    Feed feed = queryService.getFeedById(feedId);
    String name = feed.getName();
    log.info("name -> " + name 
        + " href -> " + feed.getHref());
    FeedService feedService = new LentaFeed(queryService);
    List<Post> posts = feedService.fetch(feed);
    log.info("finish " + name + " found " + posts.size());
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("result", true);
    int count = queryService.getCountInFeed(feed);
    queryService.counterSetValue(feed, count);
    result.put("count", count);
    writeJsonResp(result, resp);
  }
}
