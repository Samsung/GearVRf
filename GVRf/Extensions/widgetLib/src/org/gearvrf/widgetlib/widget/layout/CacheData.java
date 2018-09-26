package org.gearvrf.widgetlib.widget.layout;

/**
 * Widget representation in layout cache. This cache is one dimension. To store the data for
 * multi-dimension layout multiple caches have to be used, or cache has to be extended to support it.
 */

public class CacheData {
    /**
     * Creates CacheData with id
     * @param id cache data id. It is permanently assigned id and it cannot be changed
     */
    public CacheData(final int id) {
        mId = id;
    }

    /**
     * Copy-constructor for CacheData
     * @param data
     */
    public CacheData(final CacheData data) {
        mId = data.mId;
        mSize = data.mSize;
        mOffset = data.mOffset;
        mStartPadding = data.mStartPadding;
        mEndPadding = data.mEndPadding;
    }

    /**
     * Sets size of the item
     * @param size
     */
    public void setSize(final float size) {
        mSize = size;
    }

    /**
     * Gets size of the data
     * @return
     */
    public float getSize() {
        return mSize;
    }

    /**
     * Sets offset for the data. It's used for content scrolling
     * @param offset
     */
    public void setOffset(final float offset) {
        mOffset = offset;
    }

    /**
     * Gets the offset for the data used for content scrolling.
     * @return data offset
     */
    public float getOffset() {
        return mOffset;
    }

    /**
     * Sets start and end padding for the item
     * @param start start padding
     * @param end end padding
     */
    public void setPadding(final float start, final float end) {
        mStartPadding = start;
        mEndPadding = end;
    }

    /**
     * Gets start padding
     * @return
     */
    public float getStartPadding() {
        return mStartPadding;
    }

    /**
     * Gets end padding
     * @return
     */
    public float getEndPadding() {
        return mEndPadding;
    }

    /**
     * Return the string representation of the LinearLayout
     */
    public String toString() {
        return super.toString() + String.format(pattern, mId, mSize, mOffset, mStartPadding, mEndPadding);
    }

    protected float mSize;
    protected float mOffset;
    protected float mStartPadding;
    protected float mEndPadding;
    protected int mId;

    private static final String pattern = "id [%d] size [%f] offset [%f] startPadding [%f] endPadding [%f]";
}