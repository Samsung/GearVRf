package org.gearvrf.widgetlib.widget.layout.basic;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.layout.CacheDataSet;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.OrientedLayout;

import java.util.List;

import static org.gearvrf.widgetlib.log.Log.SUBSYSTEM.LAYOUT;

/**
 * A Layout that arranges its children in a single column, a single row or single stack. The
 * direction of the row can be set by calling setOrientation(). The default orientation is
 * {@link Orientation#HORIZONTAL} The alignment of all items can be specified by calling
 * {@link LinearLayout#setGravity} or specify that the children fill up any remaining
 * space in the layout by setting gravity to {@link Gravity#FILL}.
 * The default gravity is {@link Gravity#CENTER}.
 *
 * The size of the layout determines the viewport size (virtual area used by the list rendering
 * engine) if {@link Layout#isClippingEnabled()} is true. Otherwise all items are rendered in the
 * list even if they occupy larger space than the container size is. The unlimited size can be
 * specified for the layout. For layout with unlimited size only gravity {@link Gravity#CENTER}
 * can be applied.
 */
public class LinearLayout extends OrientedLayout {

    /**
     * Gravity specifies how an layout should position its content along orientation axe, within its
     * own bounds. {@link Gravity#CENTER} is applied by default.
     * The gravity makes sense only if the layout content is not scrollable and all items can be
     * fitted into the container. If the container is defined with unlimited size along the
     * orientation axis, only {@link Gravity#CENTER} is supported.
     *
     * {@link Gravity#CENTER} Place the items in the center of the container in the vertical axis for
     * {@link Orientation#VERTICAL} and horizontal axis for {@link Orientation#HORIZONTAL}

     * {@link Gravity#LEFT} Push the items to the left of the container for
     * {@link Orientation#HORIZONTAL}. It is not supported for {@link Orientation#VERTICAL}
     *
     * {@link Gravity#RIGHT} Push the items to the right of the container for
     * {@link Orientation#HORIZONTAL}. It is not supported for {@link Orientation#VERTICAL}
     *
     * {@link Gravity#TOP} Push the items to the top of the container for
     * {@link Orientation#VERTICAL}. It is not supported for {@link Orientation#HORIZONTAL}
     *
     * {@link Gravity#BOTTOM} Push the items to the bottom of the container for
     * {@link Orientation#VERTICAL}. It is not supported for {@link Orientation#HORIZONTAL}
     *
     * {@link Gravity#FRONT} Push the items to the front of the container for
     * {@link Orientation#STACK}
     *
     * {@link Gravity#BACK} Push the items to the back of the container for {@link Orientation#STACK}
     *
     * {@link Gravity#FILL} Calculate the divider amount, so the items completely fill the container.
     * The items size will not be changed.
     */
    public enum Gravity {
        LEFT,
        RIGHT,
        CENTER,
        TOP,
        BOTTOM,
        FRONT,
        BACK,
        FILL
    }

    /**
     * Return the string representation of the LinearLayout
     */
    @Override
    public String toString() {
        return super.toString() + String.format(pattern, mGravity, mUniformSize);
    }

    /**
     * Core constructor for the LinearLayout
     */
    public LinearLayout() {
        super();
        initCache();
    }

    /**
     * When set to true, all items in layout will be considered having the size of the largest child. If false, all items are
     * measured normally. Disabled by default.
     * @param enable  true to measure children using the size of the largest child, false - otherwise.
     */
    public void enableUniformSize(final boolean enable) {
        if (mUniformSize != enable) {
            mUniformSize = enable;
            if (mContainer != null) {
                mContainer.onLayoutChanged(this);
            }
        }
    }

    /**
     * @return {@link Gravity} of the layout.
     */
    public Gravity getGravity() {
        Gravity internalGravity = getGravityInternal();
        if (internalGravity != mGravity) {
            Log.w(LAYOUT, TAG, "Gravity is not valid: %s, default gravity %s " +
                    "will be used for layout", mGravity, internalGravity);
        }
        return mGravity;
    }

    protected Gravity getGravityInternal() {
        return isValidLayout(mGravity, getOrientation()) ? mGravity : Gravity.CENTER;
    }

