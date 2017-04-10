/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sample.hand.template;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Utils {
    private static final Vector3f FORWARD_VECTOR = new Vector3f(0f, 0f, -1f);
    private static final Vector3f UP_VECTOR = new Vector3f(0f, 1f, 0f);
    private static Vector3f forward = new Vector3f();
    private static Vector3f up = new Vector3f();
    private static Vector3f direction = new Vector3f();

    /**
     * This method is a clone of {@link Quaternionf#lookRotate(Vector3f, Vector3f)} with the only
     * difference that it works along the negative Z axis.
     *
     * Apply a rotation to this quaternion that maps the given direction to the negative Z axis,
     * and store the result in <code>dest</code>.
     * <p>
     * Because there are multiple possibilities for such a rotation, this method will choose the
     * one that ensures the given up direction to remain
     * parallel to the plane spanned by the <tt>up</tt> and <tt>dir</tt> vectors.
     * <p>
     * If <code>Q</code> is <code>this</code> quaternion and <code>R</code> the quaternion
     * representing the
     * specified rotation, then the new quaternion will be <code>Q * R</code>. So when
     * transforming a
     * vector <code>v</code> with the new quaternion by using <code>Q * R * v</code>, the
     * rotation added by this method will be applied first!
     * <p>
     * Reference:
     * <a href="http://answers.unity3d.com/questions/467614/what-is-the-source-code-of-quaternionlookrotation.html">http://answers.unity3d.com</a>
     *
     * @param dirX the x-coordinate of the direction to look along
     * @param dirY the y-coordinate of the direction to look along
     * @param dirZ the z-coordinate of the direction to look along
     * @param upX  the x-coordinate of the up vector
     * @param upY  the y-coordinate of the up vector
     * @param upZ  the z-coordinate of the up vector
     * @param dest will hold the result
     * @return dest
     */
    public static Quaternionf lookRotate(float dirX, float dirY, float dirZ, float upX, float upY,
                                         float upZ, Quaternionf dest) {
        // Normalize direction
        float invDirLength = (float) (1.0 / Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ));
        float dirnX = dirX * invDirLength;
        float dirnY = dirY * invDirLength;
        float dirnZ = dirZ * invDirLength;
        // left = up x dir
        float leftX, leftY, leftZ;
        leftX = upY * dirnZ - upZ * dirnY;
        leftY = upZ * dirnX - upX * dirnZ;
        leftZ = upX * dirnY - upY * dirnX;
        // normalize left
        float invLeftLength = (float) (1.0 / Math.sqrt(leftX * leftX + leftY * leftY + leftZ *
                leftZ));
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        float upnX = dirnY * leftZ - dirnZ * leftY;
        float upnY = dirnZ * leftX - dirnX * leftZ;
        float upnZ = dirnX * leftY - dirnY * leftX;

        /* Convert orthonormal basis vectors to quaternion */
        float x, y, z, w;
        double t;
        double tr = leftX + upnY + dirnZ;
        if (tr >= 0.0) {
            t = Math.sqrt(tr + 1.0);
            w = (float) (t * 0.5);
            t = 0.5 / t;
            x = (float) ((upnZ - dirnY) * t);
            y = (float) ((dirnX - leftZ) * t);
            z = (float) ((leftY - upnX) * t);
        } else {
            if (leftX > upnY && leftX > dirnZ) {
                t = Math.sqrt(1.0 + leftX - upnY - dirnZ);
                x = (float) (t * 0.5);
                t = 0.5 / t;
                y = (float) ((leftY + upnX) * t);
                z = (float) ((dirnX + leftZ) * t);
                w = (float) ((upnZ - dirnY) * t);
            } else if (upnY > dirnZ) {
                t = Math.sqrt(1.0 + upnY - leftX - dirnZ);
                y = (float) (t * 0.5);
                t = 0.5 / t;
                x = (float) ((leftY + upnX) * t);
                z = (float) ((upnZ + dirnY) * t);
                w = (float) ((dirnX - leftZ) * t);
            } else {
                t = Math.sqrt(1.0 + dirnZ - leftX - upnY);
                z = (float) (t * 0.5);
                t = 0.5 / t;
                x = (float) ((dirnX + leftZ) * t);
                y = (float) ((upnZ + dirnY) * t);
                w = (float) ((leftY - upnX) * t);
            }
        }
        dest.w = w;
        dest.x = x;
        dest.y = y;
        dest.z = z;
        return dest;
    }

    public static Quaternionf matrixRotation(Matrix4f matrix, Quaternionf rotation,
                                             Quaternionf dest) {
        rotation.transform(FORWARD_VECTOR, forward);
        rotation.transform(UP_VECTOR, up);
        forward.mulDirection(matrix);
        up.mulDirection(matrix);
        return lookRotate(forward.x, forward.y, forward.z, up.x, up.y, up.z, dest);
    }

    public static Quaternionf lookAt(Vector3f prev, Vector3f next, Quaternionf dest) {
        prev.sub(next, direction);
        Vector3f up;
        direction.normalize();

        if (Math.abs(direction.x) < 0.00001
                && Math.abs(direction.z) < 0.00001) {
            if (direction.y > 0) {
                up = new Vector3f(0.0f, 0.0f, -1.0f); // if direction points in +y
            } else {
                up = new Vector3f(0.0f, 0.0f, 1.0f); // if direction points in -y
            }
        } else {
            up = new Vector3f(0.0f, 1.0f, 0.0f); // y-axis is the general up
        }

        up.normalize();
        Vector3f right = new Vector3f();
        up.cross(direction, right);
        right.normalize();
        direction.cross(right, up);
        up.normalize();

        dest = Utils.lookRotate(direction.x, direction.y, direction.z, up.x, up.y, up.z, dest);

        return dest;
    }

    public static Matrix4f lookAt(Vector3f prev, Vector3f next, Matrix4f rotationMatrix) {

        prev.sub(next, direction);
        Vector3f up;
        direction.normalize();

        if (Math.abs(direction.x) < 0.00001
                && Math.abs(direction.z) < 0.00001) {
            if (direction.y > 0) {
                up = new Vector3f(0.0f, 0.0f, -1.0f); // if direction points in +y
            } else {
                up = new Vector3f(0.0f, 0.0f, 1.0f); // if direction points in -y
            }
        } else {
            up = new Vector3f(0.0f, 1.0f, 0.0f); // y-axis is the general up
        }

        up.normalize();
        Vector3f right = new Vector3f();
        up.cross(direction, right);
        right.normalize();
        direction.cross(right, up);
        up.normalize();

        rotationMatrix.m00 = right.x;
        rotationMatrix.m01 = right.y;
        rotationMatrix.m02 = right.z;

        rotationMatrix.m10 = up.x;
        rotationMatrix.m11 = up.y;
        rotationMatrix.m12 = up.z;

        rotationMatrix.m20 = direction.x;
        rotationMatrix.m21 = direction.y;
        rotationMatrix.m22 = direction.z;

        rotationMatrix.m30 = (next.x + prev.x) / 2.0f;
        rotationMatrix.m31 = (next.y + prev.y) / 2.0f;
        rotationMatrix.m32 = (next.z + prev.z) / 2.0f;
        return rotationMatrix;
    }
}
