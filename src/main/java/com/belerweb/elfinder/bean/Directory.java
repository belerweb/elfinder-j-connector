package com.belerweb.elfinder.bean;

import java.io.File;

import org.json.JSONException;

public class Directory extends FileItem {

  public void setFile(Volume volume, File file) {
    super.setFile(volume, file);
    try {
      this.put("dirs", true);
      this.put("mime", "directory");
      this.put("phash", volume.get("hash"));
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

}
