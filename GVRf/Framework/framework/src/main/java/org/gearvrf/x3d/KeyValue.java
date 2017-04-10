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

public class KeyValue
{
  public float[] keyValues = null;
  //private float[] keyValues = null;

  public KeyValue(float x, float y, float z)
  {
    this.keyValues = new float[3];
    this.keyValues[0] = x;
    this.keyValues[1] = y;
    this.keyValues[2] = z;
  }

  public KeyValue(float w, float x, float y, float z)
  {
    this.keyValues = new float[4];
    this.keyValues[0] = w;
    this.keyValues[1] = x;
    this.keyValues[2] = y;
    this.keyValues[3] = z;
  }

  public KeyValue(float[] values)
  {
    this.keyValues = new float[values.length];
    for (int i = 0; i < values.length; i++)
    {
      this.keyValues[i] = values[i];
    }
  }
}
