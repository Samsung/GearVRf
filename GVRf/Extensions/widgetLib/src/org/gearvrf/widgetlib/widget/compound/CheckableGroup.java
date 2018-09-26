package org.gearvrf.widgetlib.widget.compound;

import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.NodeEntry;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.basic.Checkable;
import org.gearvrf.widgetlib.widget.layout.Layout;
import org.gearvrf.widgetlib.widget.layout.OrientedLayout;
import org.gearvrf.widgetlib.widget.layout.basic.LinearLayout;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.gearvrf.widgetlib.widget.properties.JSONHelpers.optInt;

/**
 * Set of Checkable widgets 
 */
public class CheckableGroup extends GroupWidget {

    // Widgets are referenced by name; we have no "resource ID" infrastructure

    /**
     * Interface definition for a listener to be invoked when the checked button changed in this group.
     */
    public interface OnCheckChangedListener {
        /**
         * Called when the checked button has changed. When the selection is cleared, checkedId is -1.
         * @param group the group in which the checked button has changed
         * @param checkedWidget check button changed the state
         * @param checkableIndex  the unique identifier of the newly checked button in range [0, size),
         *                        where size is the number of Checkable widgets in this group
         * @param <T>
         */
        <T extends Widget & Checkable> void onCheckChanged(CheckableGroup group, T checkedWidget,
                                                           int checkableIndex);
    }

    /**
     * Core {@link CheckableGroup} constructor.
     *
     * @param context A valid {@link GVRContext}.
     * @param properties A structured set of properties for the {@code GroupWidget} instance. See
     *                       {@code widget.json} for schema.
     */
    public CheckableGroup(GVRContext context, JSONObject properties) {
        super(context, properties);
        init();
    }

    /**
     * Construct a wrapper for an existing {@link GVRSceneObject}.
     *
     * @param context
     *            The current {@link GVRContext}.
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     */
    public CheckableGroup(GVRContext context, GVRSceneObject sceneObject) {
        super(context, sceneObject);
        init();
    }

    /**
     * A constructor for wrapping existing {@link GVRSceneObject} instances.
     *
     * @param context
     *            The current {@link GVRContext}
     * @param sceneObject
     *            The {@link GVRSceneObject} to wrap.
     * @param attributes
     *            TODO
     * @throws InstantiationException
     */
    @Deprecated
    public CheckableGroup(GVRContext context, GVRSceneObject sceneObject, NodeEntry attributes)
            throws InstantiationException {
        super(context, sceneObject, attributes);
        init();
        String attr = attributes.getProperty(CheckableGroupProperties.checkedIndex);
        if (attr != null) {
            int checkedIndex = Integer.parseInt(attr);
            check(checkedIndex);
        }
    }

    /**
     * Construct a new {@link CheckableGroup}.
     *
     * @param context
     *            A valid {@link GVRContext} instance.
     * @param width
     * @param height
     */
    public CheckableGroup(GVRContext context, float width, float height) {
        super(context, width, height);
        init();
    }

    @Override
    public boolean addChild(Widget child, int index, boolean preventLayout) {
        boolean added = super.addChild(child, index, preventLayout);
        if (added && child instanceof Checkable) {
            ((Checkable) child).addOnCheckChangedListener(mCheckChangedListener);
        }
        return added;
    }

    @Override
    public Layout getDefaultLayout() {
        return mDefaultLayout;
    }

    @Override
    public boolean removeChild(Widget child, boolean preventLayout) {
        boolean removed = super.removeChild(child, preventLayout);
        if (removed && child instanceof Checkable) {
            ((Checkable) child).removeOnCheckChangedListener(mCheckChangedListener);
        }
        return removed;
    }

    /**
     * Add a listener to be invoked when the checked button changes in this group.
     * @param listener
     * @param <T>
     * @return
     */
    public <T extends Widget & Checkable> boolean addOnCheckChangedListener
            (OnCheckChangedListener listener) {
        final boolean added;
        synchronized (mListeners) {
            added = mListeners.add(listener);
        }
        if (added) {
            List<T> c = getCheckableChildren();
            for (int i = 0; i < c.size(); ++i) {
                listener.onCheckChanged(this, c.get(i), i);
            }
        }
        return added;
    }

