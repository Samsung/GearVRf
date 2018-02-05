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
 * @author m1.williams Used for the texture coordinate values in an array list
 *         Can also construct texture coordinates for X3D via coding.
 */
public class TextureCoordinates
{
  public short[] coords = new short[3];

  public TextureCoordinates()
  {
  }

  public TextureCoordinates(short x, short y, short z)
  {
    this.coords[0] = x;
    this.coords[1] = y;
    this.coords[2] = z;
  }

  public TextureCoordinates(short[] tc)
  {
    for (int i = 0; i < 3; i++)
    {
      this.coords[i] = tc[i];
    }
  }

}
