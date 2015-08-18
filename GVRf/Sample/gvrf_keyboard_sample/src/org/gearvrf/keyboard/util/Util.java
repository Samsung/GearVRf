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

package org.gearvrf.keyboard.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;

public class Util {

    public static double ratio = 0.5;
    public static boolean isLogActive = false;

    public static float convertPixelToVRFloatValue(float pixel) {

        return pixel * 0.00367f;
    }

    public static Vector3D getVec3(GVRSceneObject object) {

        Vector3D vObject = null;

        if (object != null) {

            vObject = new Vector3D(object
                    .getTransform().getPositionX(), object
                    .getTransform().getPositionY(), object
                    .getTransform().getPositionZ());
        }

        return vObject;
    }

    public static Vector3D getVec3IgnoreY(GVRSceneObject object) {

        Vector3D vObject = null;

        if (object != null) {

            vObject = new Vector3D(object
                    .getTransform().getPositionX(), 0, object
                    .getTransform().getPositionZ());
        }

        return vObject;
    }

    public static float getXRotationAngle(GVRSceneObject rotatingObject, GVRSceneObject targetObject) {
        float angle = (float) Math.toDegrees(Math.atan2(targetObject.getTransform().getPositionY()
                - rotatingObject.getTransform().getPositionY(),
                targetObject.getTransform().getPositionZ()
                        - rotatingObject.getTransform().getPositionZ()));

        if (rotatingObject.getTransform().getPositionZ() < 0) {
            if (rotatingObject.getTransform().getPositionY() > 10) {
                angle = angle + 90;
            } else if (rotatingObject.getTransform().getPositionY() < 0) {
                angle = angle - 90;
            }
        } else {
            angle = angle - 180;
        }

        return angle;
    }

    public static float getYRotationAngle(GVRSceneObject rotatingObject, GVRSceneObject targetObject) {
        return (float) Math.toDegrees(Math.atan2(targetObject.getTransform().getPositionX()
                - rotatingObject.getTransform().getPositionX(),
                targetObject.getTransform().getPositionZ()
                        - rotatingObject.getTransform().getPositionZ()));
    }

    public static float getYRotationAngle(GVRSceneObject rotatingObject, GVRTransform targetObject) {
        return (float) Math.toDegrees(Math.atan2(targetObject.getPositionX()
                - rotatingObject.getTransform().getPositionX(),
                targetObject.getPositionZ()
                        - rotatingObject.getTransform().getPositionZ()));
    }

    public static float getYRotationAngle(Vector3D rotatingVector, GVRSceneObject targetObject) {
        return (float) Math.toDegrees(Math.atan2(targetObject.getTransform().getPositionX()
                - rotatingVector.getX(),
                targetObject.getTransform().getPositionZ()
                        - rotatingVector.getZ()));
    }

    public static float getZRotationAngle(GVRSceneObject rotatingObject, GVRSceneObject targetObject) {
        float angle = (float) Math.toDegrees(Math.atan2(targetObject.getTransform().getPositionY()
                - rotatingObject.getTransform().getPositionY(),
                targetObject.getTransform().getPositionX()
                        - rotatingObject.getTransform().getPositionX()));

        return angle;
    }

    public static float getYRotationAngle(Vector3D rotatingVector, Vector3D targetObject) {
        return (float) Math.toDegrees(Math.atan2(targetObject.getX()
                - rotatingVector.getX(),
                targetObject.getZ()
                        - rotatingVector.getZ()));
    }

    public static float[] calculatePointBetweenTwoObjects(GVRSceneObject object1,
            GVRSceneObject object2, float desiredDistance) {
        float[] point = new float[3];
        float ratio = desiredDistance / (float) distance(object1, object2);
        point[0] = (1 - ratio) * object1.getTransform().getPositionX() + (ratio)
                * object2.getTransform().getPositionX();
        point[1] = (1 - ratio) * object1.getTransform().getPositionY() + (ratio)
                * object2.getTransform().getPositionY();
        point[2] = (1 - ratio) * object1.getTransform().getPositionZ() + (ratio)
                * object2.getTransform().getPositionZ();

        return point;
    }

    public static float[] calculatePointBetweenTwoObjects(GVRTransform object1,
            GVRSceneObject object2, float desiredDistance) {
        float[] point = new float[3];
        float ratio = desiredDistance / (float) distance(object1, object2.getTransform());
        point[0] = (1 - ratio) * object1.getPositionX() + (ratio)
                * object2.getTransform().getPositionX();
        point[1] = (1 - ratio) * object1.getPositionY() + (ratio)
                * object2.getTransform().getPositionY();
        point[2] = (1 - ratio) * object1.getPositionZ() + (ratio)
                * object2.getTransform().getPositionZ();

        return point;
    }

