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

package org.gearvrf.particlesystem;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * Is a GVREmitter of the plane shape. The particles are emitted from
 * a planar area in the X-Z plane of a certain width and height in a positive y-direction with a
 * random position and velocity. The direction of emmission can be changed by
 * rotating the emitter in the application.
 */

public class GVRPlaneEmitter extends GVREmitter {


    private Random mRandom = new Random();
    private float mWidth = 1.0f;
    private float mHeight = 1.0f;
    private final GVRDrawFrameListenerImpl mFrameListener;

    public GVRPlaneEmitter(GVRContext gvrContext) {
        super(gvrContext);
        mFrameListener = new GVRDrawFrameListenerImpl(this);
        mGVRContext.registerDrawFrameListener(mFrameListener);
    }

    private Vector3f getNextPosition() {

        float x = mRandom.nextFloat() * mWidth - mWidth/2;
        float z = mRandom.nextFloat() * mHeight - mHeight/2;
        return new Vector3f(x, 0, z);
    }

    private float[] generateParticlePositions()
    {
        float[] positions = new float[mEmitRate * 3];
        for ( int i = 0; i < mEmitRate * 3; i += 3 ) {

            Vector3f nextPos = getNextPosition();
            positions[i] = nextPos.x;
            positions[i+1] = nextPos.y;
            positions[i+2] = nextPos.z;
        }

        return positions;
    }

    private Vector3f getNextVelocity() {
        float velocityx = minVelocity.x;
        float velocityy = minVelocity.y;
        float velocityz = minVelocity.z;

        velocityx += mRandom.nextFloat() * (maxVelocity.x - minVelocity.x);
        velocityy += mRandom.nextFloat() * (maxVelocity.y - minVelocity.y);
        velocityz += mRandom.nextFloat() * (maxVelocity.z - minVelocity.z);

        return new Vector3f(velocityx, velocityy, velocityz);
    }

    /**
     * generate random velocities in the given range
     * @return
     */

    private float[] generateParticleVelocities()
    {
        float velocities[] = new float[mEmitRate * 3];
        for ( int i = 0; i < mEmitRate * 3; i +=3 )
        {
            Vector3f nexVel = getNextVelocity();
            velocities[i] = nexVel.x;
            velocities[i+1] = nexVel.y;
            velocities[i+2] = nexVel.z;
        }
        return velocities;
    }

    /**
     *  Generate random time stamps from the current time upto the next one second.
     *  Passed as texture coordinates to the vertex shader, an unused field is present
     *  with every pair passed.
     *
     * @param totalTime
     * @return
     */
    private float[] generateParticleTimeStamps(float totalTime)
    {
        float timeStamps[] = new float[mEmitRate * 2];
        for ( int i = 0; i < mEmitRate * 2; i +=2 )
        {
            timeStamps[i] = totalTime + mRandom.nextFloat();
            timeStamps[i + 1] = 0;
        }
        return timeStamps;
    }



    private static final class GVRDrawFrameListenerImpl implements GVRDrawFrameListener {

        private final WeakReference<GVRPlaneEmitter> mRef;
        private float totalTime = 0;
        private float mElapsedTime = 0;
        GVRDrawFrameListenerImpl(final GVRPlaneEmitter emitter) {
            mRef = new WeakReference<GVRPlaneEmitter>(emitter);
        }

        /**
         * Generate random positions, velocities and spawn times for the next future second.
         * Also, any shape-specific per-frame operations go here.
         * @param frameTime Seconds since the previous frame
         */

        @Override
        public void onDrawFrame(float frameTime) {

            final GVRPlaneEmitter emitter = mRef.get();
            if (null != emitter)
            {
                totalTime += frameTime;

                emitter.tickClock(totalTime);

                if (emitter.mEnableEmitter) {

                    mElapsedTime += frameTime;
                    if (mElapsedTime > 1.0f) {
                        mElapsedTime = 0;

                        emitter.mParticlePositions = emitter.generateParticlePositions();
                        emitter.mParticleVelocities = emitter.generateParticleVelocities();
                        emitter.mParticleGenTimes = emitter.generateParticleTimeStamps(totalTime);

                        emitter.emitWithBurstCheck(emitter.mParticlePositions, emitter.mParticleVelocities,
                                emitter.mParticleGenTimes);

                        emitter.onDrawFrame();
                    }
                }
            }
        }
    }

    protected void tickClock(float time)
    {
        super.tickClock(time);
    }

    /**
     * @param width length of the plane along the x axis
     */
    public void setPlaneWidth (float width)
    {
        mWidth = width;
    }

    /**
     * @param length length of the plane along the z axis
     */
    public void setPlaneHeight( float length )
    {
        mHeight = length;
    }

    /**
     * @inheritDoc
     */
    public void clearSystem()
    {
        mGVRContext.unregisterDrawFrameListener(mFrameListener);
        super.clearSystem();
    }

}
