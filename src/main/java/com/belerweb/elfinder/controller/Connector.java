package com.belerweb.elfinder.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.imgscalr.Scalr;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.belerweb.elfinder.bean.Target;
import com.belerweb.elfinder.service.FileSystemService;

import eu.medsea.util.MimeUtil;

@Controller
public class Connector {

  private static final String CONNECTOR = "/connector.action";

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

  private static final Integer VERSION = 2;
  private static final Boolean TRUE = new Boolean(true);
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd/");

  @Autowired
  private FileSystemService fileSystemService;

  @RequestMapping("/tmb/{target}")
  public String image(@PathVariable String target) {
    return "forward:/image/" + target + "?width=48&height=48";
  }

  @RequestMapping("/image/{target}")
  public ResponseEntity<?> image(@PathVariable String target,
      @RequestParam(required = false) Integer width,
      @RequestParam(required = false) Integer height,
      @RequestHeader(value = "If-Modified-Since", required = false) String ifModifiedSince)
      throws SQLException {
    if (ifModifiedSince != null) {
      return new ResponseEntity<byte[]>(HttpStatus.NOT_MODIFIED);
    }

    Map<String, Object> cwd = fileSystemService.getCwd(new Target(target));
    String path = (String) cwd.get("PATH");
    if (StringUtils.isBlank(path)) {
      return new ResponseEntity<byte[]>(null, null, HttpStatus.NOT_FOUND);
    }

    String mime = (String) cwd.get("MIME");
    HttpHeaders headers = new HttpHeaders();
    headers.setDate(System.currentTimeMillis());
    headers.setLastModified(0);
    headers.setExpires(System.currentTimeMillis() + 31536000000L);
    headers.setCacheControl("max-age=31536000000");
    headers.add("Content-Type", mime);
    try {
      byte[] data = FileUtils.readFileToByteArray(new File(fileSystemService.getRootDir(), path));

      if (width == null || height == null) {
        return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
      }
      BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(data));
      int oWidth = bufferedImage.getWidth();
      int oHeight = bufferedImage.getHeight();
      BufferedImage scaleImage;
      if (oWidth / (double) oHeight > width / (double) height) {
        double rate = oHeight / (double) height;
        scaleImage = Scalr.resize(bufferedImage, (int) (oWidth / rate), height);
        int x = (scaleImage.getWidth() - width) / 2;
        scaleImage = Scalr.crop(scaleImage, x, 0, width, height);
      } else {
        double rate = oWidth / (double) width;
        scaleImage = Scalr.resize(bufferedImage, width, (int) (oHeight / rate));
        int y = (scaleImage.getHeight() - height) / 2;
        scaleImage = Scalr.crop(scaleImage, 0, y, width, height);
      }
      ByteArrayOutputStream scaleOut = new ByteArrayOutputStream();
      ImageIO.write(scaleImage, mime, scaleOut);
      return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @RequestMapping(value = CONNECTOR, method = RequestMethod.OPTIONS)
  public ResponseEntity<String> option() {
    return generateResponse(null);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_OPEN)
  public ResponseEntity<String> open(@RequestParam(required = false) Boolean init,
      @RequestParam(required = false) String target, @RequestParam(required = false) Boolean tree)
      throws SQLException {
    Map<String, Object> result = new HashMap<String, Object>();
    if (TRUE.equals(init)) {// init
      result.put("api", VERSION);
      Map<String, Object> options = new HashMap<String, Object>();
      options.put("tmbUrl", "tmb/");
      result.put("options", options);
    }

    Target _target = new Target(target);
    Map<String, Object> cwd = fileSystemService.getCwd(_target);
    List<Map<String, Object>> files = fileSystemService.getFiles(_target, cwd, tree);

    result.put("cwd", dataConvert(cwd));
    result.put("files", dataConvert(files));
    result.put("uplMaxSize", "32M");
    result.put("netDrivers", new JSONArray());
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_FILE)
  public ResponseEntity<?> file(@RequestParam String target,
      @RequestParam(required = false) Boolean download) throws SQLException {
    Map<String, Object> cwd = fileSystemService.getCwd(new Target(target));
    String path = (String) cwd.get("PATH");
    if (StringUtils.isBlank(path)) {
      Map<String, Object> result = new HashMap<String, Object>();
      result.put("error", "Not a file.");
      return generateResponse(result);
    }

    MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add("Content-Type", (String) cwd.get("MIME"));
    try {
      if (TRUE.equals(download)) {
        String name = (String) cwd.get("NAME");
        headers.add("Content-Disposition", "attachment; filename=\""
            + URLEncoder.encode(name, "UTF-8") + "\";");
        headers.add("Content-Location", name);
        headers.add("Content-Transfer-Encoding", "binary");
      }
      byte[] data = FileUtils.readFileToByteArray(new File(fileSystemService.getRootDir(), path));
      return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @RequestMapping(value = CONNECTOR, params = CMD_TREE)
  public ResponseEntity<String> tree(@RequestParam String target) throws SQLException {
    Map<String, Object> result = new HashMap<String, Object>();
    Target _target = new Target(target);
    Map<String, Object> cwd = fileSystemService.getCwd(_target);
    List<Map<String, Object>> files = fileSystemService.getFiles(_target, cwd, false);
    files.add(0, cwd);
    result.put("tree", dataConvert(files));
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_PARENTS)
  public ResponseEntity<String> parents(@RequestParam String target) throws SQLException {
    Map<String, Object> result = new HashMap<String, Object>();
    Target _target = new Target(target);
    Map<String, Object> cwd = fileSystemService.getCwd(_target);
    List<Map<String, Object>> files = fileSystemService.getFiles(_target, cwd, true);
    result.put("tree", dataConvert(files));
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_LS)
  public ResponseEntity<String> ls() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_TMB)
  public ResponseEntity<String> tmb() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_SIZE)
  public ResponseEntity<String> size(@RequestParam("targets[]") String[] targets)
      throws SQLException {
    long size = 0;
    for (String target : targets) {
      Map<String, Object> obj = fileSystemService.getCwd(new Target(target));
      size = size + (Long) obj.get("SIZE");
    }
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("size", size);
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_DIM)
  public ResponseEntity<String> dim() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_MKDIR)
  public ResponseEntity<String> mkdir(@RequestParam String target, @RequestParam String name)
      throws SQLException {
    Target _target = new Target(target);
    Map<String, Object> cwd = fileSystemService.getCwd(_target);
    HashMap<String, Object> obj = new HashMap<String, Object>();
    obj.put("name", name);
    obj.put("mime", "directory");
    obj.put("ts", System.currentTimeMillis());
    obj.put("read", 1);
    obj.put("write", 1);
    obj.put("locked", 0);
    obj.put("size", 0);
    obj.put("hash", _target.getVolume() + "_" + UUID.randomUUID().toString());
    obj.put("phash", cwd.get("HASH"));
    // dir.put("dirs", null);
    fileSystemService.add(_target, cwd, obj);
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("added", new Object[] {obj});
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_MKFILE)
  public ResponseEntity<String> makefile(@RequestParam String target, @RequestParam String name)
      throws SQLException {
    Map<String, Object> result = new HashMap<String, Object>();
    Target _target = new Target(target);
    String hash = _target.getVolume() + "_" + UUID.randomUUID().toString();
    try {
      String path = generateFile(hash);
      FileUtils.touch(new File(fileSystemService.getRootDir(), path));
      Map<String, Object> cwd = fileSystemService.getCwd(_target);
      HashMap<String, Object> obj = new HashMap<String, Object>();
      obj.put("name", name);
      obj.put("mime", MimeUtil.getFirstMimeType(MimeUtil.getExtensionMimeTypes(name)));
      obj.put("ts", System.currentTimeMillis());
      obj.put("read", 1);
      obj.put("write", 1);
      obj.put("locked", 0);
      obj.put("size", 0);
      obj.put("hash", hash);
      obj.put("phash", cwd.get("HASH"));
      obj.put("path", path);
      fileSystemService.add(_target, cwd, obj);
      result.put("added", new Object[] {obj});
    } catch (IOException e) {
      result.put("error", "Can not create file.");
    }
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_RM)
  public ResponseEntity<String> rm(@RequestParam("targets[]") String[] targets) throws SQLException {
    for (String string : targets) {
      Target target = new Target(string);
      fileSystemService.delete(target.getVolume(), fileSystemService.getCwd(target));
    }
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("removed", targets);
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_RENAME)
  public ResponseEntity<String> rename(@RequestParam String target, @RequestParam String name)
      throws SQLException {
    Target _target = new Target(target);
    fileSystemService.rename(_target, name);
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("removed", new String[] {target});
    result.put("added", new Object[] {dataConvert(fileSystemService.getCwd(_target))});
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_DUPLICATE)
  public ResponseEntity<String> duplicate(@RequestParam("targets[]") String[] targets) {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_PASTE)
  public ResponseEntity<String> paste() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_UPLOAD)
  public ResponseEntity<String> upload(@RequestParam String target,
      @RequestParam(value = "upload[]") MultipartFile[] files) throws SQLException {
    Target _target = new Target(target);
    Map<String, Object> result = new HashMap<String, Object>();
    List<Map<String, Object>> added = new ArrayList<Map<String, Object>>();
    for (MultipartFile multipartFile : files) {
      String hash = _target.getVolume() + "_" + UUID.randomUUID().toString();
      String path = generateFile(hash);
      try {
        byte[] data = multipartFile.getBytes();
        FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data), new File(fileSystemService
            .getRootDir(), path));
        Map<String, Object> cwd = fileSystemService.getCwd(_target);
        HashMap<String, Object> obj = new HashMap<String, Object>();
        String name = multipartFile.getOriginalFilename();
        String mime = MimeUtil.getFirstMimeType(MimeUtil.getExtensionMimeTypes(name));
        obj.put("name", name);
        obj.put("mime", mime);
        obj.put("ts", System.currentTimeMillis());
        obj.put("read", 1);
        obj.put("write", 1);
        obj.put("locked", 0);
        obj.put("size", multipartFile.getSize());
        obj.put("hash", hash);
        obj.put("phash", cwd.get("HASH"));
        obj.put("path", path);
        if ("image/jpeg".equals(mime) || "image/png".equals(mime) || "image/gif".equals(mime)) {
          obj.put("tmb", hash);
          BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
          obj.put("dim", image.getWidth() + 'x' + image.getHeight());
        }
        fileSystemService.add(_target, cwd, obj);
        added.add(obj);
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
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_PUT)
  public ResponseEntity<String> put() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_ARCHIVE)
  public ResponseEntity<String> archive() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_EXTRACT)
  public ResponseEntity<String> extract() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_SEARCH)
  public ResponseEntity<String> search() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_INFO)
  public ResponseEntity<String> info() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_RESIZE)
  public ResponseEntity<String> resize() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  @RequestMapping(value = CONNECTOR, params = CMD_NETMOUNT)
  public ResponseEntity<String> netmount() {
    Map<String, Object> result = new HashMap<String, Object>();
    // TODO implements
    return generateResponse(result);
  }

  private ResponseEntity<String> generateResponse(Map<String, Object> result) {
    MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
    headers.add("Content-Type", "application/json;charset=utf-8");
    headers.add("Access-Control-Allow-Origin", "*");
    return new ResponseEntity<String>(new JSONObject(result).toString(), headers, HttpStatus.OK);
  }

  private Map<String, Object> dataConvert(Map<String, Object> obj) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put("name", obj.get("NAME"));
    result.put("mime", obj.get("mime"));
    result.put("ts", obj.get("TS"));
    result.put("size", obj.get("SIZE"));
    result.put("read", obj.get("READ"));
    result.put("write", obj.get("WRITE"));
    result.put("locked", obj.get("LOCKED"));
    result.put("hash", obj.get("HASH"));
    if (obj.get("VOLUMEID") != null) {
      result.put("volumeid", obj.get("VOLUMEID"));// ROOT DIR
    } else {
      result.put("phash", obj.get("PHASH"));
    }
    if (obj.get("DIM") != null) {// image
      result.put("dim", obj.get("DIM"));
      result.put("tmb", obj.get("TMB"));
    }
    if (obj.get("THASH") != null) {// symlinks
      result.put("thash", obj.get("THASH"));
      result.put("alias", obj.get("ALIAS"));
    }
    // if (obj.get("DIRS") != null) {
    // result.put("dirs", obj.get("DIRS"));
    // }
    if ((Integer) obj.get("RGT") - (Integer) obj.get("LFT") > 2) {
      result.put("dirs", 1);
    }

    return result;
  }

  private List<Map<String, Object>> dataConvert(List<Map<String, Object>> objs) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    for (Map<String, Object> map : objs) {
      result.add(dataConvert(map));
    }
    return result;
  }

  private String generateFile(String name) {
    return DATE_FORMAT.format(new Date()) + name;
  }

}
