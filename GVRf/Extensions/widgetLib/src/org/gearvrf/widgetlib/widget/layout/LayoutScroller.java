package org.gearvrf.widgetlib.widget.layout;

import java.util.HashSet;
import java.util.Set;


import android.content.Context;
import android.database.DataSetObserver;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import org.gearvrf.widgetlib.log.Log;
import static org.gearvrf.widgetlib.main.Utility.equal;

/**
 * Content scrolling manager class. It provides different ways to scroll {@link ScrollableList}
 * content.
 *
 */
public class LayoutScroller {
    /**
     * Interface to keep track of scrolling process
     */
    public interface OnScrollListener {

        /**
         * Called when the scrolling is started
         * @param startPosition
         */
        void onScrollStarted(int startPosition);

        /**
         * Called when the scrolling is finished
         * @param finalPosition
         */
        void onScrollFinished(int finalPosition);
	}

    /**
     * Interface to keep track of scrolling by pages
     */
    public interface OnPageChangedListener {
        /**
         * Called when current scrolling page is changed
         * @param page new current page
         */
        void pageChanged(int page);
    }

    /**
     * Scrollable interface. Widget has to implement this interface in order to use LayoutScroller
     */
	public interface ScrollableList {
        /**
         * Gets number of scrollable items
         * @return
         */
	    int getScrollingItemsCount();

        /**
         * Gets the width of scrollable area
         * @return
         */
	    float getViewPortWidth();

        /**
         * Gets the height of scrollable area
         * @return
         */
        float getViewPortHeight();

        /**
         * Gets the depth of scrollable area
         * @return
         */
        float getViewPortDepth();

        /**
         * Scroll to specific position in the list
         * @param pos
         * @param listener
         * @return
         */
	    boolean scrollToPosition(final int pos, final OnScrollListener listener);

        /**
         * Scroll list by offset
         * @param xOffset
         * @param yOffset
         * @param zOffset
         * @param listener
         * @return
         */
	    boolean scrollByOffset(final float xOffset, final float yOffset, final float zOffset,
                               final OnScrollListener listener);

        /**
         * Registers {@link DataSetObserver} to the list
         * @param observer
         */
	    void registerDataSetObserver(final DataSetObserver observer);

        /**
         * Unregisters {@link DataSetObserver} from the list
         * @param observer
         */
        void unregisterDataSetObserver(final DataSetObserver observer);

        /**
         * Gets current list position
         * @return
         */
        int getCurrentPosition();
    }

    /**
     * Constructs LayoutScroller instance with all pre-defined parameters
     * @param context
     * @param scrollable scrollablelist  instance
     */
	public LayoutScroller(final Context context, final ScrollableList scrollable) {
        this(context, scrollable, false, 0, 1, scrollable.getCurrentPosition());
    }

    /**
     * Constructs LayoutScroller instance with some pre-defined parameters
     * @param context
     * @param scrollable scrollablelist  instance
     * @param scrollOver set to true if the content might be scrolled over
     */
	public LayoutScroller(final Context context, final ScrollableList scrollable,
	        final boolean scrollOver) {
		this(context, scrollable, scrollOver, 0, 1, scrollable.getCurrentPosition());
	}

    /**
     * Customizable constructor for Layout Scroller
     * @param context
     * @param scrollable scrollablelist  instance
     * @param scrollOver set to true if the content might be scrolled over
     * @param pageSize number of items per page
     * @param deltaScrollAmount minimum scroll amount
     * @param currentIndex the item the content currently scrolled to
     */
	public LayoutScroller(final Context context, final ScrollableList scrollable,
	        final boolean scrollOver,
	        final int pageSize, int deltaScrollAmount, final int currentIndex) {
	    if (scrollable == null) {
	        throw new IllegalArgumentException("scrollable cannot be null!");
	    }
	    mScroller = new Scroller(context, new LinearInterpolator());
	    mScroller.extendDuration(SCROLL_DURATION);
	    mScrollable = scrollable;

		mScrollOver = scrollOver;
		mPageSize = pageSize;
		mSupportScrollByPage = pageSize > 0;
		mCurrentItemIndex = currentIndex;
		mDeltaScrollAmount = deltaScrollAmount;
		mScrollable.registerDataSetObserver(mObserver);
	}

    /**
     * Adds {@link OnScrollListener}
     * @param listener
     */
    public void addOnScrollListener(final OnScrollListener listener) {
        mOnScrollListeners.add(listener);
    }

    /**
     * Removes {@link OnScrollListener}
     * @param listener
     */
    public void removeOnScrollListener(final OnScrollListener listener) {
        mOnScrollListeners.remove(listener);
    }

