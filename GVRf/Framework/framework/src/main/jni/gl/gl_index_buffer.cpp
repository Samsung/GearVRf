/****
 *
 * VertexBuffer maintains a vertex data array with locations, normals,
 * colors and texcoords.
 *
 ****/
#define VERBOSE_LOGGING 0

#include "gl_index_buffer.h"
#include "gl_shader.h"

namespace gvr {
    GLIndexBuffer::GLIndexBuffer(int bytesPerIndex, int vertexCount)
    : IndexBuffer(bytesPerIndex, vertexCount),
      mIBufferID(-1)
    { }

    GLIndexBuffer::~GLIndexBuffer()
    {
        if (mIBufferID != -1)
        {
            GL(glDeleteBuffers(1, &mIBufferID));
            mIBufferID == -1;
        }
    }

    bool GLIndexBuffer::bindBuffer(Shader* shader)
    {
        if (mIBufferID != -1)
        {
            LOGV("IndexBuffer::bindBuffer %d", mIBufferID);
            GL(glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIBufferID));
            checkGLError("IndexBuffer::bindBuffer");
            return true;
        }
        return false;
    }

    bool GLIndexBuffer::updateGPU(Renderer* renderer)
    {
        std::lock_guard<std::mutex> lock(mUpdateLock);
        const void* data = getIndexData();

        if ((getIndexCount() == 0) || (data == NULL))
        {
            LOGE("IndexBuffer::updateGPU no index data yet");
            return false;
        }
        if (mIBufferID == -1)
        {
            GL(glGenBuffers(1, (GLuint*) &mIBufferID));
            mIsDirty = true;
            LOGV("IndexBuffer::updateGPU created index buffer %d with %d indices", mIBufferID, getIndexCount());
        }
        if (mIsDirty)
        {
            GL(glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIBufferID));
            GL(glBufferData(GL_ELEMENT_ARRAY_BUFFER, getDataSize(), mIndexData, GL_STATIC_DRAW));
            GL(glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0));
            mIsDirty = false;
            LOGV("IndexBuffer::updateGPU updated index buffer %d", mIBufferID);
        }
        return true;
    }


} // end gvrf

