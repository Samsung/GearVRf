package org.gearvrf.widgetlib.widget.custom;

import android.graphics.PointF;

import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.basic.LightTextWidget;
import org.gearvrf.widgetlib.widget.layout.basic.LinearLayout;
import org.gearvrf.widgetlib.widget.layout.OrientedLayout;
import org.gearvrf.widgetlib.widget.Widget;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.*;
import static org.gearvrf.utility.Log.tag;

/**
 * Group Widget with one or multiple buttons inline. Both horizontal and vertical orientations are
 * supported. The default orientation is {@link OrientedLayout.Orientation#HORIZONTAL} however.
 * It allows to add a button to a ControlBar and then associate an action to run when the button
 * is tapped. The control bar has one fixed dimension which is non-stretching; and another
 * dimension is calculated based on the number of controls added to the control bar.
 */
public class ControlBar extends GroupWidget {
    /** padding between the controls in linear layout */
    public static final float BUTTON_GROUP_PADDING = 0.4f;

    /** The size of control bar along the non-stretching axis */
    public static final float BAR_SIZE = 1.2f;

    /** Control bar JSON properties */
    public enum Properties {
        controls, orientation, padding, icon
    }

    /**
     * Core constructor for Control Bar with default parameters
     * Basic control bar setup: horizontal linear layout, no background
     * @param context
     */
    public ControlBar(GVRContext context) {
        this(context, BUTTON_GROUP_PADDING, OrientedLayout.Orientation.HORIZONTAL, -1, BAR_SIZE);
    }

    /**
     * Core constructor for Control Bar with orientation specified and default size are applied.
     * @param context
     * @param orientation Control bar orientation. It might be
     * {@link OrientedLayout.Orientation#HORIZONTAL} or {@link OrientedLayout.Orientation#VERTICAL}
     */
    public ControlBar(GVRContext context, final OrientedLayout.Orientation orientation) {
        this(context, BUTTON_GROUP_PADDING, orientation,
                orientation == OrientedLayout.Orientation.HORIZONTAL ? -1 : BAR_SIZE,
                orientation == OrientedLayout.Orientation.VERTICAL ? -1 : BAR_SIZE);
    }

    /**
     * Core constructor for Control Bar with orientation and size specified.
     * @param context
     * @param orientation Control bar orientation. It might be
     * {@link OrientedLayout.Orientation#HORIZONTAL} or {@link OrientedLayout.Orientation#VERTICAL}
     * @param width
     * @param height
     */
    public ControlBar(GVRContext context, final float padding,
                      final OrientedLayout.Orientation orientation,
                      float width, float height) {
        this(context, packageProperties(TAG, padding, orientation, width, height));
    }

    /**
     * Core {@link ControlBar} constructor.
     *
     * @param context A valid {@link GVRContext}.
     * @param properties A structured set of properties for the {@code ControlBar} instance. See
     *                       {@code controlbar.json} for schema.
     */
    public ControlBar(GVRContext context, JSONObject properties) {
        super(context, fixupProperties(properties));

        properties = getObjectMetadata();

        float padding = optFloat(properties, Properties.padding, BUTTON_GROUP_PADDING);
        OrientedLayout.Orientation orientation = optEnum(properties, Properties.orientation,
                OrientedLayout.Orientation.HORIZONTAL);
        mLayout = getLayout(padding, orientation);
        applyLayout(mLayout);

        mDimensions = optPointF(properties, PrivyProperties.dimensions, new PointF(-1, BAR_SIZE));
        mIsExtendableSize = orientation == OrientedLayout.Orientation.HORIZONTAL ?
                mDimensions.x < 0 : mDimensions.y < 0;

        float controlSize = orientation == OrientedLayout.Orientation.HORIZONTAL ?
                mDimensions.y : mDimensions.x;
        mControlDimensions = new PointF(controlSize - padding, controlSize - padding);

        JSONArray controls = optJSONArray(properties, Properties.controls, true);
        for (int i = 0; i < controls.length(); ++i) {
            JSONObject control = controls.optJSONObject(i);
            if (control != null) {
                String name = getString(control, Widget.Properties.name);
                addControl(name, control, null);
            }
        }
    }

