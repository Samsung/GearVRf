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


import org.gearvrf.GVRLODGroup;
import org.gearvrf.GVRSceneObject;

import static org.gearvrf.x3d.X3Dobject.TRANSFORM_ROTATION_;
import static org.gearvrf.x3d.X3Dobject.TRANSFORM_SCALE_;
import static org.gearvrf.x3d.X3Dobject.TRANSFORM_TRANSLATION_;

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

  private GVRSceneObject root = null;


  // If <Shape> a direct child of LOD, a new GVRSceneObject will be created
  // nd the shape attached to it.  When we get ending </Shape> node
  // we need to return to the parent of this inserted GVRSceneObject
  protected GVRSceneObject shapeLODSceneObject = null;
  // The GVRSceneObject was created to support LOD (level-of-detail) node,
  // and have the LOD component attached to it.
  // When we have the ending </LOD> node, be sure to return to the
  // parent of this inserted GVRSceneObject
  protected GVRSceneObject transformLODSceneObject = null;

  public LODmanager (GVRSceneObject root) {
    this.root = root;
  }

  /**
   * When parsing an <LOD> node, set the range of values
   * Values must be in sequential order.
   * @param newRange
   * @param newCenter
   */

  protected void set(float[] newRange, float[] newCenter) {
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

  private void end() {
    this.range = null;
    this.totalRange = -1;
    this.currentRange = -1;
    this.active = false;
  }

  protected int getCurrentRangeIndex() {
    return this.currentRange;
  }

  protected void increment() {
    this.currentRange++;
    if (this.currentRange >= (this.totalRange -1) ) end();
  }

  protected float getMinRange(){
    return range[currentRange];
  }

  protected float getMaxRange() {
    return range[currentRange+1];
  }

  protected boolean isActive() {
    return this.active;
  }


  // Add the currentSceneObject to an active Level-of-Detail
  protected void AddLODSceneObject(GVRSceneObject currentSceneObject) {
    if (this.transformLODSceneObject != null) {
      GVRSceneObject levelOfDetailSceneObject = null;
      if ( currentSceneObject.getParent() == this.transformLODSceneObject) {
        levelOfDetailSceneObject = currentSceneObject;
      }
      else {
        GVRSceneObject lodSceneObj = root.getSceneObjectByName((currentSceneObject.getName() + TRANSFORM_TRANSLATION_));
        if ( lodSceneObj != null ) {
          if (lodSceneObj.getParent() == this.transformLODSceneObject) {
            levelOfDetailSceneObject = lodSceneObj;
          }
        }
        if (levelOfDetailSceneObject == null) {
          lodSceneObj = root.getSceneObjectByName((currentSceneObject.getName() + TRANSFORM_ROTATION_));
          if ( lodSceneObj != null ) {
            if (lodSceneObj.getParent() == this.transformLODSceneObject) {
              levelOfDetailSceneObject = lodSceneObj;
            }
          }
        }
        if (levelOfDetailSceneObject == null) {
          lodSceneObj = root.getSceneObjectByName((currentSceneObject.getName() + TRANSFORM_SCALE_));
          if ( lodSceneObj != null ) {
            if (lodSceneObj.getParent() == this.transformLODSceneObject) {
              levelOfDetailSceneObject = lodSceneObj;
            }
          }
        }

      }
      if ( levelOfDetailSceneObject != null) {
        final GVRLODGroup lodGroup = (GVRLODGroup) this.transformLODSceneObject.getComponent(GVRLODGroup.getComponentType());
        lodGroup.addRange(this.getMinRange(), levelOfDetailSceneObject);
        this.increment();
      }
    }
  }

}



