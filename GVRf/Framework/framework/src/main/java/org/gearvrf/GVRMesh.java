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

import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
    static public final int MAX_BONES = 60;
    static public final int BONES_PER_VERTEX = 4;
    private static final String TAG = GVRMesh.class.getSimpleName();

    protected GVRVertexBuffer mVertices;
    protected GVRIndexBuffer mIndices;
    protected List<GVRBone> mBones = new ArrayList<GVRBone>();

    public GVRMesh(GVRContext gvrContext) {
        this(gvrContext, "float3 a_position float2 a_texcoord float3 a_normal ");
    }

    public GVRMesh(GVRVertexBuffer vbuffer, GVRIndexBuffer ibuffer)
    {
        super(vbuffer.getGVRContext(), NativeMesh.ctorBuffers(vbuffer.getNative(), (ibuffer != null) ? ibuffer.getNative() : 0L));
        mVertices = vbuffer;
        mIndices = ibuffer;
    }

    public GVRMesh(GVRContext gvrContext, String vertexDescriptor) {
        this(new GVRVertexBuffer(gvrContext, vertexDescriptor, 0), null);
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
        return mVertices.getFloatVec("a_position").array();
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
        mVertices.setFloatArray("a_position", vertices);
    }

    public GVRVertexBuffer getVertexBuffer() { return mVertices; }

    public GVRIndexBuffer getIndexBuffer() { return mIndices; }

    public void setVertexBuffer(GVRVertexBuffer vbuf)
    {
        if (vbuf == null)
        {
            throw new IllegalArgumentException("Vertex buffer cannot be null");
        }
        mVertices = vbuf;
        NativeMesh.setVertexBuffer(getNative(), vbuf.getNative());
    }

    public void setIndexBuffer(GVRIndexBuffer ibuf)
    {
        mIndices = ibuf;
        NativeMesh.setIndexBuffer(getNative(), (ibuf != null) ? ibuf.getNative() : 0L);
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
        return mVertices.getFloatArray("a_normal");
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
        mVertices.setFloatArray("a_normal", normals);
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
        return mVertices.getFloatArray("a_texcoord");
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
    public void setTexCoords(float[] texCoords)
    {
        setTexCoords(texCoords, 0);
    }

    public void setTexCoords(float [] texCoords, int index)
    {
        String key = (index > 0) ? ("a_texcoord" + index) : "a_texcoord";
        mVertices.setFloatArray(key, texCoords);
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
     * @return char array with the packed triangle index data.
     *
     */
    public char[] getTriangles() {
        return (mIndices != null) ? mIndices.asCharArray() : null;
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
     */
    public void setTriangles(char[] triangles)
    {
        if ((mIndices == null) && (triangles != null))
        {
            mIndices = new GVRIndexBuffer(getGVRContext(), 2, triangles.length);
            NativeMesh.setIndexBuffer(getNative(), mIndices.getNative());
        }
        mIndices.setShortVec(triangles);
    }

    public void setTriangles(int[] triangles)
    {
        if ((mIndices == null) && (triangles != null))
        {
            mIndices = new GVRIndexBuffer(getGVRContext(), 4, triangles.length);
            NativeMesh.setIndexBuffer(getNative(), mIndices.getNative());
        }
        mIndices.setIntVec(triangles);
    }

    /**
     * Get the vertex indices of the mesh. The indices for each
     * vertex to be referenced.
     *
     * @return int array with the packed index data.
     */
    public int[] getIndices() {
        return (mIndices != null) ? mIndices.asIntArray() : null;
    }

    /**
     * Sets the vertex indices of the mesh. The indices for each
     * vertex.
     *
     * @param indices
     *            int array containing the packed index data.
     */
    public void setIndices(int[] indices)
    {
        if (indices != null)
        {
            if (mIndices == null)
            {
                setIndexBuffer(new GVRIndexBuffer(getGVRContext(), 4, indices.length));
            }
            mIndices.setIntVec(indices);
        }
        else
        {
            mIndices = null;
            NativeMesh.setIndexBuffer(getNative(), 0L);
        }
    }

    public void setIndices(char[] indices)
    {
        if (indices != null)
        {
            if (mIndices == null)
            {
                setIndexBuffer(new GVRIndexBuffer(getGVRContext(), 2, indices.length));
            }
            mIndices.setShortVec(indices);
        }
        else
        {
            mIndices = null;
            NativeMesh.setIndexBuffer(getNative(), 0L);
        }
    }

    public void setIndices(CharBuffer indices)
    {
        if (indices != null)
        {
            if (mIndices == null)
            {
                setIndexBuffer(new GVRIndexBuffer(getGVRContext(), 2, indices.capacity() / 2));
            }
            mIndices.setShortVec(indices);
         }
        else
        {
            NativeMesh.setIndexBuffer(getNative(), 0L);
        }
    }

    /**
     * Get the array of {@code float} values associated with the vertex attribute
     * {@code key}.
     *
     * @param key   Name of the shader attribute
     * @return Array of {@code float} values containing the vertex data for the named channel.
     */
    public float[] getFloatArray(String key)
    {
        return mVertices.getFloatArray(key);
    }

    /**
     * Get the array of {@code integer} values associated with the vertex attribute
     * {@code key}.
     *
     * @param key   Name of the shader attribute
     * @return Array of {@code integer} values containing the vertex data for the named channel.
     */
    public int[] getIntArray(String key)
    {
        return mVertices.getIntArray(key);
    }

    /**
     * Bind an array of {@code int} values to the vertex attribute
     * {@code key}.
     *
     * @param key      Name of the vertex attribute
     * @param arr      Data to bind to the shader attribute.
     * @throws IllegalArgumentException if int array is wrong size
     */
    public void setIntArray(String key, int[] arr)
    {
        mVertices.setIntArray(key, arr);
    }

    /**
     * Bind a buffer of {@code float} values to the vertex attribute
     * {@code key}.
     *
     * @param key   Name of the vertex attribute
     * @param buf   Data buffer to bind to the shader attribute.
     * @throws IllegalArgumentException if attribute name not in descriptor or float buffer is wrong size
     */
    public void setIntVec(String key, IntBuffer buf)
    {
        mVertices.setIntVec(key, buf);
    }

    /**
     * Bind an array of {@code float} values to the vertex attribute
     * {@code key}.
     *
     * @param key   Name of the vertex attribute
     * @param arr   Data to bind to the shader attribute.
     * @throws IllegalArgumentException if attribute name not in descriptor or float array is wrong size
     */
    public void setFloatArray(String key, float[] arr)
    {
        mVertices.setFloatArray(key, arr);
    }

    /**
     * Bind a buffer of {@code float} values to the vertex attribute
     * {@code key}.
     *
     * @param key   Name of the vertex attribute
     * @param buf   Data buffer to bind to the shader attribute.
     * @throws IllegalArgumentException if attribute name not in descriptor or float buffer is wrong size
     */
    public void setFloatVec(String key, FloatBuffer buf)
    {
        mVertices.setFloatVec(key, buf);
    }

    /**
     * Calculate a bounding sphere from the mesh vertices.
     * @param sphere        float[4] array to get center of sphere and radius;
     *                      sphere[0] = center.x, sphere[1] = center.y, sphere[2] = center.z, sphere[3] = radius
     */
    public void getSphereBound(float[] sphere)
    {
        mVertices.getSphereBound(sphere);
    }

    /**
     * Calculate a bounding box from the mesh vertices.
     * @param bounds        float[6] array to get corners of box;
     *                      bounds[0,1,2] = minimum X,Y,Z and bounds[3,4,6] = maximum X,Y,Z
     */
    public void getBoxBound(float[] bounds)
    {
        mVertices.getBoxBound(bounds);
    }

    /**
     * Determine if a named attribute exists in this mesh.
     * @param key Name of the shader attribute
     * @return true if attribute exists, false if not
     */
    public boolean hasAttribute(String key) {
    	return mVertices.hasAttribute(key);
    }

    /**
     * Constructs a {@link GVRMesh mesh} that contains this mesh.
     * <p>
     * This is primarily useful with the {@link GVRPicker}, which does
     * "ray casting" to detect which scene object you're pointing to. Ray
     * casting is computationally expensive, and you generally want to limit the
     * number of {@linkplain GVRCollider triangles to check.} A simple
     * {@linkplain GVRContext#createQuad(float, float) quad} is cheap enough,
     * but with complex meshes you will probably want to cut search time by
     * registering the object's bounding box, not the whole mesh.
     * 
     * @return A {@link GVRMesh} of the bounding box.
     */
    public GVRMesh getBoundingBox()
    {
        GVRMesh meshbox = new GVRMesh(getGVRContext(), "float3 a_position");
        float[] bbox = new float[6];

        getBoxBound(bbox);
        float min_x = bbox[0];
        float min_y = bbox[1];
        float min_z = bbox[2];
        float max_x = bbox[3];
        float max_y = bbox[4];
        float max_z = bbox[5];
        float[] positions = {
                min_x, min_y, min_z,
                max_x, min_y, min_z,
                min_x, max_y, min_z,
                max_x, max_y, min_z,
                min_x, min_y, max_z,
                max_x, min_y, max_z,
                min_x, max_y, max_z,
                max_x, max_y, max_z
        };
        char indices[] = {
                0, 2, 1, 1, 2, 3, 1, 3, 7, 1, 7, 5, 4, 5, 6, 5, 7, 6, 0, 6, 2, 0, 4, 6, 0, 1, 5, 0,
                5, 4, 2, 7, 3, 2, 6, 7
        };
        meshbox.setVertices(positions);
        meshbox.setTriangles(indices);
        return meshbox;
    }

    /**
     * Sets the contents of this mesh to be a quad consisting of two triangles,
     * with the specified width and height. If the mesh descriptor allows for
     * normals and/or texture coordinates, they are added.
     *
     * @param width
     *            the quad's width
     * @param height
     *            the quad's height
     */
    public void createQuad(float width, float height)
    {
        String vertexDescriptor = getVertexBuffer().getDescriptor();
        float[] vertices = { width * -0.5f, height * 0.5f, 0.0f, width * -0.5f,
                height * -0.5f, 0.0f, width * 0.5f, height * 0.5f, 0.0f,
                width * 0.5f, height * -0.5f, 0.0f };
        setVertices(vertices);

        if (vertexDescriptor.contains("normal"))
        {
            final float[] normals = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f};
            setNormals(normals);
        }

        if (vertexDescriptor.contains("texcoord"))
        {
            final float[] texCoords = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};
            setTexCoords(texCoords);
        }

        char[] triangles = { 0, 1, 2, 1, 3, 2 };
        setTriangles(triangles);
    }

    /**
     * Creates a mesh whose vertices describe a quad consisting of two triangles,
     * with the specified width and height. If the vertex descriptor allows for
     * normals and/or texture coordinates, they are added.
     *
     * @param ctx         GVRContext to use for creating mesh.
     * @param vertexDesc  String describing vertex format of {@link GVRVertexBuffer}
     * @param width       the quad's width
     * @param height      the quad's height
     * @return A 2D, rectangular mesh with four vertices and two triangles
     */
    public static GVRMesh createQuad(GVRContext ctx, String vertexDesc, float width, float height)
    {
        GVRMesh mesh = new GVRMesh(ctx, vertexDesc);
        mesh.createQuad(width, height);
        return mesh;
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
    public void setBones(List<GVRBone> bones)
    {
        mBones.clear();
        mBones.addAll(bones);
        NativeMesh.setBones(getNative(), GVRHybridObject.getNativePtrArray(bones));
    }


    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        mVertices.prettyPrint(sb, indent);
        if (mIndices != null)
        {
            mIndices.prettyPrint(sb, indent);
        }
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
}

class NativeMesh {
    static native long ctorBuffers(long vertexBuffer, long indexBuffer);

    static native void setBones(long mesh, long[] bonePtrs);

    static native void setIndexBuffer(long mesh, long ibuf);

    static native void setVertexBuffer(long mesh, long vbuf);
}
