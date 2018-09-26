package org.gearvrf.widgetlib.widget.layout;

/**
 * Basic Cache data set interface to manage set of data.
 * Each record is identified by id. It is permanently assigned id and it cannot be changed.
 * Each record has a specific position in the set. Position might be changed if some data
 * rearrangement happened: adding/removing records to/from set.
 */
public interface CacheDataSet {
    /**
     * Cache invalidation operations
     */
    enum InvalidateOp {
        /**
         * Invalidate all cache data
         */
        ALL,
        /**
         * Invalidate data position
         */
        POSITION,
        /**
         * Invalidate data offset
         */
        OFFSET,
        /**
         * Invalidate data size
         */
        SIZE,
        /**
         * Invalidate data padding
         */
        PADDING
    };

    /**
     * Gets number of data records in Cache set
     * @return number of records
     */
    int count();

    /**
     * Invalidate all Cache data
     */
    void invalidate();

    /**
     * Invalidate cache data with specific {@link InvalidateOp}
     * @params op invalidate operation
     */
    void invalidate(InvalidateOp op);

    /**
     * Copies all data cache set records to another set
     * @param to destination data set
     */
    void copyTo(CacheDataSet to);

    /**
     * Gets total size occupied by data, padding is not counting
     * @return
     */
    float getTotalSize();

    /**
     * dumping the data for debug
     */
    void dump();

    /**
     * Gets total size occupied by data, including padding
     * @return
     */
    float getTotalSizeWithPadding();

    /**
     * Shift all data by specified amount. Applied during the content scrolling
     * @param offset shift offset
     */
    void shiftBy(final float offset);

    /**
     * Applies uniform size
     * @return uniform size based on total size and number of records
     */
    float uniformSize();

    /**
     * Applies uniform padding to all records
     * @param uniformPadding
     * @return
     */
    float uniformPadding(final float uniformPadding);

    /**
     * Creates new data record and adds it to the set
     * @param id
     * @param pos
     * @param size
     * @param startPadding
     * @param endPadding
     * @return
     */
    float addData(final int id, final int pos,
            final float size, final float startPadding, final float endPadding);

    /**
     * Checks if set contains the record with specified id
     * @param id
     * @return
     */
    boolean contains(final int id);

    /**
     * Removes data record from the set
     * @param id
     */
    void removeData(final int id);

    /**
     * Gets data offset for data record by id
     * @param id
     * @return
     */
    float getDataOffset(final int id);

    /**
     * Gets size, including padding for the data record by id
     * @param id
     * @return
     */
    float getSizeWithPadding(final int id);

    /**
     * Gets start offset for the data record by id
     * @param id
     * @return
     */
    float getStartDataOffset(final int id);

    /**
     * Gets end offset for the data record by id
     * @param id
     * @return
     */
    float getEndDataOffset(final int id);

    /**
     * Gets start dapping for the data record by id
     * @param id
     * @return
     */
    float getStartPadding(final int id);

    /**
     * Gets end dapping for the data record by id
     * @param id
     * @return
     */
    float getEndPadding(final int id);

    /**
     * Calculates and sets the offset for the CacheData with specified id, positioning it right
     * before the alignment
     * @param id CacheData id
     * @param alignment CacheData has to be positioned right before that
     * @return CacheData start offset
     */
    float setDataBefore(final int id, float alignment);

    /**
     * Calculates and sets the offset for the CacheData with specified id, positioning it right
     * after the alignment
     * @param id CacheData id
     * @param alignment CacheData has to be positioned right before that
     * @return CacheData end offset
     */
    float setDataAfter(final int id, float alignment);

    /**
     * Gets data record id by position
     * @param pos
     * @return
     */
    int getId(final int pos);

    /**
     * Gets data record position in the set by id
     * @param id
     * @return
     */
    int getPos(final int id);
}