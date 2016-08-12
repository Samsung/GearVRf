
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


import org.gearvrf.utility.Log;

import java.util.Vector;

/**
 * 
 * @author m1.williams
 * InteractiveObject has links to all the relevant objects (Sensors - TouchSensor, Anchor, ProximitySensor -
 * TimeSensor, Interpolators, Scripting [in the future] and the DEFined Item) so one autonimous interactive
 * object or animation will be in a single InteractiveObject.
 * If sensor is null, then this likely an animation only
 */

public class InteractiveObject {


  private Sensor sensor = null;
  private TimeSensor timeSensor = null;
  private Interpolator interpolator = null;
  private DefinedItem definedItem = null;
  // Scripting functionality will eventually be added
  // ScriptingObject scriptingObject = null;

  public InteractiveObject() {
  }

  public void setSensor(Sensor sensor) {
    this.sensor = sensor;
  }
  public Sensor getSensor() {
    return this.sensor;
  }

  public void setTimeSensor(TimeSensor timeSensor) {
    this.timeSensor = timeSensor;
  }
  public TimeSensor getTimeSensor() {
    return this.timeSensor;
  }

  public void setInterpolator(Interpolator interpolator) {
    this.interpolator = interpolator;
  }
  public Interpolator getInterpolator() {
    return this.interpolator;
  }

  public void setDefinedItem(DefinedItem definedItem) {
    this.definedItem = definedItem;
  }
  public DefinedItem getDefinedItem() {
    return this.definedItem;
  }

  public void printInteractiveObject() {
    Log.e("RouteB IO", " ");
    if (this.getSensor() != null)Log.e("RouteB IO", this.getSensor().name);
    if (this.getTimeSensor() != null) Log.e("RouteB IO", this.getTimeSensor().name);
    if (this.getInterpolator() != null) Log.e("RouteB IO", this.getInterpolator().name);
    if (this.getDefinedItem() != null) Log.e("RouteB IO", this.getDefinedItem().getName());
  }

}



