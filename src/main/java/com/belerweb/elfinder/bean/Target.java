package com.belerweb.elfinder.bean;

import org.springframework.util.StringUtils;

public class Target {

  private String volume;
  private String hash;

  public Target() {
    this.volume = "0";
    this.hash = "0_1";
  }

  public Target(String target) {
    this();
    if (!StringUtils.isEmpty(target) && target.matches("^[0-9a-zA-Z]+_.+")) {
      this.volume = target.substring(0, target.indexOf("_"));
      this.hash = target;
    }
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }


}
