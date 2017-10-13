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
 * Contains face indices for an indexed triangle mesh.
 * <p>
 * Each entry references a vertex in the {@link GVRVertexBuffer}
 * associated with the mesh. The indices may be either <i>char</i>
 * or <i>int</i>. The size and type of the indices are established
 * when the buffer is constructed and cannot be subsequently changed.
 * @see GVRMesh
 * @see GVRVertexBuffer
 */
public class GVRIndexBuffer extends GVRHybridObject implements PrettyPrint
{
    private static final String TAG = GVRIndexBuffer.class.getSimpleName();
    private String mDescriptor;

    /**
     * Constructs an index buffer of a give size and type.
     * @param gvrContext        GVRContext to associate index buffer with.
     * @param bytesPerIndex     Number of bytes per index: may be 2 or 4.
     * @param indexCount        Number of indices in the buffer.
     * @throws IllegalArgumentException if <i>bytesPerIndex</i> is not 2 or 4.
     */
    public GVRIndexBuffer(GVRContext gvrContext, int bytesPerIndex, int indexCount)
    {
        super(gvrContext, NativeIndexBuffer.ctor(bytesPerIndex, indexCount));
    }

    /**
     * Get the triangle vertex indices of the mesh as a CharBuffer.
     * <p>
     * The indices for each
     * triangle are represented as a packed {@code char} triplet, where
     * {@code t0} is the first triangle, {@code t1} is the second, etc.:
     * <p>
     * <code>
     * { t0[0], t0[1], t0[2], t1[0], t1[1], t1[2], ...}
     * </code>
     * <p>
     * Face indices may also be {@code int}. If you have specified
     * char indices, this function will throw an exception.
     * @return CharBuffer with the packed triangle index data.
     * @see #asIntBuffer()
     * @throws IllegalArgumentException if index buffer is not <i>char</i>
     */
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

    /**
     * Get the triangle vertex indices of the mesh as a IntBuffer.
     * <p>
     * The indices for each
     * triangle are represented as a packed {@code int} triplet, where
     * {@code t0} is the first triangle, {@code t1} is the second, etc.:
     * <p>
     * <code>
     * { t0[0], t0[1], t0[2], t1[0], t1[1], t1[2], ...}
     * </code>
     * <p>
     * Face indices may also be {@code char}. If you have specified
     * char indices, this function will throw an exception.
     * @return IntBuffer with the packed triangle index data.
     * @see #asCharBuffer()
     * @throws IllegalArgumentException if index buffer is not <i>int</i>
     */
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

    /**
     * Get the triangle vertex indices of the mesh as an integer array.
     * <p>
     * The indices for each
     * triangle are represented as a packed {@code int} triplet, where
     * {@code t0} is the first triangle, {@code t1} is the second, etc.:
     * <p>
     * <code>
     * { t0[0], t0[1], t0[2], t1[0], t1[1], t1[2], ...}
     * </code>
     * <p>
     * Face indices may also be {@code char}. If you have specified
     * char indices, this function will throw an exception.
     * @return integer array with the packed triangle index data.
     * @see #asCharArray()
     * @throws IllegalArgumentException if index buffer is not <i>int</i>
     */
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

    /**
     * Get the triangle vertex indices of the mesh as a char array.
     * <p>
     * The indices for each
     * triangle are represented as a packed {@code char} triplet, where
     * {@code t0} is the first triangle, {@code t1} is the second, etc.:
     * <p>
     * <code>
     * { t0[0], t0[1], t0[2], t1[0], t1[1], t1[2], ...}
     * </code>
     * <p>
     * Face indices may also be {@code int}. If you have specified
     * char indices, this function will throw an exception.
     * @return char array with the packed triangle index data.
     * @see #asIntArray()
     * @throws IllegalArgumentException if index buffer is not <i>char</i>
     */
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

    /**
     * Get the number of indices in this index buffer.
     * <p>
     * This value is established when the index buffer
     * is constructed and cannot be subsequently changed.
     * If no data has been provided, the index count is 0.
     * @return number of indices in the buffer.
     */
    public int getIndexCount()
    {
        return NativeIndexBuffer.getIndexCount(getNative());
    }

    /**
     * Get the number of bytes per index.
     * <p>
     * This value is established when the index buffer
     * is constructed and cannot be subsequently changed.
     * It will be eithr 2 or 4.
     * @return number of bytes per index.
     */
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