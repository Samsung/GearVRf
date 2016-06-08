package org.gearvrf.x3d;

public class Interpolator {
  String name = null;
  float[] key;
  float[] keyValue;

  public Interpolator() {
    this.name = null;
    key = null;
    keyValue = null;
  }

  public Interpolator(String name, float[] key, float[] keyValue) {
    this.name = name;
    this.key = key;
    this.keyValue = keyValue;
  }

}



