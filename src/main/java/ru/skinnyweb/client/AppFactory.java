package ru.skinnyweb.client;

import com.google.gwt.storage.client.Storage;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

import ru.skinnyweb.client.rpc.ContentRpcService;
import ru.skinnyweb.client.rpc.ContentRpcServiceImpl;


/**
 * Created by smalex on 13/04/15.
 */
public class AppFactory {
  private ContentRpcService contentRpcService;
  private final EventBus eventBus = new SimpleEventBus();
  private final Storage navStorage = Storage.getSessionStorageIfSupported();
  private final NavigationStack navigationStack = new NavigationStack();
  private final AppActivityMapper appActivityMapper = new AppActivityMapper(this);

  public AppFactory() {
  }

  public ContentRpcService getRpcService() {
    if (contentRpcService == null) {
      contentRpcService = new ContentRpcServiceImpl(new ContentRpcService.StatusObserver() {
        @Override
        public void onServerCameBack() {
        }

        @Override
        public void onServerWentAway() {
        }

        @Override
        public void onTaskFinished() {
        }

        @Override
        public void onTaskStarted(String description) {
        }
      });
    }
    return contentRpcService;
  }

  public EventBus getEventBus() {
    return eventBus;
  }

  public Storage getNavStorage() {
    return navStorage;
  }

  public NavigationStack getNavigationStack() {
    return navigationStack;
  }

  public AppActivityMapper getActivityMapper() {
    return appActivityMapper;
  }
}