    /**
     * Adds {@link OnPageChangedListener}
     * @param listener
     */
    public void addOnPageChangedListener(final OnPageChangedListener listener) {
        mOnPageChangedListeners.add(listener);
    }

    /**
     * Removes {@link OnPageChangedListener}
     * @param listener
     */
    public void removeOnPageChangedListener(final OnPageChangedListener listener) {
        mOnPageChangedListeners.remove(listener);
    }

    /**
     * Fling the content
     *
     * @param velocityX The initial velocity in the X direction. Positive numbers mean that the
     *                  finger/cursor is moving to the left on the screen, which means we want to
     *                  scroll towards the beginning.
     * @param velocityY The initial velocity in the Y direction. Positive numbers mean that the
     *                  finger/cursor is moving down the screen, which means we want to scroll
     *                  towards the top.
     * @param velocityZ TODO: Z-scrolling is currently not supported
     * @return
     */
    public boolean fling(float velocityX, float velocityY, float velocityZ) {
        boolean scrolled = true;
        float viewportX  = mScrollable.getViewPortWidth();
        if (Float.isNaN(viewportX)) {
            viewportX = 0;
        }
        float maxX = Math.min(MAX_SCROLLING_DISTANCE,
                viewportX * MAX_VIEWPORT_LENGTHS);

        float viewportY  = mScrollable.getViewPortHeight();
        if (Float.isNaN(viewportY)) {
            viewportY = 0;
        }
        float maxY = Math.min(MAX_SCROLLING_DISTANCE,
                viewportY * MAX_VIEWPORT_LENGTHS);

        float xOffset = (maxX * velocityX)/VELOCITY_MAX;
        float yOffset = (maxY * velocityY)/VELOCITY_MAX;

        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "fling() velocity = [%f, %f, %f] offset = [%f, %f]",
                velocityX, velocityY, velocityZ,
                xOffset, yOffset);

        if (equal(xOffset, 0)) {
            xOffset = Float.NaN;
        }

        if (equal(yOffset, 0)) {
            yOffset = Float.NaN;
        }

// TODO: Think about Z-scrolling
        mScrollable.scrollByOffset(xOffset, yOffset, Float.NaN, mInternalScrollListener);

