package org.gearvrf.widgetlib.adapter;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ListDataSet<T> extends DataSet<T> {
    public ListDataSet() {
        mList = new ArrayList<>();
    }

    public ListDataSet(@NonNull List<T> list) {
        setList(list);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int index) {
        return mList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return mList.isEmpty();
    }

    /**
     * Set new list data set
     * @param list new data set
     */
    public void setList(List<T> list) {
        if (list != mList) {
            mList = list;
            if (mList == null) {
                notifyInvalidated();
            } else {
                notifyChanged();
            }
        }
    }

    private List<T> mList;
}
