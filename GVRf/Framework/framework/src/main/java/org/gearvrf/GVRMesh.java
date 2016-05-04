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

package org.gearvrf;

import static org.gearvrf.utility.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gearvrf.utility.Exceptions;
import org.gearvrf.utility.Log;

/**
 * Describes an indexed triangle mesh as a set of shared vertices with integer
 * indices for each triangle.
 * 
 * Usually each mesh vertex may have a positions, normal and texture coordinate.
 * Skinned mesh vertices will also have bone weights and indices.
 * If the mesh uses a normal map for lighting, it will have tangents
 * and bitangents as well. These vertex components correspond to vertex
 * attributes in the OpenGL vertex shader.
 */
public class GVRMesh extends GVRHybridObject implements PrettyPrint {
    private static final String TAG = GVRMesh.class.getSimpleName();

    public GVRMesh(GVRContext gvrContext) {
        this(gvrContext, NativeMesh.ctor());
        mAttributeKeys = new HashSet<String>();
    }

    GVRMesh(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
        setBones(new ArrayList<GVRBone>());
        mVertexBoneData = new GVRVertexBoneData(gvrContext, this);
        mAttributeKeys = new HashSet<String>();
    }

    /**
     * Get the 3D vertices of the mesh. Each vertex is represented as a packed
     * {@code float} triplet:
     * <p>
     * <code>
     *     { x0, y0, z0, x1, y1, z1, x2, y2, z2, ... }
     * </code>
     * 
     * @return Array with the packed vertex data.
     */
    public float[] getVertices() {
        return NativeMesh.getVertices(getNative());
    }

    /**
     * Sets the 3D vertices of the mesh. Each vertex is represented as a packed
     * {@code float} triplet:
     * <p>
     * <code>{ x0, y0, z0, x1, y1, z1, x2, y2, z2, ...}</code>
     * 
     * @param vertices
     *            Array containing the packed vertex data.
     */
    public void setVertices(float[] vertices) {
        checkValidFloatArray("vertices", vertices, 3);
        mAttributeKeys.add("a_position");
        NativeMesh.setVertices(getNative(), vertices);
    }

    /**
     * Get the normal vectors of the mesh. Each normal vector is represented as
     * a packed {@code float} triplet:
     * <p>
     * <code>{ x0, y0, z0, x1, y1, z1, x2, y2, z2, ...}</code>
     * 
     * @return Array with the packed normal data.
     */
    public float[] getNormals() {
        return NativeMesh.getNormals(getNative());
    }

    /**
     * Sets the normal vectors of the mesh. Each normal vector is represented as
     * a packed {@code float} triplet:
     * <p>
     * <code>{ x0, y0, z0, x1, y1, z1, x2, y2, z2, ...}</code>
     * 
     * @param normals
     *            Array containing the packed normal data.
     */
    public void setNormals(float[] normals) {
        checkValidFloatArray("normals", normals, 3);
        mAttributeKeys.add("a_normal");
        NativeMesh.setNormals(getNative(), normals);
    }

    /**
     * Get the u,v texture coordinates for the mesh. Each texture coordinate is
     * represented as a packed {@code float} pair:
     * <p>
     * <code>{ u0, v0, u1, v1, u2, v2, ...}</code>
     * 
     * @return Array with the packed texture coordinate data.
     */
    public float[] getTexCoords() {
        return NativeMesh.getTexCoords(getNative());
    }

    /**
     * Sets the texture coordinates for the mesh. Each texture coordinate is
     * represented as a packed {@code float} pair:
     * <p>
     * <code>{ u0, v0, u1, v1, u2, v2, ...}</code>
     * 
     * @param texCoords
     *            Array containing the packed texture coordinate data.
     */
    public void setTexCoords(float[] texCoords) {
        checkValidFloatArray("texCoords", texCoords, 2);
        mAttributeKeys.add("a_texcoord");
        NativeMesh.setTexCoords(getNative(), texCoords);
    }

    /**
     * Get the triangle vertex indices of the mesh. The indices for each
     * triangle are represented as a packed {@code char} triplet, where
     * {@code t0} is the first triangle, {@code t1} is the second, etc.:
     * <p>
     * <code>
     * { t0[0], t0[1], t0[2], t1[0], t1[1], t1[2], ...}
     * </code>
     * 
     * @return Array with the packed triangle index data.
     *
     * @deprecated use {@link #getIndices()} instead.
     */
    public char[] getTriangles() {
        return NativeMesh.getTriangles(getNative());
    }

    /**
     * Sets the triangle vertex indices of the mesh. The indices for each
     * triangle are represented as a packed {@code int} triplet, where
     * {@code t0} is the first triangle, {@code t1} is the second, etc.:
     * <p>
     * <code>
     * { t0[0], t0[1], t0[2], t1[0], t1[1], t1[2], ...}
     * </code>
     * 
     * @param triangles
     *            Array containing the packed triangle index data.
     * @deprecated use {@link #setIndices()} instead.
     */
    public void setTriangles(char[] triangles) {
        checkDivisibleDataLength("triangles", triangles, 3);
        NativeMesh.setTriangles(getNative(), triangles);
    }

    /**
     * Get the vertex indices of the mesh. The indices for each
     * vertex to be referenced.
     * 
     * @return Array with the packed index data.
     */
    public char[] getIndices() {
        return NativeMesh.getIndices(getNative());
    }

    /**
     * Sets the vertex indices of the mesh. The indices for each
     * vertex.
     * 
     * @param indices
     *            Array containing the packed index data.
     */
    public void setIndices(char[] indices) {
        NativeMesh.setIndices(getNative(), indices);
    }

    /**
     * Get the array of {@code float} scalars bound to the shader attribute
     * {@code key}.
     * 
     * @param key
     *            Name of the shader attribute
     * @return Array of {@code float} scalars.
     */
    public float[] getFloatVector(String key) {
        return NativeMesh.getFloatVector(getNative(), key);
    }

    /**
     * Bind an array of {@code float} scalars to the shader attribute
     * {@code key}.
     * 
     * @param key
     *            Name of the shader attribute
     * @param floatVector
     *            Data to bind to the shader attribute.
     */
    public void setFloatVector(String key, float[] floatVector) {
        checkValidFloatVector("key", key, "floatVector", floatVector, 1);
        mAttributeKeys.add(key);
        NativeMesh.setFloatVector(getNative(), key, floatVector);
    }

    /**
     * Get the array of two-component {@code float} vectors bound to the shader
     * attribute {@code key}.
     * 
     * @param key
     *            Name of the shader attribute
     * @return Array of two-component {@code float} vectors.
     */
    public float[] getVec2Vector(String key) {
        return NativeMesh.getVec2Vector(getNative(), key);
    }

    /**
     * Bind an array of two-component {@code float} vectors to the shader
     * attribute {@code key}.
     * 
     * @param key
     *            Name of the shader attribute
     * @param vec2Vector
     *            Two-component {@code float} vector data to bind to the shader
     *            attribute.
     */
    public void setVec2Vector(String key, float[] vec2Vector) {
        checkValidFloatVector("key", key, "vec2Vector", vec2Vector, 2);
        mAttributeKeys.add(key);
        NativeMesh.setVec2Vector(getNative(), key, vec2Vector);
    }

    /**
     * Get the array of three-component {@code float} vectors bound to the
     * shader attribute {@code key}.
     * 
     * @param key
     *            Name of the shader attribute
     * @return Array of three-component {@code float} vectors.
     */
    public float[] getVec3Vector(String key) {
        return NativeMesh.getVec3Vector(getNative(), key);
    }

    /**
     * Bind an array of three-component {@code float} vectors to the shader
     * attribute {@code key}.
     * 
     * @param key
     *            Name of the shader attribute
     * @param vec3Vector
     *            Three-component {@code float} vector data to bind to the
     *            shader attribute.
     */
    public void setVec3Vector(String key, float[] vec3Vector) {
        checkValidFloatVector("key", key, "vec3Vector", vec3Vector, 3);
        mAttributeKeys.add(key);
        NativeMesh.setVec3Vector(getNative(), key, vec3Vector);
    }

    /**
     * Get the array of four-component {@code float} vectors bound to the shader
     * attribute {@code key}.
     * 
     * @param key
     *            Name of the shader attribute
     * @return Array of four-component {@code float} vectors.
     */
    public float[] getVec4Vector(String key) {
        return NativeMesh.getVec4Vector(getNative(), key);
    }

    /**
     * Bind an array of four-component {@code float} vectors to the shader
     * attribute {@code key}.
     * 
     * @param key
     *            Name of the shader attribute
     * @param vec4Vector
     *            Four-component {@code float} vector data to bind to the shader
     *            attribute.
     */
    public void setVec4Vector(String key, float[] vec4Vector) {
        checkValidFloatVector("key", key, "vec4Vector", vec4Vector, 4);
        mAttributeKeys.add(key);
        NativeMesh.setVec4Vector(getNative(), key, vec4Vector);
    }
    
    /**
     * Get the names of all the vertex attributes on this mesh.
     * @return array of string names
     */
    public Set<String> getAttributeNames() {
        if(mAttributeKeys.size() > 0)
            return mAttributeKeys;
        
        String[] attribKeys = NativeMesh.getAttribNames(getNative());
        
        for(String i : attribKeys){
            mAttributeKeys.add(i);
        }
        return mAttributeKeys;    
    }
    
    /**
     * Calculate a bounding sphere from the mesh vertices.
     * @param sphere        float[4] array to get center of sphere and radius
     * @return sphere[0] = center.x, sphere[1] = center.y, sphere[2] = center.z, sphere[3] = radius
     */
    public void getSphereBound(float[] sphere) {
        NativeMesh.getSphereBound(getNative(), sphere);
    }

    /**
     * Determine if a named attribute exists in this mesh.
     * @param key Name of the shader attribute
     * @return true if attribute exists, false if not
     */
    public boolean hasAttribute(String key) {
    	return NativeMesh.hasAttribute(getNative(), key);
    }
    
    /**
     * Constructs a {@link GVRMesh mesh} that contains this mesh.
     * 
     * <p>
     * This is primarily useful with the {@link GVRPicker}, which does
     * "ray casting" to detect which scene object you're pointing to. Ray
     * casting is computationally expensive, and you generally want to limit the
     * number of {@linkplain GVREyePointeeHolder triangles to check.} A simple
     * {@linkplain GVRContext#createQuad(float, float) quad} is cheap enough,
     * but with complex meshes you will probably want to cut search time by
     * registering the object's bounding box, not the whole mesh.
     * 
     * @return A {@link GVRMesh} of the bounding box.
     */
    public GVRMesh getBoundingBox() {
        return new GVRMesh(getGVRContext(),
                NativeMesh.getBoundingBox(getNative()));
    }

    /**
     * Returns the bones of this mesh.
     *
     * @return a list of bones
     */
    public List<GVRBone> getBones() {
        return mBones;
    }

    /**
     * Sets bones of this mesh.
     *
     * @param bones a list of bones
     */
    public void setBones(List<GVRBone> bones) {
        mBones.clear();
        mBones.addAll(bones);

        NativeMesh.setBones(getNative(), GVRHybridObject.getNativePtrArray(mBones));

        // Process bones
        int boneId = -1;
        for (GVRBone bone : mBones) {
            boneId++;

            List<GVRBoneWeight> boneWeights = bone.getBoneWeights();
            for (GVRBoneWeight weight : boneWeights) {
                int vid = weight.getVertexId();
                int boneSlot = getVertexBoneData().getFreeBoneSlot(vid);
                if (boneSlot >= 0) {
                    getVertexBoneData().setVertexBoneWeight(vid, boneSlot, boneId, weight.getWeight());
                } else {
                    Log.w(TAG, "Vertex %d (total %d) has too many bones", vid, getVertices().length / 3);
                }
            }
        }
        if (getVertexBoneData() != null) {
            mAttributeKeys.add("a_bone_indices");
            mAttributeKeys.add("a_bone_weights");
            getVertexBoneData().normalizeWeights();
        }
    }

