package org.gearvrf.widgetlib.widget.custom;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import org.gearvrf.widgetlib.widget.compound.CheckableGroup;
import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.basic.Checkable;
import org.gearvrf.widgetlib.widget.basic.CheckableButton;
import org.gearvrf.widgetlib.widget.layout.Layout;

import org.gearvrf.GVRContext;
import org.json.JSONObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.*;

/**
 * A page indicator widget which identifies the current page in relation to all available pages
 * on the current row. Pages are represented as dots. The current page can be highlighted with a
 * different color or size dot.
 */
public class PageIndicatorWidget extends CheckableGroup {
    /**
     * Listener for page selection
     */
    public interface OnPageSelectedListener {
        /**
         * This method is called when a new page becomes selected.
         * @param pageId  position index of the new selected page
         */
        void onPageSelected(final int pageId);
    }

    /**
     * Core {@link PageIndicatorWidget} constructor.
     *
     * @param context A valid {@link GVRContext}.
     * @param properties A structured set of properties for the {@code PageIndicatorWidget} instance.
     *                   See {@code pageindicatorwidget.json} for schema.
     */
    public PageIndicatorWidget(GVRContext context, JSONObject properties) {
        super(context, properties);
        init();
    }

    /**
     * Create instance of PageIndicatorWidget with specified number of pages, default selected page
     * @param context
     * @param numIndicators number of pages
     * @param defaultPageId default selected page
     */
    public PageIndicatorWidget(GVRContext context, int numIndicators, int defaultPageId) {
        this(context, packProperties(numIndicators, defaultPageId));
        init();
    }

    /**
     * Create instance of PageIndicatorWidget with specified number of pages, default selected page
     * page indicator size and touch feature.
     *
     * @param context
     * @param numIndicators number of pages
     * @param defaultPageId default selected page
     * @param indicatorWidth page indicator width
     * @param indicatorHeight page indicator height
     * @param touchEnabled true if touch enable and the page can be selected by touching on page dot.
     *                     The page cannot be unselected by touching on dot;
     */
    public PageIndicatorWidget(GVRContext context, int numIndicators, int defaultPageId,
                               float indicatorWidth, float indicatorHeight, boolean touchEnabled) {
        super(context, packProperties(numIndicators, defaultPageId, indicatorWidth, indicatorHeight,
                touchEnabled));
        init();
    }

    /**
     * Add {@link OnPageSelectedListener Page selection listener}
     * @param listener
     * @return true if listener has been added successfully
     */
    public boolean addOnPageSelectedListener(OnPageSelectedListener listener) {
        final boolean added;
        synchronized (mListeners) {
            added = mListeners.add(listener);
        }
        if (added) {
            listener.onPageSelected(mCurrentPage);
        }
        return added;
    }

    /**
     * Remove {@link OnPageSelectedListener Page selection listener}
     * @param listener
     * @return true if listener has been removed successfully
     */
    public boolean removeOnPageSelectedListener(OnPageSelectedListener listener) {
        synchronized (mListeners) {
            return mListeners.remove(listener);
        }
    }

    /**
     * Gets total number of pages
     * @return number of pages
     */
    public int getPageCount() {
        return getCheckableCount();
    }

    /**
     * Sets number of pages. If the index of currently selected page is bigger than the total number
     * of pages, first page will be selected instead.
     * @return difference between the previous number of pages and new one. Negative value is
     * returned if new number of pages is less then it was before.
     */
    public int setPageCount(final int num) {
        int diff = num - getCheckableCount();
        if (diff > 0) {
            addIndicatorChildren(diff);
        } else if (diff < 0) {
            removeIndicatorChildren(-diff);
        }
        if (mCurrentPage >=num ) {
            mCurrentPage = 0;
        }
        setCurrentPage(mCurrentPage);
        return diff;
    }

    /**
     * Gets current selected page
     * @return index of currently selected page
     */
    public int getCurrentPage() {
        return mCurrentPage;
    }

    /**
     * Sets selected page implicitly
     * @param page new selected page
     * @return true if the page has been selected  successfully
     */
    public boolean setCurrentPage(final int page) {
        Log.d(TAG, "setPageId pageId = %d", page);
        return (page >= 0 && page < getCheckableCount()) && check(page);
    }

    @Override
    protected <T extends Widget & Checkable> void notifyOnCheckChanged(final T checkableWidget) {
        super.notifyOnCheckChanged(checkableWidget);
        if (checkableWidget.isChecked()) {
            mCurrentPage = getCheckableChildren().indexOf(checkableWidget);
            Log.d(TAG, "onCheckChanged mCurrentPage = %d", mCurrentPage);

            final Object[] listeners;
            synchronized (mListeners) {
                listeners = mListeners.toArray();
            }
            for (Object listener : listeners) {
                ((OnPageSelectedListener) listener).onPageSelected(mCurrentPage);
            }
        }
    }

