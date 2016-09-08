package ru.skinnyweb.server.servlets;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.tools.appstats.AppstatsServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class Injector {
  private static final Logger log = Logger.getLogger(Injector.class.getName());
  private final Map<Class<?>, Object> binding = new HashMap<>();
  private Servlets servlets;

  public void init() {
    bindToInstance(DatastoreService.class, DatastoreServiceFactory.getDatastoreService());

    servlets = new Servlets();
    servlets.serve("^/$", FeedsListServlet.class);
    servlets.serve("^/feed-(\\d+)/$", StoriesListServlet.class);
    servlets.serve("^/feed-(\\d+)" + StoriesListServlet.FOLDER_REGEXP + "$", StoriesListServlet.class);
    servlets.serve("^/feed-(\\d+)/story-(\\d+)/$", StoryViewServlet.class);
    servlets.serve("^/story/edit/$", StoryEditServlet.class);
    servlets.serve("^/setup/$", SetupServlet.class);
    servlets.serve("^/f/.*$", CloudFilesServlet.class);
    servlets.serve("^/import/$", FeedImportServlet.class);
    servlets.serve("^/logs/$", LogsServlet.class);
    servlets.serve("^/translate/$", TranslateServlet.class);
  }

  public <T> void bindToInstance(Class<T> clazz, T t) {
    binding.put(clazz, t);
  }

  public <T> T getInstance(Class<T> clazz) {
    if (binding.containsKey(clazz)) {
      Object value = binding.get(clazz);
      if (value instanceof Boolean) {
        throw new RuntimeException("circular dependency detected");
      }
      return (T) value;
    }
    binding.put(clazz, Boolean.TRUE);
    Constructor<?>[] cons = clazz.getConstructors();
    if (cons.length != 1) {
      log.log(Level.SEVERE, "failed init servlet, should be one constructor");
      throw new RuntimeException("failed init servlet, should be one constructor");
    }
    Constructor<?> con = cons[0];
    Class<?>[] params = con.getParameterTypes();
    try {
      Object newObject;
      if (params.length > 0) {
        Object[] args = new Object[params.length];
        for (int j = 0; j < params.length; j++) {
          args[j] = getInstance(params[j]);
        }
        newObject = con.newInstance(args);
      } else {
        newObject = clazz.newInstance();
      }
      binding.put(clazz, newObject);
      return (T) newObject;
    } catch (Exception e) {
      throw new RuntimeException("failed create instance", e);
    }
  }

  public Servlets getServlets() {
    return servlets;
  }
}
