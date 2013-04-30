package com.belerweb.elfinder.bean;

import java.io.File;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

public class FileItem extends JSONObject {

  private File file;

  public void setFile(Volume volume, File file) {
    try {
      this.file = file;
      this.put("name", file.getName());
      this.put("ts", file.lastModified());
      this.put("date", new Date(file.lastModified()));
      this.put("size", file.length());
      this.put("read", file.canRead());
      this.put("write", file.canWrite());
      this.put("locked", false);
      String path =
          file.getAbsolutePath().substring(volume.getFile().getAbsolutePath().length())
              .replaceFirst("^[/\\\\]+", "");
      this.put("hash", volume.getVolumeid() + hash(path));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public File getFile() {
    return file;
  }

  protected String hash(String name) {
    return Base64.encodeBase64String(name.getBytes()).replaceAll("=+$", "");
  }
}
