package ru.skinnyweb.client;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;

import elemental.client.Browser;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.Location;
import elemental.json.JsonObject;

import ru.skinnyweb.client.controls.Component;
import ru.skinnyweb.client.event.OpenPageEvent;
import ru.skinnyweb.client.rpc.ContentRpcService;
import ru.skinnyweb.client.util.DomUtils;
import ru.skinnyweb.shared.templates.BaseTemplates;
import ru.skinnyweb.shared.templates.Jsons;

/**
 * Created by smalex on 04/03/15.
 */
public class ReaderEntryPoint implements EntryPoint, OpenPageEvent.Handler {
  private static final Logger log = Logger.getLogger(ReaderEntryPoint.class.getName());
  private final AppFactory factory = new AppFactory();
  private final AppActivityMapper activityMapper = factory.getActivityMapper();
  private final NavigationStack navigationStack = factory.getNavigationStack();
  private Component currentActivity;

  @Override
  public void onModuleLoad() {
    GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
      @Override
      public void onUncaughtException(Throwable e) {
        Window.alert("catch: " + e.toString());
        log.log(Level.SEVERE, "catch: " + e.toString(), e);
      }
    });

    Browser.getWindow().setOnpopstate(new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        String href = getCurrentPathAndQuery();
        OpenPageEvent.fire(factory.getEventBus(), href, false, "");
      }
    });

    OpenPageEvent.register(factory.getEventBus(), "", this);
    Document doc = Browser.getDocument();
    currentActivity = activityMapper.getActivity(doc.getBody().getId());
    if (currentActivity != null) {
      startActivity();
    }
    Element progressEl = doc.createDivElement();
    progressEl.setId("progress");
    progressEl.setInnerHTML("<dt/><dd/>");
    // progressEl.getStyle().setOpacity(0);
    progressEl.setClassName("start");
    doc.getBody().appendChild(progressEl);
    log.info("create progress");
  }

  private String getCurrentPathAndQuery() {
    Location location = Browser.getWindow().getDocument().getLocation();
    String href = location.getPathname();
    if (location.getSearch() != null) {
      href += location.getSearch();
    }
    return href;
  }

  private void cancelProgress() {
    Document doc = Browser.getDocument();
    Element progressEl = doc.getElementById("progress");
    progressEl.setClassName("start");
  }

  private void showProgress() {
    Document doc = Browser.getDocument();
    Element progressEl = doc.getElementById("progress");
    progressEl.setClassName("finish");
  }

  @Override
  public void onOpenPage(OpenPageEvent event) {
    final String href = event.getUrl();
    log.info("open page " + href);
    final boolean back = event.isBack();

    showProgress();

    factory.getRpcService().request(href, null, new ContentRpcService.AsyncCallback() {
      @Override
      public void onSuccessAll(JsonObject jsonObject, String responseText) {
        gotOpenPageResult(jsonObject, href, back);
        cancelProgress();
        navigationStack.pushHref(href);
      }
    });
  }

  private void removeCurrentActivity() {
    if (currentActivity != null) {
      currentActivity.exitDocument();
      currentActivity.getElement().getParentElement()
        .removeChild(currentActivity.getElement());
      currentActivity = null;
    }
  }

  private void gotOpenPageResult(JsonObject jsonObject, String href, boolean back) {
    removeCurrentActivity();
    Map<String, Object> map = Jsons.convertJsonObject(jsonObject);

    String title = Browser.getDocument().getTitle();
    Browser.getWindow().getHistory().pushState(null, title, href);

    currentActivity = activityMapper.getActivity(BaseTemplates.getString(map, "activityName"));
    if (currentActivity == null) {
      log.info("activity is null");
      return;
    }
    currentActivity.setModel(map);
    currentActivity.render(Browser.getDocument().getElementById("content"));
    if (!back) {
      log.info("scroll");
      Browser.getWindow().scrollTo(0, 0);
    }
    // if (navStorage != null) {
    //  Browser.getWindow().scrollTo(0, NumberUtils.toInt(navStorage.getItem("scroll" + href)));
    // }
  }

  private void startActivity() {
    Document doc = Browser.getDocument();
    final Map<String, List<String>> map = Window.Location.getParameterMap();
    if (!map.containsKey("jsrender")) {
      Element content = doc.getElementById("content");
      Element parent = DomUtils.getFirstChild(content);
      currentActivity.setElement(parent);
      currentActivity.decorate(currentActivity.getElement());
      return;
    }
    factory.getRpcService().request(doc.getDocumentURI(), null, new ContentRpcService.AsyncCallback() {
      @Override
      public void onSuccessAll(JsonObject jsonObject, String responseText) {
        currentActivity.setModel(Jsons.convertJsonObject(jsonObject));
        currentActivity.render(Browser.getDocument().getElementById("content"));
        // currentActivity.render(null);
      }
    });
  }
}
