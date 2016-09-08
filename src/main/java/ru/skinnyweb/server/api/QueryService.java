package ru.skinnyweb.server.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import ru.skinnyweb.server.model.BaseModel;
import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.model.Post;
import ru.skinnyweb.server.model.PostDetail;
import ru.skinnyweb.server.model.PostImport;
import ru.skinnyweb.server.utils.Entities;
import ru.skinnyweb.server.utils.Lists;
import ru.skinnyweb.server.utils.StringUtils;

public class QueryService {
  private static final Logger log = Logger.getLogger(QueryService.class.getName());
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final MemcacheService cache = MemcacheServiceFactory.getMemcacheService();

  public List<Post> getAllStories(Feed feed) {
    Query query = new Query("Post");
    query.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("feedId", feed.getKey().getId()),
        FilterOperator.EQUAL.of("archived", false)
    ));
    query.addSort("date", SortDirection.DESCENDING);
    List<Entity> entities = datastore.prepare(query)
      .asList(FetchOptions.Builder.withLimit(50));
    List<Post> res = Lists.newArrayList();
    for (Entity entity : entities) {
      res.add(toPost(entity));
    }
    return res;
  }

  public List<Post> getStarredStories(Feed feed) {
    Query query = new Query("Post");
    query.setFilter(CompositeFilterOperator.and(
        FilterOperator.EQUAL.of("feedId", feed.getKey().getId()),
        FilterOperator.EQUAL.of("starred", true)
    ));
    query.addSort("date", SortDirection.DESCENDING);
    List<Entity> entities = datastore.prepare(query)
      .asList(FetchOptions.Builder.withLimit(50));
    List<Post> res = Lists.newArrayList();
    for (Entity entity : entities) {
      res.add(toPost(entity));
    }
    return res;
  }

  public List<Feed> getAllFeeds() {
    Query query = new Query("Feed").addSort("name");
    // Filter filter = FilterOperator.EQUAL.of("archived", false);
    // query.setFilter(filter);
    List<Entity> entities = datastore.prepare(query)
      .asList(FetchOptions.Builder.withDefaults());
    List<Feed> res = Lists.newArrayList();
    for (Entity entity : entities) {
      res.add(toFeed(entity));
    }
    return res;
  }

  private Feed toFeed(Entity entity) {
    if (entity == null) return null;
    Feed feed = new Feed(toId(entity.getKey()));
    feed.setName(Entities.getPropertyString(entity, "name", ""));
    feed.setHref(Entities.getPropertyString(entity, "href", ""));

    if (entity.getProperty("listShowDescription") == null) {
      feed.setListShowDescription(true);
    } else {
      feed.setListShowDescription(
          Entities.getPropertyBoolean(entity, "listShowDescription"));
    }
    return feed;
  }

  private Id toId(Key key) {
    if (key.getId() != 0) {
      return IdFactory.createId(key.getKind(), key.getId());
    }
    return IdFactory.createId(key.getKind(), key.getName());
  }

  private PostDetail toPostDetail(Entity entity) {
    if (entity == null) return null;
    PostDetail postDetail = new PostDetail(toId(entity.getKey()));
    postDetail.setContent(Entities.getPropertyString(entity, "content", ""));
    return postDetail;
  }

  private Post toPost(Entity entity) {
    if (entity == null) return null;
    Post post = new Post(toId(entity.getKey()));
    post.setFeedId(Entities.getPropertyLong(entity, "feedId", 0L));
    post.setCreated(Entities.getPropertyDate(entity, "created"));
    post.setName(Entities.getPropertyString(entity, "name", ""));
    post.setDescription(Entities.getPropertyString(entity, "description", ""));
    post.setHref(Entities.getPropertyString(entity, "href", ""));
    post.setSrc(Entities.getPropertyString(entity, "src", ""));
    post.setCategory(Entities.getPropertyString(entity, "category", ""));
    post.setDate(Entities.getPropertyDate(entity, "date"));
    post.setUnread(Entities.getPropertyBoolean(entity, "unread"));
    post.setArchived(Entities.getPropertyBoolean(entity, "archived"));
    post.setStarred(Entities.getPropertyBoolean(entity, "starred"));
    return post;
  }

  private Entity toEntityPost(Post post) {
    Entity entity = newEntity(post.getKey());
    entity.setProperty("feedId", post.getFeedId());
    entity.setProperty("created", post.getCreated());
    entity.setProperty("name", post.getName());
    entity.setProperty("description", 
        new Text(StringUtils.trimToEmpty(post.getDescription())));
    entity.setProperty("href", post.getHref());
    entity.setProperty("src", post.getSrc());
    entity.setProperty("category", post.getCategory());
    entity.setProperty("date", post.getDate());
    entity.setProperty("unread", post.isUnread());
    entity.setProperty("archived", post.isArchived());
    entity.setProperty("starred", post.isStarred());
    return entity;
  }

  private Entity newEntity(Id key) {
    if (key.getId() != 0L) {
      return new Entity(KeyFactory.createKey(key.getKind(), key.getId()));
    } else if (key.getName() != null) {
      return new Entity(KeyFactory.createKey(key.getKind(), key.getName()));
    }
    return new Entity(key.getKind());
  }

  private Key toKey(Id key) {
    if (key.getId() != 0L) {
      return KeyFactory.createKey(key.getKind(), key.getId());
    }
    return KeyFactory.createKey(key.getKind(), key.getName());
  }

  public int getCountInFeed(Feed feed) {
    Query query = new Query("Post");
    Filter filterFeedId = FilterOperator.EQUAL.of("feedId", feed.getKey().getId());
    Filter filterArchived = FilterOperator.EQUAL.of("archived", false);
    CompositeFilter filter = CompositeFilterOperator.and(filterFeedId, filterArchived);
    query.setFilter(filter);
    return datastore.prepare(query).countEntities(FetchOptions.Builder.withLimit(1000));
  }

  protected abstract class CachedValue<T extends BaseModel> {
    public Map<T, Integer> getCounters(List<T> items) {
      Map<T, Integer> res = new HashMap<>();
      List<String> keys = Lists.newArrayList();
      for (T item : items) {
        keys.add("Count:" + item.getKey()); 
      }
      Map<String, Object> map = cache.getAll(keys);
      boolean updated = false;
      for (T item : items) {
        String key = "Count:" + item.getKey();
        Integer value = (Integer) map.get(key);
        if (value == null) {
          updated = true;
          value = calculate(item);
          map.put(key, value);
        }
        res.put(item, value);
      }
      if (updated) {
        cache.putAll(map);
      }
      return res;
    }

    public abstract Integer calculate(T feed);

    public void expire(T t) {
      if (t == null) {
        log.warning("t == null");
        return;
      }
      String key = "Count:" + t.getKey();
      cache.delete(key);
    }

    public void setValue(T t, int value) {
      String key = "Count:" + t.getKey();
      cache.put(key, value);
    }
  }

  private final CachedValue<Feed> feedCount = new CachedValue<Feed>() {
    @Override
    public Integer calculate(Feed feed) {
      return getCountInFeed(feed);
    }
  };

  public void counterExpire(Feed feed) {
    feedCount.expire(feed);
  }

  public void counterSetValue(Feed feed, int count) {
    feedCount.setValue(feed, count);
  }

  public Map<Feed, Integer> getCountInFeed(List<Feed> feeds) {
    return feedCount.getCounters(feeds);
  }

  public void put(Collection<? extends BaseModel> collection) {
    List<Entity> entities = Lists.newArrayList();
    for (BaseModel item : collection) {
      entities.add(toEntity(item));
    }
    datastore.put(entities);
  }

  public void put(Post post) {
    datastore.put(toEntityPost(post));
  }

  public PostDetail getPostDetail(long id) {
    return toPostDetail(get("PostDetail", id));
  }

  public Feed getFeedById(long id) {
    return toFeed(get("Feed", id));
  }

  public Post getPostById(long id) {
    return toPost(get("Post", id));
  }

  public Map<Id, BaseModel> get(List<Id> ids) {
    List<Key> keys = Lists.newArrayList();
    for (Id id : ids) {
      keys.add(toKey(id));
    }
    Map<Key, Entity> map = datastore.get(keys);
    Map<Id, BaseModel> res = new HashMap<>();
    for (Key key : map.keySet()) {
      BaseModel obj = toObject(map.get(key));
      res.put(obj.getKey(), obj);
    }
    return res;
  }

  private BaseModel toObject(Entity entity) {
    switch (entity.getKind()) {
      case "PostImport":
        return toPostImport(entity);
      case "Post":
        return toPost(entity);
      case "Feed":
        return toFeed(entity);
      case "PostDetail":
        return toPostDetail(entity);
    }
    throw new RuntimeException("not mapper for object " + entity);
  }

  private Entity toEntity(BaseModel item) {
    switch (item.getKey().getKind()) {
      case "PostImport":
        return toEntityPostImport((PostImport) item);
      case "Post":
        return toEntityPost((Post) item);
      case "Feed":
        return toEntityFeed((Feed) item);
      case "PostDetail":
        return toEntityPostDetail((PostDetail) item);
    }
    throw new RuntimeException("not mapper for object " + item.getKey());
  }

  private PostImport toPostImport(Entity entity) {
    return new PostImport(toId(entity.getKey()));
  }

  private Entity get(String kind, long id) {
    try {
      Key key = KeyFactory.createKey(kind, id);
      return datastore.get(key);
    } catch (EntityNotFoundException e) {
      log.log(Level.SEVERE, "entity not found " + e.getKey(), e);
    }
    return null;
  }

  private Entity toEntityFeed(Feed feed) {
    Entity entity = newEntity(feed.getKey());
    entity.setProperty("name", feed.getName());
    entity.setProperty("href", feed.getHref());
    entity.setProperty("listShowDescription", feed.isListShowDescription());
    return entity;
  }

  private Entity toEntityPostImport(PostImport postImport) {
    Entity entity = newEntity(postImport.getKey());
    return entity;
  }

  private Entity toEntityPostDetail(PostDetail storyDetail) {
    Entity entity = newEntity(storyDetail.getKey());
    entity.setUnindexedProperty("content", 
        new Text(storyDetail.getContent()));
    return entity;
  }

  public void put(PostDetail storyDetail) {
    datastore.put(toEntityPostDetail(storyDetail));
  }
}
