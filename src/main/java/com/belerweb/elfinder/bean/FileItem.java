package com.belerweb.elfinder.bean;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

import eu.medsea.util.MimeUtil;

public class FileItem extends HashMap<String, Object> {

  private static final long serialVersionUID = -3963489649975627077L;

  private File file;

  public void setFile(Volume volume, File file) {
    this.file = file;
    this.put("mime", MimeUtil.getMimeType(file));
    this.put("name", file.getName());
    this.put("ts", file.lastModified());
    this.put("date", new Date(file.lastModified()));
    this.put("size", file.length());
    this.put("read", file.canRead());
    this.put("write", file.canWrite());
    this.put("locked", false);
    String rootPath = volume.getFile().getAbsolutePath();
    String path = file.getAbsolutePath();
    path = path.substring(rootPath.length()).replaceFirst("^[/\\\\]+", "");
    path = path.replaceAll("\\\\", "/");
    this.put("hash", volume.getVolumeid() + hash(path));
    String pPath = "/";
    if (path.contains("/")) {
      pPath = path.substring(0, path.lastIndexOf("/"));
    }
    this.put("phash", volume.getVolumeid() + hash(pPath));
  }

  public File getFile() {
    return file;
  }

  protected String hash(String name) {
    return Base64.encodeBase64String(name.getBytes()).replaceAll("=+$", "");
  }
}
