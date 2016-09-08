package ru.skinnyweb.client.rpc;

import java.util.Set;
import java.util.logging.Logger;

// import ru.feedpoint.client.util.LoginUtils;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;

import elemental.client.Browser;
import elemental.events.Event;
import elemental.events.EventListener;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.xml.XMLHttpRequest;

/**
 * Created by smalex on 30/09/14.
 */
public class ContentRpcServiceImpl implements ContentRpcService {

  private static final Logger log = Logger.getLogger(ContentRpcServiceImpl.class.getName());

  /**
   * Encapsulates a linked list node that is used by {@link TaskQueue} to keep
   * an ordered list of pending {@link Task}s.
   */
  private static class Node {
    private final Task task;

    private Node next;

    Node(Task task) {
      this.task = task;
    }

    void execute(TaskQueue queue) {
      task.execute(queue);
    }
  }

  /**
   * Encapsulates a task for writing data to the server. The tasks are managed
   * by the {@link TaskQueue} and are auto-retried on failure.
   */
  private abstract static class Task {
    private TaskQueue queue;

    abstract void execute();

    void execute(TaskQueue queue) {
      this.queue = queue;
      execute();
    }

    TaskQueue getQueue() {
      return queue;
    }
  }

  /**
   * Provides a mechanism to perform write tasks sequentially and retry tasks
   * that fail.
   */
  private class TaskQueue extends RetryTimer {
    private Node head, tail;

    public void post(Task task) {
      final Node node = new Node(task);
      if (isIdle()) {
        head = tail = node;
        executeHead();
      } else {
        enqueueTail(node);
      }
    }

    private void enqueueTail(Node node) {
      assert head != null && tail != null;
      assert node != null;
      tail = tail.next = node;
    }

    private void executeHead() {
      head.execute(this);
    }

    private void executeNext() {
      head = head.next;
      if (head != null) {
        executeHead();
      } else {
        tail = null;
      }
    }

    private boolean isIdle() {
      return head == null;
    }

    private void taskFailed(Task task, boolean fatal) {
      assert task == head.task;

      // Report a failure to the Model.
      onServerFailed(fatal);

      // Schedule a retry.
      retryLater();
    }

    private void taskSucceeded(Task task) {
      assert task == head.task;
      // Report a success to the Model.
      onServerSucceeded();

      // Reset the retry counter.
      resetRetryCount();

      // Move on to the next task.
      executeNext();
    }

    @Override
    protected void retry() {
      // Retry running the head task.
      executeHead();
    }
  }

  private class RequestTask extends Task {

    private final String url;
    private final JsonObject reqObject;
    private final AsyncCallback asyncCallback;

    public RequestTask(String url, JsonObject reqObject, AsyncCallback asyncCallback) {
      this.url = url;
      this.reqObject = reqObject;
      this.asyncCallback = asyncCallback;
    }

    @Override
    void execute() {
      final XMLHttpRequest xhr = Browser.getWindow().newXMLHttpRequest();
      xhr.setOnerror(new EventListener() {
        @Override
        public void handleEvent(Event evt) {
          onError("evt " + xhr.getStatus());
        }
      });
      xhr.setOnload(new EventListener() {
        @Override
        public void handleEvent(Event evt) {
          onLoad(xhr, evt);
        }
      });
      statusObserver.onTaskStarted("loading...");
      xhr.open("POST", url);
      xhr.setRequestHeader("Content-Type", "application/json;charset=utf-8");
      if (reqObject != null) {
//        Window.alert("start req " + JsonUtil.stringify(reqObject));
//        Window.alert("reqObject.toJson() = " + reqObject.toJson());
        xhr.send(new JSONObject((JavaScriptObject) reqObject.toNative()).toString());
      } else {
        xhr.send();
      }
    }

