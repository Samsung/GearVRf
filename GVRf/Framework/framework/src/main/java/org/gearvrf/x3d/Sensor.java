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

import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;


/**
 * 
 * @author m1.williams Class of information for various sensors and what they
 *         control Sensors can send us to new viewpoints, web pages, and invoke
 *         animations based on if an object is touched, visible, within
 *         proximity of the camera.
 */
public class Sensor
{

  public enum Type
  {
    ANCHOR, PROXIMITY, TOUCH, VISIBILITY
  };


  String name = null;
  Type sensorType;
  public GVRSceneObject sensorSceneObject = null;
  private GVRKeyFrameAnimation gvrKeyFrameAnimation = null;
  private String anchorURL = null;


  public Sensor(String name, Type sensorType, GVRSceneObject sensorSceneObject)
  {

    this.name = name;
    this.sensorType = sensorType;
    this.sensorSceneObject = sensorSceneObject;
  }


  public void setGVRKeyFrameAnimation(GVRKeyFrameAnimation gvrKeyFrameAnimation)
  {
    this.gvrKeyFrameAnimation = gvrKeyFrameAnimation;
  }

  public GVRKeyFrameAnimation getGVRKeyFrameAnimation()
  {
    return this.gvrKeyFrameAnimation;
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

