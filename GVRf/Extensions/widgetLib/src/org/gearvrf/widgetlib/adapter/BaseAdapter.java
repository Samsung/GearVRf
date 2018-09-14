package org.gearvrf.widgetlib.adapter;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

public abstract class BaseAdapter implements Adapter {
    /**
     * Notifies the attached observers that the underlying data has been changed
     * and any View reflecting the data set should refresh itself.
     */
    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    /**
     * Notifies the attached observers that the underlying data is no longer
     * valid or available. Once invoked this adapter is no longer valid and
     * should not report further data set changes.
     */
    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean hasUniformViewSize() { return false; }

    @Override
    public float getUniformWidth() {
        return Float.NaN;
    }

    @Override
    public float getUniformHeight() {
        return Float.NaN;
    }

    @Override
    public float getUniformDepth() {
        return Float.NaN;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public void unregisterAllDataSetObservers() {
        mDataSetObservable.unregisterAll();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    private final DataSetObservable mDataSetObservable = new DataSetObservable();
}
