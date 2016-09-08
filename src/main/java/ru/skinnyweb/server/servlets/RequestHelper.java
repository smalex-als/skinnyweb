package ru.skinnyweb.server.servlets;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import ru.skinnyweb.client.util.NumberUtils;
import ru.skinnyweb.server.api.QueryService;
import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.model.Post;
import ru.skinnyweb.server.utils.Lists;

public class RequestHelper {
  private static final Logger log = Logger.getLogger(RequestHelper.class.getName());
  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final QueryService queryService;

  public RequestHelper(QueryService queryService) {
    this.queryService = queryService;
  }

  public Post findPost(HttpServletRequest req) {
    Pattern p = Pattern.compile("/feed-(\\d+)/story-(\\d+)/");
    Matcher m = p.matcher(req.getRequestURI());
    if (!m.matches()) {
      throw new RuntimeException("post not found " + req.getRequestURI());
    }
    long id = NumberUtils.toLong(m.group(2));
    return queryService.getPostById(id);
  }

  public Feed findFeed(HttpServletRequest req) {
    Pattern p = Pattern.compile("/feed-(\\d+)/.*");
    Matcher m = p.matcher(req.getRequestURI());
    if (!m.matches()) {
      throw new RuntimeException("feed not found " + req.getRequestURI());
    }
    long id = NumberUtils.toLong(m.group(1));
    return queryService.getFeedById(id);
  }

  public static List<Long> getParameterLongs(HttpServletRequest req, 
      String name) {
    String[] ids = req.getParameterValues(name);
    List<Long> res = Lists.newArrayList();
    if (ids != null && ids.length > 0) {
      for (int i = 0; i < ids.length; i++) {
        res.add(NumberUtils.toLong(ids[i]));
      }
    }
    return res;
  }
}

