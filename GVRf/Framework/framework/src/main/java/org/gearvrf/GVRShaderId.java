/* Copyright 2015 Samsung Electronics Co., LTD
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

package org.gearvrf;

/**
 * Opaque type that specifies a shader ID.
 * A shader ID designates a type of shader,
 * either stock or custom. Custom shaders
 * are implemented via a Java class that inherits
 * from @{link GVRShaderTemplate}
 */
public abstract class GVRShaderId
{
    final int ID;

    protected GVRShaderId(int id) {
        ID = id;
    }
}