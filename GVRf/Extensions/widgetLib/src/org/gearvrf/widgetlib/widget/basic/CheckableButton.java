package org.gearvrf.widgetlib.widget.basic;

import org.gearvrf.widgetlib.widget.layout.basic.LinearLayout;
import org.gearvrf.widgetlib.widget.NodeEntry;
import org.gearvrf.widgetlib.widget.WidgetState;
import org.gearvrf.widgetlib.widget.layout.Layout;

import java.util.LinkedHashSet;
import java.util.Set;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.json.JSONObject;

/**
 * Defines an extension for Button that makes it checkable.
 */
public abstract class CheckableButton extends Button implements Checkable {

    /**
     * Create new instance of CheckableButton with specified size
     * @param context
     * @param width button width
     * @param height button height
     */
    public CheckableButton(GVRContext context, float width, float height) {
        super(context, width, height);
        init();
    }

    public CheckableButton(GVRContext context) {
        super(context);
        init();
    }

    /**
     * Create new instance of CheckableButton with specified size
     * @param context
     */
    public CheckableButton(GVRContext context, JSONObject properties) {
        super(context, properties);
        init();
    }

    /**
     * Create new instance of CheckableButton wrapping around GVRF sceneObject parsed from the model
     *
     * @param context
     * @param sceneObject
     * @param attributes
     * @throws InstantiationException
     */
    @Deprecated
    public CheckableButton(GVRContext context, GVRSceneObject sceneObject,
                           NodeEntry attributes) throws InstantiationException {
        super(context, sceneObject, attributes);

        init();
        String attr = attributes.getProperty("checked");
        setChecked(attr != null && attr.compareToIgnoreCase("false") == 0);
    }

    /**
     * Create new instance of CheckableButton wrapping around GVRF sceneObject
     *
     * @param context
     * @param sceneObject
     * @throws InstantiationException
     */
    public CheckableButton(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
        init();
    }

    @Override
    public boolean addOnCheckChangedListener(OnCheckChangedListener listener) {
        return mCheckChangedListeners.add(listener);
    }

    @Override
    public boolean removeOnCheckChangedListener(OnCheckChangedListener listener) {
        return mCheckChangedListeners.remove(listener);
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void setChecked(final boolean checked) {
        if (checked != mIsChecked) {
            mIsChecked = checked;
            updateState();

            // Avoid infinite recursions if setChecked() is called from a
            // listener
            if (mIsBroadcasting) {
                return;
            }

            mIsBroadcasting = true;

            for (OnCheckChangedListener listener : mCheckChangedListeners) {
                listener.onCheckChanged(this, mIsChecked);
            }

            mIsBroadcasting = false;
        }
    }

    @Override
    public void toggle() {
        setChecked(!mIsChecked);
    }

    @Override
    public Layout getDefaultLayout() {
        return mDefaultLayout;
    }

    protected CheckableButton(GVRContext context, GVRMesh mesh) {
        super(context, mesh);
        init();
    }

    @Override
    protected boolean onTouch() {
        super.onTouch();
        toggle();
        return true;
    }

    /* package */
    @Override
    protected WidgetState.State getState() {
        if (mIsChecked) {
            return WidgetState.State.CHECKED;
        }
        return super.getState();
    }

    private void init() {
        setChecked(getObjectMetadata().optBoolean("checked"));
        mDefaultLayout.setGravity(LinearLayout.Gravity.LEFT);
    }

    private boolean mIsChecked;
    private boolean mIsBroadcasting;

    private final Set<OnCheckChangedListener> mCheckChangedListeners = new LinkedHashSet<OnCheckChangedListener>();

    private final LinearLayout mDefaultLayout = new LinearLayout();

    private static final String TAG = CheckableButton.class.getSimpleName();
}