    @Override
    public float getLayoutSize(final Layout.Axis axis) {
        // quick fix to count the outer padding
        float extraPadding = 0;
        if (mLayout.getOrientationAxis() == axis && mLayout.isOuterPaddingEnabled()) {
            extraPadding = mLayout.getDividerPadding(axis);
        }

        return super.getLayoutSize(axis) + extraPadding;
    }

    @Override
    public void setTexture(final int bgResId) {
        mBgResId = bgResId;
        updateMesh();
        if (mBgResId != -1) {
            super.setTexture(mBgResId);
        }
    }

    /**
     * Remove control from the control bar. Size of control bar is updated based on new number of
     * controls.
     * @param name name of the control to remove
     */
    public void removeControl(String name) {
        Widget control = findChildByName(name);
        if (control != null) {
            removeChild(control);
            if (mBgResId != -1) {
                updateMesh();
            }
        }
    }

    /**
     * Add new control at the end of control bar with specified touch listener.
     * Size of control bar is updated based on new number of controls.
     * @param name name of the control to remove
     * @param properties JSON control specific properties
     * @param listener touch listener
     */

    public void addControl(String name, JSONObject properties, Widget.OnTouchListener listener) {
        final JSONObject allowedProperties = new JSONObject();
        put(allowedProperties, Widget.Properties.name, optString(properties, Widget.Properties.name));
        put(allowedProperties, Widget.Properties.size, new PointF(mControlDimensions.x, mControlDimensions.y));
        put(allowedProperties, Widget.Properties.states, optJSONObject(properties, Widget.Properties.states));

        Widget control = new Widget(getGVRContext(), allowedProperties);
        setupControl(name, control, listener, -1);
    }

    /**
     * Add new control at the end of control bar with specified touch listener and resource.
     * Size of control bar is updated based on new number of controls.
     * @param name name of the control to remove
     * @param resId the control face
     * @param listener touch listener
     */
    public Widget addControl(String name, int resId, Widget.OnTouchListener listener) {
        return addControl(name, resId, null, listener, -1);
    }

    /**
     * Add new control at the end of control bar with specified touch listener, control label and resource.
     * Size of control bar is updated based on new number of controls.
     * @param name name of the control to remove
     * @param resId the control face
     * @param label the control label
     * @param listener touch listener
     */
    public Widget addControl(String name, int resId, String label, Widget.OnTouchListener listener) {
        return addControl(name, resId, label, listener, -1);
    }

    /**
     * Add new control at the control bar with specified touch listener, resource and position.
     * Size of control bar is updated based on new number of controls.
     * @param name name of the control to remove
     * @param resId the control face
     * @param listener touch listener
     * @param position control position in the bar
     */
    public Widget addControl(String name, int resId, Widget.OnTouchListener listener, int position) {
        Widget control = findChildByName(name);
        if (control == null) {
            control = createControlWidget(resId, name, null);
        }
        setupControl(name, control, listener, position);
        return control;
    }

    /**
     * Add new control to the control bar with specified touch listener, control label, resource
     * and position. Size of control bar is updated based on new number of controls.
     * @param name name of the control to remove
     * @param resId the control face
     * @param label the control label
     * @param listener touch listener
     * @param position control position
     */
    public Widget addControl(String name, int resId, String label,
                Widget.OnTouchListener listener, int position) {
        Widget control = findChildByName(name);
        if (control == null) {
            control = createControlWidget(resId, name, label);
        }
        setupControl(name, control, listener, position);
        return control;
    }

    /**
     * Add a touch listener for the button specified by {@code name}.  The listener is only
     * registered if the button is actually present.
     *
     * @param name The {@linkplain Widget#getName() name} of the button to listen to
     * @param listener A valid listener or null to clear
     * @return {@code True} if a valid listener was passed and the named button is present;
     *      {@code false} in all other cases.
     */
    public boolean addControlListener(String name, Widget.OnTouchListener listener) {
        Widget control = findChildByName(name);
        if (control != null) {
            return control.addTouchListener(listener);
        }

        return false;
    }

