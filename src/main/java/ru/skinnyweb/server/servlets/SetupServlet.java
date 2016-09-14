package ru.skinnyweb.server.servlets;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.skinnyweb.server.api.PostQueries;
import ru.skinnyweb.server.api.QueryService;
import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.utils.Lists;
import ru.skinnyweb.server.utils.ResultBuilder;

public class SetupServlet extends BaseServlet {
  private final QueryService queryService;
  private final PostQueries postQueries;

  public SetupServlet(QueryService queryService, PostQueries postQueries) {
    this.queryService = queryService;
    this.postQueries = postQueries;
  }

  @Override
  protected void innerGet(HttpServletRequest req, HttpServletResponse resp, String content) throws Exception {
    int cnt = postQueries.getAllFeeds().size();
    ResultBuilder builder = new ResultBuilder();
    builder.put("cnt", cnt);
    if (cnt == 0) {
      List<Feed> feeds = Lists.newArrayList();
      addFeed(feeds, "News / TechCrunch", "https://techcrunch.com/feed/", true);
      addFeed(feeds, "News / Lenta / Top", "http://m.lenta.ru/rss/top7", true);
      addFeed(feeds, "News / New York Times / Health", 
          "http://rss.nytimes.com/services/xml/rss/nyt/Health.xml", true);
      addFeed(feeds, "Jobs / Stackoverflow / Java",
          "http://stackoverflow.com/jobs/feed?searchTerm=java+&location=hillsdale%2c+nj&range=20&distanceUnits=Miles", false);
      addFeed(feeds, "Jobs / We Work Remotely",
          "https://weworkremotely.com/categories/2-programming/jobs.rss",
          false);
      queryService.put(feeds);
      builder.put("added", true);
    }
    writeJsonResp(builder.build(), resp);
  }

  private Feed addFeed(List<Feed> feeds, String name, 
      String href, boolean listShowDescription) {
    Feed feed = new Feed();
    feed.setName(name);
    feed.setHref(href);
    feed.setListShowDescription(listShowDescription);
    feeds.add(feed);
    return feed;
  }
}
