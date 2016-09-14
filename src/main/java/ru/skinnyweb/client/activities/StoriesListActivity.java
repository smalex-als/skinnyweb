package ru.skinnyweb.client.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.user.client.Window;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.json.JsonObject;

import ru.skinnyweb.client.AppFactory;
import ru.skinnyweb.client.event.OpenPageEvent;
import ru.skinnyweb.client.rpc.ContentRpcService;
import ru.skinnyweb.client.rpc.JsonAsyncCallback;
import ru.skinnyweb.client.util.DomUtils;
import ru.skinnyweb.client.util.JsonUtils;
import ru.skinnyweb.client.util.StringUtils;
import ru.skinnyweb.client.util.StyleUtils;
import ru.skinnyweb.shared.templates.Jsons;
import ru.skinnyweb.shared.templates.StoriesListPage;

public class StoriesListActivity extends BaseActivity {
  interface Presenter {
    void buttonsEnable(boolean enabled);
    void reloadArticles();
  }

  private static final Logger log = Logger.getLogger(StoriesListActivity.class.getName());
  private final ContentRpcService rpc;
  private final StoriesListPage page = new StoriesListPage();
  private final AppFactory factory;
  private Element buttonBack;
  private Element buttonRefresh;
  private Element buttonArchiveAll;
  private Element articlesContainer;
  private List<Element> toDelete = new ArrayList<>();
    
  public StoriesListActivity(AppFactory factory) {
    super(factory);
    this.factory = factory;
    rpc = factory.getRpcService();
  }

  @Override
  protected void createDom() {
    String body = page.toString(page.renderBody((Map<String, Object>) getModel()));
    decorateInternal((Element) DomUtils.htmlToDocumentFragment_(doc, body));
  }

  @Override
  public void decorateInternal(final Element element) {
    super.decorateInternal(element);

    buttonBack = getElementByClassNameRequired("buttonBack");
    buttonRefresh = getElementByClassNameRequired("buttonRefresh");
    buttonArchiveAll = getElementByClassNameRequired("buttonArchiveAll");
    articlesContainer = getElementByClassNameRequired("articlesContainer");
  }

