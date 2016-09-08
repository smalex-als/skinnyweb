package ru.skinnyweb.server.feeds;

import com.google.appengine.api.datastore.Key;

/**
 * Created by smalex on 10/08/15.
 */
public interface HasIdentify {
  Key getKey();
}
