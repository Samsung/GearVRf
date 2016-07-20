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
 *  Route contains the fields used in <Route> for animation and
 *  interactivity.  This is the base class for an array list of
 *  <ROUTE>s.  
 */
public abstract class Route
{

  private String fromNode = null;
  private String fromField = null;
  private String toNode = null;
  private String toField = null;


  /**
   * Constructor, every ROUTE has the 4 fields below
   * @param fromNode
   * @param fromField
   * @param toNode
   * @param toField
   */
  public Route(String fromNode, String fromField, String toNode, String toField)
  {

    this.fromNode = fromNode;
    this.fromField = fromField;
    this.toNode = toNode;
    this.toField = toField;
  }


  public String getRouteFromNode()
  {
    return this.fromNode;
  }

  public String getRouteFromField()
  {
    return this.fromField;
  }

  public String getRouteToNode()
  {
    return this.toNode;
  }

  public String getRouteToField()
  {
    return this.toField;
  }
}

