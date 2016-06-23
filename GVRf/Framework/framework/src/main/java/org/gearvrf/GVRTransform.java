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

import org.joml.Matrix4f;

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
    final float TO_DEGREES = (float) (180.0/Math.PI);
    final float TO_RADIANS = 1/TO_DEGREES;

    GVRTransform(GVRContext gvrContext) {
        super(gvrContext, NativeTransform.ctor());
    }

    GVRTransform(GVRContext gvrContext, GVRSceneObject owner) {
        super(gvrContext, NativeTransform.ctor());
        setOwnerObject(owner);
    }
    
    private GVRTransform(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    static public long getComponentType() {
        return NativeTransform.getComponentType();
    }
    
    /**
     * Get the X component of the transform's position.
     * 
     * @return 'X' component of the transform's position.
     */
    public float getPositionX() {
        return NativeTransform.getPositionX(getNative());
    }

    /**
     * Get the 'Y' component of the transform's position.
     * 
     * @return 'Y' component of the transform's position.
     */
    public float getPositionY() {
        return NativeTransform.getPositionY(getNative());
    }

    /**
     * Get the 'Z' component of the transform's position.
     * 
     * @return 'Z' component of the transform's position.
     */
    public float getPositionZ() {
        return NativeTransform.getPositionZ(getNative());
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
        NativeTransform.setPosition(getNative(), x, y, z);
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
        NativeTransform.setPositionX(getNative(), x);
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
        NativeTransform.setPositionY(getNative(), y);
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
        NativeTransform.setPositionZ(getNative(), z);
    }

    /**
     * Get the quaternion 'W' component.
     * 
     * @return 'W' component of the transform's rotation, treated as a
     *         quaternion.
     */
    public float getRotationW() {
        return NativeTransform.getRotationW(getNative());
    }

    /**
     * Get the quaternion 'X' component.
     * 
     * @return 'X' component of the transform's rotation, treated as a
     *         quaternion.
     */
    public float getRotationX() {
        return NativeTransform.getRotationX(getNative());
    }

    /**
     * Get the quaternion 'Y' component.
     * 
     * @return 'Y' component of the transform's rotation, treated as a
     *         quaternion.
     */
    public float getRotationY() {
        return NativeTransform.getRotationY(getNative());
    }

    /**
     * Get the quaternion 'Z' component.
     * 
     * @return 'Z' component of the transform's rotation, treated as a
     *         quaternion.
     */
    public float getRotationZ() {
        return NativeTransform.getRotationZ(getNative());
    }

    /**
     * Get the rotation around the 'Y' axis, in degrees.
     * 
     * @return The transform's current rotation around the 'Y' axis, in degrees.
     */
    public float getRotationYaw() {
        return NativeTransform.getRotationYaw(getNative()) * TO_DEGREES;
    }

    /**
     * Get the rotation around the 'X' axis, in degrees.
     * 
     * @return The transform's rotation around the 'X' axis, in degrees.
     */
    public float getRotationPitch() {
        return NativeTransform.getRotationPitch(getNative()) * TO_DEGREES;
    }

    /**
     * Get the rotation around the 'Z' axis, in degrees.
     * 
     * @return The transform's rotation around the 'Z' axis, in degrees.
     */
    public float getRotationRoll() {
        return NativeTransform.getRotationRoll(getNative()) * TO_DEGREES;
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
        NativeTransform.setRotation(getNative(), w, x, y, z);
    }

    /**
     * Get the 'X' scale
     * 
     * @return The transform's current scaling on the 'X' axis.
     */
    public float getScaleX() {
        return NativeTransform.getScaleX(getNative());
    }

    /**
     * Get the 'Y' scale
     * 
     * @return The transform's current scaling on the 'Y' axis.
     */
    public float getScaleY() {
        return NativeTransform.getScaleY(getNative());
    }

    /**
     * Get the 'Z' scale
     * 
     * @return The transform's current scaling on the 'Z' axis.
     */
    public float getScaleZ() {
        return NativeTransform.getScaleZ(getNative());
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
        NativeTransform.setScale(getNative(), x, y, z);
    }

    /**
     * Set the transform's current scaling on the 'X' axis.
     * 
     * @param x
     *            Scaling factor on the 'X' axis.
     */
    public void setScaleX(float x) {
        NativeTransform.setScaleX(getNative(), x);
    }

    /**
     * Set the transform's current scaling on the 'Y' axis.
     * 
     * @param y
     *            Scaling factor on the 'Y' axis.
     */
    public void setScaleY(float y) {
        NativeTransform.setScaleY(getNative(), y);
    }

    /**
     * Set the transform's current scaling on the 'Z' axis.
     * 
     * @param z
     *            Scaling factor on the 'Z' axis.
     */
    public void setScaleZ(float z) {
        NativeTransform.setScaleZ(getNative(), z);
    }

    /**
     * Get the 4x4 single matrix.
     * 
     * @return An array of 16 {@code float}s representing a 4x4 matrix in
     *         OpenGL-compatible column-major format.
     */
    public float[] getModelMatrix() {
        return NativeTransform.getModelMatrix(getNative());
    }

    /**
     * Get the 4x4 single local transform matrix.
     * 
     * @return An array of 16 {@code float}s representing a 4x4 matrix in
     *         OpenGL-compatible column-major format.
     */
    public float[] getLocalModelMatrix() {
        return NativeTransform.getLocalModelMatrix(getNative());
    }

    /**
     * Get the 4x4 single matrix.
     *
     * @return An a {@code Matrix4f} representing a 4x4 matrix as a JOML
     *         {@code Matrix4f} object.
     */
    public Matrix4f getModelMatrix4f() {
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.set(getModelMatrix());
        return modelMatrix;
    }

    /**
     * Get the 4x4 single single local matrix.
     *
     * @return An a {@code Matrix4f} representing a 4x4 matrix as a JOML
     *         {@code Matrix4f} object.
     */
    public Matrix4f getLocalModelMatrix4f() {
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.set(getLocalModelMatrix());
        return modelMatrix;
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
        if (mat.length != 16) {
            throw new IllegalArgumentException("Size not equal to 16.");
        }
        NativeTransform.setModelMatrix(getNative(), mat);
    }

    /**
     * Set the 4x4 model matrix and set current scaling, rotation, and
     * transformation based on this model matrix.
     *
     * @param mat
     *            A {@code Matrix4f} representing a 4x4 matrix in
     *            OpenGL-compatible column-major format.
     */
    public void setModelMatrix(Matrix4f mat) {
        setModelMatrix(mat.get(new float[16]));
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
        NativeTransform.translate(getNative(), x, y, z);
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
        NativeTransform.setRotationByAxis(getNative(), angle * TO_RADIANS, x, y, z);
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
        NativeTransform.rotate(getNative(), w, x, y, z);
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
        NativeTransform.rotateByAxis(getNative(), angle * TO_RADIANS, x, y, z);
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
        NativeTransform.rotateByAxisWithPivot(getNative(), angle * TO_RADIANS, axisX, axisY,
                axisZ, pivotX, pivotY, pivotZ);
    }

    /**
     * Modify the transform's current rotation in quaternion terms, around a
     * pivot other than the origin.
     * 
     * @param quatW
     *            'W' component of the rotation quaternion.
     * @param quatX
     *            'X' component of the rotation quaternion.
     * @param quatY
     *            'Y' component of the rotation quaternion.
     * @param quatZ
     *            'Z' component of the rotation quaternion.
     * @param pivotX
     *            'X' component of the pivot's location.
     * @param pivotY
     *            'Y' component of the pivot's location.
     * @param pivotZ
     *            'Z' component of the pivot's location.
     */
    public void rotateWithPivot(float quatW, float quatX, float quatY,
            float quatZ, float pivotX, float pivotY, float pivotZ) {
        getGVRContext().getMainScene().inValidateShadowMap();
        NativeTransform.rotateWithPivot(getNative(), quatW, quatX, quatY,
                quatZ, pivotX, pivotY, pivotZ);
    }


    /**
     * Reset the transform
     *
     * This will undo any translations, rotations, or scaling and reset the Transform back to default values.  This is the equivilent to setting the Transform to an identity matrix.
     */
    public void reset() {
        getGVRContext().getMainScene().inValidateShadowMap();
        setPosition(0, 0, 0);
        setRotation(1, 0, 0, 0);
        setScale(1, 1, 1);
    }

    @Override
    public String toString() {
        return "GVRTransform " + Integer.toHexString(hashCode()) + ", positionX = " + getPositionX()
                + ", positionY = " + getPositionY() + ", positionZ = " + getPositionZ()
                + ", scaleX = " + getScaleX() + ", scaleY = " + getScaleY() + ", scaleZ = "
                + getScaleZ() + ", rotationW = " + getRotationW() + ", rotationX = "
                + getRotationX() + ", rotationY = " + getRotationY() + ", rotationZ = "
                + getRotationZ();
    }
}

