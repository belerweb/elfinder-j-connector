package com.belerweb.elfinder.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
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
import com.belerweb.elfinder.bean.FileItem;
import com.belerweb.elfinder.bean.Volume;

@Controller
public class Connector implements InitializingBean {

  private static final Map<String, Volume> VOLUME = new HashMap<String, Volume>();

  private static final String CONNECTOR = "/connector.elfinder";
  private static final String DEFAULT_VOLUME = "V0_";

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

  @RequestMapping(value = CONNECTOR, params = CMD_OPEN)
  @ResponseBody
  public String open(@RequestParam(required = false) Boolean init,
      @RequestParam(required = false) String target, @RequestParam(required = false) Boolean tree)
      throws JSONException {
    JSONObject result = new JSONObject();
    if (TRUE.equals(init)) {
      // 初始化
      result.put("api", VERSION);
    }
    Volume volume = retrieveVolume(target);
    result.put("cwd", volume);
    JSONArray files = new JSONArray();
    files.put(volume);
    for (File file : volume.getFile().listFiles()) {
      FileItem item = null;
      if (file.isDirectory()) {
        item = new Directory();
      }
      if (file.isFile()) {
        item = new FileItem();
      }
      if (item != null) {
        item.setFile(volume, file);
        files.put(item);
      }
    }

    result.put("files", files);

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
    result.put("tree", retrieveVolume(target));
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
  public String mkdir(@RequestParam String target, @RequestParam String name) throws JSONException {
    JSONObject result = new JSONObject();
    Volume volume = retrieveVolume(target);
    File dir = new File(volume.getFile(), retrievePath(target));
    if (!dir.isDirectory()) {
      result.put("error", "File not found");
    } else {
      File newDir = new File(dir, name);
      if (newDir.mkdir()) {
        Directory added = new Directory();
        added.setFile(volume, newDir);
        result.put("added", added);
      } else {
        result.put("error", "Can not mkdir.");
      }
    }
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

  private Volume retrieveVolume(String target) throws JSONException {
    Volume defaultVolume = VOLUME.get(DEFAULT_VOLUME);
    if (StringUtils.isEmpty(target)) {
      return defaultVolume;
    }
    int underline = target.indexOf("_");
    return VOLUME.get(target.substring(0, underline + 1));
  }

  private String retrievePath(String target) throws JSONException {
    return new String(Base64.decodeBase64(target.substring(target.indexOf("_") + 1)));
  }

  @Override
  public void afterPropertiesSet() {
    String root = System.getProperty(CONFIG_ROOT, System.getenv(CONFIG_ROOT));
    if (root != null) {
      File dir = new File(root);
      if (dir.exists()) {
        if (!dir.isDirectory()) {
          // TODO
        }
      } else {
        if (!dir.mkdirs()) {
          // TODO
        }
      }
      VOLUME.put(DEFAULT_VOLUME, new Volume(DEFAULT_VOLUME, null, dir));
    }
  }
}
