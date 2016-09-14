package ru.skinnyweb.server.servlets;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.skinnyweb.server.api.PostQueries;
import ru.skinnyweb.server.model.Feed;
import ru.skinnyweb.server.model.Post;
import ru.skinnyweb.server.utils.DateUtils;
import ru.skinnyweb.server.utils.Lists;
import ru.skinnyweb.server.utils.ResultBuilder;
import ru.skinnyweb.shared.templates.StoriesListPage;


public class StoriesListServlet extends BaseServlet {
  private static final Logger log = Logger.getLogger(StoriesListServlet.class.getName());
  private final RequestHelper requestHelper; 
  private final PostQueries postQueries;

  enum Folder {
    Inbox, Recommended, Starred, Done
  }
  
  public static String FOLDER_REGEXP;
  static {
    StringBuilder sb = new StringBuilder();
    sb.append("/(");
    for (Folder folder : Folder.values()) {
      if (sb.length() > 2) {
        sb.append("|");
      }
      sb.append(folder.name().toLowerCase());
    }
    sb.append(")/");
    FOLDER_REGEXP = sb.toString();
  }

  public StoriesListServlet(RequestHelper requestHelper, PostQueries postQueries) {
    this.requestHelper = requestHelper;
    this.postQueries = postQueries;
  }

  @Override
  protected void innerGet(HttpServletRequest req, HttpServletResponse resp, String json) throws Exception {
    Feed feed = requestHelper.findFeed(req);
    String reqURI = req.getRequestURI();
    Folder folder = getSelectedFolder(reqURI);

    List<Post> posts;
    if (folder == Folder.Inbox) {
      posts = postQueries.getAll(feed.getKey().getId(), false, null); 
    } else if (folder == Folder.Starred) {
      posts = postQueries.getAll(feed.getKey().getId(), null, true); 
    } else if (folder == Folder.Done) {
      posts = postQueries.getAll(feed.getKey().getId(), null, null); 
    } else {
      posts = Lists.newArrayList();
    }
    ResultBuilder builder = new ResultBuilder();
    builder.put("activityName", "StoriesListActivity");
    builder.put("jsrender", isJsRender(req));

    builder.begin("feed");
    builder.put("id", feed.getIdAsString());
    builder.put("name", feed.getName());
    builder.end();

    builder.beginList("folders");
    for (Folder f : Folder.values()) {
      printFolder(builder, feed, f, folder);
    }
    builder.end();

    builder.beginList("articles");
    for (Post post : posts) {
      printStory(builder, post, feed);
    }
    builder.end();
    writeAllResp(req, resp, builder.build(), StoriesListPage.class);
  }

  private void printFolder(ResultBuilder builder, Feed feed, 
      Folder folder, Folder selected) {
    String name = folder.name();
    builder.begin("folder");
    builder.put("id", folder.name().toLowerCase());
    builder.put("name", name);
    String id = Long.toHexString(Long.parseLong(feed.getIdAsString()));
    builder.put("href", "/feed-" + feed.getIdAsString() 
        + "/" + name.toLowerCase() + "/");
    if (folder == selected) {
      builder.put("selected", true);
    }
    builder.end();
  }

  private void printStory(ResultBuilder builder, Post post, Feed feed) {
    String id = post.getIdAsString();
    builder.begin("article");
    builder.put("id", id);
    builder.put("date", DateUtils.printDate(post.getDate()));
    builder.put("name", post.getName());
    builder.put("category", post.getCategory());
    builder.put("href", "/feed-" + post.getFeedId() 
        + "/story-" + id + "/");
    builder.put("starred", post.isStarred());
    if (feed.isListShowDescription()) {
      builder.put("description", post.getDescription());
    }
    builder.end();
  }

  private Folder getSelectedFolder(String reqURI) {
    Pattern pattern = Pattern.compile(".*" + FOLDER_REGEXP + "$");
    Matcher m = pattern.matcher(reqURI);
    Folder folder = Folder.Inbox;
    if (m.matches()) {
      for (Folder f : Folder.values()) {
        if (f.name().equalsIgnoreCase(m.group(1))) {
          folder = f;
          break;
        }
      }
    }
    log.info("folder = " + folder);
    return folder;
  }
}
