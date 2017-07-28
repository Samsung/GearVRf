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

    /**
     * Get the X component of the transform's position.
     *
     * @return 'X' component of the transform's position.
     */
    public float getCenterX() {
        return Native3DRigidBody.getCenterX(getNative());
    }

    /**
     * Get the 'Y' component of the transform's position.
     *
     * @return 'Y' component of the transform's position.
     */
    public float getCenterY() {
        return Native3DRigidBody.getCenterY(getNative());
    }

    /**
     * Get the 'Z' component of the transform's position.
     *
     * @return 'Z' component of the transform's position.
     */
    public float getCenterZ() {
        return Native3DRigidBody.getCenterZ(getNative());
    }

    /**
     * Set absolute position.
     * <p>
     * Use {@link #setCenter(float, float, float)} to <em>move</em> the object.
     *
     * @param x 'X' component of the absolute position.
     * @param y 'Y' component of the absolute position.
     * @param z 'Z' component of the absolute position.
     */
    public void setCenter(float x, float y, float z) {
        Native3DRigidBody.setCenter(getNative(), x, y, z);
    }

    /**
     * Get the quaternion 'W' component.
     *
     * @return 'W' component of the transform's rotation, treated as a
     * quaternion.
     */
    public float getRotationW() {
        return Native3DRigidBody.getRotationW(getNative());
    }

    /**
     * Get the quaternion 'X' component.
     *
     * @return 'X' component of the transform's rotation, treated as a
     * quaternion.
     */
    public float getRotationX() {
        return Native3DRigidBody.getRotationX(getNative());
    }

    /**
     * Get the quaternion 'Y' component.
     *
     * @return 'Y' component of the transform's rotation, treated as a
     * quaternion.
     */
    public float getRotationY() {
        return Native3DRigidBody.getRotationY(getNative());
    }

    /**
     * Get the quaternion 'Z' component.
     *
     * @return 'Z' component of the transform's rotation, treated as a
     * quaternion.
     */
    public float getRotationZ() {
        return Native3DRigidBody.getRotationZ(getNative());
    }

    /**
     * Set rotation, as a quaternion.
     * <p>
     * Sets the transform's current rotation in quaternion terms.
     *
     * @param w 'W' component of the quaternion.
     * @param x 'X' component of the quaternion.
     * @param y 'Y' component of the quaternion.
     * @param z 'Z' component of the quaternion.
     */
    public void setRotation(float w, float x, float y, float z) {
        Native3DRigidBody.setRotation(getNative(), w, x, y, z);
    }

    /**
     * Get the 'X' scale
     *
     * @return The transform's current scaling on the 'X' axis.
     */
    public float getScaleX() {
        return Native3DRigidBody.getScaleX(getNative());
    }

    /**
     * Get the 'Y' scale
     *
     * @return The transform's current scaling on the 'Y' axis.
     */
    public float getScaleY() {
        return Native3DRigidBody.getScaleY(getNative());
    }

    /**
     * Get the 'Z' scale
     *
     * @return The transform's current scaling on the 'Z' axis.
     */
    public float getScaleZ() {
        return Native3DRigidBody.getScaleZ(getNative());
    }

    /**
     * Set [X, Y, Z] current scale
     *
     * @param x Scaling factor on the 'X' axis.
     * @param y Scaling factor on the 'Y' axis.
     * @param z Scaling factor on the 'Z' axis.
     */
    public void setScale(float x, float y, float z) {
        Native3DRigidBody.setScale(getNative(), x, y, z);
    }

    /**
     * Apply a central impulse vector [X, Y, Z] to this {@linkplain GVRRigidBody rigid body}
     *
     * @param x factor on the 'X' axis.
     * @param y factor on the 'Y' axis.
     * @param z factor on the 'Z' axis.
     */
    public void applyCentralForce(float x, float y, float z) {
        Native3DRigidBody.applyCentralForce(getNative(), x, y, z);
    }

    /**
     * Apply a torque vector [X, Y, Z] to this {@linkplain GVRRigidBody rigid body}
     *
     * @param x factor on the 'X' axis.
     * @param y factor on the 'Y' axis.
     * @param z factor on the 'Z' axis.
     */
    public void applyTorque(float x, float y, float z) {
        Native3DRigidBody.applyTorque(getNative(), x, y, z);
    }

    /**
     * Sets a particular acceleration vector [X, Y, Z] on this {@linkplain GVRRigidBody rigid body}
     *
     * @param x factor on the 'X' axis.
     * @param y factor on the 'Y' axis.
     * @param z factor on the 'Z' axis.
     */
    public void setGravity(float x, float y, float z) {
        Native3DRigidBody.setGravity(getNative(), x, y, z);
    }

    /**
     * Sets linear and angular damping on this {@linkplain GVRRigidBody rigid body}
     *
     * @param linear factor on how much the rigid body resists translation.
     * @param angular factor on how much the rigid body resists rotation.
     */
    public void setDamping(float linear, float angular) {
        Native3DRigidBody.setDamping(getNative(), linear, angular);
    }

    /**
     * Sets a linear velocity [X, Y, Z] on this {@linkplain GVRRigidBody rigid body}
     *
     * @param x factor on the 'X' axis.
     * @param y factor on the 'Y' axis.
     * @param z factor on the 'Z' axis.
     */
    public void setLinearVelocity(float x, float y, float z) {
        Native3DRigidBody.setLinearVelocity(getNative(), x, y, z);
    }

    /**
     * Sets an angular velocity [X, Y, Z] on this {@linkplain GVRRigidBody rigid body}
     *
     * @param x factor on the 'X' axis.
     * @param y factor on the 'Y' axis.
     * @param z factor on the 'Z' axis.
     */
    public void setAngularVelocity(float x, float y, float z) {
        Native3DRigidBody.setAngularVelocity(getNative(), x, y, z);
    }

    /**
     * Sets an angular factor [X, Y, Z] that influences torque on this {@linkplain GVRRigidBody rigid body}
     *
     * @param x factor on the 'X' axis.
     * @param y factor on the 'Y' axis.
     * @param z factor on the 'Z' axis.
     */
    public void setAngularFactor(float x, float y, float z) {
        Native3DRigidBody.setAngularFactor(getNative(), x, y, z);
    }

    /**
     * Sets an linear factor [X, Y, Z] that influences forces acting on this {@linkplain GVRRigidBody rigid body}
     *
     * @param x factor on the 'X' axis.
     * @param y factor on the 'Y' axis.
     * @param z factor on the 'Z' axis.
     */
    public void setLinearFactor(float x, float y, float z) {
        Native3DRigidBody.setLinearFactor(getNative(), x, y, z);
    }

    /**
     * Sets SleepingTresholds that, when reached, influence the deactivation of this {@linkplain GVRRigidBody rigid body}
     *
     * @param linear factor for the linearVelocity
     * @param angular factor for the angularVelocity
     */
    public void setSleepingThresholds(float linear, float angular) {
        Native3DRigidBody.setSleepingThresholds(getNative(), linear, angular);
    }

    /**
     * Set a {@linkplain GVRRigidBody rigid body} to be ignored (true) or not (false)
     *
     * @param collisionObject rigidbody object on the collision check
     * @param ignore boolean to indicate if the specified object will be ignored or not
     */
    public void setIgnoreCollisionCheck(GVRRigidBody collisionObject, boolean ignore) {
        Native3DRigidBody.setIgnoreCollisionCheck(getNative(), collisionObject.getNative(), ignore);
    }

    /**
     * Returns the gravity acceleration float array [x,y,z] on this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The gravity acceleration vector as a float array
     */
    public float[] getGravity() {
        return Native3DRigidBody.getGravity(getNative());
    }

    /**
     * Returns the linear velocity float array [x,y,z] on this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The linear velocity vector as a float array
     */
    public float[] getLinearVelocity() {
        return Native3DRigidBody.getLinearVelocity(getNative());
    }

    /**
     * Returns the angular velocity float array [x,y,z] on this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The angular velocity vector as a float array
     */
    public float[] getAngularVelocity() {
        return Native3DRigidBody.getAngularVelocity(getNative());
    }

    /**
     * Returns the angular factor float array [x,y,z] on this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The angular factor vector as a float array
     */
    public float[] getAngularFactor() {
        return Native3DRigidBody.getAngularFactor(getNative());
    }

    /**
     * Returns the linear factor float array [x,y,z] on this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The linear factor vector as a float array
     */
    public float[] getLinearFactor() {
        return Native3DRigidBody.getLinearFactor(getNative());
    }

    /**
     * Returns the damping factors [angular,linear] on this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The damping factors as a float array
     */
    public float[] getDamping() {
        return Native3DRigidBody.getDamping(getNative());
    }

    /**
     * Returns the friction factor on this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The friction factor scalar as a float
     */
    public float getFriction() {
        return Native3DRigidBody.getFriction(getNative());
    }

    /**
     * Set the friction factor of this {@linkplain GVRRigidBody rigid body}
     *
     * @param n the friction factor
     */
    public void setFriction(float n) {
        Native3DRigidBody.setFriction(getNative(), n);
    }

    /**
     * Returns the restitution factor on this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The restitution factor scalar as a float
     */
    public float getRestitution() {
        return Native3DRigidBody.getRestitution(getNative());
    }

    /**
     * Set the restitution factor of this {@linkplain GVRRigidBody rigid body}
     *
     * @param n the restitution factor
     */
    public void setRestitution(float n) {
        Native3DRigidBody.setRestitution(getNative(), n);
    }

    /**
     * Returns the continous collision detection motion threshold factor on this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The continous collision detection motion threshold factor scalar as a float
     */
    public float getCcdMotionThreshold() {
        return Native3DRigidBody.getCcdMotionThreshold(getNative());
    }

    /**
     * Set the continous collision detection motion threshold factor of this {@linkplain GVRRigidBody rigid body}
     *
     * @param n the continous collision detection motion threshold factor
     */
    public void setCcdMotionThreshold(float n) {
        Native3DRigidBody.setCcdMotionThreshold(getNative(), n);
    }

    /**
     * Returns the contact processing threshold factor for this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The contact processing threshold factor scalar as a float
     */
    public float getContactProcessingThreshold() {
        return Native3DRigidBody.getContactProcessingThreshold(getNative());
    }

    /**
     * Set the  contact processing threshold factor of this {@linkplain GVRRigidBody rigid body}
     *
     * @param n the contact processing threshold factor
     */
    public void setContactProcessingThreshold(float n) {
        Native3DRigidBody.setContactProcessingThreshold(getNative(), n);
    }

    /**
     * Returns the collision group of this {@linkplain GVRRigidBody rigid body}.
     *
     * @return The collision group id as an int
     */
    public int getCollisionGroup() {
        return mCollisionGroup;
    }

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);

        if (newOwner.getCollider() == null) {
            throw new RuntimeException("GVRRigidBody depends of a Collider attached to it's SceneObject");
        }

        doPhysicsAttach(newOwner);
    }

    @Override
    public void onDetach(GVRSceneObject oldOwner) {
        super.onDetach(oldOwner);

        doPhysicsDetach(oldOwner);
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

    private void doPhysicsAttach(GVRSceneObject newOwner) {
        Native3DRigidBody.onAttach(getNative());

        if (isEnabled()) {
            addToWorld(getWorld(newOwner));
        }
    }

    private void doPhysicsDetach(GVRSceneObject oldOwner) {
        if (isEnabled()) {
            removeFromWorld(getWorld(oldOwner));
        }

        Native3DRigidBody.onDetach(getNative());
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

    static native void onAttach(long jrigid_body);

    static native void onDetach(long jrigid_body);

    static native float getCenterX(long jrigid_body);

    static native float getCenterY(long jrigid_body);

    static native float getCenterZ(long jrigid_body);

    static native void setCenter(long jrigid_body, float x, float y, float z);

    static native float getRotationW(long jrigid_body);

    static native float getRotationX(long jrigid_body);

    static native float getRotationY(long jrigid_body);

    static native float getRotationZ(long jrigid_body);

    static native void setRotation(long jrigid_body, float w, float x, float y, float z);

    static native float getScaleX(long jrigid_body);

    static native float getScaleY(long jrigid_body);

    static native float getScaleZ(long jrigid_body);

    static native void setScale(long jrigid_body, float x, float y, float z);

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
}
