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

package org.gearvrf.utility;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;

/**
 * Utilities for mesh creation and manipulation.
 */
public class MeshUtils {

    /**
     * Scale the mesh at x, y and z axis.
     *
     * @param mesh Mesh to be scaled.
     * @param x Scale to be applied on x-axis.
     * @param y Scale to be applied on y-axis.
     * @param z Scale to be applied on z-axis.
     */
    public static void scale(GVRMesh mesh, float x, float y, float z) {
        final float [] vertices = mesh.getVertices();
        final int vsize = vertices.length;

        for (int i = 0; i < vsize; i += 3) {
            vertices[i]     *= x;
            vertices[i + 1] *= y;
            vertices[i + 2] *= z;
        }

        mesh.setVertices(vertices);
    }

    /**
     * Scale the mesh keeping its aspect ratio.
     *
     * @param mesh Mesh to be scaled.
     * @param ratio Scale to be applied.
     */
    public static void scale(GVRMesh mesh, float ratio) {
        scale(mesh, ratio, ratio, ratio);
    }

    /**
     * Resize the mesh to given size for each axis.
     *
     * @param mesh Mesh to be resized.
     * @param xsize Size for x-axis.
     * @param ysize Size for y-axis.
     * @param zsize Size fof z-axis.
     */
    public static void resize(GVRMesh mesh, float xsize, float ysize, float zsize) {
        float dim[] = getBoundingSize(mesh);

        scale(mesh, xsize / dim[0], ysize / dim[1], zsize / dim[2]);
    }

    /**
     *  Resize the given mesh keeping its aspect ration.
     * @param mesh Mesh to be resized.
     * @param size Max size for the axis.
     */
    public static void resize(GVRMesh mesh, float size) {
        float dim[] = getBoundingSize(mesh);
        float maxsize = 0.0f;

        if (dim[0] > maxsize) maxsize = dim[0];
        if (dim[1] > maxsize) maxsize = dim[1];
        if (dim[2] > maxsize) maxsize = dim[2];

        scale(mesh, size / maxsize);
    }

    /**
     *  Create a new GVRMesh from the given mesh.
     *
     * @param gvrContext current {@link GVRContext}
     * @param mesh Mesh to be cloned.
     * @return Return a new GVRMesh clone of given mesh.
     */
    public static GVRMesh clone(GVRContext gvrContext, GVRMesh mesh) {
        GVRMesh newMesh = new GVRMesh(gvrContext);

        newMesh.setVertices(mesh.getVertices());
        newMesh.setTexCoords(mesh.getTexCoords());
        newMesh.setNormals(mesh.getNormals());
        newMesh.setIndices(mesh.getIndices());

        return newMesh;
    }

    /**
     * Calcs the bonding size of given mesh.
     *
     * @param mesh Mesh to calc its bouding size.
     * @return The bounding size for x, y and z axis.
     */
    public static float[] getBoundingSize(GVRMesh mesh) {
        final float [] dim = new float[3];
        final float [] vertices = mesh.getVertices();
        final int vsize = vertices.length;
        float minx = Integer.MAX_VALUE;
        float miny = Integer.MAX_VALUE;
        float minz = Integer.MAX_VALUE;
        float maxx = Integer.MIN_VALUE;
        float maxy = Integer.MIN_VALUE;
        float maxz = Integer.MIN_VALUE;

        for (int i = 0; i < vsize; i += 3) {
            if (vertices[i] < minx) minx = vertices[i];
            if (vertices[i] > maxx) maxx = vertices[i];

            if (vertices[i + 1] < miny) miny = vertices[i + 1];
            if (vertices[i + 1] > maxy) maxy = vertices[i + 1];

            if (vertices[i + 2] < minz) minz = vertices[i + 2];
            if (vertices[i + 2] > maxz) maxz = vertices[i + 2];
        }

        dim[0] = maxx - minx;
        dim[1] = maxy - miny;
        dim[2] = maxz - minz;

        return dim;
    }

    /**
     * Creates a quad consisting of two triangles, with the specified width and
     * height.
     *
     * @param gvrContext current {@link GVRContext}
     *
     * @param width
     *            the quad's width
     * @param height
     *            the quad's height
     * @return A 2D, rectangular mesh with four vertices and two triangles
     */
    public static GVRMesh createQuad(GVRContext gvrContext, float width, float height) {
        GVRMesh mesh = new GVRMesh(gvrContext);

        float[] vertices = { width * -0.5f, height * 0.5f, 0.0f, width * -0.5f,
                height * -0.5f, 0.0f, width * 0.5f, height * 0.5f, 0.0f,
                width * 0.5f, height * -0.5f, 0.0f };
        mesh.setVertices(vertices);

        final float[] normals = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f };
        mesh.setNormals(normals);

