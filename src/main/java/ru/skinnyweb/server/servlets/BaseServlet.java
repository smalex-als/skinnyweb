package ru.skinnyweb.server.servlets;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gwt.user.server.rpc.RPCServletUtils;

import ru.skinnyweb.server.utils.StringUtils;
import ru.skinnyweb.shared.templates.StandardPage;

public abstract class BaseServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(BaseServlet.class.getName());
  private static final String DEFAULT_CONTENT_TYPE = "text/html";
  private static final String JSON_CONTENT_TYPE = "application/json";
  private static final String JSON_CHARSET = "UTF-8";

  @Override
  public final void doPost(HttpServletRequest req, HttpServletResponse resp)
  throws IOException {

    String content = null;
    String contentType = StringUtils.trimToEmpty(req.getHeader("Content-Type")).toLowerCase();
    if (contentType.startsWith(JSON_CONTENT_TYPE)) {
      try {
        content = RPCServletUtils.readContent(req, JSON_CONTENT_TYPE, JSON_CHARSET);
      } catch (IOException | ServletException e) {
        log.log(Level.SEVERE, "failed read content of request", e);
      }
    }
    try {
      innerGet(req, resp, content);
    } catch (Exception e) {
      log.log(Level.SEVERE, "servlet failed", e);
      writeResp("text/plain", "error", 500, resp);
    }
  }

  @Override
  public final void doGet(HttpServletRequest req, HttpServletResponse resp)
  throws IOException {
    try {
      innerGet(req, resp, null);
    } catch (Exception e) {
      log.log(Level.SEVERE, "servlet failed", e);
      writeResp("text/plain", "error", 500, resp);
    }
  }

  protected abstract void innerGet(HttpServletRequest req, HttpServletResponse resp, String content)
    throws Exception;

  protected void writeAllResp(HttpServletRequest req, 
      HttpServletResponse resp, Map<String, Object> map,
      Class<? extends StandardPage>  pageClass) {

    if ("json".equals(req.getParameter("mode"))) {
      writeJsonResp(map, resp);
    } else if ("GET".equals(req.getMethod())) {
      try {
        StandardPage page = pageClass.newInstance();
        String body = page.renderPage(map);
        writeResp("text/html", body, 200, resp);
      } catch (InstantiationException | IllegalAccessException e) {
        log.log(Level.SEVERE, "failed create template", e);
        throw new RuntimeException("failed create template", e);
      }
    } else {
      writeJsonResp(map, resp);
    }
  }

  protected void writeJsonResp(Map<String, Object> map, HttpServletResponse resp) {
    Gson gson = new Gson();
    String json = gson.toJson(map);
    writeResp("application/json", json, 200, resp);
  }

  protected void writeResp(String contentType, String body, int status, HttpServletResponse resp) {
    try {
      byte[] responseBytes = body.toString().getBytes("UTF-8");
      resp.setContentLength(responseBytes.length);
      resp.setStatus(status);
      resp.setContentType(contentType + "; charset=utf-8");
      resp.getOutputStream().write(responseBytes);
    } catch (Exception e) {
      log.log(Level.SEVERE, "failed write response", e);
    }
  }

  protected boolean isJsRender(HttpServletRequest req) {
    String value = StringUtils.ifEmpty(req.getParameter("jsrender"), "false");
    return Boolean.parseBoolean(value);
  }
}
