package org.gearvrf.widgetlib.widget.layout;

import org.gearvrf.widgetlib.widget.layout.basic.AbsoluteLayout;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.Widget;

/**
 * A Layout that arranges its children oriented along x, y or z axis. The orientation can be
 * set by calling setOrientation(). The default orientation is {@link  Orientation#HORIZONTAL}
 */
public abstract class OrientedLayout extends AbsoluteLayout {

    /**
     * Orientation specifies if the items lay out along its local x-axis (
     * {@link Orientation#HORIZONTAL}) or it's local y-axis (
     * {@link Orientation#VERTICAL}) or it's local z-axis (
     * {@link Orientation#STACK}).
     * The default orientation is horizontal.
     */
    public enum Orientation {
        HORIZONTAL, VERTICAL, STACK
    }

    /**
     * Return the string representation of the LinearLayout
     */
    public String toString() {
        return super.toString() + String.format(pattern, mOrientation,
                mOuterPaddingEnabled);
    }

    /**
     * Core constructor for OrientedLayout with default parameters.
     */
    public OrientedLayout() {
        super();
    }

    /**
     * @return {@link Orientation} of the layout.
     */
    public Orientation getOrientation() {
        return mOrientation;
    }

    /**
     * Set the {@link Orientation} of the layout. The new orientation can be rejected if it is in conflict with the
     * currently applied Gravity
     *
     * @param orientation
     *            One of the {@link Orientation} constants.
     */
    public void setOrientation(final Orientation orientation) {
        if (orientation != mOrientation) {
            mOrientation = orientation;
            if (mContainer != null) {
                mContainer.onLayoutChanged(this);
            }
        }
    }

    /**
     * Get the axis along the orientation
     * @return
     */
    public Axis getOrientationAxis() {
        final Axis axis;
        switch(getOrientation()) {
            case HORIZONTAL:
                axis = Axis.X;
                break;
            case VERTICAL:
                axis = Axis.Y;
                break;
            case STACK:
                axis = Axis.Z;
                break;
            default:
                Log.w(TAG, "Unsupported orientation %s", mOrientation);
                axis = Axis.X;
                break;
        }
        return axis;
    }

    /**
     * Enable/disable leading and ending padding for group of the items
     * @param enable
     */
    public void enableOuterPadding(final boolean enable) {
        if (mOuterPaddingEnabled != enable) {
            mOuterPaddingEnabled = enable;
            if (mContainer != null) {
                mContainer.onLayoutChanged(this);
            }
        }
    }

    /**
     * Checks if the outer padding is enabled
     * @return true if the outer padding is enabled, otherwise - false
     */
    public boolean isOuterPaddingEnabled() {
        return mOuterPaddingEnabled;
    }

    @Override
    public void layoutChild(final int dataIndex) {
        super.layoutChild(dataIndex);
        Widget child = mContainer.get(dataIndex);
        if (child != null) {
            final float childOffset = getDataOffset(dataIndex);
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "positionChild [%d] %s : childOffset = [%f] factor: [%f] layout: %s",
                    dataIndex, child.getName(), childOffset, getFactor(getOrientationAxis()), this);

            updateTransform(child, getOrientationAxis(), childOffset + mOffset.get(getOrientationAxis()));
        } else {
            Log.w(TAG, "positionChild: child with dataIndex [%d] was not found in layout: %s",
                    dataIndex, this);
        }
    }

    @Override
    public float calculateWidth(int[] children) {
        return getSize(children, Axis.X);
    }

    @Override
    public float calculateHeight(int[] children) {
        return getSize(children, Axis.Y);
    }

    @Override
    public float calculateDepth(int[] children) {
        return getSize(children, Axis.Z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrientedLayout)) return false;
        if (!super.equals(o)) return false;

        OrientedLayout that = (OrientedLayout) o;

        return getOrientation() == that.getOrientation() &&
                mOuterPaddingEnabled == that.mOuterPaddingEnabled;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mOuterPaddingEnabled ? 1 : 0);
        result = 31 * result + mOrientation.hashCode();
        return result;
    }

    protected OrientedLayout(final OrientedLayout rhs) {
        super(rhs);
        mOrientation = rhs.mOrientation;
        mOuterPaddingEnabled = rhs.mOuterPaddingEnabled;
    }

    protected float getDataOffset(final int dataIndex) {
        return 0;
    }

    private Orientation mOrientation = Orientation.HORIZONTAL;
    protected boolean mOuterPaddingEnabled = false;

    private float getSize(int[] children, Axis axis) {
        boolean calculateMaxSize = getOrientationAxis() != axis;
        float size = 0;
        for (int i = 0; i < children.length ; ++i) {
            int child = children[i];
            float sizeWithPadding = getMeasuredChildSizeWithPadding(child, axis);
            if (Float.isNaN(sizeWithPadding)) {
                // child is not measured yet
                sizeWithPadding = getChildSize(child, axis);
                if (i > 0 || mOuterPaddingEnabled) {
                    sizeWithPadding += getDividerPadding(axis) / 2;
                }
                if (i < children.length - 1 || mOuterPaddingEnabled) {
                    sizeWithPadding += getDividerPadding(axis) / 2;
                }
            }
            if (Float.isNaN(sizeWithPadding)) {
                return Float.NaN;
            }

            if (calculateMaxSize) {
                size = Math.max(size, sizeWithPadding);
            } else {
                size += sizeWithPadding;
            }
        }
        return size;
    }

    private static final String TAG = OrientedLayout.class.getSimpleName();
    private static final String pattern = "\nOL attributes====== orientation = %s " +
            "outerPaddingEnabled [%b]";
}
