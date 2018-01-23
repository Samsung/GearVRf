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


/***************************************************************************
 * Eye pointee made by a mesh.
 ***************************************************************************/

#include <limits>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_inverse.hpp"
#include "glm/gtx/intersect.hpp"
#include "util/gvr_log.h"
#include "mesh_collider.h"
#include "render_data.h"
#include "objects/scene_object.h"

namespace gvr
{
    MeshCollider::MeshCollider(Mesh* mesh) :
            Collider(getComponentType()), mesh_(mesh), pickCoordinates_(false),
            useMeshBounds_(false)
    {
    }

    MeshCollider::MeshCollider(Mesh* mesh, bool pickCoordinates) :
            Collider(getComponentType()), mesh_(mesh), pickCoordinates_(pickCoordinates),
            useMeshBounds_(false)
    {
    }

    MeshCollider::MeshCollider(bool useMeshBounds) :
            Collider(getComponentType()), mesh_(NULL), pickCoordinates_(false),
            useMeshBounds_(useMeshBounds)
    {
    }

    MeshCollider::~MeshCollider()
    {}

/*
 * Hit test the triangles in the mesh against the input ray.
 *
 * The ray is converted into mesh coordinates by transforming it
 * with the concatenation of the view_matrix and the model matrix
 * of the scene object which owns the collider.
 * The hit point computed is in local coordinates (same coordinate
 * space as the mesh vertices).
 *
 * @param view_matrix   camera view matrix (inverse of camera model matrix)
 * @param rayStart      origin of the ray in world coordinates
 * @param rayDir        direction of the ray in world coordinates
 *
 * @returns ColliderData structure with collision information
 */
    ColliderData MeshCollider::isHit(const glm::vec3& rayStart, const glm::vec3& rayDir)
    {
        Mesh* mesh = mesh_;
        bool pickCoordinates = pickCoordinates_;
        SceneObject* owner = owner_object();
        RenderData* rd = owner->render_data();
        glm::mat4 model_matrix = owner->transform()->getModelMatrix();
        glm::mat4 model_inverse = glm::affineInverse(model_matrix);
        glm::vec3 O(rayStart);
        glm::vec3 D(rayDir);

        /*
         * Compute the model view matrix by concatenating the
         * scene object"s model matrix with the input view matrix.
         */
        transformRay(model_inverse, O, D);
        if ((mesh == NULL) && (rd != NULL))
        {
            mesh = rd->mesh();
        }
        /*
         * Compute the point where the ray penetrates the mesh in
         * the coordinate space of the mesh.
         */
        ColliderData data;
        if (mesh != NULL)
        {
            if (useMeshBounds_)
            {
                const BoundingVolume& bounds = mesh->getBoundingVolume();
                data = MeshCollider::isHit(bounds, O, D);
            }
            else
            {
                data = MeshCollider::isHit(*mesh, O, D, pickCoordinates);
            }
            if (data.IsHit)
            {
                data.Distance = glm::distance(O, data.HitPosition);
                data.ColliderHit = this;
                data.ObjectHit = owner;
            }
        }
        return data;
    }

/*
 * Hit test the bounding sphere of the mesh against the input sphere.
 *
 * The sphere is converted into mesh coordinates by transforming it
 * with the concatenation of the view_matrix and the model matrix
 * of the scene object which owns the collider.
 * The hit point computed is in local coordinates (same coordinate
 * space as the mesh vertices).
 *
 * @param view_matrix   camera view matrix (inverse of camera model matrix)
 * @param sphere        array with origin of the sphere and radius in world coordinates
 *
 * @returns ColliderData structure with hit point and distance from camera
 */
    ColliderData MeshCollider::isHit(const float sphere[])
    {
        Mesh* mesh = mesh_;
        SceneObject* owner = owner_object();
        RenderData* rd = owner->render_data();
        glm::mat4 model_matrix = owner->transform()->getModelMatrix();
        glm::mat4 model_inverse = glm::affineInverse(model_matrix);
        float s[4] = { sphere[0], sphere[1], sphere[2], sphere[3] };

        /*
         * Compute the collider's model view matrix by concatenating the
         * collider's model matrix with the input view matrix.
         * Transform the input sphere from world coordinates
         * to collider coordinates.
         */
        transformSphere(model_inverse, s);
        if ((mesh == NULL) && (rd != NULL))
        {
            mesh = rd->mesh();
        }
        /*
         * Compute the point where the sphere penetrates the mesh in
         * the coordinate space of the mesh.
         */
        ColliderData data;
        if (mesh != NULL)
        {
            const BoundingVolume& bounds = mesh->getBoundingVolume();

            if (useMeshBounds_)
            {
                data = MeshCollider::isHit(bounds, s);
            }
            else
            {
                data = MeshCollider::isHit(*mesh, s);
            }
            data.ColliderHit = this;
        }
        return data;
    }


/**
 * Efficient means of solving Barycentric coordinates by Christer Ericson/John Calsbeek found at
 * https://gamedev.stackexchange.com/questions/23743/whats-the-most-efficient-way-to-find-barycentric-coordinates
 * @param p         3D point lying on triangle formed by points a, b, and c.
 * @param a         the first of the three points forming the triangle
 * @param b         the second of the three points forming the triangle
 * @param c         the third of the three points forming the triangle
 * @param coords    the vec3 that will hold the resulting Barcentric coordinates of p
 */
    static void
    calcBarycentric(const glm::vec3& p, const glm::vec3& a, const glm::vec3& b, const glm::vec3& c,
                    glm::vec3& coords)
    {
        glm::vec3 v0 = b - a, v1 = c - a, v2 = p - a;
        float d00 = (float) glm::dot(v0, v0);
        float d01 = (float) glm::dot(v0, v1);
        float d11 = (float) glm::dot(v1, v1);
        float d20 = (float) glm::dot(v2, v0);
        float d21 = (float) glm::dot(v2, v1);
        float denom = d00 * d11 - d01 * d01;
        coords.y = (d11 * d20 - d01 * d21) / denom;
        coords.z = (d00 * d21 - d01 * d20) / denom;
        coords.x = 1.0f - coords.y - coords.z;
    }

/**
 * Sets the Barycentric coordinates, UV coordinates, and normal corresponding to the HitPoint on the mesh
 * @param mesh          the Mesh of the object that was collided with
 * @param colliderData  the ColliderData holding the HitPoint which will also store the UV coordinates
 */
static void populateSurfaceCoords(const Mesh& mesh, ColliderData& colliderData) {
    VertexBuffer* vBuffer = mesh.getVertexBuffer();
    IndexBuffer* iBuffer = mesh.getIndexBuffer();
    int I1;
    int I2;
    int I3;
    if (iBuffer->getIndexSize() == 2) {
        const unsigned short *intData = reinterpret_cast<const unsigned short *>(iBuffer->getIndexData());
        intData += 3*colliderData.FaceIndex;
        I1 = *(intData);
        I2 = *(intData+1);
        I3 = *(intData+2);
    }
    else {
        const unsigned int *intData = reinterpret_cast<const unsigned int *>(iBuffer->getIndexData());
        intData += 3*colliderData.FaceIndex;
        I1 = *(intData);
        I2 = *(intData+1);
        I3 = *(intData+2);
    }

    const float* vertData = vBuffer->getVertexData();
    const float *V1;
    const float *V2;
    const float *V3;
    int stride = vBuffer->getVertexSize();
    V1 = vertData + (stride * I1);
    V2 = vertData + (stride * I2);
    V3 = vertData + (stride * I3);
    int index, offset, size;
    vBuffer->getInfo("a_position", index, offset, size);
    offset /= sizeof(float);
    glm::vec3 v1(V1[offset], V1[offset+1], V1[offset+2]);
    glm::vec3 v2(V2[offset], V2[offset+1], V2[offset+2]);
    glm::vec3 v3(V3[offset], V3[offset+1], V3[offset+2]);

    calcBarycentric(colliderData.HitPosition, v1, v2, v3, colliderData.BarycentricCoordinates);
    bool hasTexCoords = vBuffer->getInfo("a_texcoord", index, offset, size);
    if(hasTexCoords){
        offset /= sizeof(float);
        glm::vec2 u1(V1[offset], V1[offset+1]);
        glm::vec2 u2(V2[offset], V2[offset+1]);
        glm::vec2 u3(V3[offset], V3[offset+1]);

        colliderData.TextureCoordinates =   u1 * colliderData.BarycentricCoordinates.x
                                            + u2 * colliderData.BarycentricCoordinates.y
                                            + u3 * colliderData.BarycentricCoordinates.z;
    }
    bool hasNormals = vBuffer->getInfo("a_normal", index, offset, size);
    if(hasNormals){
        offset /= sizeof(float);
        glm::vec3 n1(V1[offset], V1[offset+1], V1[offset+2]);
        glm::vec3 n2(V2[offset], V2[offset+1], V2[offset+2]);
        glm::vec3 n3(V3[offset], V3[offset+1], V3[offset+2]);

        colliderData.NormalCoordinates =   n1 * colliderData.BarycentricCoordinates.x
                                            + n2 * colliderData.BarycentricCoordinates.y
                                            + n3 * colliderData.BarycentricCoordinates.z;
    }
}

/*
 * Hit test the input ray against the triangles of the given mesh.
 * @param mesh  mesh to hit test
 * @param rayStart  start of the pick ray in model coordinates
 * @param rayDir    direction of the pick ray in model coordinates
 * @param pickCoordinates whether or not coordinate picking info will be generated
 * @return ColliderData with the hit point and distance in model coordinates
 */
    ColliderData MeshCollider::isHit(const Mesh& mesh, const glm::vec3& rayStart, const glm::vec3& rayDir, bool pickCoordinates)
    {
        ColliderData data;
        if (mesh.getVertexCount() > 0)
        {
            mesh.forAllTriangles([&data, rayStart, rayDir](int iter, const float* v1, const float* v2, const float* v3) mutable
            {
                /*
                 * Compute the point where the ray penetrates the mesh in
                 * the coordinate space of the mesh. The hit point will
                 * be in mesh coordinates as will the distance.
                 */
                glm::vec3 hitPos;
                glm::vec3 A(v1[0], v1[1], v1[2]);
                glm::vec3 B(v2[0], v2[1], v2[2]);
                glm::vec3 C(v3[0], v3[1], v3[2]);
                float distance = rayTriangleIntersect(hitPos, rayStart, rayDir, A, B, C);
                if ((distance > 0) && (distance < data.Distance))
                {
                    data.IsHit = true;
                    data.HitPosition = hitPos;
                    data.Distance = distance;
                    data.FaceIndex = iter;
                }
            });
            if(pickCoordinates && data.IsHit)
            {
                populateSurfaceCoords(mesh, data);
            }
        }
        return data;
    }

