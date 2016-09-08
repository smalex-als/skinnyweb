package ru.skinnyweb.server.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.skinnyweb.server.api.CloudFilesService;
import ru.skinnyweb.server.api.CloudFilesService.File;

public class CloudFilesServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(CloudFilesServlet.class.getName());
  /**Used below to determine the size of chucks to read in. Should be > 1kb and < 10MB */
  private static final int BUFFER_SIZE = 16 * 1024;
  private final CloudFilesService cloudFilesService;

  public CloudFilesServlet(CloudFilesService cloudFilesService) {
    this.cloudFilesService = cloudFilesService;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    try {
      String path = req.getRequestURI();
      if (!path.startsWith("/f/")) {
        log.info("path should starts with /f/");
        resp.setStatus(404);
        return;
      }
      File file = cloudFilesService.read(path);
      if (file == null) {
        log.info("file not found");
        resp.setStatus(404);
        return;
      }
      resp.setDateHeader("Last-Modified", file.getCreated().getTime());
      resp.setStatus(HttpServletResponse.SC_OK);
      resp.setContentType(getServletContext().getMimeType(file.getPath()));
      copy(new ByteArrayInputStream(file.getBytes()), 
          resp.getOutputStream());
    } catch (Exception e) {
      log.log(Level.SEVERE, "file serve failed", e);
      resp.setStatus(500);
      return;
    }
  }

  /**
   * Transfer the data from the inputStream to the outputStream. Then close both streams.
   */
  private void copy(InputStream input, OutputStream output) throws IOException {
    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead = input.read(buffer);
      while (bytesRead != -1) {
        output.write(buffer, 0, bytesRead);
        bytesRead = input.read(buffer);
      }
    } finally {
      input.close();
      output.close();
    }
  }
}
