package ru.skinnyweb.server.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;

// Get request logs along with their app log lines and display them 5 at
// a time, using a Next link to cycle through to the next 5.
public class LogsServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(LogsServlet.class.getName());
  private static final DateTimeFormatter format = DateTimeFormat.forPattern("HH:mm:ss");

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) 
         throws IOException {

    StringBuilder sb = new StringBuilder();
  
    // We use this to break out of our iteration loop, limiting record
    // display to 5 request logs at a time.
    int limit = 25;

    // This retrieves the offset from the Next link upon user click.
    String offset = req.getParameter("offset");

    // We want the App logs for each request log
    LogQuery query = LogQuery.Builder.withDefaults();
    query.includeAppLogs(true);

    // Set the offset value retrieved from the Next link click.
    if (offset != null) {
      query.offset(offset);
    }

    // This gets filled from the last request log in the iteration
    String lastOffset = null;
    int count = 0;

    // Display a few properties of each request log.
    for (RequestLogs record : LogServiceFactory.getLogService().fetch(query)) {
      sb.append(record.getMethod() + " " + record.getResource() + "\n");

      lastOffset = record.getOffset();

      // Display all the app logs for each request log.
      for (AppLogLine appLog : record.getAppLogLines()) {
        sb.append(String.valueOf(appLog.getLogLevel()).substring(0, 1));
        // DateTime appTime = new DateTime(appLog.getTimeUsec() / 1000);
        // sb.append(" ");
        // sb.append(format.print(appTime));
        String msg = cutColumns(appLog.getLogMessage(), 2).trim();
        if (msg.startsWith("caused a new process")) {
          msg = "caused a new process...";
        }
        sb.append(": " + msg);
        sb.append("\n");
      } 
      sb.append("\n");

      if (++count >= limit) {
        break;
      }
    } 
    writeResp(sb.toString(), 200, resp);
  }  

  private void writeResp(String body, int status, HttpServletResponse resp) {
    try {
      byte[] responseBytes = body.toString().getBytes("UTF-8");
      resp.setContentLength(responseBytes.length);
      resp.setStatus(status);
      resp.setContentType("text/plain; charset=utf-8");
      resp.getOutputStream().write(responseBytes);
    } catch (Exception e) {
      log.log(Level.SEVERE, "failed write response", e);
    }
  }

  private String cutColumns(String msg, int cnt) {
    for (int i = 0; i < msg.length(); i++) {
      if (msg.charAt(i) == ' ') {
        cnt--;
        if (cnt <= 0) {
          return msg.substring(i + 1);
        }
      }
    }
    return "";
  }
} 
 
