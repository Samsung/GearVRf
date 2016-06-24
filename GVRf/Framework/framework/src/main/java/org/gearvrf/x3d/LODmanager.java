package org.gearvrf.x3d;

public class LODmanager {
  private int totalRange = -1;
  private float[] range = null;
  private float[] center = new float[3];
  private int currentRange = -1;
  private boolean active = false;



  public LODmanager () {
  }

  public void set(float[] newRange, float[] newCenter) {
    this.range = new float[newRange.length];
    for (int i = 0; i < newRange.length; i++) {
      this.range[i] = newRange[i];
    }
    for (int i = 0; i < newCenter.length; i++) {
        this.center[i] = newCenter[i];
      }
    this.totalRange = newRange.length;
    this.currentRange = 0;
    this.active = true;
  }

  public void end() {
    this.range = null;
    this.totalRange = -1;
    this.currentRange = -1;
    this.active = false;
  }

  public int getCurrentRangeIndex() {
    return this.currentRange;
  }

  public void increment() {
    this.currentRange++;
    if (this.currentRange >= (this.totalRange -1) ) end();
  }

  public float getMinRange(){
    return range[currentRange];
  }

  public float getMaxRange() {
    return range[currentRange+1];
  }

  public boolean isActive() {
    return this.active;
  }

}



