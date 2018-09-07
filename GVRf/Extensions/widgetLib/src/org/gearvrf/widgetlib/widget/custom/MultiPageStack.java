package org.gearvrf.widgetlib.widget.custom;

import android.graphics.Color;

import static org.gearvrf.widgetlib.main.Utility.equal;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.adapter.Adapter;
import org.gearvrf.widgetlib.adapter.BaseAdapter;

import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.ListWidget;
import org.gearvrf.widgetlib.widget.Widget;

import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.OrientedLayout;

import org.gearvrf.widgetlib.widget.layout.basic.LinearLayout;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;
import static org.gearvrf.utility.Log.tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  MultiPageWidget extension applying {@link OrientedLayout.Orientation#STACK stack layout} to the
 *  list of pages.
 */
public class MultiPageStack extends MultiPageWidget {
    /**
     * Create the instance of MultiPageStack
     * @param context
     * @param pageWidth width of one page
     * @param pageHeight height of one page
     * @param pageCount number of pages
     * @param maxVisiblePageCount max number of pages visible on the screen at the same time
     * @param adapter data set adapter
     */
    public MultiPageStack(final GVRContext context, final float pageWidth, final float pageHeight,
                          final int pageCount, final int maxVisiblePageCount,
                          final Adapter adapter) {
        super(context,
                new PageAdapter(context, pageCount, pageWidth, pageHeight),
                0, 0, maxVisiblePageCount);
        if (adapter != null) {
            setAdapter(adapter);
        }
        // stack layout
        mStackLayout = new LinearLayout();
        mStackLayout.setOrientation(OrientedLayout.Orientation.STACK);
        mStackLayout.setDividerPadding(DEFAULT_PAGE_PADDING_Z, Layout.Axis.Z);
        mStackLayout.setGravity(LinearLayout.Gravity.FRONT);
        mStackLayout.enableUniformSize(true);
        mStackLayout.enableClipping(true);

        applyListLayout(mStackLayout);

        // vertical shift layout
        mShiftLayout = new LinearLayout();
        mShiftLayout.setOrientation(OrientedLayout.Orientation.VERTICAL);
        mShiftLayout.setDividerPadding(DEFAULT_PAGE_PADDING_X, Layout.Axis.X);
        mShiftLayout.setDividerPadding(DEFAULT_PAGE_PADDING_Y, Layout.Axis.Y);
        mShiftLayout.setGravity(LinearLayout.Gravity.TOP);
        mShiftLayout.enableUniformSize(true);
        mShiftLayout.enableClipping(true);

        applyListLayout(mShiftLayout);
    }

    /**
     * Sets padding between the pages
     * @param padding
     * @param axis
     */
    public void setPadding(float padding, Layout.Axis axis) {
        OrientedLayout layout = null;
        switch(axis) {
            case X:
                layout = mShiftLayout;
                break;
            case Y:
                layout = mShiftLayout;
                break;
            case Z:
                layout = mStackLayout;
                break;
        }
        if (layout != null) {
            if (!equal(layout.getDividerPadding(axis), padding)) {
                layout.setDividerPadding(padding, axis);

                if (layout.getOrientationAxis() == axis) {
                    requestLayout();
                }
            }
        }

    }

    /**
     * Sets page shift orientation. The pages might be shifted horizontally or vertically relative
     * to each other to make the content of each page on the screen at least partially visible
     * @param orientation
     */
    public void setShiftOrientation(OrientedLayout.Orientation orientation) {
        if (mShiftLayout.getOrientation() != orientation &&
                orientation != OrientedLayout.Orientation.STACK) {
            mShiftLayout.setOrientation(orientation);
            requestLayout();
        }
    }

    @Override
    protected void setItemsPerPage(int itemNum) {
        super.setItemsPerPage(itemNum);
        if (mItemAdapter != null && mAdapter != null) {
            int pageCount = (int) Math.ceil((float) mItemAdapter.getCount() /itemNum);
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setPageCount =  %d", pageCount);
            ((PageAdapter)mAdapter).setCount(pageCount);
            recalculateViewPort(mAdapter);
        }
    }

    private static final String TAG = tag(MultiPageStack.class);

    /**
     * Private adapter class for list of pages
     */
    private static class PageAdapter extends BaseAdapter {
        private static final String TAG = tag(PageAdapter.class);
        private int mPageCount;
        private final GVRContext mGvrContext;
        private final float mPageWidth, mPageHeight;

        private final Map<Integer, ListWidget> mPages;

        private final List<GVRTexture> mPageBgTextures;

        private final static int[] mPageRainbowColors = {
                Color.RED,
                0xFFFFA500, // ORANGE
                Color.YELLOW,
                Color.GREEN,
                Color.CYAN,
                Color.BLUE,
                Color.MAGENTA,
        };

        private final static int[] mPageGrayColors = {
                Color.LTGRAY,
                Color.GRAY,
        };

        PageAdapter(GVRContext gvrContext, int pageCount, float pageWidth,
                    float pageHeight) {
            mGvrContext = gvrContext;
            mPageCount = pageCount;
            mPageWidth = pageWidth;
            mPageHeight = pageHeight;
            mPages = new HashMap<>(pageCount);

            mPageBgTextures = new ArrayList<>(mPageGrayColors.length);
            for (int color: mPageGrayColors){
                mPageBgTextures.add(WidgetLib.getTextureHelper().getSolidColorTexture(color));
            }
        }

        private void setCount(int pageCount) {
            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "setCount: pageCount = %d", pageCount);
            mPageCount = pageCount;
            mPages.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mPageCount;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasUniformViewSize() {
            return true;
        }

        @Override
        public float getUniformWidth() {
            return mPageWidth;
        }

        @Override
        public float getUniformHeight() {
            return mPageHeight;
        }

        @Override
        public float getUniformDepth() {
            return 0.1f;
        }

        @Override
        public Widget getView(final int position, Widget convertView, GroupWidget parent) {
            if (position < 0 && position >= mPageCount) {
                return null;
            }

            ListWidget page = mPages.get(position);
            if (page == null) {
                if (convertView != null && convertView instanceof ListWidget) {
                    page = ((ListWidget)convertView);
                    page.clear();
                } else {
                    ListWidget widget = new ListWidget(mGvrContext,
                            null, mPageWidth, mPageHeight);
                    widget.setName("Page:" + position);
                    page = widget;
                }
                int bgId = position % mPageBgTextures.size();

                page.setTexture(mPageBgTextures.get(bgId));
                mPages.put(position, page);
            }

            Log.d(Log.SUBSYSTEM.WIDGET, TAG, "getView[%d] %s", position, page);
            return page;
        }
    }

    private final LinearLayout mStackLayout, mShiftLayout;
    private static final float DEFAULT_PAGE_PADDING_Z = 5;
    private static final float DEFAULT_PAGE_PADDING_Y = -6;
    private static final float DEFAULT_PAGE_PADDING_X = -5;
}
