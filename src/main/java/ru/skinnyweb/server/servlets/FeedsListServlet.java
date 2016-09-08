package ru.skinnyweb.server.servlets;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.skinnyweb.server.api.QueryService;
import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.utils.ResultBuilder;
import ru.skinnyweb.shared.templates.FeedsListPage;

public class FeedsListServlet extends BaseServlet {
  private final QueryService queryService;

  public FeedsListServlet(QueryService queryService) {
    this.queryService = queryService;
  }

  @Override
  protected void innerGet(HttpServletRequest req, HttpServletResponse resp, String content) throws Exception {
    List<Feed> feeds = queryService.getAllFeeds();
    ResultBuilder builder = new ResultBuilder();
    builder.put("activityName", "FeedsListActivity");
    builder.put("jsrender", isJsRender(req));
    builder.beginList("feeds");
    Map<Feed, Integer> countByFeed = queryService.getCountInFeed(feeds);
    for (Feed feed : feeds) {
      printFeed(builder, feed);
      builder.beginBelow();
      builder.put("count", countByFeed.get(feed));
      builder.end();
    }
    builder.end();
    writeAllResp(req, resp, builder.build(), FeedsListPage.class);
  }

  private void printFeed(ResultBuilder builder, Feed feed) {
    builder.begin("feed");
    builder.put("id", feed.getIdAsString());
    String id = Long.toHexString(Long.parseLong(feed.getIdAsString()));
    builder.put("hex", id);
    builder.put("name", feed.getName());
    builder.put("href", "/feed-" + feed.getIdAsString() + "/");
    builder.end();
  }
}
