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

import android.content.res.AssetManager;
import android.os.SystemClock;
import android.util.LongSparseArray;

import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSceneObject.ComponentVisitor;
import org.gearvrf.ISceneObjectEvents;

/**
 * Represents a physics world where all {@link GVRSceneObject} with {@link GVRRigidBody} component
 * attached to are simulated.
 * <p>
 * {@link GVRWorld} is a component that must be attached to the scene's root object.
 */
public class GVRWorld extends GVRComponent {
    private boolean mInitialized;
    private final GVRPhysicsContext mPhysicsContext;
    private GVRWorldTask mWorldTask;
    private static final long DEFAULT_INTERVAL = 15;

    private long mNativeLoader;

    static {
        System.loadLibrary("gvrf-physics");
    }

    private final LongSparseArray<GVRRigidBody> mRigidBodies = new LongSparseArray<GVRRigidBody>();
    private final GVRCollisionMatrix mCollisionMatrix;

    /**
     * Constructs new instance to simulate the Physics World of the Scene.
     *
     * @param gvrContext The context of the app.
     */
    public GVRWorld(GVRContext gvrContext) {
        this(gvrContext, null);
    }

    /**
     * Constructs new instance to simulate the Physics World of the Scene.
     *
     * @param gvrContext The context of the app.
     * @param interval interval (in milliseconds) at which the collisions will be updated.
     */
    public GVRWorld(GVRContext gvrContext, long interval) {
        this(gvrContext, null, interval);
    }

    /**
     * Constructs new instance to simulate the Physics World of the Scene. Defaults to a 15ms
     * update interval.
     *
     * @param gvrContext The context of the app.
     * @param collisionMatrix a matrix that represents the collision relations of the bodies on the scene
     */
    public GVRWorld(GVRContext gvrContext, GVRCollisionMatrix collisionMatrix) {
        this(gvrContext, collisionMatrix, DEFAULT_INTERVAL);
    }

    /**
     * Constructs new instance to simulate the Physics World of the Scene.
     *
     * @param gvrContext The context of the app.
     * @param collisionMatrix a matrix that represents the collision relations of the bodies on the scene
     * @param interval interval (in milliseconds) at which the collisions will be updated.
     */
    public GVRWorld(GVRContext gvrContext, GVRCollisionMatrix collisionMatrix, long interval) {
        super(gvrContext, NativePhysics3DWorld.ctor());
        mInitialized = false;
        mCollisionMatrix = collisionMatrix;
        mWorldTask = new GVRWorldTask(interval);
        mPhysicsContext = GVRPhysicsContext.getInstance();
    }

    static public long getComponentType() {
        return NativePhysics3DWorld.getComponentType();
    }

    /**
     * Add a {@link GVRConstraint} to this physics world.
     *
     * @param gvrConstraint The {@link GVRConstraint} to add.
     */
    public void addConstraint(final GVRConstraint gvrConstraint) {
        mPhysicsContext.runOnPhysicsThread(new Runnable() {
            @Override
            public void run() {
                NativePhysics3DWorld.addConstraint(getNative(), gvrConstraint.getNative());
            }
        });
    }

