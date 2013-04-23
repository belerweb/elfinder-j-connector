package com.belerweb.elfinder.bean;

import java.io.File;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.DigestUtils;

public class Directory extends JSONObject {

  public Directory(File dir) throws JSONException {
    this(dir, false);
  }

  public Directory(File dir, boolean root) throws JSONException {
    if (root) {
      put("volumeid", "");
      put("phash", "");
    }
    put("dirs", true);
    put("name", dir.getName());
    put("hash", DigestUtils.md5DigestAsHex(dir.getAbsolutePath().getBytes()));
    put("mime", "directory");
    put("ts", dir.lastModified());
    put("date", new Date(dir.lastModified()));
    put("size", dir.length());
    put("read", true);
    put("write", true);
    put("locked", 1);
  }
}
