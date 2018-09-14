package org.gearvrf.widgetlib.adapter;

import android.database.DataSetObserver;

import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.Widget;

/**
 * An version of {@link android.widget.Adapter} adapted for use with
 * {@link Widget} as the "view".
 */
public interface Adapter {
    /**
     * How many items are in the data set represented by this Adapter
     *
     * @return Count of items.
     */
    int getCount();

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position
     *            Position of the item whose data we want within the adapter's
     *            data set.
     * @return The data at the specified position.
     */
    Object getItem(int position);

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position
     *            The position of the item within the adapter's data set whose
     *            row id we want.
     * @return The id of the item at the specified position.
     */
    long getItemId(int position);

    /**
     * Get the type of view that will be created by
     * {@link #getView(int, Widget, GroupWidget)} for the specified
     * item.
     *
     * @param position
     *            The position of the item within the adapter's data set whose
     *            view type we want.
     * @return An integer representing the type of view. Two views should share
     *         the same type if one can be converted to the other in
     *         {@link #getView(int, Widget, GroupWidget)}. Note:
     *         Integers must be in the range 0 to getViewTypeCount() - 1.
     *         {@link android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE} can also be
     *         returned.
     */
    int getItemViewType(int position);

    /**
     * Get a {@link Widget} that displays the data at the specified position in
     * the data set.
     *
     * @param position
     *            The position of the item within the adapter's data set of the
     *            item whose view we want.
     * @param convertView
     *            The old view to reuse, if possible. Note: You should check
     *            that this view is non-null and of an appropriate type before
     *            using. If it is not possible to convert this view to display
     *            the correct data, this method can create a new view.
     *            Heterogeneous lists can specify their number of view types, so
     *            that this View is always of the right type (see
     *            {@link #getViewTypeCount()} and {@link #getItemViewType(int)}
     *            ).
     * @param parent
     *            The parent that this view will eventually be attached to.
     * @return A Widget corresponding to the data at the specified position.
     */
    Widget getView(int position, Widget convertView, GroupWidget parent);

    /**
     * Returns the number of types of views that will be created by
     * {@link #getView(int, Widget, GroupWidget)}. Each type
     * represents a set of views that can be converted in
     * {@link #getView(int, Widget, GroupWidget)}. If the adapter
     * always returns the same type of View for all items, this method should
     * return 1.
     *
     * @return The number of types of views that will be created by this
     *         adapter.
     */
    int getViewTypeCount();

    /**
     * Indicates whether the item ids are stable across changes to the
     * underlying data.
     *
     * @return True if the same id always refers to the same object.
     */
    boolean hasStableIds();

    /**
     * Indicates whether all views have an uniform size
     *
     * @return True if view has an uniform size
     */
    boolean hasUniformViewSize();

    /**
     * Get the view width. In case of uniform size, it might be not necessary to create view to get
     * the width. Float.NAN can be returned if the width is unknown at that point.
     *
     * @return view width
     */
    float getUniformWidth();

    /**
     * Get the view height. In case of uniform size, it might be not necessary to create view to get
     * the height. Float.NAN can be returned if the height is unknown at that point.
     *
     * @return view height
     */
    float getUniformHeight();

    /**
     * Get the view depth. In case of uniform size, it might be not necessary to create view to get
     * the depth. Float.NAN can be returned if the depth is unknown at that point.
     *
     * @return view depth
     */
    float getUniformDepth();

    /**
     * @return True if this adapter doesn't contain any data.
     */
    boolean isEmpty();

    /**
     * Register an observer that is called when changes happen to the data used
     * by this adapter.
     *
     * @param observer
     *            the object that gets notified when the data set changes.
     */
    void registerDataSetObserver(DataSetObserver observer);

    /**
     * Unregister an observer that has previously been registered with this
     * adapter via {@link #registerDataSetObserver(DataSetObserver)}.
     *
     * @param observer
     *            the object to unregister.
     */
    void unregisterDataSetObserver(DataSetObserver observer);

    /**
     * Unregister all observers that have previously been registered with this
     * adapter via {@link #registerDataSetObserver(DataSetObserver)}.
     */
    void unregisterAllDataSetObservers();

}