    private static JSONObject packProperties(int numIndicators, int defaultPageId) {
        final JSONObject buttons = packButtonProperties(numIndicators, defaultPageId);
        final JSONObject properties = new JSONObject();
        put(properties, Properties.buttons, buttons);
        return properties;
    }

    private static JSONObject packProperties(int numIndicators, int defaultPageId,
                                             float indicatorWidth, float indicatorHeight,
                                             boolean touchEnabled) {
        final JSONObject buttons = packButtonProperties(numIndicators, defaultPageId,
                indicatorWidth, indicatorHeight, touchEnabled);
        final JSONObject properties = new JSONObject();
        put(properties, Properties.buttons, buttons);
        return properties;
    }

    @NonNull
    private static JSONObject packButtonProperties(int numIndicators, int defaultPageId) {
        final JSONObject buttons = new JSONObject();
        put(buttons, Properties.count, numIndicators);
        put(buttons, Properties.selected_index, defaultPageId);
        return buttons;
    }

    @NonNull
    private static JSONObject packButtonProperties(int numIndicators, int defaultPageId,
                                                   float indicatorWidth, float indicatorHeight,
                                                   boolean touchEnabled) {
        final JSONObject buttons = packButtonProperties(numIndicators, defaultPageId);
        put(buttons, Widget.Properties.size, new PointF(indicatorWidth, indicatorHeight));
        put(buttons, Properties.touch_enabled, touchEnabled);
        return buttons;
    }

    private enum Properties {
        buttons, count, padding, selected_index, touch_enabled
    }

    private void init() {
        final JSONObject metadata = getObjectMetadata();
        final JSONObject buttons;
        buttons = getJSONObject(metadata, Properties.buttons);
        mPageIndicatorButtonSize = optPointF(buttons, Widget.Properties.size, true);
        mTouchEnabled = optBoolean(buttons, Properties.touch_enabled);

        final float padding = optFloat(buttons, Properties.padding, mPageIndicatorButtonSize.y / 2);
        getDefaultLayout().setDividerPadding(padding, Layout.Axis.Y);

        final int count = optInt(buttons, Properties.count);
        final int selectedIndex = optInt(buttons, Properties.selected_index);
        Log.d(TAG, "init(%s): count = %d selectedIndex = %d", getName(), count, selectedIndex);
        if (count > 0) {
            addIndicatorChildren(count);
            check(selectedIndex);
        }

        final String name = getName();
        if (name == null || name.isEmpty()) {
            setName("PageIndicatorWidget");
        }
    }

    private void addIndicatorChildren(int numIndicators) {
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "addIndicatorChildren %d", numIndicators);
        while (numIndicators-- > 0) {
            PageIndicatorButton buttonWidget = new PageIndicatorButton(getGVRContext(),
                    mPageIndicatorButtonSize);
            buttonWidget.setName("PageIndicatorButton." + getCheckableCount());
            addChild(buttonWidget, true);
        }
        invalidateAllLayouts();
        requestLayout();
    }

    private void removeIndicatorChildren(int numIndicators) {
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "removeIndicatorChildren %d", numIndicators);

        List<PageIndicatorButton> children = getCheckableChildren();
        for (Widget child: children) {
            if (numIndicators-- <= 0) {
                break;
            }
            removeChild(child, true);
        }
        invalidateAllLayouts();
        requestLayout();
    }

    private class PageIndicatorButton extends CheckableButton {
        @SuppressWarnings("unused")
        private final String TAG = PageIndicatorButton.class.getSimpleName();
        private final PointF mSize;

        private PageIndicatorButton(GVRContext context, PointF size) {
            super(context, 0, 0);
            mSize = size;
        }

        @Override
        protected Widget createGraphicWidget() {
            return new PageIndicatorButton.Graphic(getGVRContext(), mSize);
        }

        private class Graphic extends Widget {
            Graphic(GVRContext context, PointF size) {
                super(context, size.x, size.y);
            }
        }

        @Override
        protected boolean onTouch() {
            return mTouchEnabled && super.onTouch();
        }
    }

    private final Set<OnPageSelectedListener> mListeners = new LinkedHashSet<>();

    private PointF mPageIndicatorButtonSize;
    private int mCurrentPage;
    private static final String TAG = PageIndicatorWidget.class.getSimpleName();
    private boolean mTouchEnabled;
}
