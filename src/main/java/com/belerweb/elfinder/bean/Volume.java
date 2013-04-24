package com.belerweb.elfinder.bean;

import java.io.File;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.springframework.util.StringUtils;

public class Volume extends Directory {

  private String volumeid;

  public Volume(String id, String name, File file) {
    super();
    try {
      this.volumeid = id;
      this.put("volumeid", id);
      super.setFile(this, file);
      this.put("name", StringUtils.isEmpty(name) ? "/" : name);
      this.put("hash", id + Base64.encodeBase64String(("/").getBytes()));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public String getVolumeid() {
    return volumeid;
  }

}
