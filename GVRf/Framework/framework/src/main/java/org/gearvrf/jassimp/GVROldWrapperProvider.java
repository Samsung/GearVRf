package org.gearvrf.jassimp;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/*
 * V3, M4, C, N, Q
 */
public class GVROldWrapperProvider implements AiWrapperProvider<byte[], AiMatrix4f, AiColor, AiNode, byte[]> {

    /**
     * Wraps a RGBA color.
     * <p>
     * 
     * A color consists of 4 float values (r,g,b,a) starting from offset
     * 
     * @param buffer
     *            the buffer to wrap
     * @param offset
     *            the offset into buffer
     * @return the wrapped color
     */
    @Override
    public AiColor wrapColor(ByteBuffer buffer, int offset) {
        AiColor color = new AiColor(buffer, offset);
        return color;
    }

    /**
     * Wraps a 4x4 matrix of floats.
     * <p>
     * 
     * The calling code will allocate a new array for each invocation of
     * this method. It is safe to store a reference to the passed in
     * array and use the array to store the matrix data.
     * 
     * @param data
     *            the matrix data in row-major order
     * @return the wrapped matrix
     */
    @Override
    public AiMatrix4f wrapMatrix4f(float[] data) {

        AiMatrix4f transformMatrix = new AiMatrix4f(data);
        return transformMatrix;
    }

    /**
     * Wraps a quaternion.
     * <p>
     * 
     * A quaternion consists of 4 float values (w,x,y,z) starting from
     * offset
     * 
     * @param buffer
     *            the buffer to wrap
     * @param offset
     *            the offset into buffer
     * @return the wrapped quaternion
     */
    @Override
    public byte[] wrapQuaternion(ByteBuffer buffer, int offset) {
        byte[] quaternion = new byte[4];
        buffer.get(quaternion, offset, 4);
        return quaternion;
    }

    /**
     * Wraps a scene graph node.
     * <p>
     * 
     * See {@link AiNode} for a description of the scene graph structure
     * used by assimp.
     * <p>
     * 
     * The parent node is either null or an instance returned by this
     * method. It is therefore safe to cast the passed in parent object
     * to the implementation specific type
     * 
     * @param parent
     *            the parent node
     * @param matrix
     *            the transformation matrix
     * @param meshReferences
     *            array of mesh references (indexes)
     * @param name
     *            the name of the node
     * @return the wrapped scene graph node
     */
    @Override
    public AiNode wrapSceneNode(Object parent, Object matrix,
            int[] meshReferences, String name) {

        AiNode node = new AiNode(null, matrix, meshReferences, name);

        return node;
    }

    /**
     * Wraps a vector.
     * <p>
     * 
     * Most vectors are 3-dimensional, i.e., with 3 components. The
     * exception are texture coordinates, which may be 1- or
     * 2-dimensional. A vector consists of numComponents floats (x,y,z)
     * starting from offset
     * 
     * @param buffer
     *            the buffer to wrap
     * @param offset
     *            the offset into buffer
     * @param numComponents
     *            the number of components
     * @return the wrapped vector
     */
    @Override
    public byte[] wrapVector3f(ByteBuffer buffer, int offset,
            int numComponents) {
        byte[] warpedVector = new byte[numComponents];
        buffer.get(warpedVector, offset, numComponents);
        return warpedVector;
    }

    public static float[] transpose(FloatBuffer fbuf) {
        float[] transposed = new float[16];
        int[] indices = { 0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15 };
        for (int i = 0; i < 16; ++i) {
        	transposed[indices[i]] = fbuf.get();
        }
        return transposed;
    }
}
