package ru.skinnyweb.server.api;

import java.util.List;

import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.model.Post;

public class PostQueries {
  private final QueryService queryService;

  public PostQueries(QueryService queryService) {
    this.queryService = queryService;
  }

  public List<Post> getAll(Long feedId, Boolean archived, Boolean starred) {
    Criteria c = Criteria.newCriteria("Post").cache(true).sort("-date");
    c.filter("feedId", feedId);
    if (archived != null) {
      c.filter("archived", archived);
    }
    if (starred != null) {
      c.filter("starred", starred);
    }
    return queryService.getEntities(Post.class, c);
  }

  public List<Feed> getAllFeeds() {
    Criteria c = Criteria.newCriteria("Feed").cache(true).sort("name");
    return queryService.getEntities(Feed.class, c);
  }
}
