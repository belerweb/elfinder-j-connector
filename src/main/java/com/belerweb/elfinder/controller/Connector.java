package com.belerweb.elfinder.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
  public ResponseEntity<String> open(@RequestParam(required = false) Boolean init,
      @RequestParam(required = false) String target, @RequestParam(required = false) Boolean tree) {
    Map<String, Object> result = new HashMap<String, Object>();
    Volume volume = retrieveVolume(target);
    File dir = retrieveTarget(volume, target);
    if (volume == null || dir == null || !dir.isDirectory()) {
      // TODO file not found
    }

    if (TRUE.equals(init)) {// init
      result.put("api", VERSION);
      // TODO add options
    }

    FileItem cwd = volume;
    if (!dir.equals(volume.getFile())) {
      cwd = new Directory();
      cwd.setFile(volume, dir);
    }
    result.put("cwd", cwd);


    List<FileItem> files = new ArrayList<FileItem>();
    if (TRUE.equals(tree)) {
      if (!dir.equals(volume.getFile())) {
        File parent = dir;
        do {
          parent = parent.getParentFile();
          files.addAll(0, listFileItems(volume, parent));
        } while (!parent.equals(volume.getFile()));
      }
      files.add(0, volume);
    }

    files.add(cwd);
    files.addAll(listFileItems(volume, dir));
    result.put("files", files);


    result.put("uplMaxSize", "32M");
    result.put("options", new JSONObject());
    result.put("netDrivers", new JSONArray());
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_FILE)
  public ResponseEntity<String> file() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_TREE)
  public ResponseEntity<String> tree() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_PARENTS)
  public ResponseEntity<String> parents(@RequestParam String target) {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_LS)
  public ResponseEntity<String> ls() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_TMB)
  public ResponseEntity<String> tmb() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_SIZE)
  public ResponseEntity<String> size() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_DIM)
  public ResponseEntity<String> dim() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_MKDIR)
  public ResponseEntity<String> mkdir(@RequestParam String target, @RequestParam String name) {
    Map<String, Object> result = new HashMap<String, Object>();
    Volume volume = retrieveVolume(target);
    File dir = retrieveTarget(volume, target);
    if (volume == null || dir == null || !dir.isDirectory()) {
      // TODO file not found
    }

    File newDir = new File(dir, name);
    if (newDir.mkdir()) {
      Directory added = new Directory();
      added.setFile(volume, newDir);
      result.put("added", Arrays.asList(new Directory[] {added}));
    } else {
      result.put("error", "Can not mkdir.");
    }

    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_MKFILE)
  public ResponseEntity<String> makefile(@RequestParam String target, @RequestParam String name) {
    Map<String, Object> result = new HashMap<String, Object>();
    Volume volume = retrieveVolume(target);
    File dir = retrieveTarget(volume, target);
    if (volume == null || dir == null || !dir.isDirectory()) {
      // TODO file not found
    }

    File newFile = new File(dir, name);
    try {
      if (newFile.createNewFile()) {
        FileItem added = new FileItem();
        added.setFile(volume, newFile);
        result.put("added", Arrays.asList(new FileItem[] {added}));
      } else {
        result.put("error", "Can make file.");
      }
    } catch (IOException e) {
      e.printStackTrace();
      result.put("error", "Can make file.");
    }

    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_RM)
  public ResponseEntity<String> rm() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_RENAME)
  public ResponseEntity<String> rename() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_DUPLICATE)
  public ResponseEntity<String> duplicate() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_PASTE)
  public ResponseEntity<String> paste() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_UPLOAD)
  public ResponseEntity<String> upload(@RequestParam String target,
      @RequestParam(value = "upload[]") MultipartFile[] files) {
    Map<String, Object> result = new HashMap<String, Object>();
    Volume volume = retrieveVolume(target);
    File dir = retrieveTarget(volume, target);
    if (volume == null || dir == null || !dir.isDirectory()) {
      // TODO file not found
    }
    List<FileItem> added = new ArrayList<FileItem>();
    for (MultipartFile multipartFile : files) {
      try {
        File file = new File(dir, multipartFile.getOriginalFilename());
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        FileItem fileItem = new FileItem();
        fileItem.setFile(volume, file);
        added.add(fileItem);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    result.put("added", added);
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_GET)
  public ResponseEntity<String> get() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_PUT)
  public ResponseEntity<String> put() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_ARCHIVE)
  public ResponseEntity<String> archive() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_EXTRACT)
  public ResponseEntity<String> extract() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_SEARCH)
  public ResponseEntity<String> search() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_INFO)
  public ResponseEntity<String> info() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_RESIZE)
  public ResponseEntity<String> resize() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_NETMOUNT)
  public ResponseEntity<String> netmount() {
    Map<String, Object> result = new HashMap<String, Object>();
    return generateResponse(result);
  }

  private ResponseEntity<String> generateResponse(Map<String, Object> result) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add("Content-Type", "application/json;charset=utf-8");
    return new ResponseEntity<String>(new JSONObject(result).toString(), headers, HttpStatus.OK);
  }

  private List<FileItem> listFileItems(Volume volume, File dir) {
    List<FileItem> items = new ArrayList<FileItem>();
    for (File file : dir.listFiles()) {
      FileItem item = null;
      if (file.isDirectory()) {
        item = new Directory();
      }
      if (file.isFile()) {
        item = new FileItem();
      }
      if (item != null) {
        item.setFile(volume, file);
        items.add(item);
      }
    }
    return items;
  }

  private Volume retrieveVolume(String target) {
    Volume defaultVolume = VOLUME.get(DEFAULT_VOLUME);
    if (StringUtils.isEmpty(target)) {
      return defaultVolume;
    }
    int underline = target.indexOf("_");
    return VOLUME.get(target.substring(0, underline + 1));
  }

  private File retrieveTarget(Volume volume, String target) {
    if (volume == null) {
      return null;
    }
    if (StringUtils.isEmpty(target) || !target.matches("^[0-9a-zA-Z]+_.*")) {
      return volume.getFile();
    }
    return new File(volume.getFile(), retrievePath(target));
  }

  private String retrievePath(String target) {
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
