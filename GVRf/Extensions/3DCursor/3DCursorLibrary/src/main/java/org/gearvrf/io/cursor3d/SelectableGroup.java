package org.gearvrf.io.cursor3d;

import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.util.HashSet;
import java.util.Set;

/**
 * A {@link SelectableGroup} "groups" all the {@link GVRSceneObject}s that belong to a given
 * {@link SelectableBehavior} or {@link MovableBehavior}. To make it easier to handle events
 * reported against an object that is marked as selectable or movable, attaching a selectable
 * group would ensure that events generated against that object or its children would only
 * be reported against that root object.
 */
class SelectableGroup extends GVRComponent {
    static private long TYPE_SELECTABLE_GROUP = ((long) SelectableGroup.class.hashCode() << 32) &
            (System.currentTimeMillis() & 0xffffffff);
    private GVRSceneObject parent;

    protected SelectableGroup(GVRContext gvrContext, GVRSceneObject sceneObject) {
        super(gvrContext, 0);
        this.parent = sceneObject;

        mType = TYPE_SELECTABLE_GROUP;
    }

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);
        for (GVRSceneObject child : newOwner.getChildren()) {
            child.attachComponent(this);
        }
    }

    @Override
    public void onDetach(GVRSceneObject oldOwner) {
        super.onDetach(oldOwner);
        for (GVRSceneObject child : oldOwner.getChildren()) {
            child.detachComponent(getComponentType());
        }
    }

    @Override
    public long getType() {
        return super.getType();
    }

    public GVRSceneObject getParent() {
        return parent;
    }

    public static long getComponentType() {
        return TYPE_SELECTABLE_GROUP;
    }
}