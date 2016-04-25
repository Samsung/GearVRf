package org.gearvrf.jassimp2;

import org.joml.Quaternionf;

import java.nio.ByteBuffer;

/*
 * V3, M4, C, N, Q
 */
public class GVRNewWrapperProvider implements AiWrapperProvider<float[], float[], AiColor, AiNode, Quaternionf> {
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
    public float[] wrapMatrix4f(float[] data) {
        return transpose(data);
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
    public Quaternionf wrapQuaternion(ByteBuffer buffer, int offset) {
    	float w = buffer.getFloat(offset);
    	float x = buffer.getFloat(offset + 4);
    	float y = buffer.getFloat(offset + 8);
    	float z = buffer.getFloat(offset + 12);
        return new Quaternionf(x, y, z, w);
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
        AiNode node = new AiNode((AiNode) parent, matrix, meshReferences, name);
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
    public float[] wrapVector3f(ByteBuffer buffer, int offset,
            int numComponents) {
    	return new float[] {
    			buffer.getFloat(offset),
    			buffer.getFloat(offset + 4),
    			buffer.getFloat(offset + 8)
    			};
    }

    public static float[] transpose(float[] matrix){
        float[] transposed = new float[16];
        transposed[0] = matrix[0];
        transposed[4] = matrix[1];
        transposed[8] = matrix[2];
        transposed[12] = matrix[3];
        transposed[1] = matrix[4];
        transposed[5] = matrix[5];
        transposed[9] = matrix[6];
        transposed[13] = matrix[7];
        transposed[2] = matrix[8];
        transposed[6] = matrix[9];
        transposed[10] = matrix[10];
        transposed[14] = matrix[11];
        transposed[3] = matrix[12];
        transposed[7] = matrix[13];
        transposed[11] = matrix[14];
        transposed[15] = matrix[15];
        return transposed;
    }
}
