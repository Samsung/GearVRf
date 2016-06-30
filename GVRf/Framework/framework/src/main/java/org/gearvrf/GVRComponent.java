/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf;

import java.util.List;

import org.gearvrf.utility.Exceptions;

/**
 * Base class for defining components to extend the scene object.
 *
 * Components are used to add behaviours to scene objects.
 * A GVRSceneObject can have any number of components but only
 * one component of each type. Usually the component type loosely
 * corresponds to the base class of the component. For example,
 * GVRCamera and GVRCameraRig have different component types.
 * GVROrthographicCamera and GVRPerspectiveCamera both have the
 * same type. All of the light classes have the same type as well.
 * 
 * @see GVRSceneObject.attachComponent
 * @see GVRSceneObject.getComponent
 */
public class GVRComponent extends GVRHybridObject {
    protected boolean mIsEnabled;
    protected long mType = 0;
    
    /**
     * Constructor for a component that is not attached to a scene object.
     *
     * @param gvrContext    The current GVRF context
     * @param nativePointer Pointer to the native object, returned by the native constructor
     */
    protected GVRComponent(GVRContext gvrContext, long nativeConstructor) {
        super(gvrContext, nativeConstructor);
        mIsEnabled = true;
    }
    
    /**
     * Special constructor, for descendants like {#link GVRMeshEyePointee} that
     * need to 'unregister' instances.
     * 
     * @param gvrContext
     *            The current GVRF context
     * @param nativePointer
     *            The native pointer, returned by the native constructor
     * @param cleanupHandlers
     *            Cleanup handler(s).
     * 
     * Normally, this will be a {@code private static} class
     * constant, so that there is only one {@code List} per class.
     * Descendants that supply a {@code List} and <em>also</em> have
     * descendants that supply a {@code List} should use
     * {@link CleanupHandlerListManager} to maintain a
     * {@code Map<List<NativeCleanupHandler>, List<NativeCleanupHandler>>}
     * whose keys are descendant lists and whose values are unique
     * concatenated lists - see {@link GVREyePointeeHolder} for an example.
     */
    protected GVRComponent(GVRContext gvrContext, long nativePointer,
            List<NativeCleanupHandler> cleanupHandlers) {
        super(gvrContext, nativePointer, cleanupHandlers);
    }

    protected GVRSceneObject owner;

    /**
     * @return The {@link GVRSceneObject} this object is currently attached to, or null if not attached.
     */
    public GVRSceneObject getOwnerObject() {
        return owner;
    }

    /***
     * Attach this component to a scene object.
     * @param owner scene object to become new owner.
     */
    public void setOwnerObject(GVRSceneObject owner) {
        if (owner != null)
        {
            if (getNative() != 0)
            {
                NativeComponent.setOwnerObject(getNative(), owner.getNative());
            }
            onAttach(owner);
        }
        else
        {
            onDetach(getOwnerObject());
            if (getNative() != 0)
            {
                NativeComponent.setOwnerObject(getNative(), 0L);
            }
        }
        this.owner = owner;
    }

    /**
     * Checks if the {@link GVRComponent} is attached to a {@link GVRSceneObject}.
     *
     * @return true if a {@link GVRSceneObject} is attached, else false.
     */
    public boolean hasOwnerObject() {
        return owner != null;
    }
    
    /**
     * Enable or disable this component.
     * @param flag true to enable, false to disable.
     * @see enable
     * @see disable
     * @see isEnabled
     */
    public void setEnable(boolean flag) {
        mIsEnabled = flag;
        if (getNative() != 0)
        {
            NativeComponent.setEnable(getNative(), flag);
        }
        if (flag)
        {
            onEnable();
        }
        else
        {
            onDisable();
        }
    }
    
    /**
     * Enable the component so it will be active in the scene.
     */
    public void enable() {
        setEnable(true);
    }

    /**
     * Disable the component so it will not be active in the scene.
     */
    public void disable() {
        setEnable(false);
    }
    
    /**
     * Get the enable/disable status for the component.
     * 
     * @return true if component is enabled, false if component is disabled.
     */
    public boolean isEnabled() {
        return mIsEnabled;
    }
    
    /**
     * Get the type of this component.
     * @return component type
     */
    public long getType() {
        if (getNative()!= 0) {
            return NativeComponent.getType(getNative());
        }
        return mType;
    }
    
    /**
     * Get the transform of the scene object this component is attached to.
     * 
     * @return GVRTransform of scene object
     */
    public GVRTransform getTransform() {
        return getOwnerObject().getTransform();
    }
    
    /**
     * Get the component of the specified class attached to the owner scene object.
     * 
     * If the scene object that owns this component also has a component
     * of the given type, it will be returned.
     * @return GVRComponent of requested type or null if none exists.
     */
    public GVRComponent getComponent(long type) {
        return getOwnerObject().getComponent(type);
    }
    
    /**
     * Called when a component is attached to a scene object.
     * 
     * @param newOwner  GVRSceneObject the component is attached to.
     */
    public void onAttach(GVRSceneObject newOwner) { }

    /**
     * Called when a component is detached from a scene object.
     * 
     * @param oldOwner  GVRSceneObject the component was detached from.
     */
    public void onDetach(GVRSceneObject oldOwner) { }
    
    /**
     * Called when a component is enabled.
     */
    public void onEnable() { }
    
    /**
     * Called when a component is disabled.
     */
    public void onDisable() { }
}

class NativeComponent {
    static native long getType(long component);
    static native void setOwnerObject(long component, long owner);
    static native boolean isEnabled(long component);
    static native void setEnable(long component, boolean flag);
}

