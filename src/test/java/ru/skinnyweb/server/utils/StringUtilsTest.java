package ru.skinnyweb.server.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ru.skinnyweb.server.utils.StringUtils;

public class StringUtilsTest {
  @Test
  public void testParse() {
    String str = "width:10%, name:total, title:Total";
    Map<String, String> res = StringUtils.stringToMap(str);
    Assert.assertTrue(res.containsKey("width"));
    Assert.assertTrue(res.containsKey("name"));
    Assert.assertTrue(res.containsKey("title"));
  }

}
