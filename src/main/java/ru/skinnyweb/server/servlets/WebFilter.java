package ru.skinnyweb.server.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.skinnyweb.server.servlets.Servlets.ServletDefinition;

public class WebFilter implements Filter {
  private static final Logger log = Logger.getLogger(WebFilter.class.getName());
  private static class Dispatcher {
    private Pattern pattern;
    private HttpServlet servlet;

    public Dispatcher(Pattern pattern, HttpServlet servlet) {
      this.pattern = pattern;
      this.servlet = servlet;
    }

    public Pattern getPattern() {
      return pattern;
    }

    public HttpServlet getServlet() {
      return servlet;
    }
  }

  private final List<Dispatcher> dispatchers = new ArrayList<>();

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, 
      ServletResponse servletResponse, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    HttpServlet servlet = findServlet(request);
    if (servlet != null) {
      servlet.service(request, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  private HttpServlet findServlet(HttpServletRequest request) {
    String uri = request.getRequestURI();
    for (Dispatcher dispatcher : dispatchers) {
      Pattern pattern = dispatcher.getPattern();
      if (pattern.matcher(uri).matches()) {
        return dispatcher.getServlet();
      }
    }
    return null;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Injector injector = new Injector();
    injector.init();

    final Map<String, String> initParams = Collections.unmodifiableMap(new HashMap<String, String>());
    final ServletContext context = filterConfig.getServletContext();
    for (ServletDefinition def : injector.getServlets().getServlets()) {
      try {
        HttpServlet servlet = injector.getInstance(def.getClazz());
        dispatchers.add(new Dispatcher(Pattern.compile(def.getPattern()), servlet));
        initServlet(servlet, initParams, context);
      } catch (Exception e) {
        log.log(Level.SEVERE, "failed create servlet", e);
      }
    }
  }

  private void initServlet(final HttpServlet servlet, final Map<String, String> initParams,
      final ServletContext context) {
    ServletConfig config = new ServletConfig() {

      @Override
      public String getInitParameter(String s) {
        return initParams.get(s);
      }

      @Override
      public Enumeration<String> getInitParameterNames() {
        return getEnumeration(initParams);
      }

      @Override
      public ServletContext getServletContext() {
        return context;
      }

      @Override
      public String getServletName() {
        return servlet.getClass().getSimpleName();
      }
    };
    try {
      servlet.init(config);
    } catch (ServletException e) {
      log.log(Level.SEVERE, "failed init servlet", e);
    }
  }

  private Enumeration<String> getEnumeration(Map<String, String> initParams) {
    final Iterator<String> it = initParams.keySet().iterator();
    Enumeration<String> en = new Enumeration<String>() {

      @Override
      public boolean hasMoreElements() {
        return it.hasNext();
      }

      @Override
      public String nextElement() {
        return (String) it.next();
      }
    };
    return en;
  }
}
