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

// OpenGL Cube map texture uses coordinate system different to other OpenGL functions:
// Positive x pointing right, positive y pointing up, positive z pointing inward.
// It is a left-handed system, while other OpenGL functions use right-handed system.
// The side faces are also oriented up-side down as illustrated below.
//
// Since the origin of Android bitmap is at top left, and the origin of OpenGL texture
// is at bottom left, when we use Android bitmap to create OpenGL texture, it is already
// up-side down. So we do not need to flip them again.
//
// We do need to flip the z-coordinate to be consistent with the left-handed system.
//    _________
//   /        /|
//  /________/ |
//  |        | |    +y
//  |        | |    |  +z
//  |        | /    | /
//  |________|/     |/___ +x
//
//  Positive x    Positive y    Positive z
//      ______        ______        ______
//     |      |      |      |      |      |
//  -y |      |   +z |      |   -y |      |
//  |  |______|   |  |______|   |  |______|
//  |___ -z       |___ +x       |___ +x
//
//  Negative x    Negative y    Negative z
//      ______        ______        ______
//     |      |      |      |      |      |
//  -y |      |   -z |      |   -y |      |
//  |  |______|   |  |______|   |  |______|
//  |___ +z       |___ +x       |___ -x
//
// (http://www.nvidia.com/object/cube_map_ogl_tutorial.html)
// (http://stackoverflow.com/questions/11685608/convention-of-faces-in-opengl-cubemapping)

import android.content.Context;

import org.gearvrf.utility.TextFile;

/**
 * Shader which renders a cubemap texture modulated by a color.
 * This shader ignores light sources.
 * @<code>
 *     a_position   position vertex attribute
 *     a_normal     normal vertex attribute
 *     u_mvp        model/view/projection matrix
 *     u_color      color to modulate texture map
 *     u_opacity    opacity
 *     u_texture    cubemap texture
 * </code>
 */
public class GVRCubemapShader extends GVRShader
{
    public GVRCubemapShader(GVRContext gvrContext)
    {
        super("float3 u_color float u_opacity", "samplerCube u_texture", "float3 a_position float2 a_texcoord", 300);
        Context context = gvrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.cubemap_frag));
        setSegment("VertexTemplate", TextFile.readTextFile(context, R.raw.cubemap_vert));
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setVec3("u_color", 1.0f, 1.0f, 1.0f);
        material.setFloat("u_opacity", 1.0f);
    }
}

