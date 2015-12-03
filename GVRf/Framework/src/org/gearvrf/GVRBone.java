/*
---------------------------------------------------------------------------
Open Asset Import Library - Java Binding (jassimp)
---------------------------------------------------------------------------

Copyright (c) 2006-2012, assimp team

All rights reserved.

Redistribution and use of this software in source and binary forms,
with or without modification, are permitted provided that the following
conditions are met:

 * Redistributions of source code must retain the above
  copyright notice, this list of conditions and the
  following disclaimer.

 * Redistributions in binary form must reproduce the above
  copyright notice, this list of conditions and the
  following disclaimer in the documentation and/or other
  materials provided with the distribution.

 * Neither the name of the assimp team, nor the names of its
  contributors may be used to endorse or promote products
  derived from this software without specific prior
  written permission of the assimp team.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
---------------------------------------------------------------------------
 */
package org.gearvrf;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.jassimp.AiWrapperProvider;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

/**
 * A single bone of a mesh.<p>
 *
 * A bone has a name by which it can be found in the frame hierarchy and by
 * which it can be addressed by animations. In addition it has a number of
 * influences on vertices. <p>
 *
 * This class is designed to be mutable, i.e., the returned collections are
 * writable and may be modified. <p>
 *
 * Instantaneous pose of the bone during animation is not part of this class,
 * but in {@code GVRSkeleton}. This allows multiple instances of the same mesh
 * and bones.
 */
public final class GVRBone extends GVRComponent implements PrettyPrint {
    /**
     * Constructor.
     */
    public GVRBone(GVRContext gvrContext) {
        super(gvrContext, NativeBone.ctor());
        mBoneWeights = new ArrayList<GVRBoneWeight>();
    }

    /**
     * Sets the name of the bone.
     * 
     * @param name the name of the bone.
     */
    public void setName(String name) {
        mName = name == null ? null : new String(name);
        NativeBone.setName(getNative(), mName);
    }

    /**
     * Returns the name of the bone.
     *
     * @return the name
     */
    public String getName() {
        // Name is currently read-only for native code. So it is
        // not updated from native object.
        return mName;
    }

    /**
     * Sets the list of bone weights.
     * 
     * @param boneWeights the list of weights
     */
    public void setBoneWeights(List<GVRBoneWeight> boneWeights) {
        mBoneWeights.clear();
        mBoneWeights.addAll(boneWeights);
        NativeBone.setBoneWeights(getNative(),
                                  GVRHybridObject.getNativePtrArray(boneWeights));
    }

    /**
     * Returns a list of bone weights.
     *
     * @return the bone weights
     */
    public List<GVRBoneWeight> getBoneWeights() {
        return mBoneWeights;
    }

    public void setOffsetMatrix(float[] offsetMatrix) {
        NativeBone.setOffsetMatrix(getNative(), offsetMatrix);
    }

    /**
     * Sets the final transform of the bone during animation.
     *
     * @param finalTransform The transform matrix representing
     * the bone's pose after computing the skeleton.
     */
    public void setFinalTransformMatrix(float[] finalTransform) {
        NativeBone.setFinalTransformMatrix(getNative(), finalTransform);
    }

    /**
     * Sets the final transform of the bone during animation.
     *
     * @param finalTransform The transform matrix representing
     * the bone's pose after computing the skeleton.
     */
    public void setFinalTransformMatrix(Matrix4f finalTransform) {
        float[] mat = new float[16];
        finalTransform.get(mat);
        NativeBone.setFinalTransformMatrix(getNative(), mat);
    }

    /**
     * Gets the final transform of the bone.
     *
     * @return the 4x4 matrix representing the final transform of the
     * bone during animation, which comprises bind pose and skeletal
     * transform at the current time of the animation.
     */
    public Matrix4f getFinalTransformMatrix() {
        return new Matrix4f(FloatBuffer.wrap(NativeBone.getFinalTransformMatrix(getNative())));
    }

    /**
     * Returns the offset matrix.<p>
     *
     * The offset matrix is a 4x4 matrix that transforms from mesh space to
     * bone space in bind pose.<p>
     *
     * This method is part of the wrapped API (see {@link AiWrapperProvider}
     * for details on wrappers).
     *
     * @param wrapperProvider the wrapper provider (used for type inference)
     *
     * @return the offset matrix
     */
    public Matrix4f getOffsetMatrix() {
        Matrix4f offsetMatrix = new Matrix4f();
        offsetMatrix.set(NativeBone.getOffsetMatrix(getNative()));
        return offsetMatrix;
    }

    /**
     * Gets the scene object of this bone.
     *
     * @return the scene object that represents this bone in a
     *         hierarchy of bones.
     */
    public GVRSceneObject getSceneObject() {
        return mSceneObject;
    }

    /**
     * Sets the scene object of this bone.
     *
     * @param sceneObject The scene object that represents this
     * bone in a hierarchy of bones.
     */
    public void setSceneObject(GVRSceneObject sceneObject) {
        mSceneObject = sceneObject;
    }

    /**
     * Pretty-print the object.
     */
    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        sb.append(Log.getSpaces(indent));
        sb.append(toString());
        sb.append(System.lineSeparator());
    }

    /**
     * Returns a string representation of the object.
     */
    @Override
    public String toString() {
        return "GVRBone [name=" + getName()
                + ", boneWeights=" + getBoneWeights()
                + ", offsetMatrix=" + getOffsetMatrix()
                + ", finalTransformMatrix=" + getFinalTransformMatrix()
                + "]";
    }

    /**
     * Name of the bone.
     */
    private String mName;

    /**
     * Bone weights.
     */
    private final List<GVRBoneWeight> mBoneWeights;

    /**
     * The scene object that represents the transforms of the
     * bone.
     */
    private GVRSceneObject mSceneObject;
}

class NativeBone {
    static native long ctor();
    static native void setName(long object, String mName);
    static native void setBoneWeights(long object, long[] nativePtrArray);
    static native void setOffsetMatrix(long object, float[] offsetMatrix);
    static native float[] getOffsetMatrix(long object);
    static native void setFinalTransformMatrix(long object, float[] offsetMatrix);
    static native float[] getFinalTransformMatrix(long object);
}