    /*
     * Determine if the ray penetrates an axially aligned bounding box
     * @param bounds    bounding volume (radius ignored, corners of box are used)
     * @param rayStart  origin of ray in model coordinates
     * @param rayDir    direction of ray in model coordinates
     */
    ColliderData MeshCollider::isHit(const BoundingVolume& bounds, const glm::vec3& rayStart, const glm::vec3& rayDir)
    {
        ColliderData data;
        glm::vec3 hitPos;
        if (bounds.intersect(hitPos, rayStart, rayDir))
        {
            data.IsHit = true;
            data.HitPosition = hitPos;
            data.Distance = glm::distance(rayStart, hitPos);
         }
         return data;
    }


    /*
     * Determine if the sphere penetrates the mesh collider bounds
     * @param bounds    bounding volume
     * @param sphere    sphere center and radius
     */
    ColliderData MeshCollider::isHit(const BoundingVolume& bounds, const float sphere[])
    {
        ColliderData data;
        glm::vec3 sphereCenter(sphere[0], sphere[1], sphere[2]);
        glm::vec3 colliderCenter(bounds.center());
        float r = sphere[3] + bounds.radius();
        glm::vec3 h = colliderCenter - sphereCenter;  // vector from collider to sphere
        float dist = (h.x * h.x) + (h.y * h.y) + (h.z * h.z);

        dist = sqrt(dist);
        if (dist <= r)                       // bounding sphere intersects collision sphere?
        {
            h *= bounds.radius() / dist;           // hit point on collision sphere
            LOGE("COLLIDER: sphere(%f, %f, %f) collider(%f, %f, %f) hit(%f, %f, %f) dist = %f radius = %f",
                 sphere[0], sphere[1], sphere[2],
                 colliderCenter.x, colliderCenter.y, colliderCenter.z,
                 h.x, h.y, h.z,
                 dist, r);
            data.IsHit = true;
            data.HitPosition = h;
            data.Distance = dist;
        }
        return data;
    }

