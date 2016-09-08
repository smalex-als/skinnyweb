package ru.skinnyweb.client.activities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import elemental.client.Browser;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.Selection;
import elemental.json.JsonObject;
import elemental.ranges.Range;

import ru.skinnyweb.client.AppFactory;
import ru.skinnyweb.client.event.OpenPageEvent;
import ru.skinnyweb.client.rpc.ContentRpcService;
import ru.skinnyweb.client.rpc.JsonAsyncCallback;
import ru.skinnyweb.client.util.DomUtils;
import ru.skinnyweb.client.util.JsonUtils;
import ru.skinnyweb.client.util.StyleUtils;
import ru.skinnyweb.shared.templates.StoryViewPage;

public class StoryViewActivity extends BaseActivity {
  private static final Logger log = Logger.getLogger(StoryViewActivity.class.getName());
  private AppFactory factory;
  private Element buttonBack;
  private Element buttonArchive;
  private Element buttonPrev;
  private Element buttonNext;
  private Element storyEl;
  private Element contentEl;
  private Element fontTestEl;
  private Element buttonPrevPage;
  private Element buttonNextPage;
  private Element footerEl;
  private Element innerFooterEl;
  private final ContentRpcService rpc;
  private String lastWord;
  private String lastDescription;

  public StoryViewActivity(AppFactory factory) {
    super(factory);
    this.factory = factory;
    rpc = factory.getRpcService();
  }

  @Override
  protected void createDom() {
    StoryViewPage page = new StoryViewPage();
    String body = page.toString(page.renderBody((Map<String, Object>) getModel()));
    decorateInternal((Element) DomUtils.htmlToDocumentFragment_(doc, body));
  }

  @Override
  public void decorateInternal(final Element element) {
    super.decorateInternal(element);

    storyEl = getElementByClassNameRequired("article");
    buttonBack = getElementByClassNameRequired("buttonBack");
    buttonArchive = getElementByClassNameRequired("buttonArchive");
    buttonPrev = getElementByClassNameRequired("buttonPrev");
    buttonNext = getElementByClassNameRequired("buttonNext");
    contentEl = getElementByClassNameRequired("content");
    fontTestEl = getElementByClassNameRequired("fontTest");
    // buttonPrevPage = getElementByClassNameRequired("buttonPrevPage");
    // buttonNextPage = getElementByClassNameRequired("buttonNextPage");
    footerEl = getElementByClassNameRequired("footer");
    innerFooterEl = getElementByClassNameRequired("innerFooter");
  }

