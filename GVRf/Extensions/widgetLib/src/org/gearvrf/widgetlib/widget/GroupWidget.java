package org.gearvrf.widgetlib.widget;

import android.support.annotation.NonNull;

import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.json.JSONObject;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.layout.Layout;

public class GroupWidget extends Widget {

    /**
     * Make {@link OnHierarchyChangedListener listener } interface publicly accessible for GroupWidget
     */
    public interface OnHierarchyChangedListener extends Widget.OnHierarchyChangedListener {

    }

    /**
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    public GroupWidget(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
    }

    /**
     * A constructor for wrapping existing {@link GVRSceneObject} instances.
     * Deriving classes should override and do whatever processing is
     * appropriate.
     *
     * @param context
     *            The current {@link GVRContext}
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     * @param attributes
     *            TODO
     * @throws InstantiationException
     */
    public GroupWidget(final GVRContext context,
            final GVRSceneObject sceneObject, NodeEntry attributes)
            throws InstantiationException {
        super(context, sceneObject, attributes);
    }

    /**
     * Construct a new {@link GroupWidget}.
     *
     * @param context
     *            A valid {@link GVRContext} instance.
     * @param width
     * @param height
     */
    public GroupWidget(GVRContext context, float width, float height) {
        super(context, width, height);
    }

    /**
     * Core {@link GroupWidget} constructor.
     *
     * @param context A valid {@link GVRContext}.
     * @param properties A structured set of properties for the {@code GroupWidget} instance. See
     *                       {@code widget.json} for schema.
     */
    public GroupWidget(GVRContext context, @NonNull JSONObject properties) {
        super(context, properties);
    }

    /**
     * Construct a {@link GroupWidget} whose initial properties will be entirely determined by
     * metadata.
     *
     * @param context
     *            The current {@link GVRContext}.
     */
    protected GroupWidget(GVRContext context) {
        super(context);
    }

    /**
     * Adds listener for hierarchy change. Make this method publicly accessible for GroupWidget
     * @param listener
     * @return
     */
    public boolean addOnHierarchyChangedListener(OnHierarchyChangedListener listener) {
        return super.addOnHierarchyChangedListener(listener);
    }

    /**
     * Removes listener for hierarchy change. Make this method publicly accessible for GroupWidget
     * @param listener
     * @return
     */
    public boolean removeOnHierarchyChangedListener(OnHierarchyChangedListener listener) {
        return super.removeOnHierarchyChangedListener(listener);
    }

    /**
     * Add another {@link Widget} as a child of this one. Make this method publicly accessible for
     * GroupWidget
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    @Override
    public boolean addChild(final Widget child) {
        return super.addChild(child);
    }

    /**
     * Add another {@link Widget} as a child of this one. Make this method publicly accessible for
     * GroupWidget
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param index
     *            Position at which to add the child.
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    @Override
    public boolean addChild(final Widget child, int index) {
        return super.addChild(child, index);
    }

    /**
     * Add another {@link Widget} as a child of this one.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    @Override
    public boolean addChild(Widget child, boolean preventLayout) {
        return super.addChild(child, preventLayout);
    }

    /**
     * Add another {@link Widget} as a child of this one.
     *
     * @param child
     *            The {@code Widget} to add as a child.
     * @param index
     *            Position at which to add the child.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was added; {@code false} if
     *         {@code child} was previously added to this instance.
     */
    @Override
    public boolean addChild(Widget child, int index, boolean preventLayout) {
        return super.addChild(child, index, preventLayout);
    }

    @Override
    public boolean hasChild(final Widget child) {
        return super.hasChild(child);
    }

    @Override
    public int indexOfChild(final Widget child) {
        return super.indexOfChild(child);
    }

    /**
     * Remove a {@link Widget} as a child of this instance.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    @Override
    public boolean removeChild(final Widget child) {
        return super.removeChild(child);
    }

    /**
     * Remove a {@link Widget} as a child of this instance.
     *
     * @param child
     *            The {@code Widget} to remove.
     * @param preventLayout
     *            The {@code Widget} whether to call layout().
     * @return {@code True} if {@code child} was a child of this instance and
     *         was successfully removed; {@code false} if {@code child} is not a
     *         child of this instance.
     */
    @Override
    public boolean removeChild(Widget child, boolean preventLayout) {
        return super.removeChild(child, preventLayout);
    }

    /**
     * Performs a breadth-first recursive search for a {@link Widget} with the
     * specified {@link Widget#getName() name}.
     *
     * @param name
     *            The name of the {@code Widget} to find.
     * @return The first {@code Widget} with the specified name or {@code null}
     *         if no child of this {@code Widget} has that name.
     */
    public Widget findChildByName(final String name) {
        return super.findChildByName(name);
    }

    /**
     * @return A copy of the list of {@link Widget org.gearvrf.org.gearvrf.widgetlib} that are children of
     *         this instance.
     */
    public List<Widget> getChildren() {
        return super.getChildren();
    }

    /**
     * Gets child by index
     * @param index
     * @return
     */
    public Widget getChild(int index) {
        return getChildren().get(index);
    }

    /**
     * Removes all children
     */
    public void clear() {
        List<Widget> children = getChildren();
        Log.d(TAG, "clear(%s): removing %d children", getName(), children.size());
        for (Widget child : children) {
            removeChild(child, true);
        }
        requestLayout();
    }

    /**
     * Checks if the child is currently in ViewPort
     * @param dataIndex child index
     * @return true if the child is in viewport, false - otherwise
     */
    protected boolean inViewPort(final int dataIndex) {
        boolean inViewPort = true;

        for (Layout layout: mLayouts) {
            inViewPort = inViewPort && (layout.inViewPort(dataIndex) || !layout.isClippingEnabled());
        }
        return inViewPort;
    }

    /**
     * Create a child {@link Widget} to wrap a {@link GVRSceneObject}. Deriving
     * classes can override this method to handle creation of specific Widgets.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObjectChild
     *            The {@link GVRSceneObject} to wrap.
     * @return
     * @throws InstantiationException
     */
    @Override
    protected Widget createChild(final GVRContext context,
            GVRSceneObject sceneObjectChild) throws InstantiationException {
        return super.createChild(context, sceneObjectChild);
    }

    @Override
    protected void createChildren(final GVRContext context,
                                  final GVRSceneObject sceneObject, JSONObject properties) throws InstantiationException {
        super.createChildren(context, sceneObject, properties);
    }

    protected boolean mEnableTransitionAnimation;

    public void enableTransitionAnimation(final boolean enable) {
        mEnableTransitionAnimation = enable;
    }

    public boolean isTransitionAnimationEnabled() {
        return mEnableTransitionAnimation;
    }

    @SuppressWarnings("unused")
    private static final String TAG = org.gearvrf.utility.Log.tag(GroupWidget.class);

}
