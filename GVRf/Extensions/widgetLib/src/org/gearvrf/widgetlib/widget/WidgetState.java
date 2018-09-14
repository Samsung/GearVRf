package org.gearvrf.widgetlib.widget;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import org.gearvrf.widgetlib.log.Log;
import static org.gearvrf.utility.Exceptions.RuntimeAssertion;
import org.gearvrf.widgetlib.widget.basic.Checkable;

/**
 * WidgetState class defines all possible states supported by Widget.
 */
public class WidgetState {
    /**
     * Set of Widget states:
     * {@link State#NORMAL normal} state - default one
     *
     * {@link State#FOCUSED focused} state - {@link FocusManager.Focusable#onFocus} called with
     * {@code True} and the widget currently is in focus.
     *
     * {@link State#SELECTED selected} state - {@link Widget#setSelected} called with {@code True}
     * and the widget is currently selected.See methods {@link Widget#setSelected},
     * {@link Widget#isSelected}
     *
     * {@link State#PRESSED pressed} state - {@link Widget#setPressed} called with {@code True}
     * and the widget is currently pressed. It will happen when the user presses and holds the button.
     * See methods {@link Widget#setPressed}, {@link Widget#isPressed}
     * TODO: incomplete feature! This state is not supported now by input system.
     *
     * {@link State#CHECKED checked} state - {@link Checkable#setChecked} called with {@code True}
     * and the widget is currently checked. {@link State#CHECKED} revolves around a Widget
     * implementing the {@link Checkable} interface. See methods {@link Checkable#setChecked},
     * {@link Checkable#isChecked}, {@link Checkable#toggle}
     *
     * {@link State#DISABLED disabled} state - in this state the interaction with the Widget is
     * very limited. For instance touch/focus/animation is disabled for the basic widget. The
     * specific widget might have more restrictions like {@link Checkable} cannot be
     * checked/unchecked in {@link State#DISABLED}; {@link  ListWidget} is not scrollable, etc..
     * TODO: incomplete feature! This state is not supported now.
     */
    public enum State {
        NORMAL, FOCUSED, SELECTED, PRESSED, CHECKED, DISABLED
    }

    /**
     * Creates WidgetState instance associated with the widget
     * @param widget
     * @param stateSpec
     */
    WidgetState(final Widget widget, final JSONObject stateSpec) {
        mWidget = widget;
        Log.d(TAG, "WidgetState(): states for '%s': %s", widget.getName(), stateSpec);
        if (check(stateSpec, true)) {
            // One or more states are explicitly specified
            loadStates(stateSpec);
        } else if (check(stateSpec, false)) {
            // No explicitly specified state; "NORMAL" is implied
            final WidgetState.State state = State.NORMAL;
            loadState(stateSpec, state);
        } else {
            // Either something is missing, or we've got a mix of fields
            throw RuntimeAssertion("Bad format in state spec for '%s': %s",
                    widget.getName(),
                                      stateSpec);
        }
    }

    /**
     * Gets current state
     * @return current state
     */
    WidgetState.State getState() {
        return mState;
    }

    /**
     * Sets current state
     * @param state new state
     */
    void setState(final WidgetState.State state) {
        Log.d(TAG, "setState(%s): state is %s, setting to %s", mWidget.getName(), mState, state);
        if (state != mState) {
            final WidgetState.State nextState = getNextState(state);
            Log.d(TAG, "setState(%s): next state '%s'", mWidget.getName(), nextState);
            if (nextState != mState) {
                Log.d(TAG, "setState(%s): setting state to '%s'", mWidget.getName(), nextState);
                setCurrentState(false);
                mState = nextState;
                setCurrentState(true);
            }
        }
    }

    private void setCurrentState(boolean set) {
        if (mState != null) {
            final WidgetStateInfo stateInfo = mStates.get(mState);
            if (stateInfo != null) {
                stateInfo.set(mWidget, set);
            }
        }
    }

    private WidgetState.State getNextState(final WidgetState.State state) {
        if (state == null) {
            return null;
        } else if (mStates.containsKey(state)) {
            return state;
        } else {
            // Default to NORMAL for anything that hasn't been specified
            return State.NORMAL;
        }
    }

    private boolean check(JSONObject stateSpec, boolean isExplicit) {
        final boolean hasExplicitStates = has(stateSpec, State.SELECTED)
                || has(stateSpec, State.DISABLED)
                || has(stateSpec, State.FOCUSED)
                || has(stateSpec, State.NORMAL);

        final boolean isImplicitNormal = has(stateSpec,
                                             WidgetStateInfo.Properties.animation)
                || has(stateSpec, WidgetStateInfo.Properties.material)
                || has(stateSpec, WidgetStateInfo.Properties.scene_object);

        return (hasExplicitStates == isExplicit)
                && (isImplicitNormal != isExplicit);
    }

    private boolean has(JSONObject spec, Enum<?> e) {
        return spec.has(e.name().toLowerCase(Locale.ENGLISH));
    }

    private void loadStates(final JSONObject stateSpecs) {
        Log.d(TAG, "loadStates(%s): states: %s", mWidget.getName(), stateSpecs);
        if (stateSpecs != null) {
            Iterator<String> iter = stateSpecs.keys();
            while (iter.hasNext()) {
                final String key = iter.next();
                Log.d(TAG, "loadStates(%s): key: %s", mWidget.getName(), key);
                loadState(stateSpecs, key);
            }
        }
    }

    private void loadState(JSONObject states, String key) {
        try {
            final JSONObject stateSpec = states.getJSONObject(key);
            Log.d(TAG, "loadState(%s): for state '%s': %s", mWidget.getName(), key, stateSpec);
            key = key.toUpperCase(Locale.ENGLISH);
            final WidgetState.State state = State.valueOf(key);
            loadState(stateSpec, state);
        } catch (JSONException e) {
            throw RuntimeAssertion(e, "Invalid state spec for '%s': %s",
                    mWidget.getName(), states.opt(key));
        }
    }

    private void loadState(final JSONObject stateSpec,
            final WidgetState.State state) {
        try {
            mStates.put(state, new WidgetStateInfo(mWidget, stateSpec));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e, "loadState()");
            throw RuntimeAssertion(e,
                                      "Failed to load state '%s' for '%s'",
                                      state, mWidget.getName());
        }
    }

    private WidgetState.State mState = null;
    private Widget mWidget;
    private final Map<WidgetState.State, WidgetStateInfo> mStates = new HashMap<>();

    private final static String TAG = WidgetState.class.getSimpleName();
}
