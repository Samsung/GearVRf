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

package org.gearvrf.physics;

import org.gearvrf.GVRCollider;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

/**
 * Represents a rigid body that can be static or dynamic. You can set a mass and apply some
 * physics forces.
 * <p>
 * By default it is a static body with infinity mass, value 0, and does not move under simulation.
 * A dynamic body with a mass defined is fully simulated.
 * <p>
 * Every {@linkplain org.gearvrf.GVRSceneObject scene object} can represent a rigid body since
 * it has a {@link GVRRigidBody} component attached to.
 *
 * You must setup the values of owner's {@link org.gearvrf.GVRTransform}, like initial position,
 * and the mass value of the rigid body before attach it to its owner.
 */
public class GVRRigidBody extends GVRComponent {
    public static final int DYNAMIC  = 0;
    public static final int STATIC = 1;
    public static final int KINEMATIC = 2;

    static {
        System.loadLibrary("gvrf-physics");
    }

    private final int mCollisionGroup;

    /**
     * Constructs new instance to simulate a rigid body in {@link GVRWorld}.
     *
     * @param gvrContext The context of the app.
     */
    public GVRRigidBody(GVRContext gvrContext) {
        this(gvrContext, 0.0f);
    }

    /**
     * Constructs new instance to simulate a rigid body in {@link GVRWorld}.
     *
     * @param gvrContext The context of the app.
     * @param mass The mass of this rigid body.
     */
    public GVRRigidBody(GVRContext gvrContext, float mass) {
        this(gvrContext, mass, -1);
    }

    /**
     * Constructs new instance to simulate a rigid body in {@link GVRWorld}.
     *
     * @param gvrContext The context of the app.
     * @param mass The mass of this rigid body.
     * @param collisionGroup The id of the collision's group that this rigid body belongs to
     *                       in the {@link GVRCollisionMatrix}. The rigid body collides with
     *                       everyone if {#collisionGroup} is out of the range 0...15.
     */
    public GVRRigidBody(GVRContext gvrContext, float mass, int collisionGroup) {
        super(gvrContext, Native3DRigidBody.ctor());
        Native3DRigidBody.setMass(getNative(), mass);
        mCollisionGroup = collisionGroup;
    }

