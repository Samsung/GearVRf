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

#include "mesh.h"
#include "engine/renderer/renderer.h"
#include "glm/gtc/matrix_inverse.hpp"

namespace gvr
{

    Mesh::Mesh(const char* descriptor)
    : mVertices(nullptr),
      mIndices(nullptr),
      have_bounding_volume_(false),
      vertexBoneData_()
    {
        mVertices = Renderer::getInstance()->createVertexBuffer(descriptor, 0);
    }

    Mesh::Mesh(VertexBuffer& vbuf)
    : mVertices(&vbuf), mIndices(nullptr),
      have_bounding_volume_(false),
      vertexBoneData_()
    {
    }

    Mesh *Mesh::createBoundingBox()
    {
        Mesh *mesh = new Mesh("float3 a_position");
        getBoundingVolume(); // Make sure bounding_volume is valid

        glm::vec3 min_corner = bounding_volume.min_corner();
        glm::vec3 max_corner = bounding_volume.max_corner();
        float min_x = min_corner[0];
        float min_y = min_corner[1];
        float min_z = min_corner[2];
        float max_x = max_corner[0];
        float max_y = max_corner[1];
        float max_z = max_corner[2];
        float positions[24] =
                {min_x, min_y, min_z, max_x, min_y, min_z, min_x, max_y, min_z, max_x, max_y, min_z,
                 min_x, min_y, max_z, max_x, min_y, max_z, min_x, max_y, max_z, max_x, max_y,
                 max_z};
        unsigned short indices[] =
                {0, 2, 1, 1, 2, 3, 1, 3, 7, 1, 7, 5, 4, 5, 6, 5, 7, 6, 0, 6, 2, 0, 4, 6, 0, 1, 5, 0,
                 5, 4, 2, 7, 3, 2, 6, 7};
        mesh->setVertices(positions, sizeof(positions) / (8 * sizeof(float)));
        mesh->setTriangles(indices, sizeof(indices) / (sizeof(short)));
        return mesh;
    }

// an array of size:6 with Xmin, Ymin, Zmin and Xmax, Ymax, Zmax values
    const BoundingVolume &Mesh::getBoundingVolume()
    {
        if (have_bounding_volume_)
        {
            return bounding_volume;
        }
        mVertices->getBoundingVolume(bounding_volume);
        have_bounding_volume_ = true;
        return bounding_volume;
    }

    void Mesh::getTransformedBoundingBoxInfo(glm::mat4 *Mat, float* transformed_bounding_box)
    {
        if (!have_bounding_volume_)
        {
            getBoundingVolume();
        }

        glm::mat4 M = *Mat;
        float a, b;

        //Inspired by Graphics Gems - TransBox.c
        //Transform the AABB to the correct position in world space
        //Generate a new AABB from the non axis aligned bounding box

        transformed_bounding_box[0] = M[3].x;
        transformed_bounding_box[3] = M[3].x;
        transformed_bounding_box[1] = M[3].y;
        transformed_bounding_box[4] = M[3].y;
        transformed_bounding_box[2] = M[3].z;
        transformed_bounding_box[5] = M[3].z;

        glm::vec3 min_corner = bounding_volume.min_corner();
        glm::vec3 max_corner = bounding_volume.max_corner();

        for (int i = 0; i < 3; i++)
        {
            //x coord
            a = M[i].x * min_corner.x;
            b = M[i].x * max_corner.x;
            if (a < b)
            {
                transformed_bounding_box[0] += a;
                transformed_bounding_box[3] += b;
            }
            else
            {
                transformed_bounding_box[0] += b;
                transformed_bounding_box[3] += a;
            }

            //y coord
            a = M[i].y * min_corner.y;
            b = M[i].y * max_corner.y;
            if (a < b)
            {
                transformed_bounding_box[1] += a;
                transformed_bounding_box[4] += b;
            }
            else
            {
                transformed_bounding_box[1] += b;
                transformed_bounding_box[4] += a;
            }

            //z coord
            a = M[i].z * min_corner.z;
            b = M[i].z * max_corner.z;
            if (a < b)
            {
                transformed_bounding_box[2] += a;
                transformed_bounding_box[5] += b;
            }
            else
            {
                transformed_bounding_box[2] += b;
                transformed_bounding_box[5] += a;
            }
        }
    }

    bool Mesh::getAttributeInfo(const char* attributeName,
                                int &index,
                                int &offset,
                                int &size) const
    {
        return mVertices->getInfo(attributeName, index, offset, size);
    }

