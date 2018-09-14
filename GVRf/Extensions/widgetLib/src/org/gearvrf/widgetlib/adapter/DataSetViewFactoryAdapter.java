package org.gearvrf.widgetlib.adapter;

import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.Widget;

public class DataSetViewFactoryAdapter<T extends Object> extends DataSetAdapter<T> {
    /**
     * Create {@link Adapter} with the specified data set and viewFactory associated with it.
     * @param dataSet {@link DataSet} associated with this adapter
     * @param viewFactory {@link AdapterViewFactory} associated with this adapter
     *
     */
    public DataSetViewFactoryAdapter(DataSet dataSet, AdapterViewFactory viewFactory) {
        super(dataSet);
        mViewFactory = viewFactory;
    }

    @Override
    public Widget getView(int position, Widget convertView, GroupWidget parent) {
        return mViewFactory.getView(this, position, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        return mViewFactory.getItemViewType(this, position);
    }

    @Override
    public int getViewTypeCount() {
        return mViewFactory.getViewTypeCount();
    }

    private final AdapterViewFactory mViewFactory;
}
