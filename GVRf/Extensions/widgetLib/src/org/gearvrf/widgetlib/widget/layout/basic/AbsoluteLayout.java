package org.gearvrf.widgetlib.widget.layout.basic;

import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.layout.Layout;

import java.util.List;

/**
 * A layout that leaves Widgets exactly where they are placed. AbsoluteLayout lets you specify
 * exact locations (x/y/z coordinates) of its children.
 */
public class AbsoluteLayout extends Layout {
    /**
     * Core constructor for AbsoluteLayout
     */
    public AbsoluteLayout() {
        super();
    }

    @Override
    public boolean inViewPort(final int dataIndex) {
        return true;
    }

    @Override
    public int getCenterChild() {
        return 0;
    }

    @Override
    public Direction getDirectionToChild(int dataIndex, Axis axis) {
        return Direction.NONE;
    }

    @Override
    public float getDistanceToChild(int dataIndex, Axis axis) {
        return 0;
    }

    @Override
    public float preMeasureNext(final List<Widget> measuredChildren,
                                   final Axis axis, final Direction direction) {
        return 0;
    }

    @Override
    public Layout clone() {
        return new AbsoluteLayout(this);
    }

    @Override
    public float calculateWidth(int[] children) { return 0; }

    @Override
    public float calculateHeight(int[] children) { return 0; }

    @Override
    public float calculateDepth(int[] children) { return 0; }

    protected AbsoluteLayout(final AbsoluteLayout rhs) {
        super(rhs);
    }

    @Override
    protected float getMeasuredChildSizeWithPadding(final int dataIndex, final Axis axis) {
        return getChildSize(dataIndex, axis) + getDividerPadding(axis);
    }

    protected float getTotalSizeWithPadding(final Axis axis) {
        return 0;
    }

    @Override
    protected boolean postMeasurement() {
        return true;
    }

    @Override
    protected void resetChildLayout(final int dataIndex) {
    }
}
