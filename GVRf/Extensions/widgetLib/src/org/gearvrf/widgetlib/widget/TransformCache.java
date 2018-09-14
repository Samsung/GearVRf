package org.gearvrf.widgetlib.widget;

import org.gearvrf.GVRTransform;
import org.joml.AxisAngle4f;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static org.gearvrf.widgetlib.main.Utility.equal;
import static java.lang.Math.signum;

public class TransformCache {
    public TransformCache() {

    }

    public TransformCache(final GVRTransform transform) {
        save(transform);
    }

    public TransformCache(final Widget widget) {
        save(widget);
    }

    public void save(GVRTransform transform) {
        save(transform, false);
    }

    public boolean save(GVRTransform transform, boolean notify) {
        if (notify) {
            return setPosX(transform.getPositionX())
                    | setPosY(transform.getPositionY())
                    | setPosZ(transform.getPositionZ())
                    | setRotW(transform.getRotationW())
                    | setRotX(transform.getRotationX())
                    | setRotY(transform.getRotationY())
                    | setRotZ(transform.getRotationZ())
                    | setScaleX(transform.getScaleX())
                    | setScaleY(transform.getScaleY())
                    | setScaleZ(transform.getScaleZ());
        } else {
            mPosition.x = transform.getPositionX();
            mPosition.y = transform.getPositionY();
            mPosition.z = transform.getPositionZ();
            mRotation.w = transform.getRotationW();
            mRotation.x = transform.getRotationX();
            mRotation.y = transform.getRotationY();
            mRotation.z = transform.getRotationZ();
            mScale.x = transform.getScaleX();
            mScale.y = transform.getScaleY();
            mScale.z = transform.getScaleZ();
            return false;
        }
    }

    public void restore(GVRTransform transform) {
        transform.setScale(getScaleX(), getScaleY(), getScaleZ());
        transform.setPosition(getPosX(), getPosY(), getPosZ());
        transform.setRotation(getRotW(), getRotX(), getRotY(), getRotZ());
    }

    public boolean changed(final GVRTransform transform) {
        return !(equal(getPosX(), transform.getPositionX())
                && equal(getPosY(), transform.getPositionY())
                && equal(getPosZ(), transform.getPositionZ())
                && equal(getRotW(), transform.getRotationW())
                && equal(getRotX(), transform.getRotationX())
                && equal(getRotY(), transform.getRotationY())
                && equal(getRotZ(), transform.getRotationZ())
                && equal(getScaleX(), transform.getScaleX())
                && equal(getScaleY(), transform.getScaleY())
                && equal(getScaleZ(), transform.getScaleZ()));
    }

    public void save(final Widget widget) {
        save(widget.getTransform(), false);
    }

    public boolean save(final Widget widget, boolean notify) {
        return save(widget.getTransform(), notify);
    }

    public void restore(final Widget widget) {
        widget.setScale(getScaleX(), getScaleY(), getScaleZ());
        widget.setPosition(getPosX(), getPosY(), getPosZ());
        widget.setRotation(getRotW(), getRotX(), getRotY(), getRotZ());
    }

