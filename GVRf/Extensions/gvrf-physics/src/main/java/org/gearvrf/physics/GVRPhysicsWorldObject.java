package org.gearvrf.physics;

import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.util.List;

abstract class GVRPhysicsWorldObject extends GVRComponent {

    protected GVRPhysicsWorldObject(GVRContext gvrContext, long nativePointer) {
        super(gvrContext, nativePointer);
    }

    protected GVRPhysicsWorldObject(GVRContext gvrContext, long nativePointer, List<NativeCleanupHandler> cleanupHandlers) {
        super(gvrContext, nativePointer, cleanupHandlers);
    }

    /**
     * Returns the {@linkplain GVRWorld physics world} of this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The physics world of this {@link GVRRigidBody}
     */
    public GVRWorld getWorld() {
        return getWorld(getOwnerObject());
    }

    /**
     * Returns the {@linkplain GVRWorld physics world} of the {@linkplain org.gearvrf.GVRScene scene}.
     *
     * @param owner Owner of the {@link GVRRigidBody}
     * @return Returns the {@link GVRWorld} of the scene.
     */
    private static GVRWorld getWorld(GVRSceneObject owner) {
        return getWorldFromAscendant(owner);
    }

    /**
     * Looks for {@link GVRWorld} component in the ascendants of the scene.
     *
     * @param worldOwner Scene object to search for a physics world in the scene.
     * @return Physics world from the scene.
     */
    private static GVRWorld getWorldFromAscendant(GVRSceneObject worldOwner) {
        GVRComponent world = null;

        while (worldOwner != null && world == null) {
            world = worldOwner.getComponent(GVRWorld.getComponentType());
            worldOwner = worldOwner.getParent();
        }

        return (GVRWorld) world;
    }

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);
        if (isEnabled()) {
            addToWorld(getWorld(newOwner));
        }
    }

    @Override
    public void onDetach(GVRSceneObject oldOwner) {
        super.onDetach(oldOwner);
        if (isEnabled()) {
            removeFromWorld(getWorld(oldOwner));
        }
    }

    @Override
    public void onNewOwnersParent(GVRSceneObject newOwnersParent) {
        if (isEnabled()) {
            addToWorld(getWorld(newOwnersParent));
        }
    }

    @Override
    public void onRemoveOwnersParent(GVRSceneObject oldOwnersParent) {
        if (isEnabled()) {
            removeFromWorld(getWorld(oldOwnersParent));
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();

        addToWorld(getWorld());
    }

    @Override
    public void onDisable() {
        super.onDisable();

        removeFromWorld(getWorld());
    }

    abstract protected void removeFromWorld(GVRWorld world);

    abstract protected void addToWorld(GVRWorld world);
}
