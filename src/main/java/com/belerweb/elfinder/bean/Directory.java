package com.belerweb.elfinder.bean;

import java.io.File;

public class Directory extends FileItem {

  private static final long serialVersionUID = 5358326410534682839L;

  public void setFile(Volume volume, File file) {
    super.setFile(volume, file);
    this.put("dirs", true);
    this.put("mime", "directory");
    this.put("phash", volume.get("hash"));
  }

}
