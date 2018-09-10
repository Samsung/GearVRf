package org.gearvrf.widgetlib.adapter;

public abstract class BaseAdapterViewFactory implements AdapterViewFactory {
    @Override
    public int getItemViewType(Adapter adapter, int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasUniformViewSize() {
        return false;
    }

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
}
