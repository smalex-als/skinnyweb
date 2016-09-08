package ru.skinnyweb.client.activities;

import java.util.logging.Logger;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;

import elemental.dom.Element;

import ru.skinnyweb.client.AppFactory;
import ru.skinnyweb.client.controls.Component;
import ru.skinnyweb.client.util.ArticlesNavigationHelper;

/**
 * Created by smalex on 08/07/15.
 */
public abstract class BaseActivity extends Component {
  private final Logger log = Logger.getLogger(getClass().getName());
  // private final GlobalStyleSwitcher globalStyleSwitcher;
  protected final ArticlesNavigationHelper navHelper;
  private HandlerRegistration nativePreviewHandlerRegistration;
  protected AppFactory factory;

  public BaseActivity(AppFactory factory) {
    this.factory = factory;
    // globalStyleSwitcher = new GlobalStyleSwitcher(factory);
    navHelper = new ArticlesNavigationHelper(factory);
  }

  @Override
  public void decorateInternal(final Element element) {
    super.decorateInternal(element);

    navHelper.decorateInternal(element);
  }

  @Override
  public void enterDocument() {
    log.info("enterDocument");
    super.enterDocument();
    // globalStyleSwitcher.enterDocument();
    navHelper.enterDocument();
    startHandlingKeys();
  }

  @Override
  public void exitDocument() {
    log.info("exitDocument");
    stopHandlingKeys();
    super.exitDocument();
  }

  protected boolean onPreviewNativeEvent(NativePreviewEvent event) {
    return false;
  }

  private void startHandlingKeys() {
    stopHandlingKeys();
    // log.info("startHandlingKeys");
    nativePreviewHandlerRegistration = Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        // log.info("hit key " + nativeEvent);
        if (!BaseActivity.this.onPreviewNativeEvent(event)) {
//           globalStyleSwitcher.onPreviewNativeEvent(event);
          navHelper.onPreviewNativeEvent(event);
        }
      }
    });
  }

  private void stopHandlingKeys() {
    // stop previewing page events
    if (nativePreviewHandlerRegistration != null) {
      // log.info("stopHandlingKeys");
      nativePreviewHandlerRegistration.removeHandler();
      nativePreviewHandlerRegistration = null;
    }
  }
}