    /*
     * Find the vertex in the mesh closest to the center the input sphere
     * @param mesh      mesh with vertices to compare
     * @param sphere    sphere center and radius
     */
    ColliderData MeshCollider::isHit(const Mesh& mesh, const float sphere[])
    {
        ColliderData data;
        float minDist = std::numeric_limits<float>::infinity();

        mesh.forAllVertices("a_position", [minDist, sphere, data](int vertIndex, const float* vertex) mutable
        {
            float x = vertex[0] - sphere[0];
            float y = vertex[1] - sphere[1];
            float z = vertex[2] - sphere[2];
            float dist = (x * x) + (y * y) + (z * z);

            dist = sqrt(dist);
            if ((dist < sphere[3]) && (dist < minDist)) // bounding sphere itersects this sphere?
            {
                data.IsHit = true;
                data.HitPosition.x = vertex[0];         // vertex that was hit
                data.HitPosition.y = vertex[1];
                data.HitPosition.z = vertex[2];
                data.FaceIndex = vertIndex;             // index of vertex that was hit
                minDist = dist;
            }
        });
        return data;
    }

    float MeshCollider::rayTriangleIntersect(glm::vec3& hitPos, const glm::vec3& rayStart,
                                             const glm::vec3& rayDir,
                                             const glm::vec3& V1, const glm::vec3& V2,
                                             const glm::vec3& V3)
    {
        glm::vec3 e1(V2 - V1);
        glm::vec3 e2(V3 - V1);
        glm::vec3 P = glm::cross(rayDir, e2);
        glm::vec3 T(glm::vec3(rayStart) - V1);
        float det = glm::dot(e1, P);
        const float EPSILON = 0.00001f;

        if (det > -EPSILON && det < EPSILON)
        {
            return -1;
        }

        float inv_det = 1.0f / det;
        float u = glm::dot(T, P) * inv_det;

        if (u < 0.0f || u > 1.0f)
        {
            return -1;
        }

        glm::vec3 Q = glm::cross(T, e1);
        float v = glm::dot(glm::vec3(rayDir), Q) * inv_det;

        if (v < 0.0f || (u + v) > 1.0f)
        {
            return -1;
        }

        float t = glm::dot(e2, Q) * inv_det;

        if (t > EPSILON)
        {
            hitPos = (1.0f - u - v) * V1 + u * V2 + v * V3;
            return t;
        }
        return -1;
    }
}
