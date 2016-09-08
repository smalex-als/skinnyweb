package ru.skinnyweb.client.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.web.bindery.event.shared.EventBus;

import elemental.client.Browser;
import elemental.dom.Element;

import ru.skinnyweb.client.AppFactory;
import ru.skinnyweb.client.NavigationStack;
import ru.skinnyweb.client.event.OpenPageEvent;

public class ArticlesNavigationHelper {
  private static final Logger log = Logger.getLogger(ArticlesNavigationHelper.class.getName());
  private static final String SELECTED = "selected";
  private String styleContainer = "articlesContainer";
  private String styleItem = "article";
  private Element[] els;
  private final EventBus eventBus;
  private final NavigationStack navigationStack;

  public ArticlesNavigationHelper(AppFactory factory) {
    eventBus = factory.getEventBus();
    navigationStack = factory.getNavigationStack();
  }

  public void decorateInternal(Element element) {
    updateItems(element);
  }

  public void enterDocument() {
    List<String> historyItems = navigationStack.getHistory();
    if (historyItems.isEmpty()) {
      return;
    }
    Map<String, Element> mapElements = new HashMap<String, Element>();
    for (int i = 0; i < els.length; i++) {
      Element el = DomUtils.getElementByClassName(els[i], "link");
      if (el != null) {
        mapElements.put(el.getAttribute("href"), els[i]);
      }
    }
    for (int i = historyItems.size() - 1; i >= 0; i--) {
      final Element el = mapElements.get(historyItems.get(i));
      if (el != null) {
        StyleUtils.addClassName(el, SELECTED);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
          @Override
          public void execute() {
            if (!isScrolledIntoView(el)) {
              el.scrollIntoView();
            }
          }
        });
        break;
      }
    }
  }

  public boolean onPreviewNativeEvent(NativePreviewEvent evt) {
    Event event = Event.as(evt.getNativeEvent());
    Element target = (Element) event.getEventTarget();
    if (target != null) {
      String nodeName = target.getNodeName();
      if (nodeName.equals("INPUT") || nodeName.equals("TEXTAREA")) {
        return false;
      }
    }
    if (!evt.isCanceled() && evt.getTypeInt() == Event.ONCLICK) {
      int level = 0;
      Element srcEl = target;
      while (level < 3 && srcEl != null && !"A".equals(srcEl.getTagName())) {
        srcEl = srcEl.getParentElement();
        level++;
      }
      if (srcEl != null && "A".equals(srcEl.getTagName())) {
        String href = srcEl.getAttribute("href");
        if (!href.startsWith("javascript") && !href.startsWith("https://")
            && !href.startsWith("http://")) {
          event.preventDefault();
          event.stopPropagation();
          OpenPageEvent.fire(eventBus, href, "");
        }
      }
    } else if (evt.getTypeInt() == com.google.gwt.user.client.Event.ONKEYDOWN) {
      log.info("hit key " + evt);
      if ((event.getKeyCode() == KeyCodes.KEY_UP 
            || event.getKeyCode() == 'K')
          && !(event.getMetaKey() || event.getAltKey())) {
        event.preventDefault();
        event.stopPropagation();
        handleMove(-1);
        return true;
      } else if ((event.getKeyCode() == KeyCodes.KEY_DOWN 
            || event.getKeyCode() == 'J') 
          && !(event.getMetaKey() || event.getAltKey())) {
        event.preventDefault();
        event.stopPropagation();
        handleMove(1);
        return true;
      } else if (event.getKeyCode() == KeyCodes.KEY_O
          || event.getKeyCode() == KeyCodes.KEY_ENTER) {
        event.preventDefault();
        event.stopPropagation();
        handleOpen();
        return true;
      } else if (event.getKeyCode() == 'U' 
          && !(event.getMetaKey() || event.getAltKey())) {
        // upper level
        event.preventDefault();
        event.stopPropagation();
        OpenPageEvent.fire(eventBus, navigationStack.getLevelUp(), true, "");
      }
    }
    return false;
  }

  private int getSelectedIndex() {
    for (int i = 0; i < els.length; i++) {
      if (StyleUtils.hasClassName(els[i], SELECTED)) {
        return i;
      }
    }
    return -1;
  }

  private List<String> getAllLinks() {
    List<String> links = new ArrayList<String>();
    for (int i = 0; i < els.length; i++) {
      Element el = DomUtils.getElementByClassName(els[i], "link");
      if (el != null) {
        links.add(el.getAttribute("href"));
      }
    }
    return links;
  }

  private void handleOpen() {
    int found = getSelectedIndex();
    if (found == -1) {
      return;
    }
    Element el = DomUtils.getElementByClassName(els[found], "link");
    if (el != null) {
      OpenPageEvent.fire(eventBus, el.getAttribute("href"), "");
    }
  }

  public void goPrevArticle() {
    String href = navigationStack.getPrev();
    if (href != null) {
      OpenPageEvent.fire(eventBus, href, "");
    }
  }

  public void goNextArticle() {
    String href = navigationStack.getNext();
    if (href != null) {
      OpenPageEvent.fire(eventBus, href, "");
    }
  }

  private void handleMove(int inc) {
    if (els.length == 0) {
      String nextHref = inc == 1 ? navigationStack.getNext() : navigationStack.getPrev();
      if (nextHref != null) {
        OpenPageEvent.fire(eventBus, nextHref, "");
      }
      return;
    }
    int found = getSelectedIndex();
    int next = found == -1 ? 0 : found + inc;
    if (next >= 0 && next < els.length) {
      if (found != -1) {
        StyleUtils.removeClassName(els[found], SELECTED);
      }
      StyleUtils.addClassName(els[next], SELECTED);
      if (!isScrolledIntoView(els[next])) {
        if (inc < 0) {
          int h = Browser.getWindow().getInnerHeight();
          Browser.getWindow().scrollBy(0, -h);
        } else {
          els[next].scrollIntoView();
        }
      }
    }
  }
  
  private boolean isScrolledIntoView(Element el) {
    int top = Browser.getWindow().getScrollY();
    int bottom = top + Browser.getWindow().getInnerHeight();
    int elTop = el.getOffsetTop();
    int elBottom = elTop + el.getOffsetHeight();
    return elBottom <= bottom && elTop >= top;
  }

  public void setStyleContainer(String styleContainer) {
    this.styleContainer = styleContainer;
  }

  public void setStyleItem(String styleItem) {
    this.styleItem = styleItem;
  }

  public void updateItems(Element element) {
    Element containerEl = DomUtils.getElementByClassName(element, styleContainer);
    if (containerEl == null) {
      els = new Element[0];
      return;
    }
    els = DomUtils.getElementsByClassName(element, styleItem);
    log.info("found " + els.length + " items");
    navigationStack.addChildrenUrls(getAllLinks());
  }
}
