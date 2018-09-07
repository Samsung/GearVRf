package org.gearvrf.widgetlib.widget;

import android.database.DataSetObserver;

import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.widget.animation.SimpleAnimationTracker;


import org.gearvrf.widgetlib.adapter.Adapter;
import org.gearvrf.widgetlib.log.Log;
import static org.gearvrf.widgetlib.main.Utility.equal;

import org.gearvrf.widgetlib.thread.FPSCounter;

import org.gearvrf.widgetlib.widget.animation.Animation;
import org.gearvrf.widgetlib.widget.animation.AnimationSet;
import org.gearvrf.widgetlib.widget.animation.Easing;

import org.gearvrf.widgetlib.widget.layout.basic.LinearLayout;
import org.gearvrf.widgetlib.widget.layout.basic.RingLayout;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.Layout.Axis;
import org.gearvrf.widgetlib.widget.layout.Layout.Direction;
import org.gearvrf.widgetlib.widget.layout.LayoutScroller;
import org.gearvrf.widgetlib.widget.layout.LayoutScroller.ScrollableList;
import org.gearvrf.widgetlib.widget.layout.basic.GridLayout;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optEnum;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optFloat;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optJSONObject;


import org.gearvrf.GVRContext;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The {@link ListWidget} is a {@link GroupWidget} extension that displays a list of scrollable items.
 * The list items are automatically inserted to the list using an Adapter that pulls content from a
 * source and converts each item result into a {@link Widget} that's placed into the list.
 * The {@link ListWidget} can apply {@link LinearLayout} or {@link RingLayout} or {@link GridLayout}
 *
 * The extended features for the {@link ListWidget} are
 * - data set can be updated dynamically
 * - item selection
 * - item focus listener
 * - scrolling
 */
public class ListWidget extends GroupWidget implements ScrollableList {
    /**
     * Interface definition for a callback to be invoked when an item in this {@link ListWidget}
     *  has been focused.
     */
    public interface OnItemFocusListener {

        /**
         * Callback method to be invoked when an item in this {@link ListWidget} has been focused.
         * @param list {@link ListWidget} instance
         * @param focused true if the item is focused, false - otherwise
         * @param dataIndex item position in the list
         */
        void onFocus(ListWidget list, boolean focused, int dataIndex);

        /**
         * Callback method to be invoked when the long focus occurred for the item in this {@link ListWidget} .
         * @param list {@link ListWidget} instance
         * @param dataIndex item position in the list
         */
        void onLongFocus(ListWidget list, int dataIndex);
    }

    /**
     * Add a listener for {@linkplain OnItemFocusListener#onFocus(boolean) focus}
     * and {@linkplain OnItemFocusListener#onLongFocus() long focus} notifications
     * for this object.
     *
     * @param listener
     *            An implementation of {@link OnItemFocusListener}.
     * @return {@code true} if the listener was successfully registered,
     *         {@code false} if the listener is already registered.
     */
    public boolean addOnItemFocusListener(final OnItemFocusListener listener) {
        return mItemFocusListeners.add(listener);
    }

    /**
     * Remove a previously {@linkplain #addOnItemFocusListener(OnItemFocusListener)
     * registered} focus notification {@linkplain OnItemFocusListener listener}.
     *
     * @param listener
     *            An implementation of {@link OnItemFocusListener}
     * @return {@code true} if the listener was successfully unregistered,
     *         {@code false} if the specified listener was not previously
     *         registered with this object.
     */
    public boolean removeOnItemFocusListener(final OnItemFocusListener listener) {
        return mItemFocusListeners.remove(listener);
    }

    /**
     * @return Whether the {@link ListWidget} allows its items to be focused.
     */
    public boolean getItemFocusEnabled() {
        return mItemFocusEnabled;
    }

    /**
     * Sets {@linkplain Widget#setFocusEnabled(boolean) focus enabled} (or
     * disabled) for all children of the {@link ListWidget} that were fetched from
     * the {@link Adapter}. If this is called with {@code false}, any new items
     * gotten from the {@code Adapter} will have {@code setFocusEnabled(false)}
     * called on them.
     * <p>
     * This is a convenience method only, and the current state of focus
     * enabling for each displayed item is not tracked in any way.
     * {@code Adapters} should ensure that they enable or disable focus as
     * appropriate for their views.
     *
     * @param enabled
     *            {@code True} to enable focus for all items, {@code false} to
     *            disable.
     */
    public void setItemFocusEnabled(boolean enabled) {
        if (enabled != mItemFocusEnabled) {
            mItemFocusEnabled = enabled;
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setItemFocusEnabled(%s): item focus enabled: %b",
                    getName(), enabled);

            for (Widget view: getAllViews()) {
                if (view != null) {
                    view.setFocusEnabled(enabled);
                } else {
                    Log.w(TAG, "setItemFocusEnabled(%s): Host has no view!", getName());
                }
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when an item in this {@link ListWidget}
     * has been touched.
     */
    public interface OnItemTouchListener {
        /**
         * Called when a list item is touched (tapped).
         *
         * @param list {@link ListWidget} instance
         * @param dataIndex target by touch event.
         *
         * @return {@code True} to indicate that no further processing of the
         *         touch event should take place; {@code false} to allow further
         *         processing.
         */
        boolean onTouch(ListWidget list, int dataIndex);
    }

    /**
     * Add a listener for {@linkplain OnItemTouchListener#onTouch} notification for this object.
     *
     * @param listener
     *            An implementation of {@link OnItemTouchListener}.
     * @return {@code true} if the listener was successfully registered,
     *         {@code false} if the listener is already registered.
     */
    public boolean addOnItemTouchListener(final OnItemTouchListener listener) {
        boolean added =  mItemTouchListeners.add(listener);
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "addOnItemTouchListener listener %s added = %b", listener, added);
        return added;
    }

    /**
     * Remove a previously added @linkplain #addOnItemTouchListener(OnItemTouchListener)}
     * registered} touch notification {@linkplain OnItemTouchListener listener}.
     *
     * @param listener
     *            An implementation of {@link OnItemTouchListener}
     * @return {@code true} if the listener was successfully unregistered,
     *         {@code false} if the specified listener was not previously
     *         registered with this object.
     */
    public boolean removeOnItemTouchListener(final OnItemTouchListener listener) {
        boolean removed = mItemTouchListeners.remove(listener);
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "removeOnItemTouchListener listener %s added = %b", listener, removed);
        return removed;
    }

