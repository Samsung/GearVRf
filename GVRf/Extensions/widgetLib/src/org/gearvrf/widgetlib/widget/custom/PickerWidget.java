package org.gearvrf.widgetlib.widget.custom;

import org.gearvrf.GVRContext;
import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.widget.animation.ScaleAnimation;
import org.joml.Vector3f;
import org.json.JSONObject;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.adapter.Adapter;
import org.gearvrf.widgetlib.widget.animation.Animation;
import org.gearvrf.widgetlib.widget.animation.AnimationFactory;
import org.gearvrf.widgetlib.widget.ListWidget;
import org.gearvrf.widgetlib.widget.Widget;

import java.util.HashMap;
import java.util.Map;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optJSONObject;
import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.put;

/**
 * ListWidget extension with focus animation enabled for better visualisation.
 * The regular {@link OnItemFocusListener} and {@link OnItemTouchListener} should be used however
 * for specific touch and focus processing.
 */
public final class PickerWidget extends ListWidget {

    /**
     * Create the instance of PickerWidget
     * @param context
     * @param adapter
     * @param width
     * @param height
     */
    public PickerWidget(GVRContext context, final Adapter adapter, float width, float height) {
        super(context, adapter, width, height);
        addOnItemFocusListener(mItemFocusListener);

        JSONObject properties = getObjectMetadata();
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "PickerWidget(): properties: %s", properties);
        JSONObject focusAnimationSpec = optJSONObject(properties, Properties.focus_animation, sFocusAnimationSpec);
        mFocusAnimationFactory = AnimationFactory.makeFactory(focusAnimationSpec);
        JSONObject defocusAnimationSpec = optJSONObject(properties, Properties.defocus_animation, sDefocusAnimationSpec);
        mDefocusAnimationFactory = AnimationFactory.makeFactory(defocusAnimationSpec);
    }

    /**
     * Enable or disable focus animation. If the animation is disabled, the PickerWidget operates as
     * ListWidget
     * @param enable if it is true the focus animation will be enabled, otherwise - disabled
     */
    public void enableFocusAnimation(boolean enable) {
        mFocusAnimationEnabled = enable;
    }

    /**
     * It should be called when the picker is shown
     */
    public synchronized void show() {
        setRotation(1, 0, 0, 0);
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "show Picker!");
    }

    /**
     * It should be called when the picker is hidden
     */
    public synchronized void hide() {
        if (focusedQuad != null) {

            mDefocusAnimationFactory.create(focusedQuad)
                    .setRequestLayoutOnTargetChange(false)
                    .start().finish();

            focusedQuad = null;
        }
        Log.d(Log.SUBSYSTEM.WIDGET, TAG, "hide Picker!");
    }

    private enum Properties { focus_animation, defocus_animation }

    private OnItemFocusListener mItemFocusListener = new OnItemFocusListener() {
        @Override
        public void onFocus(ListWidget list, final boolean focused, final int dataIndex) {
            Log.i(Log.SUBSYSTEM.FOCUS, TAG, "onFocus: %d [%b]", dataIndex, focused);


                    if (mFocusAnimationEnabled) {
                        Widget target = getHostView(dataIndex, false);
                        if (target != null) {
                            boolean animate = focused ^ (target == focusedQuad);

                            if (animate) {
                                Log.i(Log.SUBSYSTEM.FOCUS, TAG, "onFocus animate: %d [%b]",
                                        dataIndex, (target == focusedQuad));

                                Animation a = curAnimations.get(target);
                                if (a != null) {
                                    a.stop();
                                    curAnimations.remove(target);
                                }

                                AnimationFactory.Factory animFactory = focused ?
                                        mFocusAnimationFactory :
                                        mDefocusAnimationFactory;

                                Log.i(Log.SUBSYSTEM.FOCUS, TAG, "onFocus[%d] animFactory: %s" +
                                                "focusedQuad = %s, target = %s",
                                        dataIndex, animFactory, focusedQuad, target);


                                curAnimations.put(target,
                                        animFactory.create(target)
                                                .setRequestLayoutOnTargetChange(false)
                                                .addOnFinish(new Animation.OnFinish() {
                                                    @Override
                                                    public void finished(final Animation animation) {
                                                        WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                curAnimations.remove(animation.getTarget());
                                                            }
                                                        });

                                                    }
                                                })
                                                .start());
                            }
                        }
                        focusedQuad = focused ? target : null;
                    }
        }

        @Override
        public void onLongFocus(ListWidget list, int dataIndex) {
            Log.i(Log.SUBSYSTEM.FOCUS, TAG, TAG + ".onLongFocus: " + dataIndex);
        }
    };

    private Widget focusedQuad;
    private Map<Widget, Animation> curAnimations = new HashMap<>();
    private boolean mFocusAnimationEnabled = true;
    private final AnimationFactory.Factory mFocusAnimationFactory;
    private final AnimationFactory.Factory mDefocusAnimationFactory;

    private static final float DURATION_ANIMATION_FOCUSED_SCALE_SECS = 0.2f;
    private static final float SCALE_FOCUSED_QUAD = 1.2f;

    private static JSONObject sFocusAnimationSpec = new JSONObject();
    private static JSONObject sDefocusAnimationSpec = new JSONObject();

    static {
        put(sFocusAnimationSpec, AnimationFactory.Properties.type, AnimationFactory.Type.SCALE);
        put(sFocusAnimationSpec, Animation.Properties.duration, DURATION_ANIMATION_FOCUSED_SCALE_SECS);
        put(sFocusAnimationSpec, ScaleAnimation.Properties.scale, new Vector3f(SCALE_FOCUSED_QUAD, SCALE_FOCUSED_QUAD, 1));

        put(sDefocusAnimationSpec, AnimationFactory.Properties.type, AnimationFactory.Type.SCALE);
        put(sDefocusAnimationSpec, Animation.Properties.duration, DURATION_ANIMATION_FOCUSED_SCALE_SECS);
        put(sDefocusAnimationSpec, ScaleAnimation.Properties.scale, new Vector3f(1f, 1f, 1));
    }

    @SuppressWarnings("unused")
    private static final String TAG = PickerWidget.class.getSimpleName();
}
