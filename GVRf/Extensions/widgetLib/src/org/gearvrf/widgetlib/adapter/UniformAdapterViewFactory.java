package org.gearvrf.widgetlib.adapter;

import org.joml.Vector3f;

/**
 * {@link AdapterViewFactory} with uniform view size.
 */
public abstract class UniformAdapterViewFactory extends BaseAdapterViewFactory {
    /**
     * Create new  {@link UniformAdapterViewFactory} with specific view dimensions
     * @param dimensions uniform view dimensions using similarly for x, y, z
     */
    public UniformAdapterViewFactory(float dimensions) {
        this(new Vector3f(dimensions));
    }

    /**
     * Create new  {@link UniformAdapterViewFactory} with specific view width and height
     * @param x view width
     * @param y view height
     */
    public UniformAdapterViewFactory(float x, float y) {
        this(new Vector3f(x, y, 0));
    }

    /**
     * Create new  {@link UniformAdapterViewFactory} with specific view width and height
     * @param x view width
     * @param y view height
     * @param z view depth
     */
    public UniformAdapterViewFactory(float x, float y, float z) {
        this(new Vector3f(x, y, z));
    }

    /**
     * Create new  {@link UniformAdapterViewFactory} with specific view dimensions
     * @param dimensions 3 ax uniform view dimensions
     */
    public UniformAdapterViewFactory(Vector3f dimensions) {
        mDimensions = dimensions;
    }

    /**
     * Set view dimensions
     * @param dimensions uniform view dimensions using similarly for x, y, z
     */
    public void setDimensions(float dimensions) {
        mDimensions.set(dimensions);
    }

    /**
     * Set view dimensions
     * @param x view width
     * @param y view height
     */
    public void setDimensions(float x, float y) {
        mDimensions.set(x, y, mDimensions.z);
    }

    /**
     * Set view dimensions
     * @param x view width
     * @param y view height
     * @param z view depth
     */
    public void setDimensions(float x, float y, float z) {
        mDimensions.set(x, y, z);
    }

    /**
     * Set view dimensions
     * @param dimensions 3 ax uniform view dimensions
     */
    public void setDimensions(Vector3f dimensions) {
        mDimensions.set(dimensions);
    }

    @Override
    public boolean hasUniformViewSize() {
        return true;
    }

    @Override
    public float getUniformWidth() {
        return mDimensions.x;
    }

    @Override
    public float getUniformHeight() {
        return mDimensions.y;
    }

    @Override
    public float getUniformDepth() {
        return mDimensions.z;
    }

    private final Vector3f mDimensions;
}
