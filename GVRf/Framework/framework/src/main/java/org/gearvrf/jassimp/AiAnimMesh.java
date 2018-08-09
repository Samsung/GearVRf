package org.gearvrf.jassimp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by sushant.o on 7/10/18.
 */

public class AiAnimMesh {

    private final int SIZEOF_FLOAT = Jassimp.NATIVE_FLOAT_SIZE;

    /**
     * Number of bytes per int value.
     */
    private final int SIZEOF_INT = Jassimp.NATIVE_INT_SIZE;

    /**
     * Size of an AiVector3D in the native world.
     */
    private final int SIZEOF_V3D = Jassimp.NATIVE_AIVEKTOR3D_SIZE;


    private static final int NORMALS = 0;
    private static final int TANGENTS = 1;


    /**
     * Returns a buffer containing vertex positions.<p>
     *
     * A vertex position consists of a triple of floats, the buffer will
     * therefore contain <code>3 * getNumVertices()</code> floats
     *
     * @return a native-order direct buffer, or null if no data is available
     */
    public FloatBuffer getPositionBuffer() {
        if (m_vertices == null) {
            return null;
        }

        return m_vertices.asFloatBuffer();
    }

    /**
     * Returns a buffer containing normals.<p>
     *
     * A normal consists of a triple of floats, the buffer will
     * therefore contain <code>3 * getNumVertices()</code> floats
     *
     * @return a native-order direct buffer
     */
    public FloatBuffer getNormalBuffer() {
        if (m_normals == null) {
            return null;
        }

        return m_normals.asFloatBuffer();
    }

    /**
     * Returns a buffer containing tangents.<p>
     *
     * A tangent consists of a triple of floats, the buffer will
     * therefore contain <code>3 * getNumVertices()</code> floats
     *
     * @return a native-order direct buffer
     */

    public FloatBuffer getTangentBuffer() {
        if (m_tangents == null) {
            return null;
        }

        return m_tangents.asFloatBuffer();
    }

    /**
     *
     * @return the default weight of the animation mesh
     */

    public float getDefaultWeight() {
        return m_weight;
    }



    /**
     * This method is used by JNI. Do not call or modify.<p>
     *
     * Allocates byte buffers
     *
     * @param numVertices the number of vertices in the mesh
     * @param numFaces the number of faces in the mesh
     * @param optimizedFaces set true for optimized face representation
     * @param faceBufferSize size of face buffer for non-optimized face
     *              representation
     */
    @SuppressWarnings("unused")
    private void allocateBuffers(int numVertices) {
        /*
         * the allocated buffers are native order direct byte buffers, so they
         * can be passed directly to LWJGL or similar graphics APIs
         */

        m_numVertices = numVertices;

        /* allocate for each vertex 3 floats */
        if (m_numVertices > 0) {
            m_vertices = ByteBuffer.allocateDirect(numVertices * 3 *
                    SIZEOF_FLOAT);
            m_vertices.order(ByteOrder.nativeOrder());
        }

    }


    /**
     * This method is used by JNI. Do not call or modify.<p>
     *
     * Allocates a byte buffer for a vertex data channel
     *
     * @param channelType the channel type
     * @param channelIndex sub-index, used for types that can have multiple
     *              channels, such as texture coordinates
     */
    @SuppressWarnings("unused")
    private void allocateDataChannel(int channelType) {
        switch (channelType) {
            case NORMALS:
                m_normals = ByteBuffer.allocateDirect(
                        m_numVertices * 3 * SIZEOF_FLOAT);
                m_normals.order(ByteOrder.nativeOrder());
                break;
            case TANGENTS:
                m_tangents = ByteBuffer.allocateDirect(
                        m_numVertices * 3 * SIZEOF_FLOAT);
                m_tangents.order(ByteOrder.nativeOrder());
                break;
            default:
                throw new IllegalArgumentException("unsupported channel type");
        }
    }

    /**
     * Buffer for vertex position data.
     */
    private ByteBuffer m_vertices = null;

    /**
     * Buffer for normals.
     */
    private ByteBuffer m_normals = null;

    /**
     * Buffer for tangents.
     */
    private ByteBuffer m_tangents = null;

    /**
     * Number of vertices in this mesh.
     */
    private int m_numVertices = 0;


    /**
     * Weight of the AnimMesh.
     */
    private float m_weight = 0;


    /**
     * Buffer for bitangents.
     */
    private ByteBuffer m_bitangents = null;

    /**
     * Vertex colors.
     */
    private ByteBuffer[] m_colorsets =
            new ByteBuffer[JassimpConfig.MAX_NUMBER_COLORSETS];

    /**
     * Texture coordinates.
     */
    private ByteBuffer[] m_texcoords =
            new ByteBuffer[JassimpConfig.MAX_NUMBER_TEXCOORDS];

}