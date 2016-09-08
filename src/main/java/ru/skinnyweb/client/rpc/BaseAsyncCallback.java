package ru.skinnyweb.client.rpc;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by smalex on 30/09/14.
 */
public abstract class BaseAsyncCallback<T> implements AsyncCallback<T> {

  public final void onFailure(Throwable caught) {
    String message = caught.getMessage();
    final int pos = message.indexOf('|');
    if (pos != -1) {
      message = message.substring(pos + 1);
    }
    if (message.length() > 100) {
      message = message.substring(0, 100);
    }
    Window.alert(message);
  }

  public final void onSuccess(T result) {
    innerSuccess(result);
  }

  public abstract void innerSuccess(T result);
}
