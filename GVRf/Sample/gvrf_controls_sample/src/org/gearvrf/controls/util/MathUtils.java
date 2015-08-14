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

package org.gearvrf.controls.util;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;


public class MathUtils {

    public static float distance(GVRSceneObject obj1, GVRSceneObject obj2) {

        Vector3D v1 = new Vector3D(obj1.getTransform().getPositionX(),
                obj1.getTransform().getPositionY(),
                obj1.getTransform().getPositionZ());

        Vector3D v2 = new Vector3D(obj2.getTransform().getPositionX(),
                obj2.getTransform().getPositionY(),
                obj2.getTransform().getPositionZ());

        return (float) Vector3D.distance(v1, v2);
    }

    public static float distance(GVRTransform obj1, GVRTransform obj2) {

        Vector3D v1 = new Vector3D(obj1.getPositionX(),
                obj1.getPositionY(),
                obj1.getPositionZ());

        Vector3D v2 = new Vector3D(obj2.getPositionX(),
                obj2.getPositionY(),
                obj2.getPositionZ());

        return (float) Vector3D.distance(v1, v2);
    }

    public static float distance(GVRTransform obj1, float[] obj2) {

        Vector3D v1 = new Vector3D(obj1.getPositionX(),
                obj1.getPositionY(),
                obj1.getPositionZ());

        Vector3D v2 = new Vector3D(obj2[0],
                obj2[1],
                obj2[2]);

        return (float) Vector3D.distance(v1, v2);
    }

    public static float getYRotationAngle(GVRSceneObject rotatingObject, GVRSceneObject targetObject) {
        return (float) Math.toDegrees(Math.atan2(targetObject.getTransform().getPositionX()
                - rotatingObject.getTransform().getPositionX(),
                targetObject.getTransform().getPositionZ()
                        - rotatingObject.getTransform().getPositionZ()));
    }
}