    private void onLoad(XMLHttpRequest xhr, Event evt) {
      statusObserver.onTaskFinished();

      if (xhr.getStatus() == 404) {
        Window.Location.assign("/404/");
        getQueue().taskSucceeded(this);
        return;
      }
      if (xhr.getStatus() == 403) {
        // Window.Location.assign(LoginUtils.createLoginUrl());
        getQueue().taskSucceeded(this);
        return;
      }
      if (xhr.getStatus() != 200) {
        getQueue().taskFailed(this, false);
        return;
      }
      getQueue().taskSucceeded(this);
      // final JSONObject jsonValue = ((JSONObject) JSONParser.parseLenient(xhr.getResponseText()));
      // final JsonObject object = toJsonObject(jsonValue);
      // asyncCallback.onSuccessAll(object);
      asyncCallback.onSuccessAll(Json.parse(xhr.getResponseText()), xhr.getResponseText());
    }

    private void onError(String msg) {
      getQueue().taskFailed(this, false);
      Window.alert("ERROR: " + msg);
    }
  }

  private JsonObject toJsonObject(JSONObject in) {
    final JsonObject object = Json.createObject();
    final Set<String> keys = in.keySet();
    for (String key : keys) {
      final JSONValue inValue = in.get(key);
      final JSONString jsonString = inValue.isString();
      if (jsonString != null) {
        object.put(key, jsonString.stringValue());
        continue;
      }
      final JSONNumber jsonNumber = inValue.isNumber();
      if (jsonNumber != null) {
        object.put(key, jsonNumber.doubleValue());
        continue;
      }
      final JSONObject jsonObject = inValue.isObject();
      if (jsonObject != null) {
        object.put(key, toJsonObject(jsonObject));
        continue;
      }
      final JSONBoolean jsonBoolean = inValue.isBoolean();
      if (jsonBoolean != null) {
        object.put(key, jsonBoolean.booleanValue());
        continue;
      }
      final JSONArray jsonArray = inValue.isArray();
      if (jsonArray != null) {
        object.put(key, convertArray(jsonArray));
      }
    }
    return object;
  }

  private JsonArray convertArray(JSONArray jsonArray) {
    final JsonArray outArray = Json.createArray();
    for (int i = 0; i < jsonArray.size(); i++) {
      final JSONValue inValue = jsonArray.get(i);

      final JSONString jsonString = inValue.isString();
      if (jsonString != null) {
        outArray.set(i, jsonString.stringValue());
        continue;
      }
      final JSONNumber jsonNumber = inValue.isNumber();
      if (jsonNumber != null) {
        outArray.set(i, jsonNumber.doubleValue());
        continue;
      }
      final JSONObject jsonObject = inValue.isObject();
      if (jsonObject != null) {
        outArray.set(i, toJsonObject(jsonObject));
        continue;
      }
      final JSONBoolean jsonBoolean = inValue.isBoolean();
      if (jsonBoolean != null) {
        outArray.set(i, jsonBoolean.booleanValue());
        continue;
      }
    }
    return outArray;
  }

  /**
   * A task queue to manage all writes to the server.
   */
  private final TaskQueue taskQueue = new TaskQueue();

  /**
   * Indicates whether the RPC end point is currently responding.
   */
  private boolean offline;

  /**
   * The observer that is receiving status events.
   */
  private final StatusObserver statusObserver;

  public ContentRpcServiceImpl(StatusObserver statusObserver) {
    this.statusObserver = statusObserver;
  }

  @Override
  public void request(String url, JsonObject reqObject, AsyncCallback asyncCallback) {
        // если сервер в оффлайне не добавляем запросы в очередь
    if (!offline) {
      taskQueue.post(new RequestTask(url, reqObject, asyncCallback));
    }
  }

  /**
   * Invoked by tasks and loaders when RPC invocations begin to fail.
   */
  void onServerFailed(boolean fatal) {
    if (fatal) {
      forceApplicationReload();
      return;
    }

    if (!offline) {
      statusObserver.onServerWentAway();
      offline = true;
    }
  }

  /**
   * Invoked by tasks and loaders when RPC invocations succeed.
   */
  void onServerSucceeded() {
    if (offline) {
      statusObserver.onServerCameBack();
      offline = false;
    }
  }

  static native void forceApplicationReload() /*-{
    $wnd.location.reload();
  }-*/;
}
