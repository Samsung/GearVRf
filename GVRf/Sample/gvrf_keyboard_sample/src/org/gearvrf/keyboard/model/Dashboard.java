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

package org.gearvrf.keyboard.model;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.util.SceneObjectNames;

public class Dashboard extends GVRSceneObject {

    public static int currentDashboardHashCode;

    public static final float HEIGHT_SYNC_SPEED_FACTOR = 4.0f / 4;
    private static final float X_POSITION_OFFSET = 0.03f;
    public static final float Y_POSITION_OFFSET = -3.0f;
    private static final float Z_POSITION_OFFSET = 0.02f;
    private static final float Y_ANCHOR_POINT = 0.8f;
    private static final float Y_ANCHOR_POINT_THRESHOLD = .05f;

    private boolean heightSyncLocked = false;

    public static float HIGHT = 10.0f;
    public static float WIDTH = 8.0f;
    public boolean onFocus = false;
    private float deltaY;
    private float lastY;

    private float originalRotationX;
    private float originalRotationY;
    private float originalRotationZ;
    private float originalRotationW;

    float distanceToAnchorPoint;

    public boolean isAboveAnchorPoint() {

        if (getTransform().getPositionY() > Y_ANCHOR_POINT) {

            return true;

        }

        return false;
    }

    public Dashboard(GVRContext gvrContext, int gVRAndroidResourceTexture) {

        super(gvrContext, HIGHT, WIDTH, gvrContext.loadTexture(new GVRAndroidResource(gvrContext,
                gVRAndroidResourceTexture)));
        setName(SceneObjectNames.DASHBOARD);

        Dashboard.currentDashboardHashCode = this.hashCode();

        originalRotationX = getTransform().getRotationX();
        originalRotationY = getTransform().getRotationY();
        originalRotationZ = getTransform().getRotationZ();
        originalRotationW = getTransform().getRotationW();

        attachEyePointeeHolder();
    }

    public void resetRotation() {
        getTransform().setRotation(originalRotationW, originalRotationX, originalRotationY,
                originalRotationZ);
    }

    public void reset() {
        heightSyncLocked = false;
        deltaY = 0;
        resetRotation();
    }

    public void onUpdate() {

        distanceToAnchorPoint = Math.abs(getTransform().getPositionY() - Y_ANCHOR_POINT);
        if (distanceToAnchorPoint <= Y_ANCHOR_POINT_THRESHOLD) {
            this.heightSyncLocked = true;
        }

        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker.pickScene(getGVRContext()
                .getMainScene());

        float[] lookAt = getGVRContext().getMainScene().getMainCameraRig().getLookAt();
        deltaY = lookAt[1] - lastY;
        lastY = lookAt[1];

        onFocus = false;
        for (GVREyePointeeHolder eph : eyePointeeHolders) {
            if (eph.getOwnerObject().hashCode() == hashCode()) {
                onFocus = true;
            }
        }
    }

    public float getDeltaY() {
        return deltaY;
    }

    public boolean isHeightSyncLocked() {
        return heightSyncLocked;
    }

    public void show() {
        getGVRContext().getMainScene().addSceneObject(this);
    }

    public void hide() {
        getGVRContext().getMainScene().removeSceneObject(this);
    }

    public static float getZPositionOffset(float f) {

        if (f > 0) {

            return Z_POSITION_OFFSET * -1;
        }
        return Z_POSITION_OFFSET;
    }

    public static float getXPositionOffset(float f) {

        if (f > 0) {

            return X_POSITION_OFFSET * -1;
        }
        return X_POSITION_OFFSET;
    }

}
