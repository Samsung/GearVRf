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

import org.gearvrf.utility.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.IllegalBlockingModeException;

/**
 * Describes a set of vertices used by an indexed triangle mesh.
 * <p>
 * Usually each vertex may have a position, normal and texture coordinate.
 * Skinned mesh vertices will also have bone weights and indices.
 * If the mesh uses a normal map for lighting, it will have tangents
 * and bitangents as well. These vertex components correspond to vertex
 * attributes in the OpenGL vertex shader.
 * Multiple meshes may share a single vertex buffer.
 * <p>
 * When a vertex buffer is constructed, a string descriptor is
 * supplied which describes the format of the vertices (the name and
 * type of each vertex component). Once the format has been established,
 * it cannot be subsequently changed - you cannot add new vertex
 * components. You can change the vertex or index buffer associated
 * with a mesh at any time.
 * <p>
 * Skinned meshes have bone weights and bone indices which designate
 * which bones affect each vertex and how much. The bones are supplied
 * as a list of {@link GVRBone} objects which have the name of the bone
 * and its associated matrices. The asset loader handles constructing
 * skinned meshes and their associated bones. GearVRF keeps the bones
 * for each mesh in a uniform buffer and skinning is performed by\
 * shaders on the GPU.
 * @see GVRMesh
 * @see GVRIndexBuffer
 * @see GVRBone
 * @see GVRAssetLoader
 * @see org.gearvrf.animation.keyframe.GVRSkinningController
 */
public class GVRVertexBuffer extends GVRHybridObject implements PrettyPrint
{
    private static final String TAG = GVRVertexBuffer.class.getSimpleName();
    private String mDescriptor;

    /**
     * Construct a vertex buffer with the specified vertex layout.
     * @param gvrContext  GVRContext to associate vertex buffer with.
     * @param descriptor  string describing vertex layout.
     *                    Each vertex component has a name and a type.
     *                    The types may be "int", "float" or "mat"
     *                    followed by an integer indicating vector size.
     * @param vertexCount number of vertices in the buffer.
     *                    The vertex count cannot be changed once the
     *                    vertex buffer is constructed.
     * Vertex Descriptor Examples:
     * <ul>
     * <li>float3 a_position float2 a_texcoord float3 a_normal</li>
     * <li>float3 a_position, int4 a_bone_indices, float4 a_bone_weights</li>
     * </ul>
     */
    public GVRVertexBuffer(GVRContext gvrContext, String descriptor, int vertexCount)
    {
        super(gvrContext, NativeVertexBuffer.ctor(descriptor, vertexCount));
        mDescriptor = descriptor;
    }

    /**
     * Check if a vertex attribute is present in this buffer'
     * and has data.
     * @param attributeName name of vertex attribute
     * @return true if this vertex buffer has been populated with data.
     */
    public boolean hasAttribute(String attributeName)
    {
        return NativeVertexBuffer.isSet(getNative(), attributeName);
    }

