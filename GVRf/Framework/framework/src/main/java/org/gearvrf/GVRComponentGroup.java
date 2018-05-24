package org.gearvrf;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for components' group.
 *
 * Extend this class to attache components of same type
 * to a scene object.
 */
public class GVRComponentGroup <T extends GVRComponent> extends GVRComponent {
    private final List<T> mChildren = new CopyOnWriteArrayList<>();

    /**
     * Constructor for a component's group.
     *
     * @see GVRComponent#GVRComponent(GVRContext, long)
     */
    protected GVRComponentGroup(GVRContext gvrContext, long componentType)
    {
        super(gvrContext, NativeComponentGroup.ctor(componentType));
        mType = componentType;
    }

    /**
     * Constructor for a component's group.
     *
     * @see GVRComponent#GVRComponent(GVRContext, long)
     */
    protected GVRComponentGroup(GVRContext gvrContext, long nativeGroup, long componentType)
    {
        super(gvrContext, nativeGroup);
        mType = componentType;
    }

    /**
     * Add {@code child} as a child of this object.
     *
     * @param child
     *            {@link GVRComponent component} to add as a child of this
     *            group.
     * @return true if child was added, otherwise false
     */
    protected boolean addChildComponent(T child)
    {
        if (child.mParent == this)
        {
            return false;
        }
        if (child.mParent != null)
        {
            throw new UnsupportedOperationException("GVRComponent cannot have multiple parents");
        }
        if (child.getType() != mType)
        {
            throw new UnsupportedOperationException(child.getClass().getSimpleName() + " components cannot be added to this group");
        }
        mChildren.add(child);
        NativeComponentGroup.addChildComponent(getNative(), child.getNative());
        return true;
    }

    /**
     * Remove {@code child} as a child of this object.
     *
     * @param child
     *            {@link GVRComponent component} to remove as a child of this
     *            group.
     */
    protected void removeChildComponent(T child) {
        mChildren.remove(child);
        NativeComponentGroup.removeChildComponent(getNative(), child.getNative());
    }
}

class NativeComponentGroup {
    static native long ctor(long componentType);
    static native void addChildComponent(long component, long child);
    static native void removeChildComponent(long component, long child);
}