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
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * Is a emitter in the shape of a sphere. Particles are generated randomly from
 * within the volume of this sphere emitter of a specified radius.
 */

public class GVRSphericalEmitter extends GVREmitter{

    private Random mRandom = new Random();

    private float mRadius = 1.0f;
    private float totalTime = 0;
    private float mElapsedTime = 0;

    private GVRDrawFrameListenerImpl mFrameListener;

    public GVRSphericalEmitter(GVRContext gvrContext) {
        super(gvrContext);
        mFrameListener = new GVRDrawFrameListenerImpl(this);
        mGVRContext.registerDrawFrameListener(mFrameListener);
    }

    /**
     * Randomly generate coordinates from within the sphere. Works by generating
     * a random point in a cube of diagonal of length 2 * r and then checking if it lies
     * within sphere of radius r. Has some performance improvement over directly
     * generating a random point within a sphere of radius r, due to less
     * operations involved.
     *
     * @return
     */
    private float[] generateParticlePositions()
    {
        float[] positions = new float[mEmitRate * 3];

        for ( int i = 0; i < mEmitRate * 3; i += 3 )
        {
            float x = 0, y = 0, z = 0;
            do {
                x = mRandom.nextFloat() * 2 * mRadius - mRadius;
                y = mRandom.nextFloat() * 2 * mRadius - mRadius;
                z = mRandom.nextFloat() * 2 * mRadius - mRadius;
            }
            while (x * x + y * y + z * z > mRadius * mRadius);

            positions[i] = x;
            positions[i+1] = y;
            positions[i+2] = z;
        }
        return positions;
    }

    /**
     *  Generate random time stamps from the current time upto the next one second.
     *  Passed as texture coordinates to the vertex shader; an unused field is present
     *  with every pair passed.
     *
     * @param totalTime
     * @return
     */

    private float[] generateParticleTimeStamps(float totalTime)
    {
        float timeStamps[] = new float[mEmitRate * 2];

        if ( burstMode ) {
            for (int i = 0; i < mEmitRate * 2; i += 2) {
                timeStamps[i] = totalTime;
                timeStamps[i + 1] = 0;
            }
        }
        else {
            for (int i = 0; i < mEmitRate * 2; i += 2) {
                timeStamps[i] = totalTime + mRandom.nextFloat();
                timeStamps[i + 1] = 0;
            }
        }
        return timeStamps;

    }

    /**
     * Generate random velocities for every particle. The direction is obtained by assuming
     * the position of a particle as a vector. This normalised vector is scaled by
     * the speed range.
     *
     * @return
     */
    private float[] generateParticleVelocities()
    {
        float [] particleVelocities = new float[mEmitRate * 3];
        Vector3f temp = new Vector3f(0,0,0);
        for ( int i = 0; i < mEmitRate * 3 ; i +=3 )
        {
            temp.x = mParticlePositions[i];
            temp.y = mParticlePositions[i+1];
            temp.z = mParticlePositions[i+2];


            float velx = mRandom.nextFloat() * (maxVelocity.x- minVelocity.x)
                    + minVelocity.x;

            float vely = mRandom.nextFloat() * (maxVelocity.y - minVelocity.y)
                                + minVelocity.y;
            float velz = mRandom.nextFloat() * (maxVelocity.z - minVelocity.z)
                    + minVelocity.z;

            temp = temp.normalize();
            temp.mul(velx, vely, velz, temp);

            particleVelocities[i] = temp.x;
            particleVelocities[i+1] = temp.y;
            particleVelocities[i+2] = temp.z;
        }

        return particleVelocities;

    }

    private static final class GVRDrawFrameListenerImpl implements GVRDrawFrameListener {

        private final WeakReference<GVRSphericalEmitter> mRef;
        private float totalTime = 0;
        private float mElapsedTime = 0;
        GVRDrawFrameListenerImpl(final GVRSphericalEmitter emitter) {
            mRef = new WeakReference<GVRSphericalEmitter>(emitter);
        }

        /**
         * Generate random positions, velocities and spawn times for the next future second.
         *
         * @param frameTime Seconds since the previous frame
         */

        @Override
        public void onDrawFrame(float frameTime) {

            final GVRSphericalEmitter emitter = mRef.get();
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
     * @param radius radius of this sphere emitter
     */
    public void setRadius( float radius )
    {
        mRadius = radius;
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
