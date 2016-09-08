package ru.skinnyweb.server.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.skinnyweb.server.api.QueryService;
import ru.skinnyweb.server.feeds.PageContentFetcher;
import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.model.Post;
import ru.skinnyweb.server.model.PostDetail;
import ru.skinnyweb.server.utils.DateUtils;
import ru.skinnyweb.server.utils.ResultBuilder;
import ru.skinnyweb.server.utils.StringUtils;
import ru.skinnyweb.shared.templates.StoryViewPage;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;



public class StoryViewServlet extends BaseServlet {
  private static final Logger log = Logger.getLogger(StoryViewServlet.class.getName());
  private RequestHelper requestHelper; 
  private QueryService queryService;

  public StoryViewServlet(RequestHelper requestHelper, QueryService queryService) {
    this.requestHelper = requestHelper;
    this.queryService = queryService;
  }

  @Override
  protected void innerGet(HttpServletRequest req, HttpServletResponse resp, String json) throws Exception {
    log.info("enter...");
    ResultBuilder builder = new ResultBuilder();
    builder.put("activityName", "StoryViewActivity");
    builder.put("jsrender", isJsRender(req));
    Post post = requestHelper.findPost(req);
    String content = null;
    PostDetail storyDetail = queryService.getPostDetail(post.getId());
    if (storyDetail == null || "true".equals(req.getParameter("reload"))) {
      PageContentFetcher fetcher = new PageContentFetcher();
      content = fetcher.getBody(post.getHref());
      if (StringUtils.hasText(content)) {
        storyDetail = new PostDetail(post.getId());
        storyDetail.setContent(content);
        queryService.put(storyDetail);
      }
    } else {
      content = storyDetail.getContent();
    }

    Feed feed = queryService.getFeedById(post.getFeedId());
    builder.begin("feed");
    builder.put("id", feed.getIdAsString());
    builder.put("name", feed.getName());
    builder.put("href", "/feed-" + feed.getIdAsString() + "/");
    builder.end();
    printStory(builder, post, content);
    writeAllResp(req, resp, builder.build(), StoryViewPage.class);
  }

  private void printStory(ResultBuilder builder, Post entity, String content) {
    String id = entity.getIdAsString();
    builder.begin("story");
    builder.put("id", id);
    builder.put("date", DateUtils.printDateFull(entity.getDate()));
    builder.put("name", entity.getName());
    builder.put("category", entity.getCategory());
    builder.put("originalHref", entity.getHref());
    builder.put("description", entity.getDescription());
    if (StringUtils.hasText(content)) {
      String[] paras = content.split("\n\n");
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < paras.length; i++) {
        if (StringUtils.hasText(paras[i])) {
          sb.append("<div class='para' data-id='" + (i + 1) + "'>");
          sb.append(paras[i].replaceAll("\n", "<br>"));
          sb.append("</div>");
        }
      }
      content = sb.toString();
      builder.put("content", content);
    }
    builder.end();
  }
}