  @Override
  public void enterDocument() {
    super.enterDocument();

    addHandlerRegistration(buttonNext.addEventListener(Event.CLICK, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        navHelper.goNextArticle();
      }
    }, false));
    addHandlerRegistration(buttonPrev.addEventListener(Event.CLICK, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        navHelper.goPrevArticle();
      }
    }, false));
    addHandlerRegistration(buttonBack.addEventListener(Event.CLICK, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        levelUp();
      }
    }, false));
    addHandlerRegistration(buttonArchive.addEventListener(Event.CLICK, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        clickArchive("archived", "true");
      }
    }, false));
    addHandlerRegistration(footerEl.addEventListener(Event.CLICK, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        log.info("click!");

        DictionaryPopup popup = new DictionaryPopup();
        popup.show(buttonArchive, lastWord, lastDescription);
      }
    }, false));

    // addHandlerRegistration(buttonNextPage.addEventListener(Event.CLICK, 
    //       new EventListener() {
    //   @Override
    //   public void handleEvent(Event evt) {
    //     clickNextPage();
    //   }
    // }, false));
    addHandlerRegistration(contentEl.addEventListener(Event.CLICK, 
          new EventListener() {
      @Override
      public void handleEvent(Event evt) {
        clickTranslate();
      }
    }, false));
    StyleUtils.showElement(innerFooterEl, "none");
  }

  public void clickTranslate() {
    log.info("click");
    Selection sel = Browser.getWindow().getSelection();
    // String str = sel.toString();
    Node node = sel.getAnchorNode();
    // sel.modify("move", "forward", "character");
    // sel.modify("move", "backward", "word");
    // sel.modify("extend", "forward", "word");
    Range range = sel.getRangeAt(0);
    String text = node.getTextContent();
    int start = range.getStartOffset();
    int oldstart = start;
    log.info("start = " + start);
    log.info("length = " + text.length());
    if (start < text.length()) {
      while (start > 0) {
        if (text.charAt(start) == ' ') {
          start++;
          break;
        }
        start--;
      }
      // accept only click in middle word
      if (start != oldstart) {
        int end = start;
        while (end < text.length()) {
          char ch = text.charAt(end);
          if (!((ch >= 'a' && ch <= 'z') 
              || (ch >= 'A' && ch <= 'Z'))) {
            break;
          }
          end++;
        }
        String str = text.substring(start, end).trim();
        if (!str.isEmpty()) {
          translateWord(str);
          return;
        }
      }
    }
    StyleUtils.showElement(innerFooterEl, "none");
  }

  private void translateWord(final String str) {
    Map<String, Object> obj = new HashMap<>();
    obj.put("word", str);
    JsonObject reqObj = JsonUtils.mapToJsonObject(obj);
    rpc.request("/translate/", reqObj, new JsonAsyncCallback() {
      @Override
      public void onSuccess(JsonObject main) {
        lastWord = main.getString("word");
        lastDescription = main.getString("descriptionFull");
        lastDescription = lastDescription.replace("\n", "<br/>")
          .replace(' ', '\u00A0');
        StyleUtils.showElement(innerFooterEl, "block");
        String desc = main.getString("word") + " &mdash; " 
          + main.getString("description");
        innerFooterEl.setInnerHTML(cutRows(innerFooterEl, desc, 3));
      }
    });
  }

  private String cutRows(Element footerEl, String desc, int rows) {
    // minus margin 2em
    int maxwidth = footerEl.getClientWidth() - 32;
    // log.info("maxwidth = " + maxwidth);
    StringBuilder sb = new StringBuilder();
    String[] words = desc.split("\\s+");
    StringBuilder sbPlainText = new StringBuilder();
    int cnt = 0;
    for (String word : words) {
      double len = getClientWidth(sbPlainText.toString() + " " + word);
      if (len >= maxwidth) {
        // log.info("fail len = " + len);
        // log.info("fail text = " + sbPlainText.toString() + " " + word);
        // double len2 = getClientWidth(sbPlainText.toString());
        // log.info("ok len = " + len2);
        sb.append(sbPlainText);
        sbPlainText.delete(0, sbPlainText.length());
        if (++cnt == rows) {
          break;
        }
      }
      if (sbPlainText.length() > 0) {
        sbPlainText.append(" ");
      } 
      sbPlainText.append(word);
    }
    if (sbPlainText.length() > 0) {
      sb.append(sbPlainText);
    }
    return sb.toString();
  }

  private void clickNextPage() {
    boolean wasVisible = false;
    Element found = null;
    List<Element> children = DomUtils.getElements(contentEl);
    for (Element line : children) {
      boolean res = isElementIntoView(line);
      if (res) {
        wasVisible = true;
      } else if (wasVisible) {
        found = line;
        log.info("line " + line.getAttribute("data-id") + (res ? " visible":""));
        break;
      }
    }
    if (found != null) {
      found.scrollIntoView();
    }
  }

  private boolean isElementIntoView(Element el) {
    int d = innerFooterEl.getClientHeight();
    int top = Browser.getWindow().getScrollY();
    int bottom = top + Browser.getWindow().getInnerHeight() - d;
    int elTop = el.getOffsetTop();
    int elBottom = elTop + el.getOffsetHeight();
    return elBottom <= bottom && elTop >= top;
  }

  private double getClientWidth(String text) {
    fontTestEl.setInnerHTML(text);
    int width = fontTestEl.getClientWidth();
    fontTestEl.setInnerText("");
    return width;
  }

  private void clickArchive(String prop, String value) {
    Map<String, Object> obj = new HashMap<>();
    obj.put("id", storyEl.getAttribute("data-id"));
    obj.put("prop", prop);
    obj.put("value", value);
    JsonObject reqObj = JsonUtils.mapToJsonObject(obj);
    rpc.request("/story/edit/", reqObj, new JsonAsyncCallback() {
      @Override
      public void onSuccess(JsonObject main) {
        levelUp();
      }
    });
  }

  private void levelUp() {
    String path = Browser.getWindow().getLocation().getPathname();
    int index = path.length() - 1;
    if (index == 0 || path.charAt(index) != '/') {
      return;
    }
    index--;
    for (; index > 0 && path.charAt(index) != '/'; index--);
    if (index == 0 || path.charAt(index) != '/') {
      return;
    }
    path = path.substring(0, index + 1);
    OpenPageEvent.fire(factory.getEventBus(), path, true, "");
  }
}