class NativeTransform {
    static native long ctor();

    static native long getComponentType();

    static native float getPositionX(long transform);

    static native float getPositionY(long transform);

    static native float getPositionZ(long transform);

    static native void setPosition(long transform, float x, float y, float z);

    static native void setPositionX(long transform, float x);

    static native void setPositionY(long transform, float y);

    static native void setPositionZ(long transform, float z);

    static native float getRotationW(long transform);

    static native float getRotationX(long transform);

    static native float getRotationY(long transform);

    static native float getRotationZ(long transform);

    static native float getRotationYaw(long transform);

    static native float getRotationPitch(long transform);

    static native float getRotationRoll(long transform);

    static native void setRotation(long transform, float w, float x, float y,
            float z);

    static native float getScaleX(long transform);

    static native float getScaleY(long transform);

    static native float getScaleZ(long transform);

    static native void setScale(long transform, float x, float y, float z);

    static native void setScaleX(long transform, float x);

    static native void setScaleY(long transform, float y);

    static native void setScaleZ(long transform, float z);

    static native float[] getModelMatrix(long transform);

    static native float[] getLocalModelMatrix(long transform);

    static native void setModelMatrix(long tranform, float[] mat);

    static native void translate(long transform, float x, float y, float z);

    static native void setRotationByAxis(long transform, float angle, float x,
            float y, float z);

    static native void rotate(long transform, float w, float x, float y, float z);

    static native void rotateByAxis(long transform, float angle, float x,
            float y, float z);

    static native void rotateByAxisWithPivot(long transform, float angle,
            float axisX, float axisY, float axisZ, float pivotX, float pivotY,
            float pivotZ);

    static native void rotateWithPivot(long transform, float quatW,
            float quatX, float quatY, float quatZ, float pivotX, float pivotY,
            float pivotZ);

}
