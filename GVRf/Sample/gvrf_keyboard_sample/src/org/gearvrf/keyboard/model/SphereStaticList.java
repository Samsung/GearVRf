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

import android.content.res.Resources;
import android.content.res.TypedArray;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.shader.SphereShader;
import org.gearvrf.keyboard.util.SceneObjectNames;

import java.util.ArrayList;

public class SphereStaticList {

    public ArrayList<GVRSceneObject> listFlag;
    public static int MOVEABLE = 0;
    public static int ANSWERING = 1;
    public static int RESTORING = 2;

    public SphereStaticList(GVRContext gvrContext) {
        getSpheres(gvrContext, R.array.spheres);
    }

    public void changeLockStateAllSpheresEyePointee(final boolean lock) {
        for (GVRSceneObject sphereFlag : listFlag) {
            if (sphereFlag != null && sphereFlag.getParent() != null
                    && sphereFlag.getEyePointeeHolder() != null) {
                sphereFlag.getEyePointeeHolder().setEnable(lock);
            }
        }
    }

    public void updateSpheresMaterial() {

        for (GVRSceneObject sphereFlag : listFlag) {

            float[] mat = sphereFlag.getTransform().getModelMatrix();

            float[] light = new float[4];
            light[0] = 2.0f;
            light[1] = 4.0f;
            light[2] = 10.0f;
            light[3] = 1.0f;

            float lX = mat[0] * light[0] + mat[1] * light[1] + mat[2]
                    * light[2] + mat[3] * light[3];
            float lY = mat[4] * light[0] + mat[5] * light[1] + mat[6]
                    * light[2] + mat[7] * light[3];
            float lZ = mat[8] * light[0] + mat[9] * light[1] + mat[10]
                    * light[2] + mat[11] * light[3];

            float x = 0;// this.getGVRContext().getMainScene().getMainCameraRig().getOwnerObject().getTransform().getPositionX();
            float y = 0;// this.getGVRContext().getMainScene().getMainCameraRig().getOwnerObject().getTransform().getPositionY();
            float z = 0;// this.getGVRContext().getMainScene().getMainCameraRig().getOwnerObject().getTransform().getPositionZ();

            float eX = mat[0] * x + mat[1] * y + mat[2] * z + mat[3] * 1;
            float eY = mat[4] * x + mat[5] * y + mat[6] * z + mat[7] * 1;
            float eZ = mat[8] * x + mat[9] * y + mat[10] * z + mat[11] * 1;
            sphereFlag
                    .getRenderData()
                    .getMaterial()
                    .setVec3(SphereShader.LIGHT_KEY,
                            lX - sphereFlag.getTransform().getPositionX(),
                            lY - sphereFlag.getTransform().getPositionY(),
                            lZ - sphereFlag.getTransform().getPositionZ());
            sphereFlag.getRenderData().getMaterial()
                    .setVec3(SphereShader.EYE_KEY, eX, eY, eZ);

        }

    }

    private void getSpheres(GVRContext gvrContext, int array) {
        listFlag = new ArrayList<GVRSceneObject>();
        Resources res = gvrContext.getContext().getResources();
        TypedArray spheres = res.obtainTypedArray(array);

        for (int i = 0; i < spheres.length(); i++) {
            int type = spheres.getResourceId(i, -1);
            TypedArray sphere = res.obtainTypedArray(type);
            SphereFlag objectSphere = new SphereFlag(gvrContext, sphere);
            Vector3D parentPosition = objectSphere.getInitialPositionVector();

            GVRSceneObject parent = new GVRSceneObject(gvrContext, new GVRAndroidResource(
                    gvrContext, R.drawable.hit_area_half), new GVRAndroidResource(gvrContext,
                    R.raw.empty));
            parent.setName(SceneObjectNames.SPHERE_FLAG_PARENT);
            parent.getTransform().setPosition((float) parentPosition.getX(),
                    (float) parentPosition.getY(), (float) parentPosition.getZ());
            parent.addChildObject(objectSphere);
            listFlag.add(parent);
        }
    }

}
