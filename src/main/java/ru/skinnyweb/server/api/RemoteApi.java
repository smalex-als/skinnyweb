package ru.skinnyweb.server.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.Gson;

import ru.skinnyweb.server.utils.Charsets;
import ru.skinnyweb.server.utils.ClientCookie;
import ru.skinnyweb.server.utils.ClientCookieManager;

public class RemoteApi {
  private static final Logger log = Logger.getLogger(RemoteApi.class.getName());
  private final URLFetchService fetchService =
      URLFetchServiceFactory.getURLFetchService();
  private ClientCookieManager clientCookieManager = 
    new ClientCookieManager();

  public String updateOrder(int orderNumber, Map<String, Object> map) throws Exception {
    if (map.size() == 0) {
      return "";
    }
    String payload = new Gson().toJson(map);

    String reqUrl = "/api/v3/";
    return putJson(reqUrl, payload);
  }

  public String fetchUrl(String reqUrl) {
    try {
      URL url = new URL(reqUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setReadTimeout(10000);
      conn.setConnectTimeout(10000);
      conn.setDoOutput(true);
      conn.setRequestMethod("GET");
      conn.setInstanceFollowRedirects(false);

      Iterator<ClientCookie> it = clientCookieManager.getCookies();
      while (it.hasNext()) {
        ClientCookie cookie = it.next();
        log.info("cookie = " + cookie);
      }
      clientCookieManager.writeCookies(conn);
      int status = conn.getResponseCode();
      boolean redirect = false;
      log.info(reqUrl + " status = " + status);
      if (status == HttpURLConnection.HTTP_MOVED_TEMP
          || status == HttpURLConnection.HTTP_MOVED_PERM
          || status == HttpURLConnection.HTTP_SEE_OTHER) {
        redirect = true;
      }
      clientCookieManager.readCookies(conn);

      if (redirect) {
        return fetchUrl(conn.getHeaderField("Location"));
      }
      return readInput(conn);
    } catch (Exception ex) {
      log.log(Level.SEVERE, "fetch " + reqUrl + " failed", ex);
    }
    return null;
  }

  public String putJson(String reqUrl, String payload) throws Exception {
    URL url = new URL(reqUrl);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    conn.setDoOutput(true);
    conn.setRequestMethod("PUT");

    OutputStream writer = conn.getOutputStream();
    writer.write(payload.getBytes(Charsets.UTF_8));
    writer.close();

    int code = conn.getResponseCode();  // New items get NOT_FOUND on PUT
    log.info("code = " + code);
    if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_NOT_FOUND) {
      String out = readInput(conn);
      log.info("out = " + out);
      return out;
    }
    return null;
  }

  private String readInput(HttpURLConnection conn) throws Exception {
    StringBuilder response = new StringBuilder();
    String line;

    BufferedReader reader = new BufferedReader(
          new InputStreamReader(conn.getInputStream(), "utf-8"));
    while ((line = reader.readLine()) != null) {
      response.append(line);
    }
    reader.close();
    return response.toString();
  }

  private String fetchUrlBak(String reqUrl) throws Exception {
    URL url = new URL(reqUrl);
    FetchOptions options = FetchOptions.Builder.withDeadline(10);
    HTTPRequest req = new HTTPRequest(url, HTTPMethod.GET, options);
    // req.setHeader(new HTTPHeader("Content-Type", "application/json"));
    req.setHeader(new HTTPHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36"));
    HTTPResponse resp = fetchService.fetch(url);
    if (resp.getResponseCode() != 200) {
      log.info("error");
      return null;
    }
    return new String(resp.getContent(), Charsets.UTF_8);
  }

}
