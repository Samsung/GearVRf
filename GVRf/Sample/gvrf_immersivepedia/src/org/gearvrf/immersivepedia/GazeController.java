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

package org.gearvrf.immersivepedia;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;

public class GazeController {

    private static GVRSceneObject cursor;
    private static GVRSceneObject highlightCursor;
    private static GVRContext gvrContext = null;

    private static float CURSOR_HIGH_OPACITY = 1.0f;
    private static float CURSOR_LOW_OPACITY = 0.0f;

    private static float NORMAL_CURSOR_SIZE = 0.25f;
    private static float HIGHLIGHT_CURSOR_SIZE = 0.25f;
    private static float CURSOR_Z_POSITION = -9.0f;

    private static int CURSOR_RENDER_ORDER = 100000;

    public static void setupGazeCursor(GVRContext gvrContext) {

        GazeController.gvrContext = gvrContext;

        cursor = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(NORMAL_CURSOR_SIZE, NORMAL_CURSOR_SIZE),
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.head_tracker)));
        cursor.getTransform().setPositionZ(CURSOR_Z_POSITION);
        cursor.getRenderData().setRenderingOrder(
                GVRRenderData.GVRRenderingOrder.OVERLAY);
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().setRenderingOrder(CURSOR_RENDER_ORDER);

        highlightCursor = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(HIGHLIGHT_CURSOR_SIZE, HIGHLIGHT_CURSOR_SIZE),
                gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.highlightcursor)));
        highlightCursor.getTransform().setPositionZ(CURSOR_Z_POSITION);
        highlightCursor.getRenderData().setRenderingOrder(
                GVRRenderData.GVRRenderingOrder.OVERLAY);
        highlightCursor.getRenderData().setDepthTest(false);
        highlightCursor.getRenderData().setRenderingOrder(CURSOR_RENDER_ORDER);

        highlightCursor.getRenderData().getMaterial().setOpacity(0f);

    }

    public static void enableInteractiveCursor() {
        highlightCursor.getRenderData().getMaterial().setOpacity(CURSOR_HIGH_OPACITY);
        cursor.getRenderData().getMaterial().setOpacity(CURSOR_LOW_OPACITY);
    }

    public static void disableInteractiveCursor() {
        highlightCursor.getRenderData().getMaterial().setOpacity(CURSOR_LOW_OPACITY);
        cursor.getRenderData().getMaterial().setOpacity(CURSOR_HIGH_OPACITY);
    }

    public static void enableGaze() {
        gvrContext.getMainScene().getMainCameraRig().addChildObject(highlightCursor);
        gvrContext.getMainScene().getMainCameraRig().addChildObject(cursor);
    }

    public static void disableGaze() {
        gvrContext.getMainScene().getMainCameraRig().removeChildObject(highlightCursor);
        gvrContext.getMainScene().getMainCameraRig().removeChildObject(cursor);
    }

}
