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

  private static final String CONNECTOR = "/connector.elfinder";

  private static final String CMD_OPEN = "cmd=open";
  private static final String CMD_FILE = "cmd=file";
  private static final String CMD_TREE = "cmd=tree";
  private static final String CMD_PARENTS = "cmd=parents";
  private static final String CMD_LS = "cmd=ls";
  private static final String CMD_TMB = "cmd=tmb";
  private static final String CMD_SIZE = "cmd=size";
  private static final String CMD_DIM = "cmd=dim";
  private static final String CMD_MKDIR = "cmd=mkdir";
  private static final String CMD_MKFILE = "cmd=mkfile";
  private static final String CMD_RM = "cmd=rm";
  private static final String CMD_RENAME = "cmd=rename";
  private static final String CMD_DUPLICATE = "cmd=duplicate";
  private static final String CMD_PASTE = "cmd=paste";
  private static final String CMD_UPLOAD = "cmd=upload";
  private static final String CMD_GET = "cmd=get";
  private static final String CMD_PUT = "cmd=put";
  private static final String CMD_ARCHIVE = "cmd=archive";
  private static final String CMD_EXTRACT = "cmd=extract";
  private static final String CMD_SEARCH = "cmd=search";
  private static final String CMD_INFO = "cmd=info";
  private static final String CMD_RESIZE = "cmd=resize";
  private static final String CMD_NETMOUNT = "cmd=netmount";

  private static final String CONFIG_ROOT = "elfinder.root";

  private static final Integer VERSION = 2;
  private static final Boolean TRUE = new Boolean(true);
  private static File ROOT = null;

  @RequestMapping(value = CONNECTOR, params = CMD_OPEN)
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

  @RequestMapping(value = CONNECTOR, params = CMD_FILE)
  @ResponseBody
  public String file() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_TREE)
  @ResponseBody
  public String tree() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_PARENTS)
  @ResponseBody
  public String parents(@RequestParam String target) throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_LS)
  @ResponseBody
  public String ls() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_TMB)
  @ResponseBody
  public String tmb() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_SIZE)
  @ResponseBody
  public String size() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_DIM)
  @ResponseBody
  public String dim() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_MKDIR)
  @ResponseBody
  public String mkdir() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_MKFILE)
  @ResponseBody
  public String makefile() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_RM)
  @ResponseBody
  public String rm() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_RENAME)
  @ResponseBody
  public String rename() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_DUPLICATE)
  @ResponseBody
  public String duplicate() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_PASTE)
  @ResponseBody
  public String paste() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_UPLOAD)
  @ResponseBody
  public String upload() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_GET)
  @ResponseBody
  public String get() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_PUT)
  @ResponseBody
  public String put() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_ARCHIVE)
  @ResponseBody
  public String archive() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_EXTRACT)
  @ResponseBody
  public String extract() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_SEARCH)
  @ResponseBody
  public String search() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_INFO)
  @ResponseBody
  public String info() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_RESIZE)
  @ResponseBody
  public String resize() throws JSONException {
    JSONObject result = new JSONObject();
    return result.toString();
  }

  @RequestMapping(value = CONNECTOR, params = CMD_NETMOUNT)
  @ResponseBody
  public String netmount() throws JSONException {
    JSONObject result = new JSONObject();
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