    public static float[] calculatePointBetweenTwoObjects(GVRSceneObject object, Vector3D vector,
            float desiredDistance) {
        float[] point = new float[3];
        float ratio = desiredDistance / (float) distance(vector, object);
        point[0] = (1 - ratio) * object.getTransform().getPositionX() + (ratio)
                * (float) vector.getX();
        point[1] = (1 - ratio) * object.getTransform().getPositionY() + (ratio)
                * (float) vector.getY();
        point[2] = (1 - ratio) * object.getTransform().getPositionZ() + (ratio)
                * (float) vector.getZ();

        return point;
    }

    public static float[] calculatePointBetweenTwoObjects(GVRTransform object, Vector3D vector,
            float desiredDistance) {
        float[] point = new float[3];
        float ratio = desiredDistance / (float) distance(vector, object);
        point[0] = (1 - ratio) * object.getPositionX() + (ratio)
                * (float) vector.getX();
        point[1] = (1 - ratio) * object.getPositionY() + (ratio)
                * (float) vector.getY();
        point[2] = (1 - ratio) * object.getPositionZ() + (ratio)
                * (float) vector.getZ();

        return point;
    }

    public static double distance(GVRSceneObject object1, GVRSceneObject object2) {
        return Math.sqrt(Math.pow(object1.getTransform().getPositionX()
                - object2.getTransform().getPositionX(), 2)
                +
                Math.pow(object1.getTransform().getPositionY()
                        - object2.getTransform().getPositionY(), 2)
                +
                Math.pow(object1.getTransform().getPositionZ()
                        - object2.getTransform().getPositionZ(), 2));

    }

    public static double distance(GVRTransform object1, GVRTransform object2) {
        return Math.sqrt(Math.pow(object1.getPositionX()
                - object2.getPositionX(), 2)
                +
                Math.pow(object1.getPositionY()
                        - object2.getPositionY(), 2)
                +
                Math.pow(object1.getPositionZ()
                        - object2.getPositionZ(), 2));

    }

    public static double distance(GVRSceneObject object1, GVRTransform object2) {
        return Math.sqrt(Math.pow(object1.getTransform().getPositionX()
                - object2.getPositionX(), 2)
                +
                Math.pow(object1.getTransform().getPositionY()
                        - object2.getPositionY(), 2)
                +
                Math.pow(object1.getTransform().getPositionZ()
                        - object2.getPositionZ(), 2));

    }

    public static double distance(float ax, float ay, float az, float bx, float by, float bz) {
        return Math.sqrt(
                Math.pow(ax - bx, 2) +
                        Math.pow(ay - by, 2) + +
                        Math.pow(az - bz, 2));

    }

    public static double distance(Vector3D vector, GVRSceneObject object) {
        return Math.sqrt(Math.pow(vector.getX() - object.getTransform().getPositionX(), 2) +
                Math.pow(vector.getY() - object.getTransform().getPositionY(), 2) +
                Math.pow(vector.getZ() - object.getTransform().getPositionZ(), 2));

    }

    public static double distance(Vector3D vector, GVRTransform object) {
        return Math.sqrt(Math.pow(vector.getX() - object.getPositionX(), 2) +
                Math.pow(vector.getY() - object.getPositionY(), 2) +
                Math.pow(vector.getZ() - object.getPositionZ(), 2));

    }

    public static float getHitAreaScaleFactor(float currentDistance) {
        float clipFrom = Constants.NEAREST_SPHERE * Constants.BASE_FIXER / 100;
        float baseFactor = currentDistance * Constants.BASE_FIXER / 100;
        float addFactor = (baseFactor - clipFrom) * Constants.ADD_FIXER;
        return 1 + baseFactor + addFactor;
    }

    public static void rotateWithOpenGLLookAt(Vector3D cameraVector, Vector3D parentVector,
            GVRSceneObject object) {
        Vector3D globalUpVector = new Vector3D(0, 1, 0);
        Vector3D lookVector = parentVector.normalize();
        Vector3D rightVector = lookVector.crossProduct(globalUpVector);
        Vector3D upVector = rightVector.crossProduct(lookVector);
        Vector3D zAxis = cameraVector.subtract(parentVector).normalize();
        // Vector3D xAxis = upVector.crossProduct(zAxis).normalize();
        Vector3D xAxis = zAxis.crossProduct(upVector).normalize();
        Vector3D yAxis = xAxis.crossProduct(zAxis).normalize();
        // Vector3D yAxis = xAxis.crossProduct(zAxis).normalize();
        zAxis = zAxis.scalarMultiply(-1.f);

        float angle = (float) Vector3D.angle(parentVector, cameraVector);
        angle = (float) Math.toDegrees(angle);

        object.getTransform().rotateByAxis(angle, (float) xAxis.getX(), (float) xAxis.getY(),
                (float) xAxis.getZ());
        object.getTransform().rotateByAxis(angle, (float) yAxis.getX(), (float) yAxis.getY(),
                (float) yAxis.getZ());
        object.getTransform().rotateByAxis(angle, (float) zAxis.getX(), (float) zAxis.getY(),
                (float) zAxis.getZ());
    }

