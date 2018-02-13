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
 * @author m1.williams Class contains the Event Utilities, Section 30
 * of the X3D specification.  These include the BooleanToggle and other
 * Boolean and Integer Triggers, Filters and Toggles
 */
public class EventUtility
{

  public enum DataType
  {
    BOOLEAN, INTEGER
  };

  public enum Type
  {
    FILTER, SEQUENCER, TOGGLE, TRIGGER
  };

  private static final String TAG = Sensor.class.getSimpleName();

  private String name = null;
  private DataType dataType;
  private Type type;
  private boolean toggle;


  public EventUtility(String name, DataType dataType, Type type, boolean toggle)
  {
    this.name = name;
    this.dataType = dataType;
    this.type = type;
    this.toggle = toggle;
  }

  public String getName()
  {
    return this.name;
  }

  public DataType getDataType()
  {
    return this.dataType;
  }

  public Type getType()
  {
    return this.type;
  }

  public boolean getToggle()
  {
    return this.toggle;
  }

  public void setToggle(boolean toggle)
  {
    this.toggle = toggle;
  }


}

