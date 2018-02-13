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

public class VertexNormal
{

  private float[] vector =

  {
      0, 0, 1
  };

  public VertexNormal(float x, float y, float z)
  {
    this.vector[0] = x;
    this.vector[1] = y;
    this.vector[2] = z;

  }

  public VertexNormal(float[] vn)
  {
    for (int i = 0; i < 3; i++)
    {
      this.vector[i] = vn[i];
    }
  }

  
  public float getVertexNormalCoord(int i) {
    return vector[i];
  }

}