        return scrolled;
    }

    /**
     * Scroll to the next page. To process the scrolling by pages LayoutScroller must be constructed
     * with a pageSize greater than zero.
     * @return the new current item after the scrolling processed.
     */
    public int scrollToNextPage() {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToNextPage getCurrentPage() = %d currentIndex = %d",
                getCurrentPage(), mCurrentItemIndex);

        if (mSupportScrollByPage) {
            scrollToPage(getCurrentPage() + 1);
        } else {
            Log.w(TAG, "Pagination is not enabled!");
        }

        return mCurrentItemIndex;
	}

    /**
     * Scroll to the previous page. To process the scrolling by pages LayoutScroller must be
     * constructed with a pageSize greater than zero.
     * @return the new current item after the scrolling processed.
     */
	public int scrollToPrevPage() {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToPrevPage getCurrentPage() = %d currentIndex = %d",
                getCurrentPage(), mCurrentItemIndex);

        if (mSupportScrollByPage) {
            scrollToPage(getCurrentPage() - 1);
        } else {
            Log.w(TAG, "Pagination is not enabled!");
        }
        return mCurrentItemIndex;
	}

    /**
     * Scroll to specific page. The final page might be different from the requested one if the
     * requested page is larger than the last page. To process the scrolling by pages
     * LayoutScroller must be constructed with a pageSize greater than zero.
     * @param pageNumber page to scroll to
     * @return the new current item after the scrolling processed.
     */
	public int scrollToPage(int pageNumber) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToPage pageNumber = %d mPageCount = %d",
                pageNumber, mPageCount);

        if (mSupportScrollByPage &&
                (mScrollOver || (pageNumber >= 0 && pageNumber <= mPageCount - 1))) {
            scrollToItem(getFirstItemIndexOnPage(pageNumber));
        } else {
            Log.w(TAG, "Pagination is not enabled!");
        }
        return mCurrentItemIndex;
	}

    /**
     * Scroll to the first item
     * @return the new current item after the scrolling processed.
     */
    public int scrollToBegining() {
        return scrollToItem(0);
    }

    /**
     * Scroll to the last item
     * @return the new current item after the scrolling processed.
     */
	public int scrollToEnd() {
	    return scrollToItem(mScrollable.getScrollingItemsCount() - 1);
    }

    /**
     * Scroll to the next item.
     * @return the new current item after the scrolling processed.
     */
	public int scrollToNextItem() {
	    return scrollToItem(mCurrentItemIndex + 1);
	}

    /**
     * Scroll to the previous item.
     * @return the new current item after the scrolling processed.
     */
	public int scrollToPrevItem() {
	    return scrollToItem(mCurrentItemIndex - 1);
    }

    /**
     * Scroll to the specific position
     * @param position
     * @return the new current item after the scrolling processed.
     */
    public int scrollToItem(int position) {
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToItem position = %d", position);
        scrollToPosition(position);
        return mCurrentItemIndex;
    }

    /**
     * Gets the current page
     * @return
     */
    public int getCurrentPage() {
        int currentPage = 1;
        int count = mScrollable.getScrollingItemsCount();
        if (mSupportScrollByPage && mCurrentItemIndex >= 0 &&
                mCurrentItemIndex < count) {
            currentPage = (Math.min(mCurrentItemIndex + mPageSize - 1, count - 1)/mPageSize);
        }
        return currentPage;
    }

    private DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            int count = mScrollable.getScrollingItemsCount();
            mPageCount = mSupportScrollByPage ?
                    (int) Math.ceil((float)count/(float)mPageSize) : 1;

            if (mCurrentItemIndex >= count) {
                mCurrentItemIndex = count - 1;
                scrollToPosition(mCurrentItemIndex);
            }
        }

        @Override
        public void onInvalidated() {
            int count = mScrollable.getScrollingItemsCount();
            mPageCount = mSupportScrollByPage ?
                    (int) Math.ceil((float)count/(float)mPageSize) : 1;

            if (mCurrentItemIndex > 0) {
                mCurrentItemIndex = 0;
                scrollToPosition(mCurrentItemIndex);
            }
        }
    };

    private int getFirstItemIndexOnPage(final int pageNumber) {
        int index = 0;
        if (mSupportScrollByPage) {
            index = (pageNumber * mPageSize);
            Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "getFirstItemIndexOnPage = %d", index);
        }
        return index;
    }

    private int getValidPosition(int position) {
        int pos = position;
        if (pos >= mScrollable.getScrollingItemsCount()) {
            if (mScrollOver) {
                pos %= mScrollable.getScrollingItemsCount();
            } else {
                pos = mScrollable.getScrollingItemsCount() - 1;
            }
        } else if (pos < 0) {
            if (mScrollOver) {
                pos %= mScrollable.getScrollingItemsCount();
                pos += mScrollable.getScrollingItemsCount();
                pos %= mScrollable.getScrollingItemsCount();
            } else {
                pos = 0;
            }
        }

        pos = (pos / mDeltaScrollAmount) *
                mDeltaScrollAmount;
        return pos;
    }

    private OnScrollListener mInternalScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStarted(int startPosition) {
            for (OnScrollListener listener: mOnScrollListeners) {
                listener.onScrollStarted(startPosition);
            }
        }

        @Override
        public void onScrollFinished(int finalPosition) {
            mCurrentItemIndex = finalPosition;
            for (OnScrollListener listener: mOnScrollListeners) {
                listener.onScrollFinished(mCurrentItemIndex);
            }

            int curPage = getCurrentPage();
            for (OnPageChangedListener listener: mOnPageChangedListeners) {
                listener.pageChanged(curPage);
            }
        }
    };

    private boolean scrollToPosition(int newPosition) {
        boolean scrolled = false;
        Log.d(Log.SUBSYSTEM.LAYOUT, TAG, "scrollToPosition() mCurrentItemIndex=%d newPosition = %d",
                mCurrentItemIndex, newPosition);

        if (newPosition != mCurrentItemIndex) {
            int pos = getValidPosition(newPosition);
            scrolled = mScrollable.scrollToPosition(pos, mInternalScrollListener);
        }

        return scrolled;
    }


    private int mCurrentItemIndex;

    private final ScrollableList mScrollable;
    private final int mPageSize;
    private final boolean mScrollOver;
    private final boolean mSupportScrollByPage;

    private int mPageCount;
    private int mDeltaScrollAmount;
    private Scroller mScroller;
    private Set<OnScrollListener> mOnScrollListeners = new HashSet<>();
    private Set<OnPageChangedListener> mOnPageChangedListeners = new HashSet<>();

    private static final String TAG = LayoutScroller.class.getSimpleName();;

    private static final int SCROLL_DURATION = 5000; // 5 sec
    private static final float VELOCITY_MAX = 30000;
    private static final float MAX_VIEWPORT_LENGTHS = 4;
    private static final float MAX_SCROLLING_DISTANCE = 500;
}
