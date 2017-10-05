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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

/**
 * Describes an interleaved vertex buffer containing vertex data for a mesh.
 *
 * Usually each vertex may have a positions, normal and texture coordinate.
 * Skinned mesh vertices will also have bone weights and indices.
 * If the mesh uses a normal map for lighting, it will have tangents
 * and bitangents as well. These vertex components correspond to vertex
 * attributes in the OpenGL vertex shader.
 */
public class GVRIndexBuffer extends GVRHybridObject implements PrettyPrint
{
    private static final String TAG = GVRIndexBuffer.class.getSimpleName();
    private String mDescriptor;

    public GVRIndexBuffer(GVRContext gvrContext, int bytesPerIndex, int indexCount)
    {
        super(gvrContext, NativeIndexBuffer.ctor(bytesPerIndex, indexCount));
    }

    public CharBuffer asCharBuffer()
    {
        int n = getIndexCount();
        if (getIndexSize() != 2)
        {
            throw new UnsupportedOperationException("Cannot convert integer indices to char array");
        }
        if (n <= 0)
        {
            return null;
        }
        CharBuffer data = ByteBuffer.allocateDirect(2 * n).order(ByteOrder.nativeOrder()).asCharBuffer();
        if (!NativeIndexBuffer.getShortVec(getNative(), data))
        {
            throw new IllegalArgumentException("Cannot convert integer indices to char buffer of size " + n);
        }
        return data;
    }

    public IntBuffer asIntBuffer()
    {
        int n = getIndexCount();
        if (getIndexSize() != 4)
        {
            throw new UnsupportedOperationException("Cannot convert short indices to int array");
        }
        if (n <= 0)
        {
            return null;
        }
        IntBuffer data = ByteBuffer.allocateDirect(4 * n).order(ByteOrder.nativeOrder()).asIntBuffer();
        if (!NativeIndexBuffer.getIntVec(getNative(), data))
        {
            throw new IllegalArgumentException("Cannot convert short indices to int buffer of size " + n);
        }
        return data;
    }

    public int[] asIntArray()
    {
        int n = getIndexCount();
        if (getIndexSize() != 4)
        {
            throw new UnsupportedOperationException("Cannot convert char indices to int array");
        }
        if (n <= 0)
        {
            return null;
        }
        return NativeIndexBuffer.getIntArray(getNative());
    }

    public char[] asCharArray()
    {
        int n = getIndexCount();
        if (getIndexSize() != 2)
        {
            throw new UnsupportedOperationException("Cannot convert int indices to char array");
        }
        if (n <= 0)
        {
            return null;
        }
        return NativeIndexBuffer.getShortArray(getNative());
    }

    /**
     * Updates the indices in the index buffer from a Java char array.
     * All of the entries of the input char array are copied into
     * the storage for the index buffer. The new indices must be the
     * same size as the old indices - the index buffer size cannot be changed.
     * @param data char array containing the new values
     * @throws IllegalArgumentException if char array is wrong size
     */
    public void setShortVec(char[] data)
    {
        if (data == null)
        {
            throw new IllegalArgumentException("Input data for indices cannot be null");
        }
        if (getIndexSize() != 2)
        {
            throw new UnsupportedOperationException("Cannot update integer indices with char array");
        }
        if (!NativeIndexBuffer.setShortArray(getNative(), data))
        {
            throw new UnsupportedOperationException("Input array is wrong size");
        }
    }

    /**
     * Updates the indices in the index buffer from a Java CharBuffer.
     * All of the entries of the input buffer are copied into
     * the storage for the index buffer. The new indices must be the
     * same size as the old indices - the index buffer size cannot be changed.
     * @param data CharBuffer containing the new values
     * @throws IllegalArgumentException if char array is wrong size
     */
    public void setShortVec(CharBuffer data)
    {
        if (data == null)
        {
            throw new IllegalArgumentException("Input data for indices cannot be null");
        }
        if (getIndexSize() != 2)
        {
            throw new UnsupportedOperationException("Cannot update integer indices with char array");
        }
        if (data.isDirect())
        {
            if (!NativeIndexBuffer.setShortVec(getNative(), data))
            {
                throw new UnsupportedOperationException("Input buffer is wrong size");
            }
        }
        else if (data.hasArray())
        {
            if (!NativeIndexBuffer.setShortArray(getNative(), data.array()))
            {
                throw new UnsupportedOperationException("Input buffer is wrong size");
            }
        }
        else
        {
            throw new UnsupportedOperationException(
                    "CharBuffer type not supported. Must be direct or have backing array");
        }
    }

    /**
     * Updates the indices in the index buffer from a Java int array.
     * All of the entries of the input int array are copied into
     * the storage for the index buffer. The new indices must be the
     * same size as the old indices - the index buffer size cannot be changed.
     * @param data char array containing the new values
     * @throws IllegalArgumentException if int array is wrong size
     */
    public void setIntVec(int[] data)
    {
        if (data == null)
        {
            throw new IllegalArgumentException("Input data for indices cannot be null");
        }
        if (getIndexSize() != 4)
        {
            throw new UnsupportedOperationException("Cannot update short indices with int array");
        }
        if (!NativeIndexBuffer.setIntArray(getNative(), data))
        {
            throw new UnsupportedOperationException("Input array is wrong size");
        }
    }

    /**
     * Updates the indices in the index buffer from a Java IntBuffer.
     * All of the entries of the input int buffer are copied into
     * the storage for the index buffer. The new indices must be the
     * same size as the old indices - the index buffer size cannot be changed.
     * @param data char array containing the new values
     * @throws IllegalArgumentException if int buffer is wrong size
     */
    public void setIntVec(IntBuffer data)
    {
        if (data == null)
        {
            throw new IllegalArgumentException("Input buffer for indices cannot be null");
        }
        if (getIndexSize() != 4)
        {
            throw new UnsupportedOperationException("Cannot update integer indices with short array");
        }
        if (data.isDirect())
        {
            if (!NativeIndexBuffer.setIntVec(getNative(), data))
            {
                throw new UnsupportedOperationException("Input array is wrong size");
            }
        }
        else if (data.hasArray())
        {
            if (!NativeIndexBuffer.setIntArray(getNative(), data.array()))
            {
                throw new IllegalArgumentException("Data array incompatible with index buffer");
            }
        }
        else
        {
            throw new UnsupportedOperationException("IntBuffer type not supported. Must be direct or have backing array");
        }
    }

    public int getIndexCount()
    {
        return NativeIndexBuffer.getIndexCount(getNative());
    }

    public int getIndexSize()
    {
        return NativeIndexBuffer.getIndexSize(getNative());
    }


    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        Integer n = getIndexCount();
        sb.append(n.toString() + " indices");
        sb.append(System.lineSeparator());
    }
}

class NativeIndexBuffer {
    static native long ctor(int bytesPerIndex, int indexCount);

    static native int getIndexCount(long ibuf);

    static native int getIndexSize(long ibuf);

    static native boolean getIntVec(long ibuf, IntBuffer data);

    static native boolean setIntVec(long ibuf, IntBuffer data);

    static native char[] getShortArray(long ibuf);

    static native int[] getIntArray(long ibuf);

    static native boolean setIntArray(long ibuf, int[] data);

    static native boolean getShortVec(long ibuf, CharBuffer data);

    static native boolean setShortVec(long ibuf, CharBuffer data);

    static native boolean setShortArray(long ibuf, char[] data);
}