    /**
     * Retrieves a vertex attribute as a float buffer.
     * The attribute name must be one of the
     * attributes named in the descriptor passed to the constructor.
     * @param attributeName name of the attribute to update
     * @throws IllegalArgumentException if attribute name not in descriptor vertex attribute is not <i>float</i>
     * @see #setFloatArray(String, float[])
     * @see #getFloatVec(String)
     */
    public FloatBuffer getFloatVec(String attributeName)
    {
        int size = getAttributeSize(attributeName);
        if (size <= 0)
        {
            return null;
        }
        size *= 4 * getVertexCount();
        ByteBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        FloatBuffer data = buffer.asFloatBuffer();
        if (!NativeVertexBuffer.getFloatVec(getNative(), attributeName, data, 0, 0))
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be accessed");
        }
        return data;
    }

    /**
     * Retrieves a vertex attribute as a float array.
     * The attribute name must be one of the
     * attributes named in the descriptor passed to the constructor.
     * @param attributeName name of the attribute to update
     * @throws IllegalArgumentException if attribute name not in descriptor vertex attribute is not <i>float</i>
     * @see #setFloatVec(String, FloatBuffer)
     * @see #getFloatArray(String)
     */
    public float[] getFloatArray(String attributeName)
    {
        float[] array = NativeVertexBuffer.getFloatArray(getNative(), attributeName);
        if (array == null)
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be accessed");
        }
        return array;
    }

    /**
     * Retrieves a vertex attribute as an integer buffer.
     * The attribute name must be one of the
     * attributes named in the descriptor passed to the constructor.
     * @param attributeName name of the attribute to update
     * @throws IllegalArgumentException if attribute name not in descriptor vertex attribute is not <i>int</i>
     * @see #setIntArray(String, int[])
     * @see #getIntVec(String)
     */
    public IntBuffer getIntVec(String attributeName)
    {
        int size = getAttributeSize(attributeName);
        if (size <= 0)
        {
            return null;
        }
        size *= 4 * getVertexCount();
        ByteBuffer buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
        IntBuffer data = buffer.asIntBuffer();
        if (!NativeVertexBuffer.getIntVec(getNative(), attributeName, data, 0, 0))
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be accessed");
        }
        return data;
    }

    /**
     * Retrieves a vertex attribute as an integer array.
     * The attribute name must be one of the
     * attributes named in the descriptor passed to the constructor.
     * @param attributeName name of the attribute to update
     * @throws IllegalArgumentException if attribute name not in descriptor vertex attribute is not <i>int</i>
     * @see #setIntVec(String, IntBuffer)
     * @see #getIntArray(String)
     */
    public int[] getIntArray(String attributeName)
    {
        int[] array = NativeVertexBuffer.getIntArray(getNative(), attributeName);
        if (array == null)
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be accessed");
        }
        return array;
    }

    /**
     * Updates a vertex attribute from an integer array.
     * All of the entries of the input integer array are copied into
     * the storage for the named vertex attribute. Other vertex
     * attributes are not affected.
     * The attribute name must be one of the attributes named
     * in the descriptor passed to the constructor.
     * <p>
     * All vertex attributes have the same number of entries.
     * If this is the first attribute added to the vertex buffer,
     * the size of the input data array will determine the number of vertices.
     * Updating subsequent attributes will fail if the data array
     * size is not consistent. For example, if you create a vertex
     * buffer with descriptor "float3 a_position float2 a_texcoord"
     * and provide an array of 12 floats for "a_position" this will result in
     * a vertex count of 4. The corresponding data array for the
     * "a_texcoord" attribute should contain 8 floats.
     * @param attributeName name of the attribute to update
     * @param data integer array containing the new values
     * @throws IllegalArgumentException if attribute name not in descriptor or int array is wrong size
     */
    public void setIntArray(String attributeName, int[] data)
    {
        if (!NativeVertexBuffer.setIntArray(getNative(), attributeName, data, 0, 0))
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
        }
    }

    /**
     * Updates a vertex attribute from an integer array.
     * All of the entries of the input integer array are copied into
     * the storage for the named vertex attribute. Other vertex
     * attributes are not affected.
     * The attribute name must be one of the attributes named
     * in the descriptor passed to the constructor.
     * <p>
     * All vertex attributes have the same number of entries.
     * If this is the first attribute added to the vertex buffer,
     * the size of the input data array will determine the number of vertices.
     * Updating subsequent attributes will fail if the data array
     * size is not consistent. For example, if you create a vertex
     * buffer with descriptor "float3 a_position float2 a_texcoord"
     * and provide an array of 12 floats for "a_position" this will result in
     * a vertex count of 4. The corresponding data array for the
     * "a_texcoord" attribute should contain 8 floats.
     * @param attributeName name of the attribute to update
     * @param data integer array containing the new values
     * @param stride number of ints in the attribute
     * @param offset offset from start of array of where to start copying source data
     * @throws IllegalArgumentException if attribute name not in descriptor or int array is wrong size
     */
    public void setIntArray(String attributeName, int[] data, int stride, int offset)
    {
        if (!NativeVertexBuffer.setIntArray(getNative(), attributeName, data, stride, offset))
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
        }
    }

    /**
     * Updates a vertex attribute from a float array.
     * All of the entries of the input float array are copied into
     * the storage for the named vertex attribute. Other vertex
     * attributes are not affected.
     * The attribute name must be one of the attributes named
     * in the descriptor passed to the constructor.
     * <p>
     * All vertex attributes have the same number of entries.
     * If this is the first attribute added to the vertex buffer,
     * the size of the input data array will determine the number of vertices.
     * Updating subsequent attributes will fail if the data array
     * size is not consistent. For example, if you create a vertex
     * buffer with descriptor "float3 a_position float2 a_texcoord"
     * and provide an array of 12 floats for "a_position" this will result in
     * a vertex count of 4. The corresponding data array for the
     * "a_texcoord" attribute should contain 8 floats.
     * @param attributeName name of the attribute to update
     * @param data float array containing the new values
     * @throws IllegalArgumentException if attribute name not in descriptor or float array is wrong size
     */
    public void setFloatArray(String attributeName, float[] data)
    {
        if (!NativeVertexBuffer.setFloatArray(getNative(), attributeName, data, 0, 0))
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
        }
    }

    /**
     * Updates a vertex attribute from a float array.
     * All of the entries of the input float array are copied into
     * the storage for the named vertex attribute. Other vertex
     * attributes are not affected.
     * The attribute name must be one of the attributes named
     * in the descriptor passed to the constructor.
     * <p>
     * All vertex attributes have the same number of entries.
     * If this is the first attribute added to the vertex buffer,
     * the size of the input data array will determine the number of vertices.
     * Updating subsequent attributes will fail if the data array
     * size is not consistent. For example, if you create a vertex
     * buffer with descriptor "float3 a_position float2 a_texcoord"
     * and provide an array of 12 floats for "a_position" this will result in
     * a vertex count of 4. The corresponding data array for the
     * "a_texcoord" attribute should contain 8 floats.
     * @param attributeName name of the attribute to update
     * @param data float array containing the new values
     * @param stride number of floats in the attribute
     * @param offset offset from start of array of where to start copying source data
     * @throws IllegalArgumentException if attribute name not in descriptor or float array is wrong size
     */
    public void setFloatArray(String attributeName, float[] data, int stride, int offset)
    {
        if (!NativeVertexBuffer.setFloatArray(getNative(), attributeName, data, stride, offset))
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
        }
    }

    /**
     * Updates a vertex attribute from a float buffer.
     * All of the entries of the input float buffer are copied into
     * the storage for the named vertex attribute. Other vertex
     * attributes are not affected.
     * The attribute name must be one of the attributes named
     * in the descriptor passed to the constructor.
     * <p>
     * All vertex attributes have the same number of entries.
     * If this is the first attribute added to the vertex buffer,
     * the size of the input data buffer will determine the number of vertices.
     * Updating subsequent attributes will fail if the data buffer
     * size is not consistent. For example, if you create a vertex
     * buffer with descriptor "float3 a_position float2 a_texcoord"
     * and provide an data buffer of 12 floats for "a_position" this will result in
     * a vertex count of 4. The corresponding data buffer for the
     * "a_texcoord" attribute should contain 8 floats.
     * @param attributeName name of the attribute to update
     * @param data float buffer containing the new values
     * @throws IllegalArgumentException if attribute name not in descriptor or float buffer is wrong size
     */
    public void setFloatVec(String attributeName, FloatBuffer data)
    {
        if (data.isDirect())
        {
            if (!NativeVertexBuffer.setFloatVec(getNative(), attributeName, data, 0, 0))
            {
                throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
            }
        }
        else if (data.hasArray())
        {
            if (!NativeVertexBuffer.setFloatArray(getNative(), attributeName, data.array(), 0, 0))
            {
                throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
            }
        }
        else
        {
            throw new UnsupportedOperationException("FloatBuffer type not supported. must be direct or have backing array");
        }
    }

    /**
     * Updates a vertex attribute from a float buffer.
     * All of the entries of the input float buffer are copied into
     * the storage for the named vertex attribute. Other vertex
     * attributes are not affected.
     * The attribute name must be one of the attributes named
     * in the descriptor passed to the constructor.
     * <p>
     * All vertex attributes have the same number of entries.
     * If this is the first attribute added to the vertex buffer,
     * the size of the input data buffer will determine the number of vertices.
     * Updating subsequent attributes will fail if the data buffer
     * size is not consistent. For example, if you create a vertex
     * buffer with descriptor "float3 a_position float2 a_texcoord"
     * and provide an data buffer of 12 floats for "a_position" this will result in
     * a vertex count of 4. The corresponding data buffer for the
     * "a_texcoord" attribute should contain 8 floats.
     * @param attributeName name of the attribute to update
     * @param data float buffer containing the new values
     * @param stride number of floats in the attribute
     * @param offset offset from start of array of where to start copying source data
     * @throws IllegalArgumentException if attribute name not in descriptor or float buffer is wrong size
     */
    public void setFloatVec(String attributeName, FloatBuffer data, int stride, int offset)
    {
        if (data.isDirect())
        {
            if (!NativeVertexBuffer.setFloatVec(getNative(), attributeName, data, stride, offset))
            {
                throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
            }
        }
        else if (data.hasArray())
        {
            if (!NativeVertexBuffer.setFloatArray(getNative(), attributeName, data.array(), stride, offset))
            {
                throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
            }
        }
        else
        {
            throw new UnsupportedOperationException("FloatBuffer type not supported. must be direct or have backing array");
        }
    }

    /**
     * Updates a vertex attribute from an integer  buffer.
     * All of the entries of the input buffer are copied into
     * the storage for the named vertex attribute. Other vertex
     * attributes are not affected.
     * The attribute name must be one of the attributes named
     * in the descriptor passed to the constructor.
     * <p>
     * All vertex attributes have the same number of entries.
     * If this is the first attribute added to the vertex buffer,
     * the size of the input data array will determine the number of vertices.
     * Updating subsequent attributes will fail if the data array
     * size is not consistent. For example, if you create a vertex
     * buffer with descriptor "... float4 a_bone_weights int4 a_bone_indices"
     * and provide an array of 16 ints this will result in
     * a vertex count of 4. The corresponding data array for the
     * "a_bone_indices" attribute should also contain 16 floats.
     * @param attributeName name of the attribute to update
     * @param data IntBuffer containing the new values
     * @throws IllegalArgumentException if attribute name not in descriptor or buffer is wrong size
     */
    public void setIntVec(String attributeName, IntBuffer data)
    {
        if (!NativeVertexBuffer.setIntVec(getNative(), attributeName, data, 0, 0))
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
        }
    }

    /**
     * Updates a vertex attribute from an integer  buffer.
     * All of the entries of the input buffer are copied into
     * the storage for the named vertex attribute. Other vertex
     * attributes are not affected.
     * The attribute name must be one of the attributes named
     * in the descriptor passed to the constructor.
     * <p>
     * All vertex attributes have the same number of entries.
     * If this is the first attribute added to the vertex buffer,
     * the size of the input data array will determine the number of vertices.
     * Updating subsequent attributes will fail if the data array
     * size is not consistent. For example, if you create a vertex
     * buffer with descriptor "... float4 a_bone_weights int4 a_bone_indices"
     * and provide an array of 16 ints this will result in
     * a vertex count of 4. The corresponding data array for the
     * "a_bone_indices" attribute should also contain 16 floats.
     * @param attributeName name of the attribute to update
     * @param data IntBuffer containing the new values
     * @param stride number of ints in the attribute
     * @param offset offset from start of array of where to start copying source data
     * @throws IllegalArgumentException if attribute name not in descriptor or buffer is wrong size
     */
    public void setIntVec(String attributeName, IntBuffer data, int stride, int offset)
    {
        if (!NativeVertexBuffer.setIntVec(getNative(), attributeName, data, stride, offset))
        {
            throw new IllegalArgumentException("Attribute name " + attributeName + " cannot be updated");
        }
    }

    /**
     * Get the number of vertices in this vertex buffer.
     * <p>
     * This value is established when the vertex buffer
     * is constructed and cannot be subsequently changed.
     * If no data has been provided, the vertex count is 0.
     * @return number of vertices in the buffer.
     */
    public int getVertexCount()
    {
        return NativeVertexBuffer.getVertexCount(getNative());
    }

    /**
     * Get the vertex descriptor with the names and types of vertex attributes.
     * The types may be "int", "float" or "mat"
     * followed by an integer indicating vector size.
     * <br>
     * Vertex Descriptor Examples:
     * <ul>
     * <li>float3 a_position float2 a_texcoord float3 a_normal</li>
     * <li>float3 a_position, int4 a_bone_indices, float4 a_bone_weights</li>
     * </ul>
     * @return string descriptor for vertex layout.
     */
    public String getDescriptor()
    {
        return mDescriptor;
    }

    /**
     * Gets the number of floats/ints occupied by a particular attribute.
     * For a uniform block, this is the data area size. For a vertex array,
     * it is the size of the vertex component.
     *
     * @param name attribute name
     * @return number of floats/ints occupied by the attribute or 0 if not found
     */
    public int getAttributeSize(String name)
    {
        return NativeVertexBuffer.getAttributeSize(getNative(), name);
    }

    /**
     * Returns the bounding sphere of the vertices.
     * @param sphere destination array to get bounding sphere.
     *               The first entry is the radius, the next
     *               three are the center.
     * @return radius of bounding sphere or 0.0 if no vertices
     */
    public float getSphereBound(float[] sphere)
    {
        if ((sphere == null) || (sphere.length != 4) ||
            ((NativeVertexBuffer.getBoundingVolume(getNative(), sphere)) < 0))
        {
            throw new IllegalArgumentException("Cannot copy sphere bound into array provided");
        }
        return sphere[0];
    }

    /**
     * Returns the bounding box of the vertices.
     * @param corners destination array to get corners of bounding box.
     *               The first three entries are the minimum X,Y,Z values
     *               and the next three are the maximum X,Y,Z.
     * @return true if bounds are not empty, false if empty (no vertices)
     */
    public boolean getBoxBound(float[] corners)
    {
        int rc;
        if ((corners == null) || (corners.length != 6) ||
            ((rc = NativeVertexBuffer.getBoundingVolume(getNative(), corners)) < 0))
        {
            throw new IllegalArgumentException("Cannot copy box bound into array provided");
        }
        return rc != 0;
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        Integer n = getVertexCount();
        sb.append(getDescriptor() + " " + n.toString() + " vertices");
        sb.append(System.lineSeparator());
    }

    public void dump(String attrName) {
        NativeVertexBuffer.dump(getNative(), attrName);
    }
}

class NativeVertexBuffer {
    static native long ctor(String descriptor, int vertexCount);

    static native int getVertexCount(long vbuf);

    static native boolean isSet(long vbuf, String name);

    static native boolean getIntVec(long vbuf, String name, IntBuffer data, int stride, int offset);

    static native boolean setIntVec(long vbuf, String name, IntBuffer data, int stride, int offset);

    static native boolean getFloatVec(long vbuf, String name, FloatBuffer data, int stride, int offset);

    static native float[] getFloatArray(long vbuf, String name);

    static native int[] getIntArray(long vbuf, String name);

    static native boolean setIntArray(long vbuf, String name, int[] data, int stride, int offset);

    static native boolean setFloatVec(long vbuf, String name, FloatBuffer data, int stride, int offset);

    static native boolean setFloatArray(long vbuf, String name, float[] data, int stride, int offset);

    static native int  getAttributeSize(long vbuf, String name);

    static native int getBoundingVolume(long vbuf, float[] bv);

    static native void dump(long vbuf, String attrName);
}