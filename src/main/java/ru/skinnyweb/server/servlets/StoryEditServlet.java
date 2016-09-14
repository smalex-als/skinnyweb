package ru.skinnyweb.server.servlets;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import ru.skinnyweb.client.util.NumberUtils;
import ru.skinnyweb.server.api.Id;
import ru.skinnyweb.server.api.IdFactory;
import ru.skinnyweb.server.api.QueryService;
import ru.skinnyweb.server.model.BaseModel;
import ru.skinnyweb.server.model.Post;
import ru.skinnyweb.server.utils.Lists;
import ru.skinnyweb.server.utils.ResultBuilder;
import ru.skinnyweb.server.utils.StringUtils;

public class StoryEditServlet extends BaseServlet {
  private static final Logger log = Logger.getLogger(StoryEditServlet.class.getName());
  private final QueryService queryService;

  private static class Params {
    String prop;
    String value;
    String id;
    List<String> ids;
  }

  public StoryEditServlet(QueryService queryService) {
    this.queryService = queryService;
  }

  @Override
  protected void innerGet(HttpServletRequest req, HttpServletResponse resp, String json) throws Exception {
    List<Id> ids = Lists.newArrayList();
    Params params = null;
    String redirect = null;
    if (StringUtils.hasText(json)) {
      params = new Gson().fromJson(json, Params.class);
    } else {
      Pattern p = Pattern.compile("^/story-(\\d+)/(un)?(archive|star)/$");
      Matcher m = p.matcher(req.getRequestURI());
      if (m.matches()) {
        params = new Params();
        params.id = m.group(1);
        if (m.group(3).equals("archive")) {
          params.prop = "archived";
        } else if (m.group(3).equals("star")) {
          params.prop = "starred";
        }
        params.value = !StringUtils.hasLength(m.group(2)) ? "true" : "false";

        // TODO redirect to next #storyId
        Post post = queryService.getPostById(NumberUtils.toLong(params.id));
        redirect = "/feed-" + post.getFeedId() + "/";
      }
    }
    if (params.ids != null) {
      for (String idParam: params.ids) {
        ids.add(IdFactory.createId("Post", NumberUtils.toLong(idParam)));
      }
    } 
    if (StringUtils.hasText(params.id)) {
      ids.add(IdFactory.createId("Post", NumberUtils.toLong(params.id)));
    }

    updateEntity(ids, params.prop, params.value);
    if (redirect != null) {
      writeRedirect(resp, redirect);
    } else {
      ResultBuilder builder = new ResultBuilder();
      builder.put("result", true);
      writeJsonResp(builder.build(), resp);
    }
  }

  void updateEntity(List<Id> ids, String prop, String value) {
    Map<Id, BaseModel> map = queryService.get(ids);
    Set<Long> feedsIds = new HashSet<>();
    for (Id id : ids) {
      Post post = (Post) map.get(id);
      if (post == null) continue;
      if (prop.equals("archived")) {
        post.setArchived(Boolean.parseBoolean(value));
      } else if (prop.equals("starred")) {
        post.setStarred(Boolean.parseBoolean(value));
      } else {
        log.warning("unknown prop = '" + prop + "'");
      }
      feedsIds.add(post.getFeedId());
    }
    queryService.put(map.values());
    for (Long feedId : feedsIds) {
      queryService.counterExpire(queryService.getFeedById(feedId));
    }
  }
}