    /**
     * Remove a listener
     * @param listener
     * @return
     */
    public boolean removeOnCheckChangedListener(OnCheckChangedListener listener) {
        synchronized (mListeners) {
            return mListeners.remove(listener);
        }
    }

    /**
     * Set the specified {@link Checkable} {@link Widget} as checked, if it is a child of this
     * {@link CheckableGroup} and not already checked.
     *
     * @param checkableWidget The {@code Checkable Widget} to
     *                        {@linkplain Checkable#setChecked(boolean) set checked}.
     * @return {@code True} if {@code checkableWidget} is a child of this {@code CheckableGroup} and
     * was not already checked; {@code false} otherwise.
     */
    public <T extends Widget & Checkable> boolean check(T checkableWidget) {
        if (hasChild(checkableWidget)) {
            return checkInternal(checkableWidget, true);
        }

        return false;
    }

    /**
     * Checks the widget by index
     * @param checkableIndex The index is in the range from 0 to size -1, where size is the number of
     *                       Checkable widgets in the group. It does not take into account any
     *                       non-Checkable widgets added to the group widget.
     * @return {@code True} if {@code checkableWidget} is a child of this {@code CheckableGroup} and
     * was not already checked; {@code false} otherwise.
     */
    public <T extends Widget & Checkable> boolean check(int checkableIndex) {
        List<T> children = getCheckableChildren();
        T checkableWidget = children.get(checkableIndex);
        return checkInternal(checkableWidget, true);
    }

    /**
     * Set the specified {@link Checkable} {@link Widget} as unchecked, if it is a child of this
     * {@link CheckableGroup} and not already unchecked.
     * @param checkableWidget The {@code Checkable Widget} to
     *                        {@linkplain Checkable#setChecked(boolean) set unchecked}.
     * @return {@code True} if {@code checkableWidget} is a child of this {@code CheckableGroup} and
     * was not already unchecked; {@code false} otherwise.
     */

    public <T extends Widget & Checkable> boolean uncheck(T checkableWidget) {
        if (hasChild(checkableWidget)) {
            return checkInternal(checkableWidget, false);
        }
        return false;
    }

    /**
     * Unchecks the widget by index
     * @param checkableIndex The index is in the range from 0 to size -1, where size is the number of
     *                       Checkable widgets in the group. It does not take into account any
     *                       non-Checkable widgets added to the group widget.
     * @return {@code True} if {@code checkableWidget} is a child of this {@code CheckableGroup} and
     * was not already unchecked; {@code false} otherwise.
     */
    public <T extends Widget & Checkable> boolean uncheck(int checkableIndex) {
        List<T> children = getCheckableChildren();
        T checkableWidget = children.get(checkableIndex);
        return checkInternal(checkableWidget, false);
    }

    /**
     * Clears all checked widgets in the group
     */
    public <T extends Widget & Checkable> void clearChecks() {
        List<T> children = getCheckableChildren();
        for (T c : children) {
            c.setChecked(false);
        }
    }

    /**
     * Gets all checked widgets in the group
     * @return list of checked widgets
     */
    public <T extends Widget & Checkable> List<T> getCheckedWidgets() {
        List<T> checked = new ArrayList<>();

        for (Widget c : getChildren()) {
            if (c instanceof Checkable && ((Checkable) c).isChecked()) {
                checked.add((T) c);
            }
        }

        return checked;
    }

