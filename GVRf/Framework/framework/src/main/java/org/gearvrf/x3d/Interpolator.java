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
 * Saves interpolator key and keyvalues.
 * Used for most any interpolator: Color, Position, Orientation, Scale
 * Texture animations.
 */

public class Interpolator
{
  String name = null;
  float[] key;
  float[] keyValue;

  public Interpolator()
  {
    this.name = null;
    key = null;
    keyValue = null;
  }

  public Interpolator(String name, float[] key, float[] keyValue)
  {
    this.name = name;
    this.key = key;
    this.keyValue = keyValue;
  }

}
