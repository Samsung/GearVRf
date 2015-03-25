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

package org.gearvrf;

import java.security.InvalidParameterException;

/**
 * One of the key GVRF classes: Encapsulates a 4x4 matrix that controls how GL
 * draws a mesh.
 * 
 * Every {@link GVRSceneObject#getTransform() scene object} has a
 * {@code GVRTransform} which exposes more-or-less convenient methods to do
 * translation, rotation and scaling. Rotations can be made in either quaternion
 * or angle/axis terms; rotation values can be retrieved as either quaternion
 * components or as Euler angles.
 */
public class GVRTransform extends GVRComponent {
    GVRTransform(GVRContext gvrContext) {
        super(gvrContext, NativeTransform.ctor());
    }

    private GVRTransform(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    static GVRTransform factory(GVRContext gvrContext, long ptr) {
        GVRHybridObject wrapper = wrapper(ptr);
        return wrapper == null ? new GVRTransform(gvrContext, ptr)
                : (GVRTransform) wrapper;
    }

    @Override
    protected final boolean registerWrapper() {
        return true;
    }

    /**
     * Get the X component of the transform's position.
     * 
     * @return 'X' component of the transform's position.
     */
    public float getPositionX() {
        return NativeTransform.getPositionX(getPtr());
    }

    /**
     * Get the 'Y' component of the transform's position.
     * 
     * @return 'Y' component of the transform's position.
     */
    public float getPositionY() {
        return NativeTransform.getPositionY(getPtr());
    }

    /**
     * Get the 'Z' component of the transform's position.
     * 
     * @return 'Z' component of the transform's position.
     */
    public float getPositionZ() {
        return NativeTransform.getPositionZ(getPtr());
    }

    /**
     * Set absolute position.
     * 
     * Use {@link #translate(float, float, float)} to <em>move</em> the object.
     * 
     * @param x
     *            'X' component of the absolute position.
     * @param y
     *            'Y' component of the absolute position.
     * @param z
     *            'Z' component of the absolute position.
     */
    public void setPosition(float x, float y, float z) {
        NativeTransform.setPosition(getPtr(), x, y, z);
    }

    /**
     * Set the 'X' component of absolute position.
     * 
     * Use {@link #translate(float, float, float)} to <em>move</em> the object.
     * 
     * @param x
     *            New 'X' component of the absolute position.
     */
    public void setPositionX(float x) {
        NativeTransform.setPositionX(getPtr(), x);
    }

    /**
     * Set the 'Y' component of the absolute position.
     * 
     * Use {@link #translate(float, float, float)} to <em>move</em> the object.
     * 
     * @param y
     *            New 'Y' component of the absolute position.
     */
    public void setPositionY(float y) {
        NativeTransform.setPositionY(getPtr(), y);
    }

    /**
     * Set the 'Z' component of the absolute position.
     * 
     * Use {@link #translate(float, float, float)} to <em>move</em> the object.
     * 
     * @param z
     *            New 'Z' component of the absolute position.
     */
    public void setPositionZ(float z) {
        NativeTransform.setPositionZ(getPtr(), z);
    }

    /**
     * Get the quaternion 'W' component.
     * 
     * @return 'W' component of the transform's rotation, treated as a
     *         quaternion.
     */
    public float getRotationW() {
        return NativeTransform.getRotationW(getPtr());
    }

    /**
     * Get the quaternion 'X' component.
     * 
     * @return 'X' component of the transform's rotation, treated as a
     *         quaternion.
     */
    public float getRotationX() {
        return NativeTransform.getRotationX(getPtr());
    }

    /**
     * Get the quaternion 'Y' component.
     * 
     * @return 'Y' component of the transform's rotation, treated as a
     *         quaternion.
     */
    public float getRotationY() {
        return NativeTransform.getRotationY(getPtr());
    }

    /**
     * Get the quaternion 'Z' component.
     * 
     * @return 'Z' component of the transform's rotation, treated as a
     *         quaternion.
     */
    public float getRotationZ() {
        return NativeTransform.getRotationZ(getPtr());
    }

    /**
     * Get the rotation around the 'Y' axis, in degrees.
     * 
     * @return The transform's current rotation around the 'Y' axis, in degrees.
     */
    public float getRotationYaw() {
        return NativeTransform.getRotationYaw(getPtr());
    }

    /**
     * Get the rotation around the 'X' axis, in degrees.
     * 
     * @return The transform's rotation around the 'X' axis, in degrees.
     */
    public float getRotationPitch() {
        return NativeTransform.getRotationPitch(getPtr());
    }

    /**
     * Get the rotation around the 'Z' axis, in degrees.
     * 
     * @return The transform's rotation around the 'Z' axis, in degrees.
     */
    public float getRotationRoll() {
        return NativeTransform.getRotationRoll(getPtr());
    }

    /**
     * Set rotation, as a quaternion.
     * 
     * Sets the transform's current rotation in quaternion terms. Overrides any
     * previous rotations using {@link #rotate(float, float, float, float)
     * rotate()}, {@link #rotateByAxis(float, float, float, float)
     * rotateByAxis()} , or
     * {@link #rotateByAxisWithPivot(float, float, float, float, float, float, float)
     * rotateByAxisWithPivot()} .
     * 
     * @param w
     *            'W' component of the quaternion.
     * @param x
     *            'X' component of the quaternion.
     * @param y
     *            'Y' component of the quaternion.
     * @param z
     *            'Z' component of the quaternion.
     */
    public void setRotation(float w, float x, float y, float z) {
        NativeTransform.setRotation(getPtr(), w, x, y, z);
    }

    /**
     * Get the 'X' scale
     * 
     * @return The transform's current scaling on the 'X' axis.
     */
    public float getScaleX() {
        return NativeTransform.getScaleX(getPtr());
    }

    /**
     * Get the 'Y' scale
     * 
     * @return The transform's current scaling on the 'Y' axis.
     */
    public float getScaleY() {
        return NativeTransform.getScaleY(getPtr());
    }

    /**
     * Get the 'Z' scale
     * 
     * @return The transform's current scaling on the 'Z' axis.
     */
    public float getScaleZ() {
        return NativeTransform.getScaleZ(getPtr());
    }

    /**
     * Set [X, Y, Z] current scale
     * 
     * @param x
     *            Scaling factor on the 'X' axis.
     * @param y
     *            Scaling factor on the 'Y' axis.
     * @param z
     *            Scaling factor on the 'Z' axis.
     */
    public void setScale(float x, float y, float z) {
        NativeTransform.setScale(getPtr(), x, y, z);
    }

    /**
     * Set the transform's current scaling on the 'X' axis.
     * 
     * @param x
     *            Scaling factor on the 'X' axis.
     */
    public void setScaleX(float x) {
        NativeTransform.setScaleX(getPtr(), x);
    }

    /**
     * Set the transform's current scaling on the 'Y' axis.
     * 
     * @param y
     *            Scaling factor on the 'Y' axis.
     */
    public void setScaleY(float y) {
        NativeTransform.setScaleY(getPtr(), y);
    }

    /**
     * Set the transform's current scaling on the 'Z' axis.
     * 
     * @param z
     *            Scaling factor on the 'Z' axis.
     */
    public void setScaleZ(float z) {
        NativeTransform.setScaleZ(getPtr(), z);
    }

    /**
     * Get the 4x4 single matrix.
     * 
     * @return An array of 16 {@code float}s representing a 4x4 matrix in
     *         OpenGL-compatible column-major format.
     */
    public float[] getModelMatrix() {
        return NativeTransform.getModelMatrix(getPtr());
    }

    /**
     * Set the 4x4 model matrix and set current scaling, rotation, and 
     * transformation based on this model matrix.
     * 
     * @param mat
     *            An array of 16 {@code float}s representing a 4x4 matrix in
     *            OpenGL-compatible column-major format.
     */    
    public void setModelMatrix(float[] mat) {
        if (mat.length != 16)
            throw new InvalidParameterException("Size not equals with 16.");
        NativeTransform.setModelMatrix(getPtr(), mat);
    }

    /**
     * Move the object, relative to its current position.
     * 
     * Modify the tranform's current translation by applying translations on all
     * 3 axes.
     * 
     * @param x
     *            'X' delta
     * @param y
     *            'Y' delta
     * @param z
     *            'Z' delta
     */
    public void translate(float x, float y, float z) {
        NativeTransform.translate(getPtr(), x, y, z);
    }

    /**
     * Sets the absolute rotation in angle/axis terms.
     * 
     * Rotates using the right hand rule.
     * 
     * <p>
     * Contrast this with {@link #rotate(float, float, float, float) rotate()},
     * {@link #rotateByAxis(float, float, float, float) rotateByAxis()}, or
     * {@link #rotateByAxisWithPivot(float, float, float, float, float, float, float)
     * rotateByAxisWithPivot()}, which all do relative rotations.
     * 
     * @param angle
     *            Angle of rotation in degrees.
     * @param x
     *            'X' component of the axis.
     * @param y
     *            'Y' component of the axis.
     * @param z
     *            'Z' component of the axis.
     */
    public void setRotationByAxis(float angle, float x, float y, float z) {
        NativeTransform.setRotationByAxis(getPtr(), angle, x, y, z);
    }

    /**
     * Modify the tranform's current rotation in quaternion terms.
     * 
     * @param w
     *            'W' component of the quaternion.
     * @param x
     *            'X' component of the quaternion.
     * @param y
     *            'Y' component of the quaternion.
     * @param z
     *            'Z' component of the quaternion.
     */
    public void rotate(float w, float x, float y, float z) {
        NativeTransform.rotate(getPtr(), w, x, y, z);
    }

    /**
     * Modify the transform's current rotation in angle/axis terms.
     * 
     * @param angle
     *            Angle of rotation in degrees.
     * @param x
     *            'X' component of the axis.
     * @param y
     *            'Y' component of the axis.
     * @param z
     *            'Z' component of the axis.
     */
    public void rotateByAxis(float angle, float x, float y, float z) {
        NativeTransform.rotateByAxis(getPtr(), angle, x, y, z);
    }

    /**
     * Modify the transform's current rotation in angle/axis terms, around a
     * pivot other than the origin.
     * 
     * @param angle
     *            Angle of rotation in degrees.
     * @param axisX
     *            'X' component of the axis.
     * @param axisY
     *            'Y' component of the axis.
     * @param axisZ
     *            'Z' component of the axis.
     * @param pivotX
     *            'X' component of the pivot's location.
     * @param pivotY
     *            'Y' component of the pivot's location.
     * @param pivotZ
     *            'Z' component of the pivot's location.
     */
    public void rotateByAxisWithPivot(float angle, float axisX, float axisY,
            float axisZ, float pivotX, float pivotY, float pivotZ) {
        NativeTransform.rotateByAxisWithPivot(getPtr(), angle, axisX, axisY,
                axisZ, pivotX, pivotY, pivotZ);
    }
}

class NativeTransform {
    public static native long ctor();

