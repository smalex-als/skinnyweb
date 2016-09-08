package ru.skinnyweb.client.rpc;

import elemental.json.JsonObject;
import ru.skinnyweb.client.rpc.ContentRpcService.AsyncCallback;

public abstract class JsonAsyncCallback implements AsyncCallback {

  @Override
  public void onSuccessAll(JsonObject jsonObject, String responseText) {
    onSuccess(jsonObject);
  }

  public abstract void onSuccess(JsonObject main);
}
