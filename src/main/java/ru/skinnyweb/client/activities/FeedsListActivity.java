package ru.skinnyweb.client.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.json.JsonObject;

import ru.skinnyweb.client.AppFactory;
import ru.skinnyweb.client.rpc.ContentRpcService;
import ru.skinnyweb.client.rpc.JsonAsyncCallback;
import ru.skinnyweb.client.util.DomUtils;
import ru.skinnyweb.shared.templates.FeedsListPage;

public class FeedsListActivity extends BaseActivity {
  private final FeedsListPage page = new FeedsListPage();
  private final AppFactory factory;
  private Element buttonRefresh;
  private ContentRpcService rpc;
  private Map<String, Element> mapElements = new HashMap<>();

  public FeedsListActivity(AppFactory factory) {
    super(factory);
    this.factory = factory;
    rpc = factory.getRpcService();
    navHelper.setStyleContainer("feedsContainer");
    navHelper.setStyleItem("feed");
  }

  @Override
  protected void createDom() {
    String body = page.toString(page.renderBody((Map<String, Object>) getModel()));
    decorateInternal((Element) DomUtils.htmlToDocumentFragment_(doc, body));
  }

  @Override
  public void decorateInternal(final Element element) {
    super.decorateInternal(element);

    buttonRefresh = getElementByClassNameRequired("buttonRefresh");
  }

  @Override
  public void enterDocument() {
    super.enterDocument();
    addHandlerRegistration(buttonRefresh.addEventListener(Event.CLICK, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        clickRefresh();
        evt.stopPropagation();
        evt.preventDefault();
      }
    }, false));
  }

  private List<String> getFeedIds() {
    mapElements.clear();
    Element[] els = DomUtils.getElementsByClassName(getElement(), "feed");
    List<String> ids = new ArrayList<>();
    for (Element el : els) {
      ids.add(el.getAttribute("data-id"));
      mapElements.put(el.getAttribute("data-id"), el);
    }
    return ids;
  }

  private void clickRefresh() {
    List<String> ids = getFeedIds();
    if (!ids.isEmpty()) {
      doReq(ids, 0);
    }
  }

  private void doReq(final List<String> ids, final int pos) {
    final String id = ids.get(pos);
    updateCount(id, "<i class='fa fa-spinner fa-pulse fa-fw'></i>");
    rpc.request("/import/?feedId=" + id, null, new JsonAsyncCallback() {
      @Override
      public void onSuccess(JsonObject main) {
        if (main.getBoolean("result")) {
          updateCount(id, String.valueOf(main.getNumber("count")));
          if (pos + 1 < ids.size()) {
            doReq(ids, pos + 1);
          } else {
            // OpenPageEvent.fire(factory.getEventBus(), "/", "");
          }
        } else {
          Browser.getWindow().alert("Refresh error");
        }
      }
    });
  }

  private void updateCount(String id, String count) {
    Element el = mapElements.get(id);
    if (el == null) return;

    Element badgeEl = DomUtils.getElementByClassName(el, "badge");
    if (badgeEl == null) return;

    badgeEl.setInnerHTML(count);
  }
}
