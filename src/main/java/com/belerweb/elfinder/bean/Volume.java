package com.belerweb.elfinder.bean;

import java.io.File;

import org.springframework.util.StringUtils;

public class Volume extends Directory {

  private static final long serialVersionUID = -9221933840802407115L;

  private String volumeid;

  public Volume(String id, String name, File file) {
    super();
    this.volumeid = id;
    this.put("volumeid", id);
    super.setFile(this, file);
    this.put("name", StringUtils.isEmpty(name) ? "/" : name);
    this.put("hash", id + hash("/"));
    this.remove("phash");// volume no need
  }

  public String getVolumeid() {
    return volumeid;
  }

}