    /**
     * Remove a touch listener for the button specified by {@code name}.
     * @param name The {@linkplain Widget#getName() name} of the button to listen to
     * @param listener A valid listener or null to clear
     *
     */
    public boolean removeControlListener(String name, Widget.OnTouchListener listener) {
        Widget control = findChildByName(name);
        if (control != null) {
            return control.removeTouchListener(listener);
        }
        return false;
    }

    protected float getControlWidth() {
        return  mControlDimensions.x;
    }

    protected float getControlHeight() {
        return  mControlDimensions.y;
    }

    protected OrientedLayout getLayout(final float padding,
                                       final OrientedLayout.Orientation orientation) {
        LinearLayout  layout = new LinearLayout();
        layout.setOrientation(orientation);
        layout.setDividerPadding(padding, layout.getOrientationAxis());
        layout.enableOuterPadding(true);
        layout.setOffset(-CONTROL_PADDING_Z, Layout.Axis.Z);
        return layout;
    }

    protected Widget createControlWidget(int resId, String name, String label) {
        Widget control;
        if (label == null) {
            control = new Widget(getGVRContext(), mControlDimensions.x, mControlDimensions.y);
            control.setTexture(resId);
        } else {
            control = new LightTextWidget(getGVRContext(),
                    mControlDimensions.x, mControlDimensions.y, label);
            ((LightTextWidget) control).setBackGround(getGVRContext().getContext().getDrawable(resId));
        }
        return control;
    }

    protected void setupControl(String name, Widget control, Widget.OnTouchListener listener, int position) {
        control.setName(name);
        if (listener != null) {
            control.addTouchListener(listener);
        }
        control.setRenderingOrder(getRenderingOrder() + 1);
        addChild(control, position);
        if (mBgResId != -1) {
            updateMesh();
        }
    }

    private void updateMesh() {
        float width = mBgResId != -1 ? getControlBarWidth() : 0;
        float height = mBgResId != -1 ? getControlBarHeight() : 0;

        GVRMesh mesh = getGVRContext().createQuad(width, height);
        setMesh(mesh);
    }

    private boolean isExtendableSize(Layout.Axis axis) {
        return mIsExtendableSize && mLayout.getOrientationAxis() == axis;
    }

    private float getControlBarWidth() {
        if (isExtendableSize(Layout.Axis.X)) {
            int numOfControls = getChildCount(false);
            if (numOfControls > 0) {
                mDimensions.x = numOfControls * mDimensions.y;
            }
        }
        return mDimensions.x;
    }

    private float getControlBarHeight() {
        if (isExtendableSize(Layout.Axis.Y)) {
            int numOfControls = getChildCount(false);
            if (numOfControls > 0) {
                mDimensions.y = numOfControls * mDimensions.x;
            }
        }
        return mDimensions.y;
    }

    private static JSONObject fixupProperties(final JSONObject properties) {
        JSONObject fixedUp = copy(properties);
        PointF size = removePointF(fixedUp, Widget.Properties.size);
        if (size != null) {
            put(fixedUp, PrivyProperties.dimensions, size);
        }
        return fixedUp;
    }

    private static JSONObject packageProperties(final String name, final float padding,
                                                final OrientedLayout.Orientation orientation,
                                                float width, float height) {
        final JSONObject properties = new JSONObject();
        put(properties, Widget.Properties.name, name);
        put(properties, PrivyProperties.dimensions, new PointF(width, height));
        put(properties, Properties.padding, padding);
        put(properties, Properties.orientation, orientation);
        return properties;
    }

    private enum PrivyProperties {
        dimensions
    }

    private final PointF mDimensions;
    private PointF mControlDimensions;
    private static final String TAG = tag(ControlBar.class);
    private int mBgResId = -1;
    private static final float CONTROL_PADDING_Z = 0.05f;
    private boolean mIsExtendableSize;
    private final OrientedLayout mLayout;
}
