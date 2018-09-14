package org.gearvrf.widgetlib.widget.custom;

import android.database.DataSetObserver;

import org.gearvrf.widgetlib.adapter.Adapter;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.ListWidget;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.LayoutScroller;

import org.gearvrf.GVRContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.gearvrf.utility.Log.tag;

/**
 * The list of the items combined into the multiple pages.
 *
 * The max number of the pages visible in the list can be set by
 * {@link MultiPageWidget#setMaxVisiblePageCount(int)}
 *
 * The custom page list layout can be applied by {@link MultiPageWidget#applyListLayout}
 *
 * The custom item layout (how the items positioned into the page) can be applied by
 * {@link MultiPageWidget#applyLayout}. If the page list is empty at the moment
 * the new item layout is applied, the item layout is stored in the list and it
 * is applied to the page as soon as new page is added to the list.
 *
 * The page list adapter can be either provided in constructor or set by
 * {@link MultiPageWidget#setListAdapter} The page list adapter has to construct
 * {@link ListWidget} type of the view
 *
 * The item adapter can be set by {@link MultiPageWidget#setAdapter}
 *
 */
public class MultiPageWidget extends ListWidget {
    /**
     * Construct a new {@code MultiPageWidget} instance
     * @param context
     * @param pageAdapter  {@link Adapter} for the pages. {@link Adapter#getView} should provide
     *                  {@link ListWidget}
     * @param width
     * @param height
     */
    public MultiPageWidget(GVRContext context, final Adapter pageAdapter,
                           float width, float height, int maxVisiblePageCount) {
        super(context, pageAdapter, width, height);
        setMaxVisiblePageCount(maxVisiblePageCount);
    }

    /**
     * Set the {@link Adapter} for the items presented into the pages. The list will
     * immediately attempt to load data from the adapter.
     * {@link Adapter#getView} is
     * guaranteed to be called from the GL thread.
     *
     * @param itemAdapter
     *            An adapter or {@code null} to clear the list.
     */
    @Override
    public void setAdapter(final Adapter itemAdapter) {
        onItemChanged(itemAdapter);
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        mItemObservers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        mItemObservers.remove(observer);
    }

    /**
     * The callback is called *after* the page list relayout (affected by page adapter change)
     * has been done. Add DataSetObserver listener directly to the page adapter if you are
     * interested in the callback immediately after {@link #onChanged()}.
     * @param observer
     */
    public void registerListDataSetObserver(final DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    /**
     * Unregister the observer for the list adapter
     * @param observer
     */
    public void unregisterListDataSetObserver(final DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean addOnItemTouchListener(OnItemTouchListener listener) {
        boolean added = mOnItemTouchListeners.add(listener);
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "addOnItemTouchListener listener %s added = %b",
                listener, added);
        return added;
    }

    @Override
    public boolean removeOnItemTouchListener(OnItemTouchListener listener) {
        boolean removed = mOnItemTouchListeners.remove(listener);
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "removeOnItemTouchListener listener %s removed = %b",
                listener, removed);
        return removed;
    }

    @Override
    public boolean addOnItemFocusListener(OnItemFocusListener listener) {
        return mOnItemFocusListeners.add(listener);
    }

    @Override
    public boolean removeOnItemFocusListener(OnItemFocusListener listener) {
        return mOnItemFocusListeners.remove(listener);
    }

    @Override
    public void clear() {
        List<Widget> views = getAllViews();
        for (Widget view: views) {
            ListWidget page = ((ListWidget)view);
            if (page != null) {
                ListOnChangedListener listener = mPagesListOnChangedListeners.get(page);
                page.removeListOnChangedListener(listener);
                page.clear();
            }
        }
        mPagesListOnChangedListeners.clear();
        super.clear();
    }

