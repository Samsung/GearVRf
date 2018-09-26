package org.gearvrf.widgetlib.widget.layout.basic;

import org.gearvrf.widgetlib.widget.Vector3Axis;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.LayoutHelpers;
import org.gearvrf.widgetlib.widget.layout.OrientedLayout;

/**
 * A specialized {@link Layout} that applies the arch curvature to the children on
 * either the {@linkplain Orientation#HORIZONTAL horizontal} or the
 * {@linkplain Orientation#VERTICAL vertical} direction. The radius of the arc
 * can be specified in the class constructor.
 * It is basically designed as the secondary layout in the layout chain.
 */
public class ArchLayout extends OrientedLayout {

    /**
     * Construct a new {@link ArchLayout} with the radius.
     * The size of the container is calculated as the size of the scene object.
     *
     * @param radius - ring radius.
     */
    public ArchLayout(float radius) {
        super();

        if (radius <= 0) {
            Log.w(TAG, "setRadius: Radius cannot be negative [%f] !", radius);
        } else {
            mRadius = radius;
        }
    }

    /**
     * @return ring radius
     */
    public float getRadius() {
        return mRadius;
    }

    /**
     * Return the string representation of the ArchLayout
     */
    public String toString() {
        return super.toString() + String.format(pattern, getOrientation(), mViewPort);
    }

    protected ArchLayout(final ArchLayout rhs) {
        super(rhs);
        mRadius = rhs.mRadius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArchLayout)) return false;
        if (!super.equals(o)) return false;

        ArchLayout that = (ArchLayout) o;

        return Float.compare(that.mRadius, mRadius) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mRadius != +0.0f ? Float.floatToIntBits(mRadius) : 0);
        return result;
    }

    @Override
    public Layout clone() {
        return new ArchLayout(this);
    }

    @Override
    public void onLayoutApplied(final WidgetContainer container, final Vector3Axis viewPort) {
        super.onLayoutApplied(container, new Vector3Axis(
                                 getSizeAngle(viewPort.x),
                                 getSizeAngle(viewPort.y),
                                 getSizeAngle(viewPort.z)));
    }

    @Override
    public void setOffset(float offset, final Axis axis) {
        Log.w(Log.SUBSYSTEM.LAYOUT, TAG, "Offset is not supported for ArchLayout!");
    }

    @Override
    public void layoutChild(final int dataIndex) {
        resetChildLayout(dataIndex);
        super.layoutChild(dataIndex);
    }

    /**
     * Calculate the angle by arc length
     * @param arcLength
     * @return angle
     */
    protected float getSizeAngle(float arcLength) {
        if (mRadius <= 0) {
            throw new IllegalArgumentException("mRadius is not specified!");
        }
        return LayoutHelpers.angleOfArc(arcLength, mRadius);
    }

    /**
     * Calculate the arc length by angle and radius
     * @param angle
     * @return arc length
     */
    protected float getSizeArcLength(float angle) {
        if (mRadius <= 0) {
            throw new IllegalArgumentException("mRadius is not specified!");
        }
        return angle == Float.MAX_VALUE ? Float.MAX_VALUE :
                LayoutHelpers.lengthOfArc(angle, mRadius);
    }

    @Override
    protected float getDataOffset(final int dataIndex) {
        float offset = 0;
        Widget child = mContainer.get(dataIndex);
        if (child != null) {
            Axis axis = getOrientationAxis();
            switch(axis) {
                case X:
                    offset = getSizeAngle(child.getPositionX());
                    break;
                case Y:
                    offset = getSizeAngle(child.getPositionY());
                    break;
                case Z:
                    Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "Z not supported for Arch!");
                    break;
            }
        }
        return offset;
    }

    protected void updateTransform(Widget child, final Axis axis, float offset) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "updateTransform [%s], offset = [%f], axis = [%s]",
                child.getName(), offset, axis);

        if (Float.isNaN(offset)) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "Position is NaN for axis " + axis);
        } else {
            float factor = getFactor(axis);
            switch (axis) {
                case X:
                    child.setPositionX(0);
                    child.rotateByAxisWithPivot(-offset, 0, factor, 0,
                            0, 0, 0);
                    break;
                case Y:
                    child.setPositionY(0);
                    child.rotateByAxisWithPivot(-offset, factor, 0, 0,
                            0, 0, 0);
                    break;
                case Z:
                    super.updateTransform(child, axis, offset);
                    break;
            }
            child.onTransformChanged();
        }
    }

    @Override
    protected void resetChildLayout(final int dataIndex) {
        Widget child = mContainer.get(dataIndex);
        if (child != null) {
            child.setRotation(1, 0, 0, 0);
//        child.setPosition(0, 0, 0);
            updateTransform(child, Axis.Z, mRadius);
        }
    }

    private float mRadius = 0;
    private static final String TAG = ArchLayout.class.getSimpleName();
    private static final String pattern = "\nAL attributes====== orientation = %s  size [%s]";

}