    /**
     * Remove a {@link GVRFixedConstraint} from this physics world.
     *
     * @param gvrConstraint the {@link GVRFixedConstraint} to remove.
     */
    public void removeConstraint(final GVRConstraint gvrConstraint) {
        mPhysicsContext.runOnPhysicsThread(new Runnable() {
            @Override
            public void run() {
                NativePhysics3DWorld.removeConstraint(getNative(), gvrConstraint.getNative());
            }
        });
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
    public void addBody(final GVRRigidBody gvrBody) {
        mPhysicsContext.runOnPhysicsThread(new Runnable() {
            @Override
            public void run() {
                if (contains(gvrBody)) {
                    return;
                }

                if (gvrBody.getCollisionGroup() < 0 || gvrBody.getCollisionGroup() > 15
                        || mCollisionMatrix == null) {
                    NativePhysics3DWorld.addRigidBody(getNative(), gvrBody.getNative());
                } else {
                    NativePhysics3DWorld.addRigidBodyWithMask(getNative(), gvrBody.getNative(),
                            mCollisionMatrix.getCollisionFilterGroup(gvrBody.getCollisionGroup()),
                            mCollisionMatrix.getCollisionFilterMask(gvrBody.getCollisionGroup()));
                }

                mRigidBodies.put(gvrBody.getNative(), gvrBody);
            }
        });
    }

    /**
     * Remove a {@link GVRRigidBody} from this physics world.
     *
     * @param gvrBody the {@link GVRRigidBody} to remove.
     */
    public void removeBody(final GVRRigidBody gvrBody) {
        mPhysicsContext.runOnPhysicsThread(new Runnable() {
            @Override
            public void run() {
                if (contains(gvrBody)) {
                    NativePhysics3DWorld.removeRigidBody(getNative(), gvrBody.getNative());
                    mRigidBodies.remove(gvrBody.getNative());
                }
            }
        });
    }

    private void startSimulation() {
        mWorldTask.start();
    }

    private void stopSimulation() {
        mWorldTask.stop();
    }

    private void generateCollisionEvents() {
        GVRCollisionInfo collisionInfos[] = NativePhysics3DWorld.listCollisions(getNative());

        String onEnter = "onEnter";
        String onExit = "onExit";

        for (GVRCollisionInfo info : collisionInfos) {
            if (info.isHit) {
                sendCollisionEvent(info, onEnter);
            } else if (mRigidBodies.get(info.bodyA) != null
                    && mRigidBodies.get(info.bodyB) != null) {
                // If both bodies are in the scene.
                sendCollisionEvent(info, onExit);
            }
        }

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
        rootSceneObject.forAllComponents(mComponentVisitor, GVRRigidBody.getComponentType());

        if (!mInitialized) {
            rootSceneObject.getEventReceiver().addListener(mSceneEventsHandler);
        } else if (isEnabled()){
            startSimulation();
        }
    }

    private void doPhysicsDetach(GVRSceneObject rootSceneObject) {
        rootSceneObject.forAllComponents(mComponentVisitor, GVRRigidBody.getComponentType());

        if (!mInitialized) {
            rootSceneObject.getEventReceiver().removeListener(mSceneEventsHandler);
        }
        if (isEnabled()) {
            stopSimulation();
        }
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
    public void onEnable() {
        super.onEnable();

        if (getOwnerObject() != null && mInitialized) {
            startSimulation();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        stopSimulation();
    }

    public void setGravity(final float x, final float y, final float z) {
        mPhysicsContext.runOnPhysicsThread(new Runnable() {
            @Override
            public void run() {
                NativePhysics3DWorld.setGravity(getNative(), x, y, z);
            }
        });
    }

    public void getGravity(float[] gravity) {
        NativePhysics3DWorld.getGravity(getNative(), gravity);
    }

    private class GVRWorldTask implements Runnable {
        private boolean running = false;
        private final long intervalMillis;
        private float timeStep;
        private int maxSubSteps;
        private long simulationTime;
        private long lastSimulTime;


        public GVRWorldTask(long milliseconds) {
            intervalMillis = milliseconds;
        }

        @Override
        public void run() {
            if (!running) {
                return;
            }


            simulationTime = SystemClock.uptimeMillis();

            /* To debug physics step
            if (BuildConfig.DEBUG && timeStep != simulationTime - lastSimulTime) {
                Log.v("GVRPhysicsWorld", "onStep " + timeStep + "ms" + ", subSteps " + maxSubSteps);
            }*/

            timeStep  = simulationTime - lastSimulTime;
            maxSubSteps = (int) (timeStep * 60) / 1000 + 1;

            NativePhysics3DWorld.step(getNative(), timeStep, maxSubSteps);

            generateCollisionEvents();

            lastSimulTime = simulationTime;

            simulationTime = intervalMillis + simulationTime - SystemClock.uptimeMillis();
            if (simulationTime < 0) {
                simulationTime += intervalMillis;
            }
            // Time of the next simulation;
            simulationTime = simulationTime + SystemClock.uptimeMillis();

            mPhysicsContext.runAtTimeOnPhysicsThread(this, simulationTime);


        }

        public void start() {
            // To avoid concurrency
            mPhysicsContext.runOnPhysicsThread(new Runnable() {
                @Override
                public void run() {
                    if (!running) {
                        running = true;
                        lastSimulTime = SystemClock.uptimeMillis();
                        mPhysicsContext.runDelayedOnPhysicsThread(GVRWorldTask.this,
                                intervalMillis);
                    }
                }
            });
        }

        public void stop() {
            // To avoid concurrency
            mPhysicsContext.runOnPhysicsThread(new Runnable() {
                @Override
                public void run() {
                    if (running) {
                        running = false;
                        mPhysicsContext.removeTask(GVRWorldTask.this);
                    }
                }
            });
        }
    }

    private ISceneObjectEvents mSceneEventsHandler = new ISceneObjectEvents() {

        @Override
        public void onInit(GVRContext gvrContext, GVRSceneObject sceneObject) {
            if (mInitialized)
                return;

            mInitialized = true;
            getOwnerObject().getEventReceiver().removeListener(this);

            if (isEnabled()) {
                startSimulation();
            }
        }

        @Override
        public void onLoaded() {}

        @Override
        public void onAfterInit() {}

        @Override
        public void onStep() {}
    };

    private ComponentVisitor mComponentVisitor = new ComponentVisitor() {

        @Override
        public boolean visit(GVRComponent gvrComponent) {
            if (!gvrComponent.isEnabled()) {
                return false;
            }

            if (GVRWorld.this.owner != null) {
                addBody((GVRRigidBody) gvrComponent);
            } else {
                removeBody((GVRRigidBody) gvrComponent);
            }
            return true;
        }
    };
}

class NativePhysics3DWorld {
    static native long ctor();

    static native long getComponentType();

    static native boolean addConstraint(long jphysics_world, long jconstraint);

    static native boolean removeConstraint(long jphysics_world, long jconstraint);

    static native boolean addRigidBody(long jphysics_world, long jrigid_body);

    static native boolean addRigidBodyWithMask(long jphysics_world, long jrigid_body, long collisionType, long collidesWith);

    static native void removeRigidBody(long jphysics_world, long jrigid_body);

    static native void step(long jphysics_world, float jtime_step, int maxSubSteps);

    static native void getGravity(long jworld, float[] array);

    static native void setGravity(long jworld, float x, float y, float z);

    static native GVRCollisionInfo[] listCollisions(long jphysics_world);
}
