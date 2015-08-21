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

package org.gearvrf.controls;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.controls.util.RenderingOrder;

public class Clouds extends GVRSceneObject {

    public GVRSceneObject[] clouds;
    private final int NUMBER_OF_CLOUDS = 4;
    private final int FULL_ROTATION = 360;
    private final int CLOUD_ANGLE = 30;
    private final float CLOUD_OFFSET = 0.5f;
    private final int CLOUD_ROTATION_DURATION = 1800;

    public Clouds(GVRContext gvrContext, float cloudDistance, int numberOfClouds) {
        super(gvrContext);

        GVRMesh[] mesh = new GVRMesh[NUMBER_OF_CLOUDS];
        mesh[0] = gvrContext.createQuad(2.1f, 1.6f);
        mesh[1] = gvrContext.createQuad(4.2f, 2.1f);
        mesh[2] = gvrContext.createQuad(6.7f, 2.4f);
        mesh[3] = gvrContext.createQuad(6.2f, 2.4f);

        GVRTexture[] texture = new GVRTexture[NUMBER_OF_CLOUDS];
        texture[0] = gvrContext.loadTexture(
                new GVRAndroidResource(gvrContext, R.drawable.cloud_01));
        texture[1] = gvrContext.loadTexture(
                new GVRAndroidResource(gvrContext, R.drawable.cloud_02));
        texture[2] = gvrContext.loadTexture(
                new GVRAndroidResource(gvrContext, R.drawable.cloud_03));
        texture[3] = gvrContext.loadTexture(
                new GVRAndroidResource(gvrContext, R.drawable.cloud_04));
        clouds = new GVRSceneObject[numberOfClouds];

        for (int i = 0; i < numberOfClouds; i++) {
            float angle = FULL_ROTATION / numberOfClouds;
            int random = i % NUMBER_OF_CLOUDS;
            // int random = (int) (Math.random() * 3);
            clouds[i] = new GVRSceneObject(gvrContext, mesh[random], texture[random]);
            clouds[i].getTransform().setPositionZ(-cloudDistance);
            gvrContext.getMainScene().addSceneObject(clouds[i]);
            clouds[i].getTransform().rotateByAxisWithPivot((float)
                    (Math.random() + CLOUD_OFFSET) * CLOUD_ANGLE, 1, 0, 0, 0, 0, 0);
            clouds[i].getTransform().rotateByAxisWithPivot(angle * i, 0, 1, 0, 0, 0, 0);
            clouds[i].getRenderData().setRenderingOrder(RenderingOrder.CLOUDS);
            GVRAnimation anim = new GVRRotationByAxisWithPivotAnimation(
                    clouds[i], CLOUD_ROTATION_DURATION, FULL_ROTATION, 0, 1, 0, 0, 0, 0);
            anim.start(gvrContext.getAnimationEngine());

        }

    }
}
