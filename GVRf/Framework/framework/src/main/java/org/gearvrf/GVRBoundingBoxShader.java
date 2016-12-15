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
 * Renders a bounding box for occlusion query.
 * This shader ignores light sources.
 * @<code>
 *     u_mvp        model/view/projection matrix
 *     a_position   position vertex attribute
 * </code>
 */
public class GVRBoundingBoxShader extends GVRShader
{
    private String vertexShader =
        "precision highp  float;\n" +
        "attribute vec3 a_position;\n" +
        "uniform mat4 u_mvp;\n" +
        "void main() { gl_Position = u_mvp * vec4(a_position, 1.0); }\n";

    private String fragmentShader =
        "precision mediump  float;\n" +
        "void main() { gl_FragColor = vec4(0.0, 1.0, 0.0, 0.0); }\n";

    public GVRBoundingBoxShader()
    {
        super("", "", "float3 a_position");
        setSegment("FragmentTemplate", fragmentShader);
        setSegment("VertexTemplate", vertexShader);
    }
}
