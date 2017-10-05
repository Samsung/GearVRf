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
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderManager;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.GVRTexture;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static android.opengl.GLES20.GL_POINTS;

/**
 *  This class is responsible for instancing meshes, with vertices that act as
 *  the actual particles when rendered using GL_POINTS.
 */

class Particles {

    private  GVRContext mGVRContext;
    private GVRMaterial material;
    private GVRMesh mParticleMesh;
    private float mAge;
    private float mSize;
    private Vector3f mAcceleration;
    private float mParticleSizeRate;
    private float mFadeWithAge;
    private GVRTexture mTexture;
    private Vector4f mColorMultiplier;
    private float mNoiseFactor;

    private GVRShaderId particleID;

    Particles(GVRContext gvrContext, float age, float particleSize,
                     Vector3f acceleration, float particleSizeRate, boolean fadeWithAge,
                     GVRTexture tex, Vector4f color, float noiseFactor) {

        mGVRContext = gvrContext;
        mAge = age;
        mSize = particleSize;
        mAcceleration = acceleration;
        mParticleSizeRate = particleSizeRate;
        mColorMultiplier = color;
        if (fadeWithAge)
            mFadeWithAge = 1.0f;
        else
            mFadeWithAge = 0.0f;
        mTexture = tex;
        mNoiseFactor = noiseFactor;
    }

    /**
     * Creates and returns a GVRSceneObject with the specified mesh attributes.
     *
     * @param vertices the vertex positions of that make up the mesh. (x1, y1, z1, x2, y2, z2, ...)
     * @param velocities the velocity attributes for each vertex. (vx1, vy1, vz1, vx2, vy2, vz2...)
     * @param particleTimeStamps the spawning times of each vertex. (t1, 0,  t2, 0,  t3, 0 ..)
     *
     * @return The GVRSceneObject with this mesh.
     */

    GVRSceneObject makeParticleMesh(float[] vertices, float[] velocities,
                                           float[] particleTimeStamps )
    {
        mParticleMesh = new GVRMesh(mGVRContext);

        //pass the particle positions as vertices, velocities as normals, and
        //spawning times as texture coordinates.
        mParticleMesh.setVertices(vertices);
        mParticleMesh.setNormals(velocities);
        mParticleMesh.setTexCoords(particleTimeStamps);

        particleID = new GVRShaderId(ParticleShader.class);
        material = new GVRMaterial(mGVRContext, particleID);

        material.setVec4("u_color", mColorMultiplier.x, mColorMultiplier.y,
                mColorMultiplier.z, mColorMultiplier.w);
        material.setFloat("u_particle_age", mAge);
        material.setVec3("u_acceleration", mAcceleration.x, mAcceleration.y, mAcceleration.z);
        material.setFloat("u_particle_size", mSize);
        material.setFloat("u_size_change_rate", mParticleSizeRate);
        material.setFloat("u_fade", mFadeWithAge);
        material.setFloat("u_noise_factor", mNoiseFactor);

        GVRRenderData renderData = new GVRRenderData(mGVRContext);
        renderData.setMaterial(material);
        renderData.setMesh(mParticleMesh);
        material.setMainTexture(mTexture);

        GVRSceneObject meshObject = new GVRSceneObject(mGVRContext);
        meshObject.attachRenderData(renderData);
        meshObject.getRenderData().setMaterial(material);

        // Set the draw mode to GL_POINTS, disable writing to depth buffer, enable depth testing
        // and set the rendering order to transparent.
        // Disabling writing to depth buffer ensure that the particles blend correctly
        // and keeping the depth test on along with rendering them
        // after the geometry queue makes sure they occlude, and are occluded, correctly.

        meshObject.getRenderData().setDrawMode(GL_POINTS);
        meshObject.getRenderData().setDepthTest(true);
        meshObject.getRenderData().setDepthMask(false);
        meshObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);

        return meshObject;
    }
}
