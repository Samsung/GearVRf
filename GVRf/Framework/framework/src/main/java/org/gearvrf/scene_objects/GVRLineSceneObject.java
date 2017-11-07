/* Copyright 2017 Samsung Electronics Co., LTD
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
package org.gearvrf.scene_objects;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;

/**
 * Created by j.reynolds on 7/10/2017.
 */

/***
 * A {@link GVRSceneObject} representing a line or ray
 */
public class GVRLineSceneObject extends GVRSceneObject {

    /**
     * The simple constructor; creates a line of length 1.
     *
     * @param gvrContext current {@link GVRContext}
     */
    public GVRLineSceneObject(GVRContext gvrContext){
        this(gvrContext, 1.0f);
    }

    /**
     * Creates a line based on the passed {@code length} argument
     *
     * @param gvrContext    current {@link GVRContext}
     * @param length        length of the line/ray
     */
    public GVRLineSceneObject(GVRContext gvrContext, float length){
        super(gvrContext, generateLine(gvrContext, length));
        this.getRenderData().setDrawMode(android.opengl.GLES30.GL_LINES);
        this.getRenderData().disableLight();
    }

    private static GVRMesh generateLine(GVRContext gvrContext, float length){
        GVRMesh mesh = new GVRMesh(gvrContext);
        float[] vertices = {
                0,          0,          0,
                0,          0,          -length
        };
        mesh.setVertices(vertices);
        return mesh;
    }
}