        final float[] texCoords = { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
                1.0f };
        mesh.setTexCoords(texCoords);

        char[] triangles = { 0, 1, 2, 1, 3, 2 };
        mesh.setTriangles(triangles);

        return mesh;
    }

    /**
     * Creates a rounded quad with the specified width and height.
     *
     * @param gvrContext current {@link GVRContext}
     * @param width The quad's width
     * @param height The quad's height
     * @param rx Ratio for x-axis.
     * @param ry Ration for y-axis.
     * @param segments Number of segments for the corner.
     *
     * @return A 2D, rounded quad.
     */
    public static GVRMesh createRoundQuad(GVRContext gvrContext, float width, float height,
                                            float rx, float ry, int segments) {
        GVRMesh mesh = new GVRMesh(gvrContext);

        if (segments < 0)
            segments = 0;

        // adjust to internal logic
        segments += 2;

        if (rx > width * 0.5f) rx = width * 0.5f;
        if (ry > height * 0.5f) ry = height * 0.5f;

        final double angle = (float) Math.PI * 0.5f / (segments - 1);

        final float[] vertices = new float[3 * (4 * segments + 1)];
        final float[] normals = new float[vertices.length];
        final char[] triangles = new char[vertices.length - 3];
        final float[] texcoords = new float[2 * (4 * segments + 1)];

        // Center to the triangles
        vertices[vertices.length - 3] = 0.0f;
        vertices[vertices.length - 2] = 0.0f;
        vertices[vertices.length - 1] = 0.0f;

        normals[normals.length - 3] = 0.0f;
        normals[normals.length - 2] = 0.0f;
        normals[normals.length - 1] = 1.0f;

        texcoords[texcoords.length - 2] = 0.5f;
        texcoords[texcoords.length - 1] = 0.5f;

        for (char i = 0; i < segments; i++) {
            final float cos = (float) Math.cos(i * angle);
            final float sin = (float) Math.sin(i * angle);
            final float dx = (1.0f - cos) * 2.0f * rx / width;
            final float dy = (1.0f - sin) * 2.0f * ry / height;
            final float x0 = (1.0f - dx) * 0.5f;
            final float y0 = (1.0f - dy) * 0.5f;
            final float x = x0 * width;
            final float y = y0 * height;

            // top right
            vertices[3 * i + 0 + 0 * segments] = x;
            vertices[3 * i + 1 + 0 * segments] = y;
            vertices[3 * i + 2 + 0 * segments] = 0.0f;

            // top left
            vertices[6 * segments - (3 * i + 3)] = -x;
            vertices[6 * segments - (3 * i + 2)] = y;
            vertices[6 * segments - (3 * i + 1)] = 0.0f;

            // down left
            vertices[3 * i + 0 + 6 * segments] = -x;
            vertices[3 * i + 1 + 6 * segments] = -y;
            vertices[3 * i + 2 + 6 * segments] = 0.0f;

            // down right
            vertices[12 * segments - (3 * i + 3)] = x;
            vertices[12 * segments - (3 * i + 2)] = -y;
            vertices[12 * segments - (3 * i + 1)] = 0.0f;


            // top right
            texcoords[2 * i + 0 + 0 * segments] = 0.5f + x0;
            texcoords[2 * i + 1 + 0 * segments] = 0.5f - y0;

            // top left
            texcoords[4 * segments - (2 * i + 2)] = 0.5f - x0;
            texcoords[4 * segments - (2 * i + 1)] = 0.5f - y0;

            // down left
            texcoords[2 * i + 0 + 4 * segments] = 0.5f - x0;
            texcoords[2 * i + 1 + 4 * segments] = 0.5f + y0;

            // down right
            texcoords[8 * segments - (2 * i + 2)] = 0.5f + x0;
            texcoords[8 * segments - (2 * i + 1)] = 0.5f + y0;
        }

        final int num_vertices = (vertices.length / 3) - 1;
        for (char i = 0; i < num_vertices; i++) {
            normals[3 * i] = 0.0f;
            normals[3 * i + 1] = 0.0f;
            normals[3 * i + 2] = 1.0f;

            triangles[3 * i] = i;
            triangles[3 * i + 1] = (char) ((i + 1) % num_vertices);
            triangles[3 * i + 2] = (char) num_vertices;
        }

        mesh.setVertices(vertices);
        mesh.setNormals(normals);
        mesh.setTexCoords(texcoords);
        mesh.setIndices(triangles);

        return mesh;
    }
}
