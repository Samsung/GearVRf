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

package org.gearvrf.widgetplugin;

/**
 * GVRWidgetSceneObjectMeshInfo provides way to create GVRWidgetSceneObject
 * meshes quickly.
 */
public class GVRWidgetSceneObjectMeshInfo {

    /**
     * Following four variables correspond to top left and bottom right X and Y
     * world coordinates of GVRWidgetSceneObject, creating a rectangle
     */
    public float mTopLeftX;
    public float mTopLeftY;
    public float mBottomRightX;
    public float mBottomRightY;

    /**
     * Following two variables correspond to top left and bottom right X and Y
     * screen coordinates of parent libGDX GLSurfaceView which the app created
     * from which our GVRWidgetSceneObjects are created, kindly refer @GVRWidgetSceneObject
     * for more info on how these scene objects are created from the view
     */

    public int[] mTopLeftViewCoords;

    public int[] mBottomRightViewCoords;

    /**
     * Z is constant for easy mesh creation, app can transform widget after
     * creation to play with it.
     */

    public float mZ = 0.0f;

    public GVRWidgetSceneObjectMeshInfo(float topLeftX, float topLeftY,
            float bottomRightX, float bottomRightY, int[] topLeftViewCoords,
            int[] bottomRightViewCoords) {
        mTopLeftX = topLeftX;
        mTopLeftY = topLeftY;
        mBottomRightX = bottomRightX;
        mBottomRightY = bottomRightY;
        mTopLeftViewCoords = topLeftViewCoords;
        mBottomRightViewCoords = bottomRightViewCoords;

    }
}