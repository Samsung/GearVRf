
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

import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;


/**
 * 
 * @author m1.williams
 * Individual <ROUTE> that verifies the links 'from' and 'to' object.
 * already added to the scene.
 * Also will have links to and from those objects.
 * After the scene is parsed, an array of RouteObject will be
 * reviwed to set up the Interactive Object.
 */

public class RouteObject extends Route {

   // If a ROUTE is associated with GVRKeyFrameAnimation, then
   // this value will point to it.
   // Assists with Touch Sensors
  /*
   private GVRKeyFrameAnimation gvrKeyFrameAnimation = null;
   */
  Sensor sensorFrom = null;
  TimeSensor timeSensorFrom = null;
  TimeSensor timeSensorTo = null;
  Interpolator interpolatorFrom = null;
  Interpolator interpolatorTo = null;
  DefinedItem definedItemTo = null;
  // Scripting functionality will eventually be added
  // ScriptingObject scriptingObject = null;

  public RouteObject(String fromNode, String fromField, String toNode, String toField) {
    super(fromNode, fromField, toNode, toField);
  }

/*
  public void setGVRKeyFrameAnimation(GVRKeyFrameAnimation gvrKeyFrameAnimation) {
  	this.gvrKeyFrameAnimation = gvrKeyFrameAnimation;
  }

  public GVRKeyFrameAnimation getGVRKeyFrameAnimation() {
  	return this.gvrKeyFrameAnimation;
  }
*/
}



