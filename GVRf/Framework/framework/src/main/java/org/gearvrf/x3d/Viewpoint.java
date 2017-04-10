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


/**
 * 
 * @author m1.williams 
 * Class part of a linked list of <Viewpoint> nodes X3D can
 *         have multiple viewpoints and switch the active viewpoint.
 * 
 */

public class Viewpoint
{
  private float[] centerOfRotation =
  {
      0, 0, 0
  };
  private String description = "";
  private float fieldOfView = (float) Math.PI / 4;
  private boolean jump = true;
  private String name = "";
  private float[] orientation =
  {
      0, 0, 1, 0
  };
  private float[] position =
  {
      0, 0, 10
  };
  private boolean retainUserOffsets = false;
  private boolean isBound = false;
  GVRSceneObject parent = null;

  public Viewpoint()
  {
  }

  public Viewpoint(float[] centerOfRotation, String description,
      float fieldOfView, boolean jump, String name, float[] orientation,
      float[] position, boolean retainUserOffsets, GVRSceneObject parent)
  {
    this.description = description;
    this.fieldOfView = fieldOfView;
    this.jump = jump;
    this.name = name;
    this.retainUserOffsets = retainUserOffsets;
    for (int i = 0; i < 3; i++)
    {
      this.centerOfRotation[i] = centerOfRotation[i];
      this.orientation[i] = orientation[i];
      this.position[i] = position[i];
    }
    this.orientation[3] = orientation[3];
    this.parent = parent;
  }

  public float[] getCenterOfRotation()
  {
    return this.centerOfRotation;
  }

  public String getDescription()
  {
    return this.description;
  }

  public float getFieldOfView()
  {
    return this.fieldOfView;
  }

  public boolean getJump()
  {
    return this.jump;
  }

  public String getName()
  {
    return this.name;
  }

  public float[] getOrientation()
  {
    return this.orientation;
  }

  public float[] getPosition()
  {
    return this.position;
  }

  public boolean getRetainUserOffsets()
  {
    return this.retainUserOffsets;
  }

  public GVRSceneObject getParent()
  {
    return this.parent;
  }

  public boolean getIsBound()
  {
    return this.isBound;
  }

  public void setIsBound(boolean isBound)
  {
    this.isBound = isBound;
  }
}
