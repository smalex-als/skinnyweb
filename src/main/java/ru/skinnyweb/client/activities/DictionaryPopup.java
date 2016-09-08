package ru.skinnyweb.client.activities;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

import elemental.dom.Element;
import elemental.html.SpanElement;
import elemental.json.JsonObject;

import ru.skinnyweb.client.util.StringUtils;

/**
 * Created by smalex on 05/03/15.
 */
public class DictionaryPopup extends PopupPanel {
  private HTML descriptionHTML = new HTML();
  private Map<String, JsonObject> cache = new HashMap<>();
  private SpanElement selectedElement;

  public DictionaryPopup() {
    super(true, true);
    setStyleName("abstract-dialog");
    FlowPanel flowPanel = new FlowPanel();
    flowPanel.add(descriptionHTML);
    flowPanel.add(new HTML("<br/>"));

    ScrollPanel scrollPanel = new ScrollPanel(flowPanel);
    scrollPanel.setHeight("300px");
    scrollPanel.setWidth((Window.getClientWidth() - 30) + "px");
    setWidget(scrollPanel);
    addCloseOnEscape();
  }

  private void addCloseOnEscape() {
    Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      public void onPreviewNativeEvent(Event.NativePreviewEvent previewEvent) {
        if (isAutoHideEnabled() && previewEvent.getTypeInt() == Event.ONKEYDOWN) {
          final Event event = Event.as(previewEvent.getNativeEvent());
          if (event.getKeyCode() == KeyCodes.KEY_ESCAPE || event.getKeyCode() == KeyCodes.KEY_X) {
            hide();
          }
        }
      }
    });
  }

  @Override
  public void hide() {
    super.hide();
//    RootPanel.getBodyElement().removeClassName(css.noscroll());
//    Window.enableScrolling(true);
    this.selectedElement = null;
  }

  @Override
  public void show() {
//    Window.enableScrolling(false);
//    RootPanel.getBodyElement().addClassName(css.noscroll());
//    Document doc = Browser.getDocument();
//    final Element el = doc.getBody();
    setStyleName("abstract-dialog");
    super.show();
  }


  public void show(final Element el, String word, String description) {
    String html = "Not found";
    if (StringUtils.hasLength(description)) {
      html = description.replace("\n", "<br/>");
//      .replace(' ', '\u00A0');
      html = "<div><div class=\"description-word\">" + word + "</div>" +
          "<div class=\"description-text\">" + html + "</div></div>";
    }
    descriptionHTML.setHTML(html);
    setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      public void setPosition(int offsetWidth, int offsetHeight) {
        position(DictionaryPopup.this, el, offsetWidth, offsetHeight);
      }
    });
  }

  public int getAbsoluteTop(Element elem) {
    return toInt32(getSubPixelAbsoluteTop(elem));
  }

  /**
   * Fast helper method to convert small doubles to 32-bit int.
   * <p/>
   * <p>Note: you should be aware that this uses JavaScript rounding and thus
   * does NOT provide the same semantics as <code>int b = (int) someDouble;</code>.
   * In particular, if x is outside the range [-2^31,2^31), then toInt32(x) would return a value
   * equivalent to x modulo 2^32, whereas (int) x would evaluate to either MIN_INT or MAX_INT.
   */
  protected static native int toInt32(double val) /*-{
    return val | 0;
  }-*/;


  private native double getSubPixelAbsoluteTop(Element elem) /*-{
    var top = 0;
    var curr = elem;
    // This intentionally excludes body which has a null offsetParent.
    while (curr.offsetParent) {
      top -= curr.scrollTop;
      curr = curr.parentNode;
    }
    while (elem) {
      top += elem.offsetTop;
      elem = elem.offsetParent;
    }
    return top;
  }-*/;


  public int getAbsoluteLeft(Element elem) {
    return toInt32(getSubPixelAbsoluteLeft(elem));
  }

  private native double getSubPixelAbsoluteLeft(Element elem) /*-{
    var left = 0;
    var curr = elem;
    // This intentionally excludes body which has a null offsetParent.
    while (curr.offsetParent) {
      left -= curr.scrollLeft;
      curr = curr.parentNode;
    }
    while (elem) {
      left += elem.offsetLeft;
      elem = elem.offsetParent;
    }
    return left;
  }-*/;


  /**
   * Positions the popup, called after the offset width and height of the popup
   * are known.
   *
   * @param relativeObject the ui object to position relative to
   * @param offsetWidth    the drop down's offset width
   * @param offsetHeight   the drop down's offset height
   */
  private void position(PopupPanel popup, final Element relativeObject, int offsetWidth, int offsetHeight) {
    // Calculate left position for the popup. The computation for
    // the left position is bidi-sensitive.

    int textBoxOffsetWidth = relativeObject.getOffsetWidth();

    // Compute the difference between the popup's width and the
    // textbox's width
    int offsetWidthDiff = offsetWidth - textBoxOffsetWidth;

    int left;

    if (LocaleInfo.getCurrentLocale().isRTL()) { // RTL case

//      int textBoxAbsoluteLeft = getAbsoluteLeft(relativeObject);
      int textBoxAbsoluteLeft = getAbsoluteLeft(relativeObject);

      // Right-align the popup. Note that this computation is
      // valid in the case where offsetWidthDiff is negative.
      left = textBoxAbsoluteLeft - offsetWidthDiff;

      // If the suggestion popup is not as wide as the text box, always
      // align to the right edge of the text box. Otherwise, figure out whether
      // to right-align or left-align the popup.
      if (offsetWidthDiff > 0) {

        // Make sure scrolling is taken into account, since
        // box.getAbsoluteLeft() takes scrolling into account.
        int windowRight = Window.getClientWidth() + Window.getScrollLeft();
        int windowLeft = Window.getScrollLeft();

        // Compute the left value for the right edge of the textbox
        int textBoxLeftValForRightEdge = textBoxAbsoluteLeft + textBoxOffsetWidth;

        // Distance from the right edge of the text box to the right edge
        // of the window
        int distanceToWindowRight = windowRight - textBoxLeftValForRightEdge;

        // Distance from the right edge of the text box to the left edge of the
        // window
        int distanceFromWindowLeft = textBoxLeftValForRightEdge - windowLeft;

        // If there is not enough space for the overflow of the popup's
        // width to the right of the text box and there IS enough space for the
        // overflow to the right of the text box, then left-align the popup.
        // However, if there is not enough space on either side, stick with
        // right-alignment.
        if (distanceFromWindowLeft < offsetWidth && distanceToWindowRight >= offsetWidthDiff) {
          // Align with the left edge of the text box.
          left = textBoxAbsoluteLeft;
        }
      }
    } else { // LTR case

      // Left-align the popup.
      // left = getAbsoluteLeft(relativeObject);
      left = 0;

      // If the suggestion popup is not as wide as the text box, always align to
      // the left edge of the text box. Otherwise, figure out whether to
      // left-align or right-align the popup.
      if (offsetWidthDiff > 0) {
        // Make sure scrolling is taken into account, since
        // box.getAbsoluteLeft() takes scrolling into account.
        int windowRight = Window.getClientWidth() + Window.getScrollLeft();
        int windowLeft = Window.getScrollLeft();

        // Distance from the left edge of the text box to the right edge
        // of the window
        int distanceToWindowRight = windowRight - left;

        // Distance from the left edge of the text box to the left edge of the
        // window
        int distanceFromWindowLeft = left - windowLeft;

        // If there is not enough space for the overflow of the popup's
        // width to the right of hte text box, and there IS enough space for the
        // overflow to the left of the text box, then right-align the popup.
        // However, if there is not enough space on either side, then stick with
        // left-alignment.
        if (distanceToWindowRight < offsetWidth && distanceFromWindowLeft >= offsetWidthDiff) {
          // Align with the right edge of the text box.
          left -= offsetWidthDiff;
        }
      }
    }

    // Calculate top position for the popup

    int top = getAbsoluteTop(relativeObject);

    // Make sure scrolling is taken into account, since
    // box.getAbsoluteTop() takes scrolling into account.
    int windowTop = Window.getScrollTop();
    int windowBottom = Window.getScrollTop() + Window.getClientHeight();

    // Distance from the top edge of the window to the top edge of the
    // text box
    int distanceFromWindowTop = top - windowTop;

    // Distance from the bottom edge of the window to the bottom edge of
    // the text box
    int distanceToWindowBottom = windowBottom - (top + relativeObject.getOffsetHeight());

    // If there is not enough space for the popup's height below the text
    // box and there IS enough space for the popup's height above the text
    // box, then then position the popup above the text box. However, if there
    // is not enough space on either side, then stick with displaying the
    // popup below the text box.
    if (distanceToWindowBottom < offsetHeight && distanceFromWindowTop >= offsetHeight) {
      top -= offsetHeight;
    } else {
      // Position above the text box
      top += relativeObject.getOffsetHeight();
    }
    popup.setPopupPosition(left, top);
  }

}
