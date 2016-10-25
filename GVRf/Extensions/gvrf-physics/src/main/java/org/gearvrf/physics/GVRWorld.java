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

import android.util.LongSparseArray;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSceneObject.ComponentVisitor;
import org.gearvrf.ISceneObjectEvents;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Represents a physics world where all {@link GVRSceneObject} with {@link GVRRigidBody} component
 * attached to are simulated.
 * <p>
 * {@link GVRWorld} is a component that must be attached to the scene's root object.
 */
public class GVRWorld extends GVRBehavior implements ISceneObjectEvents, ComponentVisitor {

    static {
        System.loadLibrary("gvrf-physics");
    }

    private final LongSparseArray<GVRRigidBody> mRigidBodies = new LongSparseArray<GVRRigidBody>();
    private final LinkedList<GVRCollisionInfo> mPreviousCollisions = new LinkedList<GVRCollisionInfo>();

    public GVRWorld(GVRContext gvrContext) {
        super(gvrContext, NativePhysics3DWorld.ctor());
        mHasFrameCallback = false;
    }

    static public long getComponentType() {
        return NativePhysics3DWorld.getComponentType();
    }

    /**
     * Returns true if the physics world contains the the specified rigid body.
     *
     * @param rigidBody Rigid body the to check if it is present in the world.
     * @return true if the world contains the specified rigid body.
     */
    public boolean contains(GVRRigidBody rigidBody) {
        return mRigidBodies.get(rigidBody.getNative()) != null;
    }

    /**
     * Add a {@link GVRRigidBody} to this physics world.
     *
     * @param gvrBody The {@link GVRRigidBody} to add.
     */
    public void addBody(GVRRigidBody gvrBody) {
        if (!contains(gvrBody)) {
            NativePhysics3DWorld.addRigidBody(getNative(), gvrBody.getNative());
            mRigidBodies.put(gvrBody.getNative(), gvrBody);
        }
    }

    /**
     * Remove a {@link GVRRigidBody} from this physics world.
     *
     * @param gvrBody the {@link GVRRigidBody} to remove.
     */
    public void removeBody(GVRRigidBody gvrBody) {
        if (contains(gvrBody)) {
            NativePhysics3DWorld.removeRigidBody(getNative(), gvrBody.getNative());
            mRigidBodies.remove(gvrBody.getNative());
        }
    }

    @Override
    public void onDrawFrame(float frameTime) {
        NativePhysics3DWorld.step(getNative(), frameTime);

        generateCollisionEvents();
    }

    private void generateCollisionEvents() {
        GVRCollisionInfo collisionInfos[] = NativePhysics3DWorld.listCollisions(getNative());

        String eventName = "onEnter";
        for (GVRCollisionInfo info : collisionInfos) {

            if (mPreviousCollisions.contains(info)) {
                //eventName = "onInside";
                mPreviousCollisions.remove(info);
            } else {
                sendCollisionEvent(info, eventName);
            }
        }

        eventName = "onExit";
        for (GVRCollisionInfo cp: mPreviousCollisions) {
            sendCollisionEvent(cp, eventName);
        }

        mPreviousCollisions.clear();
        Collections.addAll(mPreviousCollisions, collisionInfos);
    }

    private void sendCollisionEvent(GVRCollisionInfo info, String eventName) {
        GVRSceneObject bodyA = mRigidBodies.get(info.bodyA).getOwnerObject();
        GVRSceneObject bodyB = mRigidBodies.get(info.bodyB).getOwnerObject();

        getGVRContext().getEventManager().sendEvent(bodyA, ICollisionEvents.class, eventName,
                bodyA, bodyB, info.normal, info.distance);

        getGVRContext().getEventManager().sendEvent(bodyB, ICollisionEvents.class, eventName,
                bodyB, bodyA, info.normal, info.distance);
    }

    private void doPhysicsAttach(GVRSceneObject rootSceneObject) {
        if (!mHasFrameCallback) {
            rootSceneObject.getEventReceiver().addListener(this);
        } else if (isEnabled()){
            startListening();
        }
        rootSceneObject.forAllComponents(this, GVRRigidBody.getComponentType());
    }

    private void doPhysicsDetach(GVRSceneObject rootSceneObject) {
        if (!mHasFrameCallback) {
            rootSceneObject.getEventReceiver().removeListener(this);
        }
        if (isEnabled()) {
            stopListening();
        }
        rootSceneObject.forAllComponents(this, GVRRigidBody.getComponentType());
    }

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);

        //FIXME: Implement a way to check if already exists a GVRWold attached to the scene
        if (newOwner.getParent() != null) {
            throw new RuntimeException("GVRWold must be attached to the scene's root object!");
        }

        doPhysicsAttach(newOwner);
    }

    @Override
    public void onDetach(GVRSceneObject oldOwner) {
        super.onDetach(oldOwner);

        doPhysicsDetach(oldOwner);
    }

    @Override
    public void onInit(GVRContext gvrContext, GVRSceneObject sceneObject) {
        if (mHasFrameCallback)
            return;

        mHasFrameCallback = true;
        getOwnerObject().getEventReceiver().removeListener(this);

        if (isEnabled()) {
            startListening();
        }
    }

    @Override
    public void onLoaded() {

    }

    @Override
    public void onAfterInit() {

    }

    @Override
    public void onStep() {

    }

    @Override
    public boolean visit(GVRComponent gvrComponent) {
        if (!gvrComponent.isEnabled()) {
            return false;
        }
        if (this.owner != null) {
            addBody((GVRRigidBody) gvrComponent);
        } else {
            removeBody((GVRRigidBody) gvrComponent);
        }
        return true;
    }
}

class NativePhysics3DWorld {
    static native long ctor();

    static native long getComponentType();

    static native boolean addRigidBody(long jphysics_world, long jrigid_body);

    static native void removeRigidBody(long jphysics_world, long jrigid_body);

    static native void step(long jphysics_world, float jtime_step);

    static native GVRCollisionInfo[] listCollisions(long jphysics_world);
}