    /**
     * Set the max number of visible views in the list
     * It will automatically enable viewport flag {@link Layout#isClippingEnabled()}
     * The existing viewport set for ListWidget will be overridden  based on the viewCount
     * @param pageCount
     */
    public void setMaxVisiblePageCount(final int pageCount) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setMaxVisiblePageCount pageCount = %d mLayouts.size = %d",
                pageCount, mLayouts.size());
        if (mMaxVisiblePageCount != pageCount) {
            mMaxVisiblePageCount = pageCount;
            recalculateViewPort(mAdapter);
            requestLayout();
        }
    }

    /**
     * Get the max number of visible views in the list.
     * @return return maximum number of visible pages
     */
    public int getMaxVisiblePageCount() {
        return mMaxVisiblePageCount;
    }

    /**
     * Apply the layout to the each page in the list
     * @param itemLayout item layout in the page
     * @return true if the new layout is applied successfully, otherwise - false
     */
    @Override
    public boolean applyLayout(Layout itemLayout) {
        boolean applied = false;
        if (itemLayout != null && mItemLayouts.add(itemLayout)) {

            // apply the layout to all visible pages
            List<Widget> views = getAllViews();
            for (Widget view: views) {
                view.applyLayout(itemLayout.clone());
            }
            applied = true;
        }
        return applied;
    }

    /**
     * Apply the layout to the page list
     * @param listLayout page list layout
     * @return true if the new list layout is applied successfully, otherwise - false
     */
    public boolean applyListLayout(Layout listLayout) {
        return super.applyLayout(listLayout);
    }

    /**
     * Remove the item layout {@link Layout} from the chain
     * @param itemLayout {@link Layout} item layout
     * @return true if layout has been removed successfully , false - otherwise
     */
    public boolean removeLayout(final Layout itemLayout) {
        boolean removed = false;
        if (itemLayout != null && mItemLayouts.remove(itemLayout)) {
            // remove the layout from all visible pages
            List<Widget>  views = getAllViews();
            for (Widget view: views) {
                view.removeLayout(itemLayout);
            }
            removed = true;
        }
        return removed;
    }

    /**
     * Remove the layout {@link Layout} from the chain
     * @param listLayout {@link Layout} page list layout
     * @return true if layout has been removed successfully , false - otherwise
     */
    public boolean removeListLayout(final Layout listLayout) {
        return super.removeLayout(listLayout);
    }

    @Override
    public void enableSelectOnTouch(boolean enable) {
        mSelectItemOnTouchEnabled = enable;
    }

    @Override
    public void enableMultiSelection(boolean enable) {
        if (isMultiSelectionEnabled() != enable) {
            super.enableMultiSelection(enable);
            for (Widget view : getAllViews()) {
                ListWidget page = (ListWidget) view;
                page.enableMultiSelection(enable);
            }
        }
    }

    @Override
    public boolean isSelectOnTouchEnabled() {
        return mSelectItemOnTouchEnabled;
    }

    @Override
    public boolean clearSelection(boolean requestLayout) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "clearSelection [%d]", mSelectedItemsList.size());

        boolean updateLayout = false;
        List<Widget>  views = getAllViews();

        for (Widget view: views) {
            ListWidget page = (ListWidget)view;
            updateLayout = page.clearSelection(requestLayout) || updateLayout;
        }
        if (updateLayout && requestLayout) {
            requestLayout();
        }
        clearSelectedItemsList();
        return updateLayout;
    }

    /**
     * Select or deselect an item at position {@code pos}.
     *
     * @param dataIndex
     *            item position in the adapter
     * @param select
     *            operation to perform select or deselect.
     * @return {@code true} if the requested operation is successful,
     *         {@code false} otherwise.
     */
    public boolean selectItem(int dataIndex, boolean select) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "selectItem [%d] select [%b]", dataIndex, select);
        if (dataIndex < 0 || dataIndex >= mItemAdapter.getCount()) {
            throw new IndexOutOfBoundsException("Selection index [" + dataIndex + "] is out of bounds!");
        }

        boolean done = updateSelectedItemsList(dataIndex, select);

        if (done) {
            List<Widget> views = new ArrayList<>();

            if (mItemsPerPage >= 0) {
                int pageIndex = dataIndex / mItemsPerPage;
                Widget view = getListView(pageIndex);
                if (view != null) {
                    views.add(view);
                }
            } else {
                views = getAllViews();
            }

            for (Widget view: views) {
                if (selectItem(((ListWidget) view), dataIndex, select)) {
                    requestLayout();
                    break;
                }
            }
        }

        return done;
    }

    @Override
    public Widget getView(int dataIndex) {
        Widget itemView = null;
        for (Widget view: getAllViews()) {
            ListWidget page = (ListWidget)view;
            SelectingAdapter adapter = ((SelectingAdapter) page.mAdapter);
            int localPosition = adapter.getLocalPosition(dataIndex);
            if (localPosition != -1) {
                itemView = page.getView(localPosition);
                break;
            }
        }
        return itemView;
    }

    /**
     * Check whether the item at position {@code pos} is selected.
     *
     * @param dataIndex
     *            item position in adapter
     * @return {@code true} if the item is selected, {@code false} otherwise.
     */
    public boolean isSelected(int dataIndex) {
        return mItemAdapter != null &&
                dataIndex < mItemAdapter.getCount() &&
                mSelectedItemsList.contains(dataIndex);
    }

    // default ScrollableList implementation should work with the items but not pages
    // getPageScrollable should be used to operate with pages

    @Override
    public int getScrollingItemsCount() {
        return mItemAdapter == null ? 0 : mItemAdapter.getCount();
    }

    @Override
    public float getViewPortWidth() {
        return mAdapter != null && mAdapter.hasUniformViewSize() ?
                mAdapter.getUniformWidth() : MultiPageWidget.super.getViewPortWidth();
    }

    @Override
    public float getViewPortHeight() {
        return mAdapter != null && mAdapter.hasUniformViewSize() ?
                mAdapter.getUniformHeight() : MultiPageWidget.super.getViewPortHeight();
    }

    @Override
    public float getViewPortDepth() {
        return mAdapter != null && mAdapter.hasUniformViewSize() ?
                mAdapter.getUniformDepth() : MultiPageWidget.super.getViewPortDepth();
    }

    @Override
    public boolean scrollToPosition(int pos, final LayoutScroller.OnScrollListener listener) {
        // needs to be reimplemented to work with items not pages
        return  MultiPageWidget.super.scrollToPosition(pos, listener);
    }

    @Override
    public boolean scrollByOffset(float xOffset, float yOffset, float zOffset,
                                  final LayoutScroller.OnScrollListener listener) {
        // needs to be reimplemented to work with items not pages
        return  MultiPageWidget.super.scrollByOffset(xOffset, yOffset, zOffset, listener);
    }

    /**
     *  Provides the scrollableList implementation for page scrolling
     *  @return {@link LayoutScroller.ScrollableList} implementation, passed to {@link LayoutScroller}
     *  for the processing the scrolling
     */
    public LayoutScroller.ScrollableList getPageScrollable() {
        return new LayoutScroller.ScrollableList() {

            @Override
            public int getScrollingItemsCount() {
                return  MultiPageWidget.super.getScrollingItemsCount();
            }

            @Override
            public float getViewPortWidth() {
                return  MultiPageWidget.super.getViewPortWidth();
            }

            @Override
            public float getViewPortHeight() {
                return  MultiPageWidget.super.getViewPortHeight();
            }

            @Override
            public float getViewPortDepth() {
                return  MultiPageWidget.super.getViewPortDepth();
            }

            @Override
            public boolean scrollToPosition(int pos, final LayoutScroller.OnScrollListener listener) {
                return  MultiPageWidget.super.scrollToPosition(pos, listener);
            }

            @Override
            public boolean scrollByOffset(float xOffset, float yOffset, float zOffset,
                                          final LayoutScroller.OnScrollListener listener) {
                return  MultiPageWidget.super.scrollByOffset(xOffset, yOffset, zOffset, listener);
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {
                MultiPageWidget.super.registerDataSetObserver(observer);
            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {
                MultiPageWidget.super.unregisterDataSetObserver(observer);
            }

            @Override
            public int getCurrentPosition() {
                return MultiPageWidget.super.getCurrentPosition();
            }

        };
    }

    /**
     * Gets the page view by index
     * @param dataIndex page index in page adapter
     * @return
     */
    protected Widget getListView(int dataIndex) {
        return  super.getView(dataIndex);
    }

    /**
     * Sets the number of views fit one page. This method is called when the layout measurement is
     * finished for the first page. The MultiPageWidget expects the same number of widgets per each
     * page, that works good for data adapter with universal view size, see
     * {@link Adapter#hasUniformViewSize()}
     *
     * @param itemNum number of view per page
     */
    protected void setItemsPerPage(int itemNum) {
        mItemsPerPage = itemNum;
    }

    /**
     * Recalculate view port for the multi page widget based on new adapter.
     * Data adapter has to have universal view size, see {@link Adapter#hasUniformViewSize()}
     *
     * @param adapter
     */
    protected void recalculateViewPort(final Adapter adapter) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "recalculateViewPort mMaxVisiblePageCount = %d mAdapter =%s " +
                        "mAdapter.hasUniformViewSize() = %b",
                mMaxVisiblePageCount, adapter, (adapter != null ? adapter.hasUniformViewSize() : false));

        if (mMaxVisiblePageCount < Integer.MAX_VALUE && adapter != null && adapter.hasUniformViewSize()) {
            int num = Math.min(mMaxVisiblePageCount, adapter.getCount());
            int[] ids = new int[num];
            for (int i = 0; i < num; ++i) {
                ids[i] = i;
            }
            float width = 0, height = 0, depth = 0;
            for (Layout listLayout: mLayouts) {
                listLayout.enableClipping(true);
                float w = listLayout.calculateWidth(ids);
                if (!Float.isNaN(w)) {
                    width = Math.max(w, width);
                }
                float h = listLayout.calculateHeight(ids);
                if (!Float.isNaN(h)) {
                    height = Math.max(h, height);
                }
                float d = listLayout.calculateDepth(ids);
                if (!Float.isNaN(d)) {
                    depth = Math.max(d, depth);
                }
            }
//            Log.d(Log.SUBSYSTEM.LAYOUT,
            Log.d(TAG, "recalculateViewPort(%s) mMaxVisiblePageCount = %d [%f, %f, %f]",
                    getName(), mMaxVisiblePageCount, width, height, depth);

            setViewPortWidth(width);
            setViewPortHeight(height);
            setViewPortDepth(depth);
        }
    }

    @Override
    protected boolean setupView(Widget view, final int dataIndex) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getViewFromAdapter index = %d", dataIndex);

        super.setupView(view, dataIndex);
        ListWidget page = (ListWidget)view;
        for (Layout layout: mItemLayouts) {
            if (!page.hasLayout(layout)) {
                page.applyLayout(layout.clone());
            }
        }
        if (mItemsPerPage == -1) {
            ListOnChangedListener listener = new PageOnChangedListener(dataIndex);
            page.addListOnChangedListener(listener);
            mPagesListOnChangedListeners.put(page, listener);
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getViewFromAdapter index = %d registerOnChangeListener",
                    dataIndex);
        }

        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setupView: page %s, mSelectItemOnTouchEnabled = %b",
                page.getName(), mSelectItemOnTouchEnabled);
        setAdapter(page, dataIndex, mItemAdapter);
        return false;
    }

    @Override
    protected void doOnItemAdded(Widget item) {
        super.doOnItemAdded(item);

        ListWidget page = (ListWidget)item;

        page.addOnItemFocusListener(mInternalOnItemFocusListener);
        page.addOnItemTouchListener(mInternalOnItemTouchListener);
        page.enableMultiSelection(isMultiSelectionEnabled());
    }

    @Override
    protected void doOnItemRemoved(Widget item) {
        ListWidget page = (ListWidget)item;
        page.removeOnItemFocusListener(mInternalOnItemFocusListener);
        page.addOnItemTouchListener(mInternalOnItemTouchListener);
        super.doOnItemRemoved(item);
    }

    @Override
    protected void onRecycle(Widget view, int dataIndex) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onRecycle [%d] %s", dataIndex, view);
        if (view != null) {
            final ListWidget page = (ListWidget) view;
            setAdapter(page, dataIndex, null);

            ListOnChangedListener listener = mPagesListOnChangedListeners.get(page);
            if (listener != null) {
                page.removeListOnChangedListener(listener);
                mPagesListOnChangedListeners.remove(page);
            }
            page.clearSelectedItemsList();
        }

        super.onRecycle(view, dataIndex);
    }

    @Override
    protected void notifyOnInvalidated() {
        super.notifyOnInvalidated();
        if (mOnItemInvalidated) {
            for (DataSetObserver observer : mItemObservers) {
                observer.onInvalidated();
            }
            mOnItemInvalidated = false;
            mOnItemChanged = false;
        }
    }

    @Override
    protected void notifyOnChanged() {
        super.notifyOnChanged();
        if (mOnItemChanged) {
            for(DataSetObserver observer: mItemObservers) {
                observer.onChanged();
            }
            mOnItemChanged = false;
        }
    }

    private List<Integer> getLocalSelectedItemsList(SelectingAdapter adapter) {
        List<Integer> list = new ArrayList<>();
        for (int index: mSelectedItemsList) {
            int localIndex = adapter.getLocalPosition(index);
            if (localIndex >= 0) {
                list.add(localIndex);
            }
        }
        return list;
    }

    private DataSetObserver mInternalItemsObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mOnItemChanged = true;
            MultiPageWidget.this.onItemChanged(mItemAdapter);
        }

        @Override
        public void onInvalidated() {
            setItemsPerPage(-1);
            mOnItemInvalidated = true;
            MultiPageWidget.this.onItemChanged(mItemAdapter);
        }
    };

    private void onItemChanged(final Adapter adapter) {
        runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (adapter != mItemAdapter) {
                    if (mItemAdapter != null) {
                        try {
                            mItemAdapter.unregisterDataSetObserver(mInternalItemsObserver);
                        } catch (IllegalStateException e) {
                            Log.w(TAG, "onItemChanged(%s): internal observer not registered on adapter!", getName());
                        }
                        clear();
                        mOnItemInvalidated = true;
                        notifyOnInvalidated();
                    }
                    mItemAdapter = adapter;
                    if (mItemAdapter != null) {
                        mItemAdapter.registerDataSetObserver(mInternalItemsObserver);
                    }

                }
                MultiPageWidget.this.onChanged();
            }
        });
    }

    private void setAdapter(ListWidget page, final int pageIndex, final Adapter adapter) {
        if (adapter == null) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setAdapter page[%d] = %s adapter = %s",
                    pageIndex, page, adapter);

            page.setAdapter(null);
        } else if (page.mAdapter == null ||
                adapter != (((SelectingAdapter)page.mAdapter).mAdapter)) {

            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setAdapter page = %s adapter = %s",
                    page, adapter);
            SelectingAdapter pageAdapter = new SelectingAdapter(adapter);
            if (mItemsPerPage >= 0) {
                int start = pageIndex * mItemsPerPage;
                int length = Math.min(mItemsPerPage, adapter.getCount() - start);
                pageAdapter.setBounds(start, length);
                page.updateSelectedItemsList(getLocalSelectedItemsList(pageAdapter), true);
            }
            page.setAdapter(pageAdapter);
        }
    }

    /**
     * Set the {@link Adapter} for the page list. The list will
     * immediately attempt to load data from the adapter.
     * {@link Adapter#getView} is
     * guaranteed to be called from the GL thread.
     * {@link Adapter#getView} should provide {@link ListWidget}
     *
     * @param listAdapter
     *            An adapter or {@code null} to clear the list.
     */
    private void setListAdapter(final Adapter listAdapter) {
        super.setAdapter(listAdapter);
    }

    private class PageOnChangedListener implements ListOnChangedListener {
        private final int mPageIndex;

        PageOnChangedListener(int index) {
            mPageIndex = index;
        }

        @Override
        public void onChangedStart(ListWidget list) {
            if (list.mAdapter != null) {
                SelectingAdapter adapter = ((SelectingAdapter) list.mAdapter);
                int start = 0;
                if (mPageIndex > 0) {
                    ListWidget prevPage = (ListWidget)getItem(mPageIndex - 1);
                    start = prevPage.getAllViews().size();
                }
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onChangedStart list = %s , index = %d start = %d",
                        list, mPageIndex, start);
                adapter.setStart(start);
            } else {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onChangedStart list = %s , index = %d adapter is null ",
                        list, mPageIndex);
            }
        }

        @Override
        public void onChangedFinished(ListWidget list, int numOfMeasuredViews) {
            if (list.mAdapter != null && numOfMeasuredViews > 0) {
                SelectingAdapter adapter = ((SelectingAdapter) list.mAdapter);

                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onChangedFinished list = %s , index = %d end = %d",
                        list, mPageIndex, numOfMeasuredViews);
                adapter.setLength(numOfMeasuredViews);
                selectItems(list, getLocalSelectedItemsList(adapter), true);

                if (adapter.hasUniformViewSize() && mAdapter.hasUniformViewSize()) {
                    setItemsPerPage(numOfMeasuredViews);
                    Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onChangedFinished mItemsPerPage = %d", mItemsPerPage);
                }
            } else {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onChangedStart list = %s , index = %d adapter is null ",
                        list, mPageIndex);
            }
        }
    }

    /**
     * Page adapter using for picking data for one page only
     */
    private static class SelectingAdapter implements Adapter {
        private final static String TAG = tag(SelectingAdapter.class);

        private final Adapter mAdapter;
        private int mStart, mEnd;

        // [0, adapter.getCount() - 1]
        SelectingAdapter(Adapter adapter) {
            this(adapter, 0, adapter.getCount() - 1);
        }

        // [start, end]
        SelectingAdapter(Adapter adapter,int start, int end) {
            mAdapter = adapter;
            mStart = Math.max(0, start);
            mEnd = Math.min(adapter.getCount() - 1, end);
        }

        void setBounds(int start, int length) {
            int end = start + length - 1;
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setBounds  old [%d, %d] new [%d, %d]",
                    mStart, mEnd, start, end);
            mStart = start;
            mEnd = end;
        }

        void setStart(int start) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setStart  old [%d, %d] new [%d, %d]",
                    mStart, mEnd, start, mEnd);
            mStart = start;
        }

        void setLength(int length) {
            int end = mStart + length - 1;
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setLength  old [%d, %d] new [%d, %d]",
                    mStart, mEnd, mStart, end);
            mEnd = end;
        }

        private int getGlobalPosition(int position) {
            return position < 0 || position >= getCount() ?
                    -1 : mStart + position;
        }

        private int getLocalPosition(int position) {
            return containsGlobalPosition(position) ? position - mStart : -1;
        }

        private boolean containsGlobalPosition(int dataIndex) {
            return dataIndex >= mStart && dataIndex <= mEnd;
        }

        @Override
        public int getCount() {
            return mEnd - mStart + 1;
        }

        @Override
        public Object getItem(int position) {
            return mAdapter.getItem(getGlobalPosition(position));
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(getGlobalPosition(position));
        }

        @Override
        public int getItemViewType(int position) {
            return mAdapter.getItemViewType(getGlobalPosition(position));
        }

        @Override
        public Widget getView(int position, Widget convertView, GroupWidget parent) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getView pos = %d, realPos = %d start = %d, end = %d",
                    position, getGlobalPosition(position), mStart, mEnd);
            return mAdapter.getView(getGlobalPosition(position), convertView, parent);
        }

        @Override
        public int getViewTypeCount() {
            return mAdapter.getViewTypeCount();
        }

        @Override
        public boolean hasStableIds() {
            return mAdapter.hasStableIds();
        }

        @Override
        public boolean hasUniformViewSize() {
            return mAdapter.hasUniformViewSize();
        }

        @Override
        public float getUniformWidth() {
            return mAdapter.getUniformWidth();
        }

        @Override
        public float getUniformHeight() {
            return mAdapter.getUniformHeight();
        }

        @Override
        public float getUniformDepth() {
            return mAdapter.getUniformDepth();
        }

        @Override
        public boolean isEmpty() {
            return getCount() == 0;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            // mAdapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            // mAdapter.unregisterDataSetObserver(observer);
        }

        @Override
        public void unregisterAllDataSetObservers() {
            //mAdapter.unregisterAllDataSetObservers();
        }
    }

    private Map<ListWidget, ListOnChangedListener> mPagesListOnChangedListeners = new HashMap<>();

    private void selectItems(ListWidget page, List<Integer> dataIndexes, boolean select) {
        // select items in the page
        for (int dataIndex: dataIndexes) {
            page.selectItem(dataIndex, select);
        }
    }

    private boolean toggleItem(ListWidget page, int dataIndex) {
        SelectingAdapter adapter = ((SelectingAdapter) page.mAdapter);
        int globalPosition = adapter.getGlobalPosition(dataIndex);
        if (globalPosition < 0) {
            throw new IndexOutOfBoundsException("Selection index [" + dataIndex + "] is out of bounds!");
        }

        boolean select = !isSelected(globalPosition);

        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "selectItem dataIndex [%d] global [%d]  select [%b]",
                dataIndex, globalPosition, select);

        return updateSelectedItemsList(globalPosition, select) ?
                page.selectItem(dataIndex, select) : false;
    }

    private boolean selectItem(ListWidget page, int dataIndex, boolean select) {
        SelectingAdapter adapter = ((SelectingAdapter) page.mAdapter);
        int localPosition = adapter.getLocalPosition(dataIndex);
        return (localPosition >= 0)  ? page.selectItem(localPosition, select) : false;
    }

    private OnItemTouchListener mInternalOnItemTouchListener = new OnItemTouchListener() {
        public boolean onTouch(ListWidget list, int dataIndex) {

            if (isSelectOnTouchEnabled()) {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG,
                        "mSelectOnTouchListener[%s] for index = %d", list.getName(), dataIndex);
                toggleItem(list, dataIndex);
            }

            Set<OnItemTouchListener> copyList = new HashSet<>(mOnItemTouchListeners);
            SelectingAdapter adapter = ((SelectingAdapter) list.mAdapter);
            if (adapter != null) {
                int globalPosition = adapter.getGlobalPosition(dataIndex);

                for (OnItemTouchListener listener : copyList) {
                    listener.onTouch(MultiPageWidget.this, globalPosition);
                }
            }
            return true;
        };
    };

    private OnItemFocusListener mInternalOnItemFocusListener = new OnItemFocusListener() {
        public void onFocus(ListWidget list, boolean focused, int dataIndex) {
            Set<OnItemFocusListener> copyList = new HashSet<>(mOnItemFocusListeners);

            SelectingAdapter adapter = ((SelectingAdapter) list.mAdapter);
            int globalPosition = adapter.getGlobalPosition(dataIndex);
            for (OnItemFocusListener listener: copyList) {
                listener.onFocus(MultiPageWidget.this, focused, globalPosition);
            }

        }
        public void onLongFocus(ListWidget list, int dataIndex) {
            Set<OnItemFocusListener> copyList = new HashSet<>(mOnItemFocusListeners);

            SelectingAdapter adapter = ((SelectingAdapter) list.mAdapter);
            int globalPosition = adapter.getGlobalPosition(dataIndex);
            for (OnItemFocusListener listener: copyList) {
                listener.onLongFocus(MultiPageWidget.this, globalPosition);
            }

        }
    };

    /**
     * Adapter associated with the items in the pages
     */
    protected Adapter mItemAdapter;


    // use the separate flag for the item selection because the pages are not selectable
    private boolean mSelectItemOnTouchEnabled;

    private static final String TAG = tag(MultiPageWidget.class);
    private int mItemsPerPage = -1;

    /**
     * Keep tracking the item layouts in the page list. If the page list is empty at the moment
     * {@link MultiPageWidget#applyLayout is called, the item layout is stored in the list and it
     * is applied to the page as soon as the page is added to the list.
     */
    private final Set<Layout> mItemLayouts = new HashSet<>();
    private Set<OnItemTouchListener> mOnItemTouchListeners = new LinkedHashSet<>();
    private Set<DataSetObserver> mItemObservers = new HashSet<>();
    private Set<OnItemFocusListener> mOnItemFocusListeners = new LinkedHashSet<>();
    private boolean mOnItemChanged;
    private boolean mOnItemInvalidated;
    private int mMaxVisiblePageCount = Integer.MAX_VALUE;

}
