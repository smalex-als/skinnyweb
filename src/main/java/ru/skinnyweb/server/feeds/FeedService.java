package ru.skinnyweb.server.feeds;

import java.util.List;

import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.model.Post;

public interface FeedService {
  List<Post> fetch(Feed feed) throws Exception;
}