    public static float applyRatioAt(double d) {
        return (float) (d * ratio);
    }

    public static boolean checkcheckEyePointeeHolder(GVRContext gvrContext, Object... varargsObject) {
        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker.pickScene(gvrContext.getMainScene());

        if (eyePointeeHolders.length != 0 && varargsObject.length >= 1) {
            for (GVREyePointeeHolder eyePointeeHolder : eyePointeeHolders) {

                for (int i = 0; i < varargsObject.length; i++) {
                    if (eyePointeeHolder.hashCode() == ((GVRSceneObject) varargsObject[i])
                            .getEyePointeeHolder().hashCode())
                        return false;

                }

            }
        }

        return true;
    }

    public static float convertRotation180to360(float input) {
        float result = input;
        if (input < 0) {
            result = (input * -1) + 180;

        }

        return result;
    }

    public static void logRotation(GVRSceneObject object) {

        Log.d("RotationUtil", " RotationPitch :" + object.getTransform().getRotationPitch());
        Log.d("RotationUtil", " RotationRoll :" + object.getTransform().getRotationRoll());
        Log.d("RotationUtil", " RotationYaw :" + object.getTransform().getRotationYaw());
        Log.d("RotationUtil", " RotationW :" + object.getTransform().getRotationW());
        Log.d("RotationUtil", " RotationX :" + object.getTransform().getRotationX());
        Log.d("RotationUtil", " RotationY :" + object.getTransform().getRotationY());
        Log.d("RotationUtil", " RotationZ :" + object.getTransform().getRotationZ());

    }

    public static float getDistanceDegree(float oldRotation, float actualRotation, boolean clockWise) {
        float distance;

        if (clockWise) {

            distance = clockWise(oldRotation, actualRotation);

        } else {
            distance = antiClockUnWise(oldRotation, actualRotation);
            distance = distance * -1;
        }

        return distance;

    }

    private static float antiClockUnWise(float first, float secund) {
        float result = 0;

        if (first == 0 && secund == 0) {

            result = 0;
            Log("antiClockUnWise", "first == 0 && secund == 0 result : " + result);
            return result;

        }

        if (first >= 0 && secund >= 0) {

            result = first - secund;

            Log("antiClockUnWise", " first > 0 && secund > 0 and result : " + result);

        }

        if (first <= 0 && secund <= 0) {

            result = (secund * -1) - (first * -1);

            Log("antiClockUnWise", "first < 0 && secund < 0 and result : " + result);

        }

        if (first >= 0 && secund <= 0) {

            result = first + (secund * -1);

            Log("antiClockUnWise", "first > 0 && secund < 0 and result : " + result);

        }

        if (first <= 0 && secund >= 0) {

            result = (180 + first) + (180 - secund);

            Log("antiClockUnWise", "first < 0 && secund > 0 and result : " + result);

        }

        return result;
    }

    public static void Log(String tag, String msg) {
        if (isLogActive) {

            Log.d(tag, msg);
        }

    }

    private static float clockWise(float first, float secund) {
        float result = 0;

        if (first == 0 && secund == 0) {

            result = 0;
            Log("clockWise", "first == 0 && secund == 0 result : " + result);
            return result;

        }

        if (first >= 0 && secund >= 0) {

            result = secund - first;

            Log("clockWise", "first > 0 && secund > 0 and result : " + result);

        }

        if (first <= 0 && secund <= 0) {

            result = (first * -1) - (secund * -1);

            Log("clockWise", "first < 0 && secund < 0 and result : " + result);

        }

        if (first <= 0 && secund >= 0) {

            result = secund + (first * -1);

            Log("clockWise", "first < 0 && secund > 0 and result : " + result);

        }

        if (first >= 0 && secund <= 0) {

            result = (180 + secund) + (180 - first);

            Log("clockWise", "first :" + first + " > 0 && secund :" + secund + " < 0 and result : "
                    + result);

        }

        return result;
    }

    public static void loadAudioClipAndPlay(Context context, int idResource) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, idResource);
        mediaPlayer.start();
    }

    public static float getZRotationAngle(GVRSceneObject rotatingObject, GVRTransform targetObject) {
        float angle = (float) Math.toDegrees(Math.atan2(targetObject.getPositionY()
                - rotatingObject.getTransform().getPositionY(),
                targetObject.getPositionX()
                        - rotatingObject.getTransform().getPositionX()));

        return angle;
    }

}
