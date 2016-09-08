package ru.skinnyweb.client;

import ru.skinnyweb.client.activities.FeedsListActivity;
import ru.skinnyweb.client.activities.StoriesListActivity;
import ru.skinnyweb.client.activities.StoryViewActivity;
import ru.skinnyweb.client.controls.Component;

public class AppActivityMapper {
  private final AppFactory factory;

  /**
   * @param factory
   */
  public AppActivityMapper(AppFactory factory) {
    this.factory = factory;
  }

  public Component getActivity(String id) {
    if ("FeedsListActivity".equals(id)) {
      return new FeedsListActivity(factory);
    } else if ("StoriesListActivity".equals(id)) {
      return new StoriesListActivity(factory);
    } else if ("StoryViewActivity".equals(id)) {
      return new StoryViewActivity(factory);
    }
    return null;
  }
}
