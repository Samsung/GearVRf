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

import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ISensorEvents;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;
import org.joml.Vector3f;


/**
 * 
 * @author m1.williams Class of information for various sensors and what they
 *         control Sensors can send us to new viewpoints, web pages, and invoke
 *         animations based on if an object is touched, visible, within
 *         proximity of the camera.
 */
public class Sensor extends GVRBaseSensor
{

  public enum Type
  {
    ANCHOR, PROXIMITY, TOUCH, VISIBILITY
  };

  private static final String TAG = Sensor.class.getSimpleName();
  public static final String IS_OVER = "isOver";
  public static final String IS_ACTIVE = "isActive";

  private String name = null;
  private Type sensorType;
  private GVRKeyFrameAnimation gvrKeyFrameAnimation = null;
  private String anchorURL = null;
  private Vector3f hitPoint = new Vector3f();


  public Sensor(String name, Type sensorType, GVRSceneObject sensorSceneObject)
  {
    super(sensorSceneObject.getGVRContext());
    this.name = name;
    this.sensorType = sensorType;
    sensorSceneObject.attachComponent(this);
  }

  void addISensorEvents(ISensorEvents sensorEvents){
    getOwnerObject().getEventReceiver().addListener(sensorEvents);
  }

  public void setGVRKeyFrameAnimation(GVRKeyFrameAnimation gvrKeyFrameAnimation)
  {
    this.gvrKeyFrameAnimation = gvrKeyFrameAnimation;
  }
  public GVRKeyFrameAnimation getGVRKeyFrameAnimation()
  {
    return this.gvrKeyFrameAnimation;
  }

  public String getName()
  {
    return this.name;
  }

  public Type getSensorType()
  {
    return this.sensorType;
  }

  public void setHitPoint(float[] hp)
  {
    this.hitPoint.set(hp[0], hp[1], hp[2]);
  }

  public Vector3f getHitPoint()
  {
    return this.hitPoint;
  }

  public void setAnchorURL(String anchorURL)
  {
    this.anchorURL = anchorURL;
  }

  public String getAnchorURL()
  {
    return this.anchorURL;
  }

}