    /**
     * @return Whether the {@link ListWidget} allows its items to be touchable.
     */
    public boolean getItemTouchable() {
        return mItemTouchable;
    }

    /**
     * Sets {@linkplain Widget#setTouchable(boolean) touch enabled} (or
     * disabled) for all children of the {@link ListWidget} that were fetched from
     * the {@link Adapter}. If this is called with {@code false}, any new items
     * gotten from the {@code Adapter} will have {@code setTouchable(false)}
     * called on them.
     * <p>
     * This is a convenience method only, and the current state of focus
     * enabling for each displayed item is not tracked in any way.
     * {@code Adapters} should ensure that they enable or disable touch as
     * appropriate for their views.
     *
     * @param enabled
     *            {@code True} to enable touch for all items, {@code false} to
     *            disable.
     */
    public void setItemTouchable(boolean enabled) {
        if (enabled != mItemTouchable) {
            mItemTouchable = enabled;
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "mItemTouchable(%s): item touch enabled: %b", getName(), enabled);

            for (Widget view: getAllViews()) {
                view.setTouchable(enabled);
            }
        }
    }

    /**
     * Construct a new {@code ListWidget} instance with defined properties. Adapter is not setup
     *
     * @param gvrContext
     *            The active {@link GVRContext}.
     * @param properties A structured set of properties for the {@code ListWidget} instance. See
     *                       {@code widget.json} for schema.
     */
    public ListWidget(final GVRContext gvrContext, final JSONObject properties) {
        super(gvrContext, properties);
        init(gvrContext, null);
    }

    /**
     * Construct a new {@code ListWidget} instance with  LinearLayout applied by default
     *
     * @param gvrContext
     *            The active {@link GVRContext}.
     * @param adapter  {@link Adapter} associated with this layout.
     * @param width
     * @param height
     */
    public ListWidget(final GVRContext gvrContext, final Adapter adapter, float width, float height) {
        super(gvrContext, width, height);
        init(gvrContext, adapter);
    }

    /**
     * Definition of properties for the {@code ListWidget}
     */
    public enum Properties { transition_animation, rate, easing }

    /**
     * Set the {@link Adapter} for the {@code ListWidget}. The list will
     * immediately attempt to load data from the adapter.
     *
     * @param adapter
     *            An adapter or {@code null} to clear the list.
     */
    public void setAdapter(final Adapter adapter) {
        onChanged(adapter);
    }

    /**
     * The callback is called *after* the list relayout (affected by adapter change) has been done.
     * Add DataSetObserver listener directly to the adapter if you are interested in the callback
     * immediately after {@link #onChanged()}.
     * @param observer
     */
    public void registerDataSetObserver(final DataSetObserver observer) {
        mObservers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        mObservers.remove(observer);
    }


    @Override
    public float getLayoutSize(final Layout.Axis axis) {
        return mContent.getLayoutSize(axis);
    }

    @Override
    public boolean applyLayout(final Layout layout) {
        boolean ret = layout == getDefaultLayout() ?
                super.applyLayout(layout) :
                mContent.applyLayout(layout);

        mContent.setViewPortWidth(getViewPortWidth());
        mContent.setViewPortHeight(getViewPortHeight());
        mContent.setViewPortDepth(getViewPortDepth());

        if (ret && mAdapter != null) {
            onChanged();
        }
        return ret;
    }

    @Override
    public boolean removeLayout(final Layout layout) {
        boolean ret = mContent.removeLayout(layout);
        if (ret && mAdapter != null) {
            onChanged();
        }
        return ret;
    }

    @Override
    public boolean hasLayout(Layout layout) {
        return mContent.hasLayout(layout);
    }

    /**
     * Listener for list changes
     */
    public interface ListOnChangedListener {
        /**
         * Called from {@link #onChanged()} before any relayout happens but after the data set
         * is refreshed.
         * @param list
         */
        void onChangedStart(ListWidget list);
        /**
         * Called from {@link ContentWidget#measureLayout(Layout)}} before actual relayout happens
         * but after all measurements are done
         * @param list
         * @param numOfMeasuredViews number of views in the list after the rearrangement.
         */
        void onChangedFinished(ListWidget list, int numOfMeasuredViews);
    }

    /**
     * Add {@link ListOnChangedListener listener}
     * @param listener
     */
    public void addListOnChangedListener(final ListOnChangedListener listener) {
        mOnChangedListeners.add(listener);
    }

    /**
     * Remove {@link ListOnChangedListener listener}
     * @param listener
     */
    public void removeListOnChangedListener(final ListOnChangedListener listener) {
        mOnChangedListeners.remove(listener);
    }

    @Override
    public boolean isTransitionAnimationEnabled() {
        return mContent.mEnableTransitionAnimation;
    }

    @Override
    public void enableTransitionAnimation(final boolean enable) {
        mContent.enableTransitionAnimation(enable);
    }

    /**
     * Get all views from the list content
     * @return list of views currently visible
     */
    public List<Widget> getAllViews() {
        List<Widget> views = new ArrayList<>();
        for (Widget child: mContent.getChildren()) {
            Widget item =  ((ListItemHostWidget) child).getGuest();
            if (item != null) {
                views.add(item);
            }
        }
        return views;
    }

    /**
     * Get view from the list content by {@linkplain Adapter#getItem(int) data set index}.  This may
     * not be the same as the {@linkplain GroupWidget#getChild(int) Widget child index}.
     * @return Child Widget if the data at {@code dataIndex} is currently visible in the
     * {@code ListWidget}, {@code null} if it is not.
     */
    public Widget getView(int dataIndex) {
        Widget view = null;
        for (Widget child: mContent.getChildren()) {
            ListItemHostWidget host = ((ListItemHostWidget) child);
            if (host.getDataIndex() == dataIndex) {
                view = host.getGuest();
                break;
            }
        }
        return view;
    }

    /**
     * Clear all views from the list
     */
    public void clear() {
        clearSelection(false);
        mContent.clear();
    }

    /**
     * Stop scrolling immediately; interrupting the scrolling animation
     */
    public void stopScrolling() {
        if (isScrolling()) {
            mScroller.stopScrolling();
        }
    }

    /**
     * Enable/disable multi-selection option
     * @param enable
     */
    public void enableMultiSelection(boolean enable) {
        if (enable != mMultiSelectionSupported) {
            clearSelection();
            mMultiSelectionSupported = enable;
        }
    }

    /**
     * Enable/disable select on touch
     * @param enable
     */
    public void enableSelectOnTouch(boolean enable) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "enableSelectOnTouch %s enable = %b", getName(), enable);
        mSelectOnTouchEnabled = enable;
    }

    /**
     * @return {@code true} if the multi-selection option is enabled
     *         {@code false} otherwise.
     */
    public boolean isMultiSelectionEnabled() {
        return mMultiSelectionSupported;
    }

    /**
     * @return {@code true} if the select on touch option is enabled
     *         {@code false} otherwise.
     */
    public boolean isSelectOnTouchEnabled() {
        return mSelectOnTouchEnabled;
    }


    /**
     * Clear the selection of all items.
     * @return {@code true} if at least one item was deselected,
     *         {@code false} otherwise.
     */
    public boolean clearSelection() {
        return clearSelection(true);
    }

    /**
     * Clear the selection of all items.
     * @param requestLayout request layout after clear selection if the flag is true, no layout
     *                      requested otherwise
     * @return {@code true} if at least one item was deselected,
     *         {@code false} otherwise.
     */
    public boolean clearSelection(boolean requestLayout) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "clearSelection [%d]", mSelectedItemsList.size());

        boolean updateLayout = false;
        List<ListItemHostWidget>  views = getAllHosts();
        for (ListItemHostWidget host: views) {
            if (host.isSelected()) {
                host.setSelected(false);
                updateLayout = true;
                if (requestLayout) {
                    host.requestLayout();
                }
            }
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
        if (dataIndex < 0 || dataIndex >= mContent.size()) {
            throw new IndexOutOfBoundsException("Selection index [" + dataIndex + "] is out of bounds!");
        }

        updateSelectedItemsList(dataIndex, select);

        ListItemHostWidget hostWidget = getHostView(dataIndex, false);
        if (hostWidget != null) {
            hostWidget.setSelected(select);
            hostWidget.requestLayout();
            return true;
        }

        return false;
    }

    /**
     * Reverse the selection state for the view
     * @param dataIndex
     * @return {@code true} if the requested operation is successful,
     *         {@code false} otherwise.
     */
    public boolean toggleItem(int dataIndex) {
        return selectItem(dataIndex, !isSelected(dataIndex));
    }

    /**
     * Clear the list of selected items
     */
    public void clearSelectedItemsList() {
        mSelectedItemsList.clear();
    }

    public boolean updateSelectedItemsList(List<Integer> dataIndexs, boolean select) {
        boolean updated = false;
        for (int dataIndex: dataIndexs) {
            updated = updateSelectedItemsList(dataIndex, select) || updated;
        }
        return updated;
    }

    /**
     * Update the selection state of the item
     * @param dataIndex data set index
     * @param select if it is true the item is marked as selected, otherwise - unselected
     * @return true if the selection state has been changed successfully, otherwise - false
     */
    public boolean updateSelectedItemsList(int dataIndex, boolean select) {
        boolean done = false;
        boolean contains =  isSelected(dataIndex);
        if (select) {
            if (!contains) {
                if (!mMultiSelectionSupported) {
                    clearSelection(false);
                }
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "updateSelectedItemsList add index = %d", dataIndex);
                mSelectedItemsList.add(dataIndex);
                done = true;
            }
        } else {
            if (contains) {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "updateSelectedItemsList remove index = %d", dataIndex);
                mSelectedItemsList.remove(dataIndex);
                done = true;
            }
        }
        return done;
    }

    /**
     * Check whether the item at position {@code pos} is selected.
     *
     * @param dataIndex
     *            item position in adapter
     * @return {@code true} if the item is selected, {@code false} otherwise.
     */
    public boolean isSelected(int dataIndex) {
        return dataIndex < mContent.size() && mSelectedItemsList.contains(dataIndex);
    }

    /**
     * @return get the list of selected views.
     */
    public Set<Integer> getSelectedItems() {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getSelectedItems: size = %d", mSelectedItemsList.size());
        for (int id: mSelectedItemsList) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "selected: <%d>", id);
        }

        return new HashSet<>(mSelectedItemsList);
    }

    //  =================== Scrolling <start> =========================

    @Override
    public boolean scrollToPosition(final int dataIndex,
                                    final LayoutScroller.OnScrollListener listener) {
        if (isScrolling()) {
            return false;
        }

        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToPosition, position = %d", dataIndex);

        boolean scrolled = false;
        if (dataIndex >= 0 && dataIndex < getScrollingItemsCount()) {
            onScrollImpl(dataIndex, listener);
            scrolled = true;
        } else {
            Log.w(Log.SUBSYSTEM.LAYOUT, TAG, "Scroll out of bounds pos = [%d] getDataCount() = [%d]",
                    dataIndex, getScrollingItemsCount());
        }
        return scrolled;
    }

    /**
     * Scroll all items in the {@link ListWidget} by {@code rotation} degrees}.
     *
     * @param xOffset
     * @param yOffset
     * @param zOffset
     *            The amount to scroll, in degrees.
     */
    @Override
    public boolean scrollByOffset(final float xOffset, final float yOffset, final float zOffset,
                                  final LayoutScroller.OnScrollListener listener) {
        if (isScrolling()) {
            return false;
        }

        Vector3Axis offset = new Vector3Axis(xOffset, yOffset, zOffset);
        if (offset.isInfinite() || offset.isNaN()) {
            Log.e(TAG, new IllegalArgumentException(),
                    "Invalid scrolling delta: %s", offset);
            return false;
        }
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollBy(%s): offset %s", getName(), offset);

        onScrollImpl(offset, listener);
        return true;
    }

    @Override
    public int getScrollingItemsCount() {
        return getDataCount();
    }

    @Override
    public int getCurrentPosition() {
        int pos = 0;
        for (Layout layout : mContent.mLayouts) {
            pos = layout.getCenterChild();
        }
        return pos;
    }

    /**
     * Get offset of the item along X
     * TODO: This feature is not implemented, we do not have plan to implemented it soonish.
     * TODO: It is in use in VRTop 1.0 only,  the interface has to be cleanup as soon as VRTop 1.0
     * TODO: will be moved out from Wonderwall project.
     *
     * @param position:  the position in the dataset.
     * @return  the x-offset of the view displaying the data at position;
     *          if that data is not being displayed, return Float.NaN.
     */
    public float getItemOffsetX(int position) {
        if (position < 0 || position >= getDataCount()) {
            return Float.NaN;
        }
        return Float.NaN;
    }

    //===================== Scrolling <end> =============================

    //  Scrolling processor
    private class ScrollingProcessor {
        private int mScrollToPosition = -1;
        private Vector3Axis mScrollByOffset = new Vector3Axis(Float.NaN, Float.NaN, Float.NaN);
        private SimpleAnimationTracker animationTracker = WidgetLib.getSimpleAnimationTracker();
        private boolean mScrolling = false;
        private LayoutScroller.OnScrollListener mListener;
        private boolean mForce = false;

        private class ScrollAnimation extends Animation {
            private final float mShiftBy;
            private float mShiftedBy;
            private final Layout mLayout;
            private final Axis mAxis;

            ScrollAnimation(Widget target, Layout layout, float shiftBy, Axis axis) {
                super(target, Math.abs(shiftBy/mAnimationRate));
                mShiftBy = shiftBy;
                mLayout = layout;
                mAxis = axis;
            }

            @Override
            public void animate(Widget target, float ratio) {
                FPSCounter.timeCheck("ScrollAnimation <ratio: " + ratio + "> " + target);
                float shifted  = mShiftedBy;
                mShiftedBy = ratio * mShiftBy;
                mLayout.shiftBy(mShiftedBy - shifted, mAxis);
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "animate: target <%s> shift = %f axis = %s", target.getName(), mShiftedBy, mAxis);

                switch(mAxis) {
                    case X:
                        target.translate(mShiftedBy - shifted, 0, 0);
                        break;
                    case Y:
                        target.translate(0, -(mShiftedBy - shifted), 0);
                        break;
                    case Z:
                        target.translate(0, 0, -(mShiftedBy - shifted));
                        break;
                }
            }
        }

        boolean doScrolling() {
            return mScrolling;
        }

        ScrollingProcessor(final int pos, final LayoutScroller.OnScrollListener listener) {
            mListener = listener;
            mScrollToPosition = pos;
        }

        ScrollingProcessor(final Vector3Axis offset, final LayoutScroller.OnScrollListener listener) {
            mListener = listener;
            mScrollByOffset = offset;
        }

        private float preMeasure(Layout layout, Axis axis, List<Widget> measuredChildren) {
            float offset = Float.NaN;
            Direction direction = Direction.NONE;
            if (mScrollByOffset.isNaN()) {
                direction = layout.getDirectionToChild(mScrollToPosition, axis);
            } else {
                float distance = mScrollByOffset.get(axis);
                if (!Float.isNaN(distance)) {
                    direction = distance < 0 ? Direction.FORWARD : Direction.BACKWARD;
                }
            }
            if (direction != Direction.NONE) {
                offset = layout.preMeasureNext(measuredChildren, axis, direction);
                // reached the end of list, just move to some amount
                if (Float.isNaN(offset)) {
                    if (mScrollByOffset.isNaN()) {
                        // calculate the current distance to scrolled position
                        offset = layout.getDistanceToChild(mScrollToPosition, axis);
                    } else {
                        // the offset should not exceed the distance between the currently centered
                        // item and the tail/head
                        if (direction == Direction.FORWARD) {
                           offset = Math.max(mScrollByOffset.get(axis),
                                   layout.getDistanceToChild(mContent.size() - 1, axis));
                        } else if (direction == Direction.BACKWARD) {
                            offset = Math.min(mScrollByOffset.get(axis),
                                    layout.getDistanceToChild(0, axis));
                        }
                    }
                }
            }
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "preMeasure direction = %s offset = %f axis = %s", direction, offset, axis);
            return offset;
        }

        private void startShifting(AnimationSet.Builder builder, Layout layout, float offset, Axis axis) {
            if (Float.isNaN(offset) || equal(offset, 0)) {
                return;
            }

            float distance = mScrollByOffset.isNaN() ?
                    layout.getDistanceToChild(mScrollToPosition, axis) :
                        mScrollByOffset.get(axis);
            float shiftBy = offset;

            if (!Float.isNaN(distance)) {
                if (offset < 0) {
                    shiftBy = Math.max(distance, offset);
                } else {
                    shiftBy = Math.min(distance, offset);
                }

                if (!mScrollByOffset.isNaN()) {
                    mScrollByOffset.set(distance - shiftBy, axis);
                }
            }

            if (!equal(shiftBy, 0)) {
                if (isTransitionAnimationEnabled()) {
                    ScrollAnimation animation = new ScrollAnimation(ListWidget.this.mContent, layout, shiftBy, axis);
                    builder.add(animation);
                } else {
                    layout.shiftBy(shiftBy, axis);
                    mContent.requestLayout();
                }
            }
        }

        void scroll() {

            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scroll() mScrollToPosition = %d mScrollByOffset = %s",
                  mScrollToPosition, mScrollByOffset);

            FPSCounter.timeCheck("scroll mScrollToPosition [" + mScrollToPosition + "] <START>");

            AnimationSet.Builder builder = new AnimationSet.Builder(ListWidget.this.mContent);
            if (!mScrolling) {
                WidgetLib.getMainThread().
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mListener != null) {
                                    mListener.onScrollStarted(getCurrentPosition());
                                }
                            }
                        });
            }
            mScrolling = true;


            //FPSCounter.startCheck("ScrollingAnimation");

            for (Layout layout: mContent.mLayouts) {
                // measure all directions. Finally measuredChildren has to contain all
                // views required for shifting toward the scrolling position.
                List<Widget> measuredChildren = new ArrayList<>();
                float xOffset = preMeasure(layout, Axis.X, measuredChildren);
                float yOffset = preMeasure(layout, Axis.Y, measuredChildren);
                float zOffset = preMeasure(layout, Axis.Z, measuredChildren);

                if (Float.isNaN(xOffset) && Float.isNaN(yOffset) && Float.isNaN(zOffset)) {
                    continue;
                }

                for (Widget view: measuredChildren) {
                    Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measured item: %s set in layout xOffset= %f yOffset= %f zOffset= %f",
                          view.getName(), xOffset, yOffset, zOffset);
                }

                startShifting(builder, layout, xOffset, Axis.X);
                startShifting(builder, layout, yOffset, Axis.Y);
                startShifting(builder, layout, zOffset, Axis.Z);
            }
            if (builder.isEmptySet()) {
                stopScrolling();
            } else {
                Animation animation = builder.build();
                animation.setInterpolator(mAnimationEasing);
                animation.track(animationTracker,
                        new Runnable() {
                            public void run() {
                                FPSCounter.startCheck("ScrollingAnimation");
                            }
                        },
                        new Animation.OnFinish() {
                            @Override
                            public final void finished(Animation animation) {
                                finish();
                                FPSCounter.stopCheck("ScrollingAnimation");
                            }
                        });
            }
            FPSCounter.timeCheck("scroll mScrollToPosition [" + mScrollToPosition + "] <END>");
        }

        void stopScrolling() {
            mForce = true;
            if (!animationTracker.interruptAll()) {
                finish();
            }
        }

        void finish() {
            if (!mScrolling) {
                return;
            }

            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "finish scrolling with force = %b", mForce);
            FPSCounter.timeCheck("finish mScrollToPosition [" + mScrollToPosition + "] <START>");

            int pos = mPreferableCenterPosition;
            for (Layout layout: mContent.mLayouts) {
                pos = layout.getCenterChild();
                if (!mForce) {
                    mForce = mScrollByOffset.isNaN() ?
                            pos == mScrollToPosition :

                            (equal(mScrollByOffset.get(Axis.X), 0) ||
                                    Float.isNaN(mScrollByOffset.get(Axis.X))) &&
                                    (equal(mScrollByOffset.get(Axis.Y), 0) ||
                                            Float.isNaN(mScrollByOffset.get(Axis.Y))) &&
                                    (equal(mScrollByOffset.get(Axis.Z), 0) ||
                                            Float.isNaN(mScrollByOffset.get(Axis.Z)));
                }
            }

            if (mForce) {
                mPreferableCenterPosition = pos;
                mTrimRequest = true;
                mScroller = null;
                mScrolling = false;
                mContent.requestLayout();
                WidgetLib.getMainThread().
                        runOnMainThread(new Runnable() {
                            @Override
                            public void run() {

                                if (mListener != null) {
                                    mListener.onScrollFinished(mPreferableCenterPosition);
                                }
                            }
                        });
            } else {
                scroll();
            }
            FPSCounter.timeCheck("finish mScrollToPosition [" + mScrollToPosition + "] <END>");
            mForce = false;
        }
    }

    protected boolean setupView(Widget view, final int dataIndex) {
        return isSelected(dataIndex);
    }

    /**
     * Set up the view at specified position in {@link Adapter}
     * @param dataIndex position in {@link Adapter} associated with this layout.
     * @return host view
     */
    private void setupHost(ListItemHostWidget host, Widget view, final int dataIndex) {
        boolean selected = setupView(view, dataIndex);
        host.setGuest(view, dataIndex);
        host.setSelected(selected);
        host.requestLayout();

        if (mContent.getChildren().contains(host)) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setupItem(%s): added item(%s) at dataIndex [%d]",
                    getName(), view.getName(), dataIndex);
        } else {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setupItem(%s): reuse item(%s) at dataIndex [%d]",
                    getName(), view.getName(), dataIndex);
        }
    }

    /**
     * Get view displays the data at the specified position in the {@link Adapter}
     * @param index - item index in {@link Adapter}
     * @param host - view using as a host for the adapter view
     * @return view displays the data at the specified position
     */
    protected Widget getViewFromAdapter(final int index, ListItemHostWidget host) {
        return mAdapter == null ? null : mAdapter.getView(index, host.getGuest(), host);
    }

    /**
     * Get the item id associated with the specified position in the {@link Adapter}.
     *
     * @param index
     *            The position of the item within the adapter's data set
     * @return The id of the item at the specified position.
     */
    protected long getItemId(final int index) {
        long id = -1;
        if (index < getDataCount() && index >= 0 && mAdapter != null) {
            id =  mAdapter.getItemId(index);
        }
        return id;
    }

    protected Widget getItem(int index) {
        Widget view = null;
        ListItemHostWidget host = mContent.get(index);
        if (host != null) {
            view = host.getGuest();
        }
        return view;
    }

    protected void notifyOnInvalidated() {
        if (mOnInvalidated) {
            for (DataSetObserver observer : mObservers) {
                observer.onInvalidated();
            }
            mOnInvalidated = false;
            mOnChanged = false;
        }
    }

    protected void notifyOnChanged() {
        if (mOnChanged) {
            for (DataSetObserver observer : mObservers) {
                observer.onChanged();
            }
            mOnChanged = false;
        }
    }

    /**
    * Get the view at specified list position and with specified position in {@link Adapter}
    * New host view might be created or the recycleable view might be reused if possible.
    * @param dataIndex position in {@link Adapter} associated with this layout.
    * @return host view
    */
    protected ListItemHostWidget getRecycleableView(int dataIndex) {
        ListItemHostWidget host = null;
        try {
            host = getHostView(dataIndex);
            if (host != null) {
                if (host.isRecycled()) {
                    Widget view = getViewFromAdapter(dataIndex, host);
                    if (view != null) {
                        setupHost(host, view, dataIndex);
                    }
                }
                boolean added = mContent.addChild(host, true);
                host.layout();
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getRecycleableView: item [%s] is added [%b] to the list",
                        host, added);
            }
        } catch (Exception e) {
            Log.e(TAG, e, "getRecycleableView(%s): exception at %d: %s",
                    getName(), dataIndex, e.getMessage());
        }
        return host;
    }

    /**
     * Create blank host view
     * @param gvrContext
     * @return host view
     */
    protected ListItemHostWidget makeHost(GVRContext gvrContext) {
        ListItemHostWidget host = new ListItemHostWidget(gvrContext);
        return host;
    }

    /**
     * Inner class to host the view from the Adapter.
     * Host widget can be recycled and reused later as a host for another guest widget.
     * Basically the host is the wrapper for the guest widget but it has its own scene
     * object associated with it. It makes possible to transform the host widget with
     * no affecting the guest transformation.
     */
    protected class ListItemHostWidget extends GroupWidget {
        public ListItemHostWidget(GVRContext gvrContext) {
            super(gvrContext, 0, 0);
            recycle();
        }

        /**
         * Attach the specific guest widget to the host.
         *
         * @param guest guest widget associated with the host. It can be null.
         * @param dataIndex data index in adapter
         */
        public void setGuest(Widget guest, int dataIndex) {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "setHostedWidget(%s): hosting [%s], same: %b", getName(),
                        guest == null ? "<null>" : guest.getName(),
                        guest == mGuestWidget);
            if (guest != mGuestWidget) {
                if (mGuestWidget != null && mGuestWidget.getParent() == this) {
                    removeChild(mGuestWidget, true);
                }
                mGuestWidget = guest;
                if (mGuestWidget != null) {
                    addChild(mGuestWidget, true);
                    hostWidth = mGuestWidget.getWidth();
                    hostHeight = mGuestWidget.getHeight();
                    hostDepth = mGuestWidget.getDepth();
                    setName("HostWidget <" + mGuestWidget.getName() + ">");
                }
            }
            mDataIndex = dataIndex;
        }

        @Override
        public float getLayoutWidth() {
            return mAdapter != null && mAdapter.hasUniformViewSize() ?
                    mAdapter.getUniformWidth():
                    mGuestWidget != null ?
                            mGuestWidget.getLayoutWidth() : 0;
        }

        @Override
        public float getLayoutHeight() {
            return mAdapter != null && mAdapter.hasUniformViewSize() ?
                    mAdapter.getUniformHeight():
                    mGuestWidget != null ?
                            mGuestWidget.getLayoutHeight() : 0;
        }

        @Override
        public float getLayoutDepth() {
            return mAdapter != null && mAdapter.hasUniformViewSize() ?
                    mAdapter.getUniformDepth():
                    mGuestWidget != null ?
                            mGuestWidget.getLayoutDepth() : 0;
        }

        /**
         * Recycle the host. It can be later reused for another guest widget.
         */
        public void recycle() {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "recycle(%s), dataIndex = %d", getName(), mDataIndex);
            setSelected(false);
            setGuest(null, -1);
            setViewPortVisibility(ViewPortVisibility.INVISIBLE);
            hostWidth = hostHeight = hostDepth = 0;
        }

        @Override
        public void onTransformChanged() {
            super.onTransformChanged();
            if (!isRecycled()) {
                boolean inViewport = ListWidget.this.inViewPort(mDataIndex);
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onTransformChanged inViewPort [%s], visible = %b",
                        getName(), inViewport);
                if (inViewport) {
                    Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onTransformChanged: FULLY_VISIBLE [%s] position = [%f, %f, %f]",
                            getName(), getPositionX(), getPositionY(), getPositionZ());
                    setViewPortVisibility(ViewPortVisibility.FULLY_VISIBLE);

                } else if (getViewPortVisibility() != ViewPortVisibility.INVISIBLE) {
                    Log.d(TAG, "view [%s] is outside the viewport : recycle(it!)", getName());
                    ListWidget.this.recycle(this);
                }
            } else {
                Log.w(TAG, "onTransformChanged on recycled view [%s]!", getName());
            }
        }

        @Override
        public float getWidth() {
            return hostWidth;
        }

        @Override
        public float getHeight() {
            return hostHeight;
        }

        @Override
        public float getDepth() {
            return hostDepth;
        }

        @Override
        public void setSelected(final boolean selected) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "host.setSelected [%s] index = %d selected [%b]",
                    this, getDataIndex(), selected);

            super.setSelected(selected);
            if (!isRecycled()) {
                mGuestWidget.setSelected(selected);
            }
        }

        /**
         * @return guest widget associated with the host. It can be null if the host is not visible.
         */
        public Widget getGuest() {
            return mGuestWidget;
        }

        private boolean isRecycled() {
            return mDataIndex == -1 || mGuestWidget == null;
        }

        public int getDataIndex() {
            return mDataIndex;
        }

        private float hostWidth, hostHeight, hostDepth;
        private Widget mGuestWidget;
        private int mDataIndex = -1;
    }
    private boolean isScrolling() {
        return mScroller == null ? false : mScroller.doScrolling();
    }

    /**
     * This method is called if the data set has been scrolled.
     */
    private void onScrollImpl(final Vector3Axis offset, final LayoutScroller.OnScrollListener listener) {
        if (!isScrolling()) {
            mScroller = new ScrollingProcessor(offset, listener);
            mScroller.scroll();
        }
    }

    private ScrollingProcessor mScroller = null;
    private void onScrollImpl(final int scrollToPosition, final LayoutScroller.OnScrollListener listener) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onScrollImpl(): scrollToPosition = %d animated = %b",
                scrollToPosition, isTransitionAnimationEnabled());

        if (isTransitionAnimationEnabled()) {
            if (!isScrolling()) {
                mScroller = new ScrollingProcessor(scrollToPosition, listener);
                mScroller.scroll();
            }
        } else {
            WidgetLib.getMainThread().
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onScrollStarted(getCurrentPosition());
                            }
                        }
                    });
            onChangedImpl(scrollToPosition);
            WidgetLib.getMainThread().
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onScrollFinished(getCurrentPosition());
                            }
                        }
                    });
        }
    }

    /**
     * Recycle all views in the list. The host views might be reused for other data to
     * save resources on creating new widgets.
     */
    protected void recycleChildren() {
        for (ListItemHostWidget host: getAllHosts()) {
            recycle(host);
        }
        mContent.onTransformChanged();
        mContent.requestLayout();
    }

    /**
     * Called before recycling the host
     * @param view hosting view
     * @param dataIndex data set index
     */
    protected void onRecycle(Widget view, int dataIndex) {
    }

    private void recycle(ListItemHostWidget host) {
        mContent.removeChild(host, true);

        if (!host.isRecycled()) {
            Widget view = host.getGuest();
            onRecycle(view, host.getDataIndex());

            mContent.invalidateAllLayouts(host);

            host.recycle();
            if (!mRecycledViews.contains(host)) {
                mRecycledViews.add(host);
            }
        }
    }

    /**
     * This method is called if the data set has been changed. Subclasses might want to override
     * this method to add some extra logic.
     *
     * Go through all items in the list:
     * - reuse the existing views in the list
     * - add new views in the list if needed
     * - trim the unused views
     * - request re-layout
     *
     * @param preferableCenterPosition the preferable center position. If it is -1 - keep the
     * current center position.
     */
    private void onChangedImpl(final int preferableCenterPosition) {
        for (ListOnChangedListener listener: mOnChangedListeners) {
            listener.onChangedStart(this);
        }

        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onChangedImpl(%s): items [%d] views [%d] mLayouts.size() = %d " +
                        "preferableCenterPosition = %d",
                getName(), getDataCount(), getViewCount(), mContent.mLayouts.size(), preferableCenterPosition);

        // TODO: selectively recycle data based on the changes in the data set
        mPreferableCenterPosition = preferableCenterPosition;
        recycleChildren();
    }


    private class ContentWidget extends GroupWidget {

        ContentWidget(GVRContext gvrContext) {
            super(gvrContext, 0, 0);
        }
        @Override
        protected boolean measureLayout(Layout layout) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "[%s] measure layout = %s", this, layout);
            int centerPosition = mPreferableCenterPosition;
            // scrolling is in progress, do not remeasure the layout!
            Collection<Widget> measuredChildren = new LinkedHashSet<>();
            layout.measureUntilFull(centerPosition, measuredChildren);
            centerPosition = layout.getCenterChild();

            for (Widget next: measuredChildren) {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measureLayout<next>: [%s] %s", next.getName(), next);
            }

            List<ListItemHostWidget> views = getAllHosts();
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measureLayout: [%d] [%d]", measuredChildren.size(), views.size());
            int count = 0;
            for (ListItemHostWidget host : views) {
                if (!measuredChildren.contains(host)) {
                    Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measureLayout: item [%s] %s recycling", host.getName(), host);
                    recycle(host);
                } else {
                    count++;
                }
            }

            for (ListOnChangedListener listener : mOnChangedListeners) {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "mOnChangedListeners listener[%s] count[%d]", listener, count);
                listener.onChangedFinished(ListWidget.this, count);
            }
            mTrimRequest = true;

            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "measure layout mPreferableCenterPosition = %d, newPosition = %d",
                    mPreferableCenterPosition, centerPosition);

            return true;
        }

        @Override
        protected boolean onLayout() {
            boolean changed = false;

            if (!isScrolling()) {
                changed = super.onLayout();
            } else {
                for (Layout layout : mLayouts) {
                    layout.layoutChildren();
                }
            }

            if (mTrimRequest) {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "Trim %d items ", mRecycledViews.size());
                mRecycledViews.clear();
                mTrimRequest = false;
            }

            notifyOnInvalidated();
            notifyOnChanged();
            return changed;
        }

        @Override
        public boolean isChanged() {
            return isScrolling() ? true : super.isChanged();
        }

        /**
         * WidgetContainer implementation through Adapter
         */
        @Override
        public ListItemHostWidget get(final int dataIndex) {
            return getRecycleableView(dataIndex);
        }

        @Override
        public int size() {
            return getDataCount();
        }

        @Override
        public int getDataIndex(Widget widget) {
            return widget instanceof ListItemHostWidget ?
                    ((ListItemHostWidget) widget).getDataIndex() :
                    super.getDataIndex(widget);
        }

        @Override
        public boolean isDynamic() {
            return true;
        }

        @Override
        public void onTransformChanged() {
            super.onTransformChanged();
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "mContent onTransformChanged: position = [%f, %f, %f]",
                    getPositionX(), getPositionY(), getPositionZ());

        }
    }

    private List<ListItemHostWidget> getAllHosts() {
        List<ListItemHostWidget> hosts = new ArrayList<>();
        for (Widget child: mContent.getChildren()) {
            hosts.add((ListItemHostWidget)child);
        }
        return hosts;
    }

    private void onChanged(final Adapter adapter) {
        runOnGlThread(new Runnable() {
            @Override
            public void run() {
                if (adapter != mAdapter) {
                    mOnInvalidated = true;
                    if (mAdapter != null) {
                        try {
                            mAdapter.unregisterDataSetObserver(mInternalObserver);
                        } catch (IllegalStateException e) {
                            Log.w(TAG, "onChanged(%s): internal observer not registered on adapter!", getName());
                        }
                        clear();
                        notifyOnInvalidated();
                    }
                    mAdapter = adapter;
                    if (mAdapter != null) {
                        mAdapter.registerDataSetObserver(mInternalObserver);
                    }

                }
                onChangedImpl(-1);
            }
        });
    }

    private Set<ListOnChangedListener> mOnChangedListeners = new HashSet<>();

    protected ListItemHostWidget getHostView(int dataIndex) {
        return getHostView(dataIndex, true);
    }

    protected ListItemHostWidget getHostView(int dataIndex, boolean enforceNew) {
        ListItemHostWidget host = null;

        for (Widget child: mContent.getChildren()) {
            ListItemHostWidget next  = (ListItemHostWidget)child;
            if (next.getDataIndex() == dataIndex) {
                host = next;
                break;
            }
        }

        if (host == null  && enforceNew) {
            if (!mRecycledViews.isEmpty()) {
                host = mRecycledViews.remove(0);
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "reuse recycled view: %s", host);

            } else {
                host = makeHost(getGVRContext());
            }
        }

        return host;
    }

    protected int getViewCount() {
        return mContent.getChildren().size();
    }


    protected int getDataCount() {
        return mAdapter == null ? 0 : mAdapter.getCount();
    }

    @Override
    protected boolean inViewPort(final int dataIndex) {
        return mContent.inViewPort(dataIndex);
    }

    private void init(final GVRContext gvrContext, final Adapter adapter) {
        JSONObject properties = getObjectMetadata();
        JSONObject transitionAnimationProperties = optJSONObject(properties,
                Properties.transition_animation, true);
        Log.d(TAG, "init(%s): transition_animation: %s", getName(), transitionAnimationProperties);
        mAnimationRate = optFloat(transitionAnimationProperties, Properties.rate, ANIMATION_SPEED);
        mAnimationEasing = optEnum(transitionAnimationProperties, Properties.easing, Easing.LINEAR);
        Log.d(TAG, "init(%s): easing: %s", getName(), mAnimationEasing);

        mContent = new ContentWidget(gvrContext);
        mContent.setName("Content<" + getName() + ">");
        mContent.addOnHierarchyChangedListener(mOnListItemsUpdatedListener);
        addChild(mContent);
        onChanged(adapter);
    }

    private OnFocusListener mOnFocusListener = new OnFocusListener() {
        @Override
        public boolean onFocus(final Widget widget, final boolean focused) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onFocus(%s) widget= %s focused [%b]", getName(), widget, focused);
            Widget parent = widget.getParent();
            if (parent instanceof ListItemHostWidget) {
                int dataIndex = ((ListItemHostWidget) parent).getDataIndex();
                for (OnItemFocusListener listener : mItemFocusListeners) {
                    listener.onFocus(ListWidget.this, focused, dataIndex);
                }
            } else {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "Focused widget is not a list item!");
            }
            return false;
        }

        @Override
        public boolean onLongFocus(Widget widget) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onLongFocus(%s) widget= %s", getName(), widget.getName());
            Widget parent = widget.getParent();
            boolean ret = false;
            if (parent instanceof ListItemHostWidget) {
                int dataIndex = ((ListItemHostWidget) parent).getDataIndex();
                for (OnItemFocusListener listener : mItemFocusListeners) {
                    listener.onLongFocus(ListWidget.this, dataIndex);
                }
                ret = true;
            } else {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "Long focused widget is not a list item!");
            }
            return ret;
        }
    };


    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(Widget widget, final float[] coords) {
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onTouch(%s) widget= %s mSelectOnTouchEnabled=%b ",
                    getName(), widget, mSelectOnTouchEnabled);
            Widget parent = widget.getParent();
            if (parent instanceof ListItemHostWidget) {
                ListItemHostWidget host = (ListItemHostWidget) parent;
                int dataIndex = host.getDataIndex();
                if (mSelectOnTouchEnabled) {
                    toggleItem(dataIndex);
                }

                for (OnItemTouchListener listener : mItemTouchListeners) {
                    listener.onTouch(ListWidget.this, dataIndex);
                }
            } else {
                Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "onTouch widget is not a list item!");
            }
            return false;
        }
    };

    private DataSetObserver mInternalObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mInternalObserverChangedImpl();
        }

        @Override
        public void onInvalidated() {
            mInternalObserverInvalidatedImpl();
        }
    };

    protected void mInternalObserverChangedImpl() {
        mOnChanged = true;
        ListWidget.this.onChanged();
    }

    protected void mInternalObserverInvalidatedImpl() {
        clear();
        mOnInvalidated = true;
        ListWidget.this.onChanged();
    }

    private OnHierarchyChangedListener mOnListItemsUpdatedListener = new OnHierarchyChangedListener() {
        public void onChildWidgetAdded(Widget parent, Widget child) {
            Widget item = ((ListItemHostWidget)child).getGuest();
            if (item != null) {
                doOnItemAdded(item);
            }
        }
        public void onChildWidgetRemoved(Widget parent, Widget child) {
            Widget item = ((ListItemHostWidget)child).getGuest();
            if (item != null) {
                doOnItemRemoved(item);
            }
        }
    };


    protected void onChanged() {
        onChanged(mAdapter);
    }

    protected void doOnItemAdded(Widget item) {
        item.setFocusEnabled(mItemFocusEnabled);
        item.setTouchable(mItemTouchable);

        item.addFocusListener(mOnFocusListener);
        item.addTouchListener(mOnTouchListener);
    }

    protected void doOnItemRemoved(Widget item) {
        item.setTouchable(false);
        item.setFocusEnabled(false);

        item.removeFocusListener(mOnFocusListener);
        item.removeTouchListener(mOnTouchListener);
    }

    private static final String TAG = ListWidget.class.getSimpleName();
    private Set<DataSetObserver> mObservers = new HashSet<>();


    private static final float ANIMATION_SPEED = 20f; // 20 unit per sec
    private final Set<OnItemFocusListener> mItemFocusListeners = new LinkedHashSet<>();
    private boolean mItemFocusEnabled = true;

    private final Set<OnItemTouchListener> mItemTouchListeners = new LinkedHashSet<>();
    private boolean mItemTouchable = true;

    private int mPreferableCenterPosition = -1;

    private ContentWidget mContent;
    private float mAnimationRate;
    private Easing mAnimationEasing;

    public Adapter mAdapter;
    private boolean mOnChanged;
    private boolean mOnInvalidated;

    private List<ListItemHostWidget> mRecycledViews = new ArrayList<>();
    private boolean mTrimRequest;

    private boolean mMultiSelectionSupported;
    private boolean mSelectOnTouchEnabled;
    protected Set<Integer> mSelectedItemsList = new HashSet<>();
}
