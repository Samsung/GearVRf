package org.gearvrf.widgetlib.widget;

import org.gearvrf.widgetlib.widget.layout.Layout;
import static org.gearvrf.widgetlib.main.Utility.equal;

import org.gearvrf.utility.RuntimeAssertion;
import org.joml.Vector3f;

/**
 * ViewPort class defines the layout container dimensions. It is similar to {@link Vector3f} but
 * extended by Layout specific methods.
 */
public class Vector3Axis extends Vector3f {
    /**
     * Create new Vector3Axis instance
     * @param x X-dimension
     * @param y Y-dimension
     * @param z Z-dimension
     */
    public Vector3Axis(final float x, final float y, final float z) {
        super(x, y, z);
    }

    /**
     * Create new Vector3Axis instance
     * @param v 3-Dimensions vector
     */
    public Vector3Axis(Vector3f v) {
        super(v.x, v.y, v.z);
    }

    /**
     * Create new Vector3Axis instance with (0, 0, 0) size as a default
     */
    public Vector3Axis() {
        super();
    }

    /**
     * Gets axis dimension
     * @param axis Axis. It might be either {@link Layout.Axis#X X} or
     * {@link Layout.Axis#Y Y} or {@link Layout.Axis#Z Z}
     * @return axis dimension
     */
    public float get(Layout.Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
            default:
                throw new RuntimeAssertion("Bad axis specified: %s", axis);
        }
    }

    /**
     * Sets axis dimension
     * @param val dimension
     * @param axis Axis. It might be either {@link Layout.Axis#X X} or
     * {@link Layout.Axis#Y Y} or {@link Layout.Axis#Z Z}
     */
    public void set(float val, Layout.Axis axis) {
        switch (axis) {
            case X:
                x = val;
                break;
            case Y:
                y = val;
                break;
            case Z:
                z = val;
                break;
            default:
                throw new RuntimeAssertion("Bad axis specified: %s", axis);
        }
    }

    /**
     * Checks if vector is {@link Float#isNaN} or not.
     * @return true if all dimensions are {@link Float#isNaN}, otherwise - false
     */
    public boolean isNaN() {
        return Float.isNaN(x) && Float.isNaN(y) && Float.isNaN(z);
    }

    /**
     * Checks if vector is {@link Float#isInfinite} or not.
     * @return true if all dimensions are {@link Float#isInfinite}, otherwise - false
     */
    public boolean isInfinite() {
        return Float.isInfinite(x) && Float.isInfinite(y) && Float.isInfinite(z);
    }

    /**
     * Calculate delta with another vector
     * @param v another vector
     * @return delta vector
     */
    public Vector3Axis delta(Vector3f v) {
        Vector3Axis ret = new Vector3Axis(Float.NaN, Float.NaN, Float.NaN);
        if (x != Float.NaN && v.x != Float.NaN && !equal(x, v.x)) {
            ret.set(x - v.x, Layout.Axis.X);
        }
        if (y != Float.NaN && v.y != Float.NaN && !equal(y, v.y)) {
            ret.set(y - v.y, Layout.Axis.Y);
        }
        if (z != Float.NaN && v.z != Float.NaN && !equal(z, v.z)) {
            ret.set(z - v.z, Layout.Axis.Z);
        }
        return ret;
    }
}
