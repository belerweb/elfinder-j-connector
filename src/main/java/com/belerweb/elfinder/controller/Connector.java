package com.belerweb.elfinder.controller;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.belerweb.elfinder.bean.Directory;

@Controller
public class Connector implements InitializingBean {

  private static final String CONFIG_ROOT = "elfinder.root";

  private static final Integer VERSION = 2;
  private static final Boolean TRUE = new Boolean(true);
  private static File ROOT = null;

  @RequestMapping(value = "/connector.elfinder", params = "cmd=open")
  @ResponseBody
  public String open(@RequestParam(required = false) Boolean init,
      @RequestParam(required = false) String target, @RequestParam(required = false) Boolean tree)
      throws JSONException {
    JSONObject result = new JSONObject();
    if (init == null || !init) {
      if (target == null) {
        result.put("error", "target is required");
        return result.toString();
      }
    } else {
      if (StringUtils.isEmpty(target)) {
        result.put("cwd", new Directory(ROOT, true));
      } else {
        result.put("error", "File not found");
        return result.toString();
      }
      result.put("api", VERSION);
    }

    result.put("files", new JSONArray());

    if (TRUE.equals(tree)) {}

    result.put("uplMaxSize", "32M");
    result.put("options", new JSONObject());
    result.put("netDrivers", new JSONArray());

    return result.toString();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    String root = System.getProperty(CONFIG_ROOT, System.getenv(CONFIG_ROOT));
    if (root != null) {
      ROOT = new File(root);
      if (ROOT.exists()) {
        if (!ROOT.isDirectory()) {
          // TODO
        }
      } else {
        if (!ROOT.mkdirs()) {
          // TODO
        }
      }
    }
  }
}