    public static native float getPositionX(long transform);

    public static native float getPositionY(long transform);

    public static native float getPositionZ(long transform);

    public static native void setPosition(long transform, float x, float y,
            float z);

    public static native void setPositionX(long transform, float x);

    public static native void setPositionY(long transform, float y);

    public static native void setPositionZ(long transform, float z);

    public static native float getRotationW(long transform);

    public static native float getRotationX(long transform);

    public static native float getRotationY(long transform);

    public static native float getRotationZ(long transform);

    public static native float getRotationYaw(long transform);

    public static native float getRotationPitch(long transform);

    public static native float getRotationRoll(long transform);

    public static native void setRotation(long transform, float w, float x,
            float y, float z);

    public static native float getScaleX(long transform);

    public static native float getScaleY(long transform);

    public static native float getScaleZ(long transform);

    public static native void setScale(long transform, float x, float y, float z);

    public static native void setScaleX(long transform, float x);

    public static native void setScaleY(long transform, float y);

    public static native void setScaleZ(long transform, float z);

    public static native float[] getModelMatrix(long transform);

    public static native void setModelMatrix(long tranform, float[] mat);

    public static native void translate(long transform, float x, float y,
            float z);

    public static native void setRotationByAxis(long transform, float angle,
            float x, float y, float z);

    public static native void rotate(long transform, float w, float x, float y,
            float z);

    public static native void rotateByAxis(long transform, float angle,
            float x, float y, float z);

    public static native void rotateByAxisWithPivot(long transform,
            float angle, float axisX, float axisY, float axisZ, float pivotX,
            float pivotY, float pivotZ);
}
