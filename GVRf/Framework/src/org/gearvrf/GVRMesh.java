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

import org.gearvrf.utility.Exceptions;

import android.util.Log;
import static org.gearvrf.utility.Preconditions.*;

/**
 * This is one of the key GVRF classes: It holds GL meshes.
 * 
 * A GL mesh is a net of triangles that define an object's surface geometry.
 */
public class GVRMesh extends GVRHybridObject {
    public GVRMesh(GVRContext gvrContext) {
        super(gvrContext, NativeMesh.ctor());
    }

    private GVRMesh(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    static GVRMesh factory(GVRContext gvrContext, long ptr) {
        GVRHybridObject wrapper = wrapper(ptr);
        return wrapper == null ? new GVRMesh(gvrContext, ptr)
                : (GVRMesh) wrapper;
    }

    @Override
    protected final boolean registerWrapper() {
        return true;
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
        return NativeMesh.getVertices(getPtr());
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
        NativeMesh.setVertices(getPtr(), vertices);
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
        return NativeMesh.getNormals(getPtr());
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
        NativeMesh.setNormals(getPtr(), normals);
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
        return NativeMesh.getTexCoords(getPtr());
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
        NativeMesh.setTexCoords(getPtr(), texCoords);
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
     */
    public char[] getTriangles() {
        return NativeMesh.getTriangles(getPtr());
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
    public void setTriangles(char[] triangles) {
        checkNotNull("triangles", triangles);
        checkDivisibleDataLength("triangles", triangles.length, 3);
        NativeMesh.setTriangles(getPtr(), triangles);
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
        return NativeMesh.getFloatVector(getPtr(), key);
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
        NativeMesh.setFloatVector(getPtr(), key, floatVector);
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
        return NativeMesh.getVec2Vector(getPtr(), key);
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
        NativeMesh.setVec2Vector(getPtr(), key, vec2Vector);
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
        return NativeMesh.getVec3Vector(getPtr(), key);
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
        NativeMesh.setVec3Vector(getPtr(), key, vec3Vector);
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
        return NativeMesh.getVec4Vector(getPtr(), key);
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
        NativeMesh.setVec4Vector(getPtr(), key, vec4Vector);
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
        return new GVRMesh(getGVRContext(), NativeMesh.getBoundingBox(getPtr()));
    }

    private void checkValidFloatVector(String keyName, String key,
            String vectorName, float[] vector, int expectedComponents) {
        checkStringNotNullOrEmpty(keyName, key);
        checkNotNull(vectorName, vector);
        checkDivisibleDataLength(vectorName, vector.length, expectedComponents);
        checkVectorLengthWithVertices(vectorName, vector.length,
                expectedComponents);
    }

    private void checkValidFloatArray(String parameterName, float[] data,
            int expectedComponents) {
        checkNotNull(parameterName, data);
        checkDivisibleDataLength(parameterName, data.length, expectedComponents);
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
}

class NativeMesh {
    public static native long ctor();

    public static native float[] getVertices(long mesh);

    public static native void setVertices(long mesh, float[] vertices);

    public static native float[] getNormals(long mesh);

    public static native void setNormals(long mesh, float[] normals);

    public static native float[] getTexCoords(long mesh);

    public static native void setTexCoords(long mesh, float[] texCoords);

    public static native char[] getTriangles(long mesh);

    public static native void setTriangles(long mesh, char[] triangles);

    public static native float[] getFloatVector(long mesh, String key);

    public static native void setFloatVector(long mesh, String key,
            float[] floatVector);

    public static native float[] getVec2Vector(long mesh, String key);

    public static native void setVec2Vector(long mesh, String key,
            float[] vec2Vector);

    public static native float[] getVec3Vector(long mesh, String key);

    public static native void setVec3Vector(long mesh, String key,
            float[] vec3Vector);

    public static native float[] getVec4Vector(long mesh, String key);

    public static native void setVec4Vector(long mesh, String key,
            float[] vec4Vector);

    public static native long getBoundingBox(long mesh);
}
