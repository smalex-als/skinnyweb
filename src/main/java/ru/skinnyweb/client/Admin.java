package ru.skinnyweb.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.Window;

import elemental.dom.Element;
import elemental.json.JsonObject;
import ru.skinnyweb.client.rpc.ContentRpcService;
import ru.skinnyweb.client.rpc.ContentRpcServiceImpl;
import ru.skinnyweb.client.util.DomUtils;

public class Admin implements EntryPoint {
  private boolean loadingDone;

  public void onModuleLoad() {
  }

  ContentRpcService.StatusObserver observer 
    = new ContentRpcService.StatusObserver() {
      @Override
      public void onServerCameBack() {
      }

      @Override
      public void onServerWentAway() {
      }

      @Override
      public void onTaskFinished() {
      }

      @Override
      public void onTaskStarted(String description) {
      }
    };
}
