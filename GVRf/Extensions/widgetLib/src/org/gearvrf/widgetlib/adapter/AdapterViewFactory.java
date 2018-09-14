package org.gearvrf.widgetlib.adapter;

import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.Widget;

public interface AdapterViewFactory {

    /**
     * Get the type of view that will be created by
     * {@link #getView(Adapter, int, Widget, GroupWidget)} for the specified
     * item.
     *
     *
     * @param adapter The {@link Adapter} this factory is being used with.
     * @param position The position of the item within the adapter's data set whose
     *                 view type we want.
     * @return An integer representing the type of view. Two views should share
     * the same type if one can be converted to the other in
     * {@link #getView(Adapter, int, Widget, GroupWidget)}. Note:
     * Integers must be in the range 0 to getViewTypeCount() - 1.
     * {@link android.widget.Adapter#IGNORE_ITEM_VIEW_TYPE} can also be
     * returned.
     */
    int getItemViewType(Adapter adapter, int position);

    /**
     * Get a {@link Widget} that displays the data at the specified position in
     * the data set. Should only be called from GL thread.
     *
     *
     * @param adapter     The {@link Adapter} this factory is being used with.
     * @param position    The position of the item within the adapter's data set of the
     *                    item whose view we want.
     * @param convertView The old view to reuse, if possible. Note: You should check
     *                    that this view is non-null and of an appropriate type before
     *                    using. If it is not possible to convert this view to display
     *                    the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so
     *                    that this View is always of the right type (see
     *                    {@link #getViewTypeCount()} and {@link #getItemViewType(Adapter, int)}).
     * @param parent      The parent that this view will eventually be attached to.
     * @return A Widget corresponding to the data at the specified position.
     */
    Widget getView(Adapter adapter, int position, Widget convertView, GroupWidget parent);

    /**
     * Returns the number of types of views that will be created by
     * {@link #getView(Adapter, int, Widget, GroupWidget)}. Each type
     * represents a set of views that can be converted in
     * {@link #getView(Adapter, int, Widget, GroupWidget)}. If the adapter
     * always returns the same type of View for all items, this method should
     * return 1.
     *
     * @return The number of types of views that will be created by this
     * adapter.
     */
    int getViewTypeCount();

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
}