    /**
     * Gets all checked widget indexes in the group. The indexes are counted from 0 to size -1,
     * where size is the number of Checkable widgets in the group. It does not take into account
     * any non-Checkable widgets added to the group widget.
     *
     * @return list of checked widget indexes
     */
    public <T extends Widget & Checkable> List<Integer> getCheckedWidgetIndexes() {
        List<Integer> checked = new ArrayList<>();
        List<Widget> children = getChildren();

        final int size = children.size();
        for (int i = 0, j = -1; i < size; ++i) {
            Widget c = children.get(i);
            if (c instanceof Checkable) {
                ++j;
                if (((Checkable) c).isChecked()) {
                    checked.add(j);
                }
            }
        }

        return checked;
    }

    /**
     * Gets all Checkable widgets in the group
     * @return list of Checkable widgets
     */
    public <T extends Widget & Checkable> List<T> getCheckableChildren() {
        List<Widget> children = getChildren();
        ArrayList<T> result = new ArrayList<>();
        for (Widget c : children) {
            if (c instanceof Checkable) {
                result.add((T) c);
            }
        }
        return result;
    }

    /**
     * Gets the number of Checkable widgets in the group
     * @return
     */
    public int getCheckableCount() {
        return getCheckableChildren().size();
    }

    /**
     * Allow or does not allow the multiple checks in the same group.
     * In case of single check group, checking one check button that belongs to a checkable group
     * unchecks any previously checked button within the same group.
     * Initially, all of the buttons are unchecked. While it is not possible to uncheck a particular
     * button, the checkable group can be cleared to remove the checked state.
     * @param allow if it is true the checkable group does not allow to have more than one checked
     *              button in the group. If it is false - any number of buttons can be checked at
     *              the same time.
     */
    public void setAllowMultiCheck(boolean allow) {
        mAllowMultiCheck = allow;
    }

    /**
     * Check if the group is multi check group
     * @return true if any malti checks are allowed, otherwise it return false.
     */
    public boolean getAllowMultiCheck() {
        return mAllowMultiCheck;
    }

    private <T extends Widget & Checkable> boolean checkInternal(T checkableWidget, boolean check) {
        if (checkableWidget.isChecked() != check) {
            checkableWidget.setChecked(check);
            return true;
        }
        return false;
    }

    private void init() {
        JSONObject metadata = getObjectMetadata();
        int checkedIndex = optInt(metadata, CheckableGroupProperties.checkedIndex, -1);
        if (checkedIndex >= 0) {
            check(checkedIndex);
        }
        mDefaultLayout.setOrientation(OrientedLayout.Orientation.VERTICAL);
    }

    private <T extends Widget & Checkable> void onCheckChanged(Checkable checkable) {
        if (mProtectFromCheckChanged) {
            return;
        }

        mProtectFromCheckChanged = true;
        if (!mAllowMultiCheck) {
            if (!checkable.isChecked()) {
                if (getCheckedWidgetIndexes().size() == 0) {
                    checkable.setChecked(true);
                }
            } else {
                List<T> children = getCheckableChildren();
                for (Widget w : children) {
                    Checkable c = (Checkable) w;
                    if (c != checkable) {
                        c.setChecked(false);
                    }
                }
            }
        }
        mProtectFromCheckChanged = false;

        notifyOnCheckChanged((T) checkable);
    }

    protected <T extends Widget & Checkable> void notifyOnCheckChanged(final T checkableWidget) {
        final Object[] listeners;
        synchronized (mListeners) {
            listeners = mListeners.toArray();
        }
        int index = getCheckableChildren().indexOf(checkableWidget);
        for (Object listener : listeners) {
            ((OnCheckChangedListener) listener).onCheckChanged(this, checkableWidget, index);
        }
    }

    private enum CheckableGroupProperties {
        checkedIndex
    }

    private boolean mAllowMultiCheck;
    private Set<OnCheckChangedListener> mListeners = new LinkedHashSet<>();
    private boolean mProtectFromCheckChanged;
    private LinearLayout mDefaultLayout = new LinearLayout();

    private Checkable.OnCheckChangedListener mCheckChangedListener = new Checkable
            .OnCheckChangedListener() {
        @Override
        public void onCheckChanged(Checkable checkable, boolean checked) {
            CheckableGroup.this.onCheckChanged(checkable);
        }
    };
}
