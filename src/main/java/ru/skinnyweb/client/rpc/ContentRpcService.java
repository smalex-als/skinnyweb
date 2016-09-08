package ru.skinnyweb.client.rpc;

import elemental.json.JsonObject;

/**
 * Created by smalex on 30/09/14.
 */
public interface ContentRpcService {
  /**
   * An observer interface that provides callbacks useful for giving the user
   * feedback about calls to the server.
   */
  interface StatusObserver {
    /**
     * Invoked when RPC calls begin to succeed again after a failure was
     * reported.
     */
    void onServerCameBack();

    /**
     * Invoked when RPC calls to the server are failing.
     */
    void onServerWentAway();

    /**
     * Invoked when current task has finished. This is often used to stop
     * displaying status Ui that was made visible in the
     * {@link StatusObserver#onTaskStarted(String)} callback.
     */
    void onTaskFinished();

    /**
     * Invoked when a task that requires user feedback starts.
     *
     * @param description
     *          a description of the task that is starting
     */
    void onTaskStarted(String description);
  }

  interface AsyncCallback {
    void onSuccessAll(JsonObject jsonObject, String responseText);
  }

  abstract class BaseAsyncCallback implements AsyncCallback {

    @Override
    public void onSuccessAll(JsonObject jsonObject, String responseText) {
      onSuccess(jsonObject);
    }

    public abstract void onSuccess(JsonObject main);
  }

  void request(String url, JsonObject reqObject, AsyncCallback asyncCallback);
}