    public boolean changed(final Widget widget) {
        return changed(widget.getTransform());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Pos: {").append(getPosX()).append(',')
                .append(getPosY()).append(',').append(getPosZ()).append("}, Rot: {")
                .append(getRotW()).append(',').append(getRotX()).append(',').append(getRotY())
                .append(',').append(getRotZ()).append("}, Scale: {").append(getScaleX())
                .append(',').append(getScaleY()).append(',').append(getScaleZ())
                .append('}');
        return b.toString();
    }

    public boolean translate(float x, float y, float z) {
        boolean changed = !equal(0, x) | !equal(0, y) | !equal(0, z);
        mPosition.add(x, y, z);
        return changed;
    }

    public boolean setPosition(float posX, float posY, float posZ) {
        return setPosX(posX) | setPosY(posY) | setPosZ(posZ);
    }

    public boolean setPosX(float posX) {
        boolean changed = !equal(getPosX(), posX);
        mPosition.x = posX;
        return changed;
    }

    public boolean setPosY(float posY) {
        boolean changed = !equal(getPosY(), posY);
        mPosition.y = posY;
        return changed;
    }

    public boolean setPosZ(float posZ) {
        boolean changed = !equal(getPosZ(), posZ);
        mPosition.z = posZ;
        return changed;
    }

    public boolean rotate(float w, float x, float y, float z) {
        final boolean changed = !equal(getRotW(), w) | !equal(getRotX(), x) |
                !equal(getRotY(), y) | !equal(getRotZ(), z);
        mRotation.mul(w, x, y, z);
        return changed;
    }

    public boolean rotateByAxis(float angle, float x, float y, float z) {
        final boolean changed = !equal(0, angle) | !equal(0, x) |
                !equal(0, y) | !equal(0, z);
        mRotation.rotateAxis(angle, x, y, z);
        return changed;
    }

    public boolean rotateByAxisWithPivot(float angle, float axisX, float axisY, float axisZ,
                                         float pivotX, float pivotY, float pivotZ) {
        final Quaternionf oldRotation = new Quaternionf(mRotation);
        final Vector3f oldPosition = new Vector3f(mPosition);

        Quaternionf axisRotation = new Quaternionf(new AxisAngle4f(angle, axisX, axisY, axisZ));
        mRotation.mul(axisRotation);

        mPosition
                .sub(pivotX, pivotY, pivotZ)
                .rotate(axisRotation)
                .add(pivotX, pivotY, pivotZ);

        return !oldPosition.equals(mPosition) | !oldRotation.equals(mRotation);
    }

    public boolean rotateWithPivot(float w, float x, float y, float z,
                                   float pivotX, float pivotY, float pivotZ) {
        final Quaternionf oldRotation = new Quaternionf(mRotation);
        final Vector3f oldPosition = new Vector3f(mPosition);

        Quaternionf rotation = new Quaternionf(w, x, y, z);
        mRotation.mul(rotation);

        new Vector3f(mPosition)
                .sub(pivotX, pivotY, pivotZ)
                .rotate(rotation)
                .add(pivotX, pivotY, pivotZ, mPosition);

        return !oldPosition.equals(mPosition) | !oldRotation.equals(mRotation);
    }

    public boolean setRotationByAxis(float angle, float x, float y, float z) {
        AxisAngle4f axisAngle = new AxisAngle4f();
        mRotation.get(axisAngle);
        boolean changed = equal(axisAngle.angle, angle) | equal(axisAngle.x, x) |
                equal(axisAngle.y, y) | equal(axisAngle.z, z);
        mRotation.setAngleAxis(angle, x, y, z);
        return changed;
    }

    public boolean setRotation(float rotW, float rotX, float rotY, float rotZ) {
        return setRotW(rotW) | setRotX(rotX) | setRotY(rotY) | setRotZ(rotZ);
    }

    public boolean setRotW(float rotW) {
        boolean changed = !equal(getRotW(), rotW);
        mRotation.w = rotW;
        return changed;
    }

    public boolean setRotX(float rotX) {
        boolean changed = !equal(getRotX(), rotX);
        mRotation.x = rotX;
        return changed;
    }

    public boolean setRotY(float rotY) {
        boolean changed = !equal(getRotY(), rotY);
        mRotation.y = rotY;
        return changed;
    }

    public boolean setRotZ(float rotZ) {
        boolean changed = !equal(getRotZ(), rotZ);
        mRotation.z = rotZ;
        return changed;
    }

    public boolean setScale(float scaleX, float scaleY, float scaleZ) {
        return setScaleX(scaleX) | setScaleY(scaleY) | setScaleZ(scaleZ);
    }

    public boolean setScaleX(float scaleX) {
        boolean changed = !equal(getScaleX(), scaleX);
        mScale.x = scaleX;
        return changed;
    }

    public boolean setScaleY(float scaleY) {
        boolean changed = !equal(getScaleY(), scaleY);
        mScale.y = scaleY;
        return changed;
    }

    public boolean setScaleZ(float scaleZ) {
        boolean changed = !equal(getScaleZ(), scaleZ);
        mScale.z = scaleZ;
        return changed;
    }

    public float getPosX() {
        return mPosition.x;
    }

    public float getPosY() {
        return mPosition.y;
    }

    public float getPosZ() {
        return mPosition.z;
    }

    public float getRotW() {
        return mRotation.w;
    }

    public float getRotX() {
        return mRotation.x;
    }

    public float getRotY() {
        return mRotation.y;
    }

    public float getRotZ() {
        return mRotation.z;
    }

    public float getScaleX() {
        return mScale.x;
    }

    public float getScaleY() {
        return mScale.y;
    }

    public float getScaleZ() {
        return mScale.z;
    }

    public float getPitch() {
        return mRotation.getEulerAnglesXYZ(new Vector3f()).x;
}

    public float getRoll() {
        return mRotation.getEulerAnglesXYZ(new Vector3f()).z;
    }

    public float getYaw() {
        return mRotation.getEulerAnglesXYZ(new Vector3f()).y;
    }

    public Matrix4f getMatrix4f() {
        return new Matrix4f().translationRotateScale(mPosition, mRotation, mScale);
    }

    public boolean setModelMatrix(float[] matrix) {
        Vector3f xAxis = new Vector3f(matrix[0], matrix[1], matrix[2]);
        Vector3f yAxis = new Vector3f(matrix[4], matrix[5], matrix[6]);
        Vector3f zAxis = new Vector3f(matrix[8], matrix[9], matrix[10]);
        Vector3f newPosition = new Vector3f(matrix[12], matrix[13], matrix[14]);

        Vector3f scratch = new Vector3f();
        float zs = zAxis.dot(xAxis.cross(yAxis, scratch));
        float ys = yAxis.dot(zAxis.cross(xAxis, scratch));
        float xs = xAxis.dot(yAxis.cross(zAxis, scratch));

        xs = signum(xs);
        ys = signum(ys);
        zs = signum(zs);

        xs = (xs > 0.0 ? -1 : 1);
        ys = (ys > 0.0 ? -1 : 1);
        zs = (zs > 0.0 ? -1 : 1);

        Vector3f newScale = new Vector3f();
        newScale.x = xs * (float) Math.sqrt(matrix[0] * matrix[0]
                + matrix[1] * matrix[1]
                + matrix[2] * matrix[2]);
        newScale.y = ys * (float) Math.sqrt(matrix[4] * matrix[4]
                + matrix[5] * matrix[5]
                + matrix[6] * matrix[6]);
        newScale.z = zs * (float) Math.sqrt(matrix[8] * matrix[8]
                + matrix[9] * matrix[9]
                + matrix[10] * matrix[10]);


        Matrix3f rotationMat = new Matrix3f(
                matrix[0] / newScale.x, matrix[1] / newScale.y, matrix[2] / newScale.z,
                matrix[4] / newScale.x, matrix[5] / newScale.y, matrix[6] / newScale.z,
                matrix[8] / newScale.x, matrix[9] / newScale.y, matrix[10] / newScale.z);

        Quaternionf newRotation = new Quaternionf().setFromNormalized(rotationMat);

        boolean changed = !mPosition.equals(newPosition) | !mScale.equals(newScale) | !mRotation.equals(newRotation);

        mPosition = newPosition;
        mScale = newScale;
        mRotation = newRotation;

        return changed;
    }

    public boolean reset() {
        final boolean changed = !equal(getPosX(), 0) | !equal(getPosY(), 0) |
                !equal(getPosZ(), 0) | !equal(getRotW(), 1) |
                !equal(getRotX(), 0) | !equal(getRotY(), 0) |
                !equal(getRotZ(), 0) | !equal(getScaleX(), 1) |
                !equal(getScaleY(), 1) | !equal(getScaleZ(), 1);

        setPosition(0, 0, 0);
        setRotation(1, 0, 0, 0);
        setScale(1, 1, 1);

        return changed;
    }

    private Vector3f mPosition = new Vector3f();

    private Quaternionf mRotation = new Quaternionf();

    private Vector3f mScale = new Vector3f();
}