    /**
     * Set the {@link Gravity} of the layout.
     * The new gravity can be rejected if it is in conflict with the currently applied Orientation
     *
     * @param gravity
     *            One of the {@link Gravity} constants.
     */
    public void setGravity(final Gravity gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            if (mContainer != null) {
                mContainer.onLayoutChanged(this);
            }
        }
    }

    /**
     * Sets divider padding for axis. If axis does not match the orientation, it has no effect.
     * @param padding
     * @param axis {@link Axis}
     */
    public void setDividerPadding(float padding, final Axis axis) {
        if (axis == getOrientationAxis()) {
            super.setDividerPadding(padding, axis);
        } else {
            Log.w(TAG, "Cannot apply divider padding for wrong axis [%s], orientation = %s",
                  axis, getOrientation());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinearLayout)) return false;
        if (!super.equals(o)) return false;

        LinearLayout that = (LinearLayout) o;

        if (mUniformSize != that.mUniformSize) return false;
        return mGravity == that.mGravity;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mUniformSize ? 1 : 0);
        result = 31 * result + mGravity.hashCode();
        return result;
    }

    @Override
    public Layout clone() {
        return new LinearLayout(this);
    }


    @Override
    public void dump() {
        super.dump();
        dumpCaches();
    }

    @Override
    public void layoutChildren() {
        if (Log.isEnabled(LAYOUT)) {
            dumpCaches();
        }
        super.layoutChildren();
    }

    @Override
    public Widget measureChild(final int dataIndex, boolean calculateOffset) {
        return measureChild(dataIndex, calculateOffset, mCache);
    }

    @Override
    public int getCenterChild() {
        return getCenterChild(mCache);
    }

    @Override
    public float getDistanceToChild(int dataIndex, Axis axis) {
        return getDistanceToChild(dataIndex, axis, mCache);
    }

    @Override
    public float preMeasureNext(final List<Widget> measuredChildren,
                                final Axis axis, final Direction direction) {
        float totalSize = Float.NaN;
        int dataIndex = getNextDataId(axis, direction);

        if (dataIndex >= 0) {
            totalSize = getTotalSizeWithPadding(axis);

            Widget widget = measureChild(dataIndex);
            totalSize = (direction == Direction.BACKWARD ? 1 : -1) *
                    (getTotalSizeWithPadding(axis) - totalSize);
            if (widget != null && measuredChildren != null) {
                measuredChildren.add(widget);
            }
        }
        return totalSize;
    }

    @Override
    public Direction getDirectionToChild(final int dataIndex, final Axis axis) {
        Direction direction = Direction.NONE;
        int centerId = getCenterChild();
        if (axis == getOrientationAxis() && centerId != dataIndex &&
                dataIndex >= 0 && dataIndex < mContainer.size()) {
            direction = dataIndex > centerId ? Direction.FORWARD :
                    Direction.BACKWARD;
        }
        return direction;
    }

    @Override
    public void shiftBy(final float offset, final Axis axis) {
        super.shiftBy(offset, axis);
        if (mCache != null && !Float.isNaN(offset) && axis == getOrientationAxis()) {
            mCache.shiftBy(offset);
        }
    }

    @Override
    public boolean inViewPort(final int dataIndex) {
        return inViewPort(dataIndex, mCache);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        invalidateCache();
    }

    @Override
    public void invalidate(final int dataIndex) {
        Log.d(LAYOUT, TAG, "invalidate item [%d]", dataIndex);
        invalidateCache(dataIndex);
        super.invalidate(dataIndex);
    }

    protected LinearLayout(final LinearLayout rhs) {
        super(rhs);
        mGravity = rhs.mGravity;
        mUniformSize = rhs.mUniformSize;
    }

    /**
     * Check if the layout is unlimited along the orientation axe
     */
    private boolean isUnlimitedSize() {
        return mViewPort != null && mViewPort.get(getOrientationAxis()) == Float.POSITIVE_INFINITY;
    }

    /**
     * Check if the gravity and orientation are not in conflict one with other.
     * @param gravity
     * @param orientation
     * @return true if orientation and gravity can be applied together, false - otherwise
     */
    protected boolean isValidLayout(Gravity gravity, Orientation orientation) {
        boolean isValid = true;

        switch (gravity) {
            case TOP:
            case BOTTOM:
                isValid = (!isUnlimitedSize() && orientation == Orientation.VERTICAL);
                break;
            case LEFT:
            case RIGHT:
                isValid = (!isUnlimitedSize() && orientation == Orientation.HORIZONTAL);
                break;
            case FRONT:
            case BACK:
                isValid = (!isUnlimitedSize() && orientation == Orientation.STACK);
                break;
            case FILL:
                isValid = !isUnlimitedSize();
                break;
            case CENTER:
                break;
            default:
                isValid = false;
                break;
        }
        if (!isValid) {
            Log.w(TAG, "Cannot set the gravity %s and orientation %s - " +
                    "due to unlimited bounds or incompatibility", gravity, orientation);
        }
        return isValid;
    }

    /**
     * Calculate the layout offset
     */
    protected float getLayoutOffset() {
        //final int offsetSign = getOffsetSign();
        final float axisSize = getViewPortSize(getOrientationAxis());
        float layoutOffset = - axisSize / 2;
        Log.d(LAYOUT, TAG, "getLayoutOffset(): dimension: %5.2f, layoutOffset: %5.2f",
              axisSize, layoutOffset);

        return layoutOffset;
    }

    /**
     * Calculate the starting content offset based on the layout orientation and Gravity
     * @param totalSize total size occupied by the content
     */
    protected float getStartingOffset(final float totalSize) {
        final float axisSize = getViewPortSize(getOrientationAxis());
        float startingOffset = 0;

        switch (getGravityInternal()) {
            case LEFT:
            case FILL:
            case TOP:
            case FRONT:
                startingOffset = -axisSize / 2;
                break;
            case RIGHT:
            case BOTTOM:
            case BACK:
                startingOffset = (axisSize / 2 - totalSize);
                break;
            case CENTER:
                startingOffset = -totalSize / 2;
                break;
            default:
                Log.w(TAG, "Cannot calculate starting offset: " +
                        "gravity %s is not supported!", mGravity);
                break;

        }

        Log.d(LAYOUT, TAG, "getStartingOffset(): totalSize: %5.4f, dimension: %5.4f, startingOffset: %5.4f",
              totalSize, axisSize, startingOffset);

        return startingOffset;
    }

    /**
     * Get the total number of records in the cache data set(s)
     * @return
     */
    protected int getCacheCount() {
        return mCache.count();
    }

    @Override
    protected float getDataOffset(final int dataIndex) {
        return mCache.getDataOffset(dataIndex);
    }

    protected void dumpCaches() {
        if (mCache != null) {
            mCache.dump();
        }
    }

    @Override
    protected float getMeasuredChildSizeWithPadding(final int dataIndex, final Axis axis) {
        return axis != getOrientationAxis() ? Float.NaN :
            getMeasuredChildSizeWithPadding(dataIndex, mCache);
    }

    @Override
    protected float getTotalSizeWithPadding(final Axis axis) {
        return axis != getOrientationAxis() ? Float.NaN :
                getTotalSizeWithPadding(mCache);
    }

    protected float getTotalSizeWithPadding(CacheDataSet cache) {
        return cache.getTotalSizeWithPadding();
    }

    protected float getMeasuredChildSizeWithPadding(final int dataIndex, CacheDataSet cache) {
        return cache.getSizeWithPadding(dataIndex);
    }

    protected int getNextDataId(final Axis axis, final Direction direction) {
        int dataIndex = -1;

        if (axis == getOrientationAxis()) {
            switch(direction) {
                case BACKWARD: {
                    dataIndex = getCacheCount() == 0 ? 0 :
                        getFirstDataIndex() - 1;
                    break;
                }
                case FORWARD: {
                    dataIndex = getCacheCount() == 0 ? 0 :
                        getLastDataIndex() + 1;
                    if (dataIndex >= mContainer.size()) {
                        dataIndex = -1;
                    }
                    break;
                }
                case NONE:
                    break;
            }
            Log.d(LAYOUT, TAG, "dataIndex = %d mCache.count() = %d",
                      dataIndex, getCacheCount());

        }
        return dataIndex;
    }


    protected int getFirstDataIndex() {
        return mCache.getId(0);
    }

    protected int getLastDataIndex() {
        return mCache.getId(getCacheCount() - 1);
    }

    @Override
    protected boolean postMeasurement() {
        return postMeasurement(mCache);
    }

    protected float getDistanceToChild(int dataIndex, Axis axis, CacheDataSet cache) {
        float distance = Float.NaN;

        if (axis == getOrientationAxis() && cache.contains(dataIndex)) {
            switch (getGravityInternal()) {
                case TOP:
                case LEFT:
                case FRONT:
                case FILL:
                    distance = getLayoutOffset() - cache.getStartDataOffset(dataIndex);
                    if (!mOuterPaddingEnabled) {
                        distance -= cache.getStartPadding(dataIndex);
                    }
                    break;
                case BOTTOM:
                case RIGHT:
                case BACK:
                    distance = -getLayoutOffset() - cache.getEndDataOffset(dataIndex);
                    if (!mOuterPaddingEnabled) {
                        distance += cache.getEndPadding(dataIndex);
                    }
                    break;
                case CENTER:
                    distance = -cache.getDataOffset(dataIndex);
                    break;
                default:
                    break;
            }
        }
        Log.d(LAYOUT, TAG, "getDistanceToChild dataIndex = %d distance = %f ",
                dataIndex, distance);
        return distance;
    }

    // <<<<<< measureUntilFull helper methods
    @Override
    protected boolean changeDirection(int currentIndex, int centerIndex, boolean inBounds) {
        boolean changed = false;
        if (getGravityInternal() == Gravity.CENTER &&
                currentIndex <= centerIndex &&
                currentIndex == 0 || !inBounds) {

            changed = true;
        }
        return changed;
    }

    @Override
    protected int getNextIndex(int currentIndex, int centerIndex, boolean changeDirection) {
        int sign = 1;
        int i  = currentIndex;
        switch (getGravityInternal()) {
            case TOP:
            case LEFT:
            case FRONT:
            case FILL:
                break;
            case BOTTOM:
            case RIGHT:
            case BACK:
                sign = -1;
                break;
            case CENTER:
                sign = i > centerIndex ? 1 : -1;
            default:
                break;
        }

        if (changeDirection) {
            i = centerIndex;
            sign *= -1;
        }

        i += sign;
        return i;
    }

    // >>>>>> measureUntilFull helper methods


    protected int getCenterChild(CacheDataSet cache) {
        if (cache.count() == 0)
            return -1;

        int id = cache.getId(0);
        switch (getGravityInternal()) {
            case TOP:
            case LEFT:
            case FRONT:
            case FILL:
                break;
            case BOTTOM:
            case RIGHT:
            case BACK:
                id = cache.getId(cache.count() - 1);
                break;
            case CENTER:
                int i = cache.count() / 2;
                while (i < cache.count() && i >= 0) {
                    id =  cache.getId(i);
                    if (cache.getStartDataOffset(id) <= 0) {
                        if (cache.getEndDataOffset(id) >= 0) {
                            break;
                        } else {
                            i++;
                        }
                    } else {
                        i--;
                    }
                }
                break;
            default:
                break;
        }

        Log.d(LAYOUT, TAG, "getCenterChild = %d ", id);
        return id;
    }

    protected Widget measureChild(final int dataIndex, boolean calculateOffset, CacheDataSet cache) {
        // measure and setup size for new item
        if (isChildMeasured(dataIndex)) {
            Log.w(TAG, "Item [%d] has been already measured!", dataIndex);
        } else {
            float size = getChildSize(dataIndex, getOrientationAxis());

            // add at the end by default
            int pos = cache.count();
            int firstIndex = getFirstDataIndex();
            if (firstIndex >= 0) {
                pos  =  (dataIndex < firstIndex ^ getOffsetSign() == 1 ) ? cache.count() : 0;
            } else {
                // pos in the middle  TODO: figure out why it does not work
                // pos = cache.searchPos(dataIndex);
            }

            Log.d(LAYOUT, TAG, "measureChild [%d] has been added at pos [%d]! cache.count() = %d size = %f",
                    dataIndex, pos, cache.count(), size);

            cache.addData(dataIndex, pos, size, getDivider() / 2, getDivider() / 2);
        }
        if (calculateOffset) {
            computeOffset(dataIndex, cache);
        }
        return super.measureChild(dataIndex, calculateOffset);
    }

    protected boolean postMeasurement(CacheDataSet cache) {
        // if uniform size feature is enabled - uniform size for all items in the cache
        // uniform size is supported for static dataset only.
        if (!mContainer.isDynamic() && mUniformSize) {
            cache.uniformSize();
        }

        if (getGravityInternal() == Gravity.FILL) {
            if (mViewPort.isClippingEnabled(getOrientationAxis()) &&
                    cache.getTotalSize() >= getViewPortSize(getOrientationAxis())) {
                // reset padding for all items if size of the data exceeds the view port
                cache.uniformPadding(0);
            } else {
                // if uniform padding feature is enabled - uniform padding for all items in the cache
                cache.uniformPadding(computeUniformPadding(cache));
            }
        }
        return computeOffset(cache);
    }

    /**
     * Compute the offset for the item in the layout cache
     * @return true if the item fits the container, false otherwise
     */
    protected boolean computeOffset(final int dataIndex, CacheDataSet cache) {
        float layoutOffset = getLayoutOffset();
        int pos = cache.getPos(dataIndex);
        float startDataOffset = Float.NaN;
        float endDataOffset = Float.NaN;
        if (pos > 0) {
            int id = cache.getId(pos - 1);
            if (id != -1) {
                startDataOffset = cache.getEndDataOffset(id);
                if (!Float.isNaN(startDataOffset)) {
                    endDataOffset = cache.setDataAfter(dataIndex, startDataOffset);
                }
            }
        } else if (pos == 0) {
            int id = cache.getId(pos + 1);
            if (id != -1) {
                endDataOffset = cache.getStartDataOffset(id);
                if (!Float.isNaN(endDataOffset)) {
                    startDataOffset = cache.setDataBefore(dataIndex, endDataOffset);
                }
            } else {
                startDataOffset = getStartingOffset((cache.getTotalSizeWithPadding()));
                endDataOffset = cache.setDataAfter(dataIndex, startDataOffset);
            }
        }

        Log.d(LAYOUT, TAG, "computeOffset [%d, %d]: startDataOffset = %f endDataOffset = %f",
                dataIndex, pos, startDataOffset, endDataOffset);

        boolean inBounds = !Float.isNaN(cache.getDataOffset(dataIndex)) &&
                endDataOffset > layoutOffset &&
                startDataOffset < -layoutOffset;

        return inBounds;
    }

    /**
     * Compute the offset for the item in the layout based on the offsets of neighbors
     * in the layout. The other offsets are not patched. If neighbors offsets have not
     * been computed the offset of the item will not be set.
     * @return true if the item fits the container, false otherwise
     */
    protected boolean computeOffset(CacheDataSet cache) {
        // offset computation: update offset for all items in the cache
        float startDataOffset = getStartingOffset((cache.getTotalSizeWithPadding()));
        float layoutOffset = getLayoutOffset();

        boolean inBounds = startDataOffset < -layoutOffset;

        for (int pos = 0; pos < cache.count(); ++pos) {
            int id = cache.getId(pos);
            if (id != -1) {
                float endDataOffset = cache.setDataAfter(id, startDataOffset);
                inBounds = inBounds &&
                        endDataOffset > layoutOffset &&
                        startDataOffset < -layoutOffset;
                startDataOffset = endDataOffset;
                Log.d(LAYOUT, TAG, "computeOffset [%d] = %f" , id, cache.getDataOffset(id));
            }
        }

        return inBounds;
    }

    protected boolean inViewPort(final int dataIndex, CacheDataSet cache) {
        float startData = cache.getStartDataOffset(dataIndex) + cache.getStartPadding(dataIndex);
        float endData = cache.getEndDataOffset(dataIndex) - cache.getEndPadding(dataIndex);

        float layoutOffset = getLayoutOffset();


        boolean inViewport = (endData > layoutOffset &&
                startData < -layoutOffset);

        Log.d(LAYOUT, TAG, "inViewPort [%d] = %b data[%f, %f] layout [%f, %f]",
                dataIndex, inViewport, startData, endData, layoutOffset, -layoutOffset);
        return inViewport;
    }

    /**
     * Compute the proportional padding for all items in the cache
     * @param cache Cache data set
     * @return the uniform padding amount
     */
    protected float computeUniformPadding(final CacheDataSet cache) {
        float axisSize = getViewPortSize(getOrientationAxis());
        float totalPadding = axisSize - cache.getTotalSize();
        float uniformPadding = totalPadding > 0 && cache.count() > 1 ?
                totalPadding / (cache.count() - 1)  : 0;
        return uniformPadding;
    }

    /**
     * Calculate the padding between the items in the layout. It depends on the layout settings
     * @return  padding
     */
    protected float getDivider() {
        return getGravityInternal() == Gravity.FILL ? 0 :
            getDividerPadding(getOrientationAxis());
    }

    /**
     * @return the offset sign applied to the child positioning
     */
    protected int getOffsetSign() {
        return 1;
    }

    protected void invalidateCache() {
        mCache.invalidate();
        mCache.enableOuterPadding(mOuterPaddingEnabled);
    }

    protected void invalidateCache(final int dataIndex) {
        mCache.removeData(dataIndex);
    }

    @Override
    protected void resetChildLayout(final int dataIndex) {
        Widget child = mContainer.get(dataIndex);
        if (child != null) {
            child.setPosition(0, 0, 0);
        }
    }

    /**
     * Initialize cache data set
     */
    protected void initCache() {
        mCache = new LinearCacheDataSet(mOuterPaddingEnabled);
    }

    protected LinearCacheDataSet mCache;
    protected boolean mUniformSize;
    private Gravity mGravity = Gravity.CENTER;
    protected static final String TAG = LinearLayout.class.getSimpleName();

    private static final String pattern = "\nLL attributes====== gravity = %s " +
            "uniformSize = %b";

}