    static public long getComponentType() {
        return Native3DRigidBody.getComponentType();
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

    /**
     * Establishes how this rigid body will behave in the simulation.
     *
     * @param type type of simulation desired for the rigid body:
     * <table>
     * <tr><td>DYNAMIC</td><td>Collides with other objects, moved by simulation</td></tr>
     * <tr><td>STATIC</td><td>Collides with other objects, does not move</td></tr>
     * <tr><td>KINEMATIC</td><td>Collides with other objects, moved by application</td></tr>
     * </table>
     */
    public void setSimulationType(int type)
    {
        Native3DRigidBody.setSimulationType(getNative(), type);
    }

    /**
     * Queries how this rigid body will behave in the simulation.
     *
     * @return type of simulation desired for the rigid body
     * <table>
     * <tr><td>DYNAMIC</td><td>Collides with other objects, moved by simulation</td></tr>
     * <tr><td>STATIC</td><td>Collides with other objects, does not move</td></tr>
     * <tr><td>KINEMATIC</td><td>Collides with other objects, moved by application</td></tr>
     * </table>
     */
    public int getSimulationType()
    {
       return Native3DRigidBody.getSimulationType(getNative());
    }

    /**
     * Returns the mass of the body.
     *
     * @return The mass of the body.
     */
    public float getMass() {
        return Native3DRigidBody.getMass(getNative());
    }

    /**
     * Set mass.
     *
     * @param mass The mass to the body.
     */
    public void setMass(float mass) {
        Native3DRigidBody.setMass(getNative(), mass);
    }

    public void applyCentralForce(float x, float y, float z) {
        Native3DRigidBody.applyCentralForce(getNative(), x, y, z);
    }

    public void applyTorque(float x, float y, float z) {
        Native3DRigidBody.applyTorque(getNative(), x, y, z);
    }

    public void setGravity(float x, float y, float z) {
        Native3DRigidBody.setGravity(getNative(), x, y, z);
    }

    public void setDamping(float linear, float angular) {
        Native3DRigidBody.setDamping(getNative(), linear, angular);
    }

    public void setLinearVelocity(float x, float y, float z) {
        Native3DRigidBody.setLinearVelocity(getNative(), x, y, z);
    }

    public void setAngularVelocity(float x, float y, float z) {
        Native3DRigidBody.setAngularVelocity(getNative(), x, y, z);
    }

    public void setAngularFactor(float x, float y, float z) {
        Native3DRigidBody.setAngularFactor(getNative(), x, y, z);
    }

    public void setLinearFactor(float x, float y, float z) {
        Native3DRigidBody.setLinearFactor(getNative(), x, y, z);
    }

    public void setSleepingThresholds(float linear, float angular) {
        Native3DRigidBody.setSleepingThresholds(getNative(), linear, angular);
    }

    public void setIgnoreCollisionCheck(GVRRigidBody collisionObject, boolean ignore) {
        Native3DRigidBody.setIgnoreCollisionCheck(getNative(), collisionObject.getNative(), ignore);
    }

    public float[] getGravity() {
        return Native3DRigidBody.getGravity(getNative());
    }

    public float[] getLinearVelocity() {
        return Native3DRigidBody.getLinearVelocity(getNative());
    }

    public float[] getAngularVelocity() {
        return Native3DRigidBody.getAngularVelocity(getNative());
    }

    public float[] getAngularFactor() {
        return Native3DRigidBody.getAngularFactor(getNative());
    }

    public float[] getLinearFactor() {
        return Native3DRigidBody.getLinearFactor(getNative());
    }

    public float[] getDamping() {
        return Native3DRigidBody.getDamping(getNative());
    }

    public float getFriction() {
        return Native3DRigidBody.getFriction(getNative());
    }

    public void setFriction(float n) {
        Native3DRigidBody.setFriction(getNative(), n);
    }

    public float getRestitution() {
        return Native3DRigidBody.getRestitution(getNative());
    }

    public void setRestitution(float n) {
        Native3DRigidBody.setRestitution(getNative(), n);
    }

    public float getCcdMotionThreshold() {
        return Native3DRigidBody.getCcdMotionThreshold(getNative());
    }

    public void setCcdMotionThreshold(float n) {
        Native3DRigidBody.setCcdMotionThreshold(getNative(), n);
    }

    public float getContactProcessingThreshold() {
        return Native3DRigidBody.getContactProcessingThreshold(getNative());
    }

    public void setContactProcessingThreshold(float n) {
        Native3DRigidBody.setContactProcessingThreshold(getNative(), n);
    }

    public int getCollisionGroup() {
        return mCollisionGroup;
    }

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        if (newOwner.getCollider() == null) {
            throw new UnsupportedOperationException("You must have a collider attached to the scene object before attaching the rigid body");
        }
        if ((newOwner.getRenderData() == null) ||
            (newOwner.getRenderData().getMesh() == null)) {
            throw new UnsupportedOperationException("You must have a mesh attached to the scene object before attaching the rigid body");
        }
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


    private void addToWorld(GVRWorld world) {
        if (world != null) {
            world.addBody(this);
        }
    }

    private void removeFromWorld(GVRWorld world) {
        if (world != null) {
            world.removeBody(this);
        }
    }
}

class Native3DRigidBody {
    static native long ctor();

    static native long getComponentType();

    static native float getMass(long jrigid_body);

    static native void setMass(long jrigid_body, float jmass);

    static native void applyCentralForce(long jrigid_body, float x, float y, float z);

    static native void applyTorque(long jrigid_body, float x, float y, float z);

    static native void setGravity(long jrigid_body, float x, float y, float z);

    static native void setDamping(long jrigid_body, float linear, float angular);

    static native void setLinearVelocity(long jrigid_body, float x, float y, float z);

    static native void setAngularVelocity(long jrigid_body, float x, float y, float z);

    static native void setAngularFactor(long jrigid_body, float x, float y, float z);

    static native void setLinearFactor(long jrigid_body, float x, float y, float z);

    static native void setFriction(long jrigid_body, float n);

    static native void setRestitution(long jrigid_body, float n);

    static native void setSleepingThresholds(long jrigid_body, float linear, float angular);

    static native void setCcdMotionThreshold(long jrigid_body, float n);

    static native void setContactProcessingThreshold(long jrigid_body, float n);

    static native void setIgnoreCollisionCheck(long jrigid_body, long jcollision_object, boolean ignore);

    static native float[] getGravity(long jrigid_body);

    static native float[] getLinearVelocity(long jrigid_body);

    static native float[] getAngularVelocity(long jrigid_body);

    static native float[] getAngularFactor(long jrigid_body);

    static native float[] getLinearFactor(long jrigid_body);

    static native float[] getDamping(long jrigid_body);

    static native float getFriction(long jrigid_body);

    static native float getRestitution(long jrigid_body);

    static native float getCcdMotionThreshold(long jrigid_body);

    static native float getContactProcessingThreshold(long jrigid_body);

    static native int getSimulationType(long jrigid_body);

    static native void setSimulationType(long jrigid_body, int jtype);
}
