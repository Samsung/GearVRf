package org.gearvrf.widgetlib.adapter;

import android.database.DataSetObservable;

public abstract class DataSet<T> extends DataSetObservable {
    /**
     * How many items are in the data set represented by this Adapter
     *
     * @return Count of items.
     */
    public abstract int getCount();

    /**
     * Get the data item associated with the specified index in the data set.
     *
     * @param index
     *            The position of the item whose data we want within the data set.
     * @return The data at the specified index.
     */
    public abstract T getItem(int index);

    /**
     * Get the row id associated with the specified index in the data set.
     *
     * @param index
     *            The position of the item within the data set whose row id we want.
     * @return The id of the item at the specified index.
     */
    public abstract long getItemId(int index);

    /**
     * Indicates whether the item ids are stable across changes to the
     * underlying data.
     *
     * @return True if the same id always refers to the same object.
     */
    public abstract boolean hasStableIds();

    /**
     * @return True if this data set doesn't contain any data.
     */
    public boolean isEmpty() {
        return getCount() < 1;
    }
}