  @Override
  public void enterDocument() {
    super.enterDocument();
    toDelete.clear();

    addHandlerRegistration(buttonBack.addEventListener(Event.CLICK, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        OpenPageEvent.fire(factory.getEventBus(), "/", "");
        evt.stopPropagation();
        evt.preventDefault();
      }
    }, false));
    addHandlerRegistration(buttonRefresh.addEventListener(Event.CLICK, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        clickRefresh();
        evt.stopPropagation();
        evt.preventDefault();
      }
    }, false));
    addHandlerRegistration(buttonArchiveAll.addEventListener(Event.CLICK, 

          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        clickArchiveAll();
        evt.stopPropagation();
        evt.preventDefault();
      }
    }, false));
    addHandlerRegistration(getElement().addEventListener(Event.CLICK,
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        Element el = (Element) evt.getTarget();
        if (handleBodyClick(el)) {
          evt.preventDefault();
          evt.stopPropagation();
        } else {
          final Element objEl = findParentElementByAttr(el, "data-id");
          if (objEl != null) {
            Element linkEl = DomUtils.getElementByClassName(objEl, "link");
            if (linkEl != null) {
              OpenPageEvent.fire(factory.getEventBus(), linkEl.getAttribute("href"), "");
            }
          }
        }
      }
    }, false));

    addHandlerRegistration(Browser.getDocument().addEventListener(Event.SCROLL, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        scheduleRemove(null);
      }
    }, false));
  }

  private boolean handleBodyClick(final Element el) {
    log.info("handleBodyClick");
    final Element targetEl = findParentElementHasClassName(el, "pure-button");
    final Element objEl = findParentElementByAttr(el, "data-id");
    if (targetEl == null) {
      log.warning("button not found");
      return false;
    }
    if (objEl == null) {
      log.warning("object element not found");
      return false;
    }
    final String id = objEl.getAttribute("data-id");
    if (!StringUtils.hasText(id)) {
      log.warning("data-id not found");
      return false;
    }
    if (StyleUtils.hasClassName(targetEl, "buttonArchive")) {
      clickArchive(objEl, id);
    } else if (StyleUtils.hasClassName(targetEl, "buttonPin")) {
      clickPin(targetEl, id, true);
    } else if (StyleUtils.hasClassName(targetEl, "buttonUnpin")) {
      clickPin(targetEl, id, false);
    } else {
      log.warning("action not found in className: " + targetEl.getClassName());
      return false;
    }
    return true;
  }

  private Element findParentElementByAttr(Element el, String attr) {
    while (el != null && el.getAttribute(attr) == null) {
      el = el.getParentElement();
    }
    return el;
  }

  private Element findParentElementHasClassName(Element el, String className) {
    while (el != null && !StyleUtils.hasClassName(el, className)) {
      el = el.getParentElement();
    }
    return el;
  }

  private void clickArchive(Element el, String id) {
    // int height = el.getClientHeight() - 24;
    String html = "<div style=\"display: table-cell; vertical-align: middle;\">" 
      + "<div>Archived</div></div>";
    el.setInnerHTML(html);
    // el.getStyle().setHeight(height, "px");
    el.setClassName("articleArchived");
    rpcPostEdit(id, "archived", true);
    scheduleRemove(el);
  }

  private void clickPin(Element el, String id, boolean value) {
    StyleUtils.toggleClass(el, "buttonUnpin", value);
    StyleUtils.toggleClass(el, "buttonPin", !value);
    Element imgEl = DomUtils.getElementByClassName(el, "fa");
    StyleUtils.toggleClass(imgEl, "fa-star", value);
    StyleUtils.toggleClass(imgEl, "fa-star-o", !value);
    rpcPostEdit(id, "starred", value);
  }

  private void scheduleRemove(final Element el) {
    if (!toDelete.isEmpty()) {
      for (Element e : toDelete) {
        new ItemRemoveAnim(e).run(800);
      }
      toDelete.clear();
    }
    if (el != null) {
      toDelete.add(el);
    }
  }

  private void rpcPostEdit(String id, String prop, boolean value) {
    Map<String, Object> obj = new HashMap<>();
    obj.put("id", id);
    obj.put("prop", prop);
    obj.put("value", Boolean.valueOf(value));
    JsonObject reqObj = JsonUtils.mapToJsonObject(obj);
    rpc.request("/story/edit/", reqObj, new JsonAsyncCallback() {
      @Override
      public void onSuccess(JsonObject main) {
      }
    });
  }

  private void reloadArticles() {
    rpc.request("/feed-" + getFeedId() + "/", null, new JsonAsyncCallback() {
      @Override
      public void onSuccess(JsonObject main) {
        Map<String, Object> model = Jsons.convertJsonObject(main);
        String articles = page.toString(page.renderArticles(model));
        articlesContainer.setInnerHTML(articles);
        navHelper.updateItems(getElement());
      }
    });
  }

  private String getFeedId() {
    Element el = DomUtils.getElementByClassName(getElement(), "feed");
    return el.getAttribute("data-id");
  }

  private void clickRefresh() {
    articlesContainer.setInnerHTML("Loading...");
    rpc.request("/import/?feedId=" + getFeedId(), null, 
        new JsonAsyncCallback() {
      @Override
      public void onSuccess(JsonObject main) {
        if (main.getBoolean("result")) {
          reloadArticles();
        } else {
          Browser.getWindow().alert("Refresh error");
        }
      }
    });
  }

  private List<String> getArticlesId() {
    Element[] els = DomUtils.getElementsByClassName(getElement(), "article");
    List<String> ids = new ArrayList<>();
    for (Element el : els) {
      ids.add(el.getAttribute("data-id"));
    }
    return ids;
  }

  private void clickArchiveAll() {
    Map<String, Object> obj = new HashMap<>();
    obj.put("ids", getArticlesId());
    obj.put("prop", "archived");
    obj.put("value", "true");
    JsonObject reqObj = JsonUtils.mapToJsonObject(obj);

    articlesContainer.setInnerHTML("Archiving...");
    rpc.request("/story/edit/", reqObj, new JsonAsyncCallback() {
      @Override
      public void onSuccess(JsonObject main) {
        if (main.getBoolean("result")) {
          OpenPageEvent.fire(factory.getEventBus(), "/", "");
        } else {
          Browser.getWindow().alert("Archive error");
        }
      }
    });
  }

  private void clickAdd() {
  }
}
