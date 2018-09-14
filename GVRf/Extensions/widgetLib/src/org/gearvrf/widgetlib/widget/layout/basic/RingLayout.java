package org.gearvrf.widgetlib.widget.layout.basic;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.Vector3Axis;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.LayoutHelpers;

/**
 * A specialized {@link LinearLayout} that lays its children out in an arc on
 * either the {@linkplain Orientation#HORIZONTAL horizontal} plane (the plane
 * lying on the x and z axes) or the {@linkplain Orientation#VERTICAL vertical}
 * plane (the plane lying on the x and y axes). The length of the arc and the
 * radius of the arc can be specified in the class constructor.
 *
 */
public class RingLayout extends LinearLayout {

    /**
     * Units to define the size . {@link Units#DEGREE} - define the angle in
     * degrees {@link Units#ARC_LENGTH} - defines the arc length
     */
    public enum Units {
        DEGREE, ARC_LENGTH
    }

    /**
     * Construct a new {@link RingLayout} with the radius. The size of the
     * container is calculated as the size of the scene object.
     *
     * @param radius
     *            - ring radius.
     */
    public RingLayout(float radius) {
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
     * Set the amount of padding between child objects. The actual padding can
     * be different from that if the {@link Gravity#FILL } is set. The divider
     * padding can be specified by either angle or length of the arch.
     *
     * @param padding
     * @param units
     *            {@link Units} units the padding is defined in
     */
    public void setDividerPadding(final float padding, final Units units, final Axis axis) {
        super.setDividerPadding(units == Units.ARC_LENGTH ?
                getSizeAngle(padding) : padding, axis);
    }

    @Override
    public String toString() {
        return super.toString() + String.format(pattern, mRadius);
    }

    protected RingLayout(final RingLayout rhs) {
        super(rhs);
        mRadius = rhs.mRadius;
    }

    @Override
    public Layout clone() {
        return new RingLayout(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RingLayout)) return false;
        if (!super.equals(o)) return false;

        RingLayout that = (RingLayout) o;

        return Float.compare(that.mRadius, mRadius) == 0;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mRadius != +0.0f ? Float.floatToIntBits(mRadius) : 0);
        return result;
    }

    /**
     * Calculate the angle by arc length
     *
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
     *
     * @param angle
     * @return arc length
     */
    protected float getSizeArcLength(float angle) {
        if (mRadius <= 0) {
            throw new IllegalArgumentException("mRadius is not specified!");
        }
        return angle == Float.MAX_VALUE ? Float.MAX_VALUE : LayoutHelpers
                .lengthOfArc(angle, mRadius);
    }

    @Override
    public void onLayoutApplied(final WidgetContainer container, final Vector3Axis viewPort) {
        super.onLayoutApplied(container,
                              new Vector3Axis(getSizeAngle(viewPort.x),
                                              getSizeAngle(viewPort.y),
                                              getSizeAngle(viewPort.z)));
    }

    @Override
    public float getChildSize(final int dataIndex, Axis axis) {
        final float segment = super.getChildSize(dataIndex, axis);
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getChildSize for %d segment = %f", dataIndex, segment);

        return getSizeAngle(segment);
    }

    @Override
    protected int getOffsetSign() {
        int sign = 1;
        switch (getOrientation()) {
            case VERTICAL:
                sign = -1;
                break;
            case HORIZONTAL:
            case STACK:
                sign = 1;
                break;
            default:
                Log.w(TAG, "Unsupported orientation %s", getOrientation());
                break;
        }

        return sign;
    }

    @Override
    public void layoutChild(final int dataIndex) {
        resetChildLayout(dataIndex);
        super.layoutChild(dataIndex);
    }

    @Override
    public void setOffset(float offset, final Axis axis) {
        Log.w(Log.SUBSYSTEM.LAYOUT, TAG, "Offset is not supported for RingLayout!");
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
                    child.rotateByAxisWithPivot(-offset, 0, factor, 0,
                            0, 0, 0);
                    break;
                case Y:
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
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "clearChildPosition %s", child);

            child.setRotation(1, 0, 0, 0);
            child.setPosition(0, 0, 0);
            updateTransform(child, Axis.Z, mRadius);
        }
    }

    private float mRadius = 0;
    private static final String pattern = "\nRL attributes======  radius = %f";
}
