package ru.skinnyweb.server.servlets;

import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import ru.skinnyweb.server.api.RemoteApi;
import ru.skinnyweb.server.utils.StringUtils;

public class TranslateServlet extends BaseServlet {
  private static String YANDEX_KEY = "trnsl.1.1.20160905T193310Z.99ea2520208776fc.73109b4a72ba245f78948ff68a9e8050aece6b85";
  private static class Params {
    String word;
  }

  @Override
  protected void innerGet(HttpServletRequest req, HttpServletResponse resp, String json) throws Exception {
    if (!StringUtils.hasText(json)) {
      return;
    }
    Params params = new Gson().fromJson(json, Params.class);
    RemoteApi remoteApi = new RemoteApi();
    
    String ans = remoteApi.fetchUrl("https://dictionary-1366.appspot.com/translate?" 
        + "type=full"
        + "&word=" + URLEncoder.encode(params.word, "UTF-8"));
    Map<String, Object> map = new Gson().fromJson(ans, Map.class);
    writeJsonResp(map, resp);
  }
}
