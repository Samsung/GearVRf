/* Copyright 2016 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.x3d;


/**
 * 
 * @author m1.williams
 * Contains data used for Level-of-Detail
 * Retains the data for constructing the parameters
 * of the level of detail
 * 
 */

public class LODmanager {
  private int totalRange = -1;
  private float[] range = null;
  private float[] center = new float[3];
  private int currentRange = -1;
  private boolean active = false;


  public LODmanager () {
  }

  /**
   * When parsing an <LOD> node, set the range of values
   * Values must be in sequential order.
   * @param newRange
   * @param newCenter
   */

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