    /**
     * Gets the vertex bone data.
     *
     * @return the vertex bone data.
     */
    public GVRVertexBoneData getVertexBoneData() {
        return mVertexBoneData;
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        sb.append(getVertices() == null ? 0 : Integer.toString(getVertices().length / 3));
        sb.append(" vertices, ");
        sb.append(getIndices() == null ? 0 : Integer.toString(getIndices().length / 3));
        sb.append(" triangles, ");
        sb.append(getTexCoords() == null ? 0 : Integer.toString(getTexCoords().length / 2));
        sb.append(" tex-coords, ");
        sb.append(getNormals() == null ? 0 : Integer.toString(getNormals().length / 3));
        sb.append(" normals, ");
        sb.append(getBones() == null ? 0 : Integer.toString(getBones().size()));
        sb.append(" bones");
        sb.append(System.lineSeparator());

        // Bones
        List<GVRBone> bones = getBones();
        if (!bones.isEmpty()) {
            sb.append(Log.getSpaces(indent));
            sb.append("Bones:");
            sb.append(System.lineSeparator());

            for (GVRBone bone : bones) {
                bone.prettyPrint(sb, indent + 2);
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }

    private void checkValidFloatVector(String keyName, String key,
            String vectorName, float[] vector, int expectedComponents) {
        checkStringNotNullOrEmpty(keyName, key);
        checkDivisibleDataLength(vectorName, vector, expectedComponents);
        checkVectorLengthWithVertices(vectorName, vector.length,
                expectedComponents);
    }

    private void checkValidFloatArray(String parameterName, float[] data,
            int expectedComponents) {
        checkDivisibleDataLength(parameterName, data, expectedComponents);
    }

    private void checkVectorLengthWithVertices(String parameterName,
            int dataLength, int expectedComponents) {
        int verticesNumber = getVertices().length / 3;
        int numberOfElements = dataLength / expectedComponents;
        if (dataLength / expectedComponents != verticesNumber) {
            throw Exceptions
                    .IllegalArgument(
                            "The input array %s should be an array of %d-component elements and the number of elements should match the number of vertices. The current number of elements is %d, but the current number of vertices is %d.",
                            parameterName, expectedComponents,
                            numberOfElements, verticesNumber);
        }
    }

    private List<GVRBone> mBones = new ArrayList<GVRBone>();
    private GVRVertexBoneData mVertexBoneData;
    private Set<String> mAttributeKeys;
}

class NativeMesh {
    static native long ctor();
    
    static native String[] getAttribNames(long mesh);
    
    static native float[] getVertices(long mesh);

    static native void setVertices(long mesh, float[] vertices);

    static native float[] getNormals(long mesh);

    static native void setNormals(long mesh, float[] normals);

    static native float[] getTexCoords(long mesh);

    static native void setTexCoords(long mesh, float[] texCoords);

    static native char[] getTriangles(long mesh);

    static native void setTriangles(long mesh, char[] triangles);

    static native char[] getIndices(long mesh);

    static native void setIndices(long mesh, char[] indices);

    static native float[] getFloatVector(long mesh, String key);

    static native void setFloatVector(long mesh, String key, float[] floatVector);

    static native float[] getVec2Vector(long mesh, String key);

    static native void setVec2Vector(long mesh, String key, float[] vec2Vector);

    static native float[] getVec3Vector(long mesh, String key);

    static native void setVec3Vector(long mesh, String key, float[] vec3Vector);

    static native float[] getVec4Vector(long mesh, String key);

    static native void setVec4Vector(long mesh, String key, float[] vec4Vector);

    static native long getBoundingBox(long mesh);

    static native void setBones(long mesh, long[] bonePtrs);
    
    static native void getSphereBound(long mesh, float[] sphere);
    
    static native boolean hasAttribute(long mesh, String key);

}
