package ru.skinnyweb.client.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Created by smalex on 13/12/15.
 */
public class OpenPageEvent extends Event<OpenPageEvent.Handler> {
  private static final Type<OpenPageEvent.Handler> TYPE = new Type<OpenPageEvent.Handler>();

  private String url;
  private boolean back;

  public static void fire(EventBus eventBus, String url, boolean back, String sourceName) {
    eventBus.fireEventFromSource(new OpenPageEvent(url, back), sourceName);
  }

  public static void fire(EventBus eventBus, String url, String sourceName) {
    eventBus.fireEventFromSource(new OpenPageEvent(url, false), sourceName);
  }

  public static HandlerRegistration register(EventBus eventBus, String sourceName, Handler handler) {
    return eventBus.addHandlerToSource(TYPE, sourceName, handler);
  }

  /**
   * @param url
   */
  public OpenPageEvent(String url, boolean back) {
    this.url = url;
    this.back = back;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onOpenPage(this);
  }

  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  public boolean isBack() {
    return back;
  }

  public interface Handler {
    void onOpenPage(OpenPageEvent event);
  }
}
