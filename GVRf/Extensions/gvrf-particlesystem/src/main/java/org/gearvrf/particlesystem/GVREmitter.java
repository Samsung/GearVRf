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

import android.util.Pair;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;


/**
 * The base emitter class from which shape-specific emitter classes inherit.
 * This class is used to set up the the behaviour of the particle system in general.
 * Also, this is used to set the specific particle properties.
 *
 * Every emitter has a set of scene objects as its children. These scene
 * objects each have a mesh attached to them, with the number of vertices in the mesh
 * being equal to the emit rate of the emitter, i.e., every second a new scene object with a mesh
 * having an emitRate number of vertices is added to the emitter as a child.
 * These vertices act as the particles of the system. Consequently, all those
 * children objects which have exceeded their age limits are deleted every second.
 *
 */

class GVREmitter extends GVRSceneObject {

    private int MAX_EMIT_RATE = 500;

    protected int mEmitRate = 300;
    protected boolean mEnableEmitter = true;
    protected GVRContext mGVRContext = null;


    //list of active scene objects containing particle meshes,
    // and their corresponding spawn times.
    protected ArrayList<Pair<GVRSceneObject, Float>> meshInfo = null;


    //particle properties
    protected float mMaxAge = 1.5f;
    protected float mParticleSize = 50.0f;
    protected Vector3f minVelocity = new Vector3f(0,1.5f,0);
    protected Vector3f maxVelocity = new Vector3f(0,4.5f,0);
    protected Vector3f mEnvironmentAcceleration ;
    private Vector4f mColor;
    private float mParticleSizeRate = 0.0f;
    private float mNoiseFactor = 0.0f;
    private boolean mFadeWithAge = false;
    private GVRTexture mParticleTexture;

    //attributes of the particles generated every second.
    protected float[] mParticlePositions;
    protected float[] mParticleVelocities;
    protected float [] mParticleGenTimes;

    protected boolean burstMode = false;
    private boolean executeOnce = true;

    //values corresponding to the particles which degine the bounding volume of the system.
    private float[] particleBoundingVolume;
    private float[] BVSpawnTimes;
    private float[] BVVelocities;

    private float currTime = 0;
    ArrayList<Integer> idxsToDelete;
    
    public GVREmitter(GVRContext gvrContext)
    {
        super(gvrContext);
        mGVRContext = gvrContext;
        meshInfo = new ArrayList<Pair<GVRSceneObject, Float>>();
        idxsToDelete = new ArrayList<Integer>();
        mEnvironmentAcceleration = new Vector3f(0.0f,0.0f,0.0f);
        mColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        setParticleVolume(100,100,100);
    }

    /**
     * Iterate over the active meshes and delete the ones whose every particle
     * has exceeded the maximum age limit.
     * <p>
     * Particle meshes are generated for upto one second in the future.
     * Therefore the maximum time particle mesh should be allowed to remain
     * in the scene is (maxAge of the particles + one second).
     * <p>
     */

    protected void onDrawFrame() {

        int numActiveMeshes = meshInfo.size();
        for (int i = 0; i < numActiveMeshes; i++)
        {
            float currMeshSpawnTime = meshInfo.get(i).second;
            if (currTime - currMeshSpawnTime > mMaxAge + 1) {
                GVRSceneObject toDelete = meshInfo.get(i).first;
                GVREmitter.this.removeChildObject(toDelete);
                idxsToDelete.add(i);
            }
        }
        for( int i = idxsToDelete.size() - 1; i >=0; i -- ) {
            meshInfo.remove((int)idxsToDelete.get(i));
        }

        idxsToDelete.clear();
    }

    /**
     * If the burst mode is on, emit the particles only once.
     *
     * @param particlePositions
     * @param particleVelocities
     * @param particleTimeStamps
     */

    protected  void emitWithBurstCheck(float[] particlePositions, float[] particleVelocities,
                         float[] particleTimeStamps)
    {
        if ( burstMode )
        {
            if ( executeOnce )
            {
                emit(particlePositions, particleVelocities, particleTimeStamps);
                executeOnce = false;
            }
        }
        else
        {
            emit(particlePositions, particleVelocities, particleTimeStamps);
        }

    }

    /**
     * Append the bounding volume particle positions, times and velocities to the existing mesh
     * before creating a new scene object with this mesh attached to it.
     * Also, append every created scene object and its creation time to corresponding array lists.
     *
     * @param particlePositions
     * @param particleVelocities
     * @param particleTimeStamps
     */

    private void emit(float[] particlePositions, float[] particleVelocities,
                      float[] particleTimeStamps)
    {
        float[] allParticlePositions = new float[particlePositions.length + particleBoundingVolume.length];
        System.arraycopy(particlePositions, 0, allParticlePositions, 0, particlePositions.length);
        System.arraycopy(particleBoundingVolume, 0, allParticlePositions,
                        particlePositions.length, particleBoundingVolume.length);

        float[] allSpawnTimes = new float[particleTimeStamps.length + BVSpawnTimes.length];
        System.arraycopy(particleTimeStamps, 0, allSpawnTimes, 0, particleTimeStamps.length);
        System.arraycopy(BVSpawnTimes, 0, allSpawnTimes, particleTimeStamps.length, BVSpawnTimes.length);

        float[] allParticleVelocities = new float[particleVelocities.length + BVVelocities.length];
        System.arraycopy(particleVelocities, 0, allParticleVelocities, 0, particleVelocities.length);
        System.arraycopy(BVVelocities, 0, allParticleVelocities, particleVelocities.length, BVVelocities.length);


        Particles particleMesh = new Particles(mGVRContext, mMaxAge,
                mParticleSize, mEnvironmentAcceleration, mParticleSizeRate, mFadeWithAge,
                mParticleTexture, mColor, mNoiseFactor);


        GVRSceneObject particleObject = particleMesh.makeParticleMesh(allParticlePositions,
                allParticleVelocities, allSpawnTimes);

        this.addChildObject(particleObject);
        meshInfo.add(Pair.create(particleObject, currTime));
    }


