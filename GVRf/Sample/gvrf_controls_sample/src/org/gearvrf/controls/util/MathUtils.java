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
import org.gearvrf.GVRCameraRig;
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

    public static float[] direction(GVRTransform origin, GVRTransform dest) {

        float[] dir = new float[3];

        dir[0] = (dest.getPositionX() - origin.getPositionX());
        dir[1] = (dest.getPositionY() - origin.getPositionY());
        dir[2] = (dest.getPositionZ() - origin.getPositionZ());
        return dir;

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

    public static float getYRotationAngle(GVRSceneObject rotatingObject,
            GVRCameraRig targetObject) {
        return (float) Math.toDegrees(Math.atan2(targetObject.getTransform().getPositionX()
                - rotatingObject.getTransform().getPositionX(),
                targetObject.getTransform().getPositionZ()
                        - rotatingObject.getTransform().getPositionZ()));
    }

    public static float[] rotateDirection(float[] rotation, float[] point)
    {
        float num = rotation[0] * 2f;
        float num2 = rotation[1] * 2f;
        float num3 = rotation[2] * 2f;
        float num4 = rotation[0] * num;
        float num5 = rotation[1] * num2;
        float num6 = rotation[2] * num3;
        float num7 = rotation[0] * num2;
        float num8 = rotation[0] * num3;
        float num9 = rotation[1] * num3;
        float num10 = rotation[3] * num;
        float num11 = rotation[3] * num2;
        float num12 = rotation[3] * num3;
        float[] result = new float[3];
        result[0] = (1f - (num5 + num6)) * point[0] + (num7 - num12) * point[1] + (num8 + num11)
                * point[2];
        result[1] = (num7 + num12) * point[0] + (1f - (num4 + num6)) * point[1] + (num9 - num10)
                * point[2];
        result[2] = (num8 - num11) * point[0] + (num9 + num10) * point[1] + (1f - (num4 + num5))
                * point[2];
        return result;
    }

    public static float[] rotateDirection(GVRTransform rotationQuat, float[] point)
    {
        float[] rotation = new float[4];
        rotation[0] = rotationQuat.getRotationX();
        rotation[1] = rotationQuat.getRotationY();
        rotation[2] = rotationQuat.getRotationZ();
        rotation[3] = rotationQuat.getRotationW();

        float num = rotation[0] * 2f;
        float num2 = rotation[1] * 2f;
        float num3 = rotation[2] * 2f;
        float num4 = rotation[0] * num;
        float num5 = rotation[1] * num2;
        float num6 = rotation[2] * num3;
        float num7 = rotation[0] * num2;
        float num8 = rotation[0] * num3;
        float num9 = rotation[1] * num3;
        float num10 = rotation[3] * num;
        float num11 = rotation[3] * num2;
        float num12 = rotation[3] * num3;
        float[] result = new float[3];
        result[0] = (1f - (num5 + num6)) * point[0] + (num7 - num12) * point[1] + (num8 + num11)
                * point[2];
        result[1] = (num7 + num12) * point[0] + (1f - (num4 + num6)) * point[1] + (num9 - num10)
                * point[2];
        result[2] = (num8 - num11) * point[0] + (num9 + num10) * point[1] + (1f - (num4 + num5))
                * point[2];
        return result;
    }

    public static float[] quaternionMultiply(float[] lhs, float[] rhs)
    {

        float[] quat = new float[4];
        quat[0] = lhs[3] * rhs[0] + lhs[0] * rhs[3] + lhs[1] * rhs[2] - lhs[2] * rhs[1];
        quat[1] = lhs[3] * rhs[1] + lhs[1] * rhs[3] + lhs[2] * rhs[0] - lhs[0] * rhs[2];
        quat[2] = lhs[3] * rhs[2] + lhs[2] * rhs[3] + lhs[0] * rhs[1] - lhs[1] * rhs[0];
        quat[3] = lhs[3] * rhs[3] - lhs[0] * rhs[0] - lhs[1] * rhs[1] - lhs[2] * rhs[2];
        return quat;
    }

    public static float[] quaternionMultiply(GVRTransform quat1, GVRTransform quat2)
    {

        float[] lhs = new float[4];
        lhs[0] = quat1.getRotationX();
        lhs[1] = quat1.getRotationY();
        lhs[2] = quat1.getRotationZ();
        lhs[3] = quat1.getRotationW();
        float[] rhs = new float[4];
        rhs[0] = quat2.getRotationX();
        rhs[1] = quat2.getRotationY();
        rhs[2] = quat2.getRotationZ();
        rhs[3] = quat2.getRotationW();
        float[] quat = new float[4];
        quat[0] = lhs[3] * rhs[0] + lhs[0] * rhs[3] + lhs[1] * rhs[2] - lhs[2] * rhs[1];
        quat[1] = lhs[3] * rhs[1] + lhs[1] * rhs[3] + lhs[2] * rhs[0] - lhs[0] * rhs[2];
        quat[2] = lhs[3] * rhs[2] + lhs[2] * rhs[3] + lhs[0] * rhs[1] - lhs[1] * rhs[0];
        quat[3] = lhs[3] * rhs[3] - lhs[0] * rhs[0] - lhs[1] * rhs[1] - lhs[2] * rhs[2];
        return quat;
    }
    
    public static float getYAngleEuler(GVRSceneObject object) {

        // xAngle = atan2(2*y*w - 2*x*z, 1 - 2*y*y - 2*z*z) //GVRF y
        // yAngle = atan2(2*x*w - 2*y*z, 1 - 2*x*x - 2*z*z) //GVRF x
        // zAngle = asin(2*x*y + 2*z*w) //GVRF

        // http://www.tinkerforge.com/en/doc/Software/Bricks/IMU_Brick_CSharp.html

        float xq = object.getTransform().getRotationX();
        float yq = object.getTransform().getRotationY();
        float zq = object.getTransform().getRotationZ();
        float wq = object.getTransform().getRotationW();

        double yAngle = Math.atan2(2 * yq * wq - 2 * xq * zq, 1 - 2 * yq * yq - 2 * zq * zq);

        return (float) yAngle;
    }

}
