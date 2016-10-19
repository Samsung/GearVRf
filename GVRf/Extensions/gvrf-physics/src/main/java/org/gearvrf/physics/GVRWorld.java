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
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ISceneObjectEvents;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Represents a physics world where all {@link GVRSceneObject} with {@link GVRRigidBody} component
 * attached to are simulated.
 * <p>
 * {@link GVRWorld} is a component that must be attached to the scene's root object.
 */

public class GVRWorld extends GVRBehavior implements ISceneObjectEvents {
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

    public boolean addBody(GVRRigidBody gvrBody) {
        mRigidBodies.put(gvrBody.getNative(), gvrBody);
        return NativePhysics3DWorld.addRigidBody(getNative(), gvrBody.getNative());
    }

    public void removeBody(GVRRigidBody gvrBody) {
        mRigidBodies.remove(gvrBody.getNative());
        NativePhysics3DWorld.removeRigidBody(getNative(), gvrBody.getNative());
    }

    @Override
    public void onDrawFrame(float frameTime) {
        NativePhysics3DWorld.step(getNative(), frameTime);

        generateCollisionEvents();
    }

    private void generateCollisionEvents() {
        GVRCollisionInfo collisionInfos[] = NativePhysics3DWorld.listCollisions(getNative());

        if (collisionInfos == null || collisionInfos.length <= 0) {
            return;
        }

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

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);

        //FIXME: Implement a way to check if already exists a GVRWold attached to the scene
        if (newOwner.getParent() != null) {
            throw new RuntimeException("GVRWold must be attached to the scene's root object!");
        }

        if (!mHasFrameCallback) {
            newOwner.getEventReceiver().addListener(this);
        }
    }

    @Override
    public void onDetach(GVRSceneObject oldOwner) {
        super.onDetach(oldOwner);

        if (!mHasFrameCallback) {
            oldOwner.getEventReceiver().removeListener(this);
        }
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
}

class NativePhysics3DWorld {
    static native long ctor();

    static native long getComponentType();

    static native boolean addRigidBody(long jphysics_world, long jrigid_body);

    static native void removeRigidBody(long jphysics_world, long jrigid_body);

    static native void step(long jphysics_world, float jtime_step);

    static native GVRCollisionInfo[] listCollisions(long jphysics_world);
}
