package ru.skinnyweb.server.servlets;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;

public class Servlets {
  public static class ServletDefinition {
    private String pattern;
    private Class<?extends HttpServlet> clazz;

    public ServletDefinition(String pattern, Class<?extends HttpServlet> clazz) {
      this.pattern = pattern;
      this.clazz = clazz;
    }

    public String getPattern() {
      return pattern;
    }

    public Class<? extends HttpServlet> getClazz() {
      return clazz;
    }
  }

  private final List<ServletDefinition> servlets = new ArrayList<>();

  public Servlets serve(String regexp, Class<?extends HttpServlet> clazz) {
    servlets.add(new ServletDefinition(regexp, clazz));
    return this;
  }

  public List<ServletDefinition> getServlets() {
    return servlets;
  }
}