    /**
     * Create a bouding volume for the particle system centered at its position with
     * the specified width, height and depth. This is important to do because the parent scene
     * object might fall outside the viewing frustum and cause the entire system to be
     * culled.
     * This function creates 8 particles (mesh vertices) with very large spawning time
     * attributes (i.e. they are always discarded) which define the volume of the system. The
     * system is assumed to stay inside this volume.
     * @param width volume length (along x-axis)
     * @param height volume height (along y-axis)
     * @param depth volume depth (along z-axis)
     */

    public void setParticleVolume(final float width, final float height, final float depth)
    {
        final GVRTransform thisTransform = this.getTransform();
        if (null != mGVRContext) {
            mGVRContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {Vector3f center = new Vector3f(thisTransform.getPositionX(),
                        thisTransform.getPositionY(), thisTransform.getPositionZ());

                    particleBoundingVolume = new float[]{center.x - width/2, center.y - height/2, center.z - depth/2,
                            center.x - width/2, center.y - height/2, center.z + depth/2,
                            center.x + width/2, center.y - height/2, center.z + depth/2,
                            center.x + width/2, center.y - height/2, center.z - depth/2,

                            center.x - width/2, center.y + height/2, center.z - depth/2,
                            center.x - width/2, center.y + height/2, center.z + depth/2,
                            center.x + width/2, center.y + height/2, center.z + depth/2,
                            center.x - width/2, center.y + height/2, center.z - depth/2};

                    BVSpawnTimes = new float[]{Float.MAX_VALUE, 0, Float.MAX_VALUE, 0, Float.MAX_VALUE,
                            0, Float.MAX_VALUE, 0, Float.MAX_VALUE, 0, Float.MAX_VALUE, 0,
                            Float.MAX_VALUE, 0, Float.MAX_VALUE, 0};

                    BVVelocities = new float[24];
                    for ( int i = 0; i < 24; i ++ )
                        BVVelocities[i] = 0;

                }
            });
        }


    }

    protected void tickClock(float time)
    {
        currTime = time;

        for (int i = 0; i < meshInfo.size(); i ++)
        {
            GVRSceneObject obj = meshInfo.get(i).first;
            obj.getRenderData().getMaterial().setFloat("u_time", time);
        }
    }

    /**
     * @param emitRate The rate( #particles/second ) at which this emitter emits particles.
     *                 Currently clamped to 600 particles/second.
     */
    public void setEmitRate(int emitRate)
    {
        if ( emitRate > MAX_EMIT_RATE)
            mEmitRate = MAX_EMIT_RATE;
        else
            mEmitRate = emitRate;
    }

    /**
     * The rate ( #particles/second ) at which this emitter is emitting
     * @return
     */
    public int getEmitRate()
    {
        return mEmitRate;
    }

    /**
     * The ( emit rate * age ) decide the total number of particles in the scene.
     * @param age The time (in seconds) the particles from this emitter will remain active in the
     *            scene.
     */
    public void setParticleAge ( float age )
    {
        mMaxAge = age;
    }

    /**
     * @param size the initial particle size.
     */
    public void setParticleSize ( float size )
    {
        mParticleSize = size;
    }

    /**
     * The range of velocities that a particle generated from this emitter can have.
     * @param minV Minimum velocity that a particle can have
     * @param maxV Maximum velocity that a particle can have
     */
    public void setVelocityRange( final Vector3f minV, final Vector3f maxV )
    {
        if (null != mGVRContext) {
            mGVRContext.runOnGlThread(new Runnable() {

                @Override
                public void run() {
                    minVelocity = minV;
                    maxVelocity = maxV;
                }
            });
        }
    }

    /**
     *
     * @param acceleration the acceleration of the environment which affects the particle motion.
     */
    public void setEnvironmentAcceleration( Vector3f acceleration )
    {
        mEnvironmentAcceleration = acceleration;
    }

    /**
     *
     * @param rate The rate at which the particle size should increase or decrease. Lower value
     *             clamped to 1.0f
     */

    //todo: need to vary particle size in v shader wrt this value
    public void setParticleSizeChangeRate( float rate )
    {
        mParticleSizeRate = rate;
    }

    /**
     * @param fade Set this to true to linearly decrease the transparency of the particles with
     *             their age.
     */
    public void setFadeWithAge ( boolean fade )
    {
        mFadeWithAge = fade;
    }

    /**
     * @param mode True/false to enable/disable the burst mode for this emitter.
     */

    public void setBurstMode(boolean mode)
    {
        burstMode = mode;
    }

    /**
     * @param tex Texture of the particle.
     */
    public void setParticleTexture(GVRTexture tex)
    {
        mParticleTexture = tex;
    }

    /**
     *
     * @param color The color value to be multiplied to the particle texture.
     */
    public void  setColorMultiplier( Vector4f color )
    {
        mColor = color;
    }

    /**
     *
     * @param noise Noise factor from 0 to 1.
     */
    public void setNoiseFactor(float noise)
    {
        if ( noise < 0 )
            noise = 0;
        if ( noise > 1 )
            noise = 1;

        mNoiseFactor = noise;
    }

    /**
     * Unregister and remove all the child objects with meshes from this emitter
     */
    public void clearSystem()
    {
        int nchildren = this.getChildrenCount();
        for( int i = 0; i < nchildren; i ++ )
        {
            this.removeChildObject(this.getChildByIndex(0));
        }
    }

}