    bool Mesh::getVertices(float *vertices, int nelems)
    {
        return mVertices->getFloatVec("a_position", vertices, nelems, 3);
    }

    bool Mesh::setVertices(const float *vertices, int nelems)
    {
        return mVertices->setFloatVec("a_position", vertices, nelems, 3);
    }

    bool Mesh::setNormals(const float *normals, int nelems)
    {
        return mVertices->setFloatVec("a_normal", normals, nelems, 3);
    }

    bool Mesh::getNormals(float *normals, int nelems)
    {
        return mVertices->getFloatVec("a_normal", normals, nelems, 3);
    }

    bool Mesh::setIndices(const unsigned int *indices, int nindices)
    {
        if (!mIndices)
        {
            mIndices = Renderer::getInstance()->createIndexBuffer(sizeof(int), nindices);
        }
        return mIndices->setIntVec(indices, nindices);
    }

    bool Mesh::setTriangles(const unsigned short *indices, int nindices)
    {
        if (!mIndices)
        {
            mIndices = Renderer::getInstance()->createIndexBuffer(sizeof(short), nindices);
        }
        return mIndices->setShortVec(indices, nindices);
    }

    bool Mesh::getIndices(unsigned short *dest, int nindices)
    {
        if (mIndices)
        {
            return mIndices->getShortVec(dest, nindices);
        }
        return false;
    }

    bool Mesh::getLongIndices(unsigned int *dest, int nindices)
    {
        if (mIndices)
        {
            return mIndices->getIntVec(dest, nindices);
        }
        return false;
    }

    bool Mesh::setFloatVec(const char* attrName, const float *src, int nelems)
    {
        return mVertices->setFloatVec(attrName, src, nelems, 0);
    }

    bool Mesh::setIntVec(const char* attrName, const int *src, int nelems)
    {
        return mVertices->setIntVec(attrName, src, nelems, 1);
    }

    bool Mesh::getFloatVec(const char* attrName, float *dest, int nelems)
    {
        return mVertices->getFloatVec(attrName, dest, nelems, 0);
    }

    bool Mesh::getIntVec(const char* attrName, int *dest, int nelems)
    {
        return mVertices->getIntVec(attrName, dest, nelems, 1);
    }

    void Mesh::forAllIndices(std::function<void(int iter, int index)> func)
    {
        if (!mIndices)
        {
            for (int i = 0; i < getVertexCount(); ++i)
            {
                func(i, i);
            }
        }
        else if (mIndices->getIndexSize() == 2)
        {
            const unsigned short* indexData = reinterpret_cast<const unsigned short *>(mIndices->getIndexData());
            for (int i = 0; i < mIndices->getIndexCount(); ++i)
            {
                int v = *(indexData + i);
                func(i, v);
            }
        }
        else
        {
            const unsigned int* indexData = reinterpret_cast<const unsigned int *>(mIndices->getIndexData());
            for (int i = 0; i < mIndices->getIndexCount(); ++i)
            {
                int v = *(indexData + i);
                func(i, v);
            }
        }
    }


    void Mesh::forAllVertices(const char* attrName,
                              std::function<void(int iter, const float *vertex)> func) const
    {
        mVertices->forAllVertices(attrName, func);
    }

    void Mesh::forAllTriangles(std::function<void(int iter, const float *v1,
                                                  const float *v2, const float *v3)> func) const
    {
        int n = getIndexCount();
        const float* vertData = mVertices->getVertexData();
        const float *V1;
        const float *V2;
        const float *V3;
        int stride = mVertices->getVertexSize();
        if (mIndices->getIndexSize() == 2)
        {
            const unsigned short* intData = reinterpret_cast<const unsigned short*>(mIndices->getIndexData());
            for (int i = 0; i < n; i += 3)
            {
                V1 = vertData + (stride * *intData++);
                V2 = vertData + (stride * *intData++);
                V3 = vertData + (stride * *intData++);
                func(i / 3, V1, V2, V3);
            }
        }
        else
        {
            const unsigned int* intData = mIndices->getIndexData();
            for (int i = 0; i < n; i += 3)
            {
                V1 = vertData + (stride * *intData++);
                V2 = vertData + (stride * *intData++);
                V3 = vertData + (stride * *intData++);
                func(i / 3, V1, V2, V3);
            }
        }
    }

}


