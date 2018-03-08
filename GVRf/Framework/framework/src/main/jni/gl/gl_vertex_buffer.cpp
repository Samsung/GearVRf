/****
 *
 * VertexBuffer maintains a vertex data array with locations, normals,
 * colors and texcoords.
 *
 ****/
#include "gl_vertex_buffer.h"
#include "gl_index_buffer.h"
#include "gl_shader.h"

#define VERBOSE_LOGGING 0
#include "util/gvr_log.h"

namespace gvr {
    GLVertexBuffer::GLVertexBuffer(const char* layout_desc, int vertexCount)
    : VertexBuffer(layout_desc, vertexCount),
      mVBufferID(-1), mVArrayID(-1), mProgramID(-1)
    {
    }

    GLVertexBuffer::~GLVertexBuffer()
    {
        if (mVArrayID != -1)
        {
            GL(glDeleteVertexArrays(1, &mVArrayID));
            mVArrayID = -1;
        }
        if (mVBufferID != -1)
        {
            GL(glDeleteBuffers(1, &mVBufferID));
            mVBufferID = -1;
        }
    }

    /***
     * Binds a VertexBuffer to a specific shader.
     * The binding occurs if the VertexBuffer was previously used
     * by a different shader. The GL vertex array object is
     * configuration so that its vertex attributes match the
     * associated shader. The IndexBuffer is also associated
     * with the GL vertex array.
     * @param shader shader vertex buffer is rendered with
     * @param ibuf index buffer
     */
    void GLVertexBuffer::bindToShader(Shader* shader, IndexBuffer* ibuf)
    {
        GLuint programId = static_cast<GLShader*>(shader)->getProgramId();

        GL(glBindVertexArray(mVArrayID));
        if (ibuf)
        {
            ibuf->bindBuffer(shader);
        }
        LOGV("VertexBuffer::bindToShader bind vertex array %d to shader %d", mVBufferID, programId);
        GL(glBindBuffer(GL_ARRAY_BUFFER, mVBufferID));

        if (mProgramID == programId)
        {
            return;
        }
        mProgramID = programId;

        shader->getVertexDescriptor().forEachEntry([this, programId](const DataDescriptor::DataEntry &e)
        {
            GLint loc = glGetAttribLocation(programId, e.Name);

            if (!e.NotUsed)                             // shader uses this vertex attribute?
            {
                const DataDescriptor::DataEntry* entry = find(e.Name);
                if ((entry != nullptr) && entry->IsSet) // mesh uses this vertex attribute?
                {
                    if (loc >= 0)                       // attribute found in shader?
                    {
                        GL(glEnableVertexAttribArray(loc)); // enable this attribute in GL
                        GL(glVertexAttribPointer(loc, entry->Size / sizeof(float),
                                              entry->IsInt ? GL_INT : GL_FLOAT, GL_FALSE,
                                              getTotalSize(), reinterpret_cast<GLvoid*>(entry->Offset)));
                        LOGV("VertexBuffer: vertex attrib #%d %s loc %d ofs %d",
                             e.Index, e.Name, loc, entry->Offset);
                        checkGLError("VertexBuffer::bindToShader");
                    }
                    else                                // mesh uses attribute but shader does not
                    {
                        LOGV("SHADER: vertex attribute %s has no location in shader", e.Name);
                    }
                }
                else if (loc >= 0)                      // shader uses attribute but mesh does not
                {
                    GL(glDisableVertexAttribArray(loc));
                    LOGE("SHADER: shader needs vertex attribute %s but it is not found", e.Name);
                }
            }
        });
    }

    bool GLVertexBuffer::updateGPU(Renderer* renderer, IndexBuffer* ibuf, Shader* shader)
    {
        std::lock_guard<std::mutex> lock(mLock);
        const float* vertexData = getVertexData();
        if ((getVertexCount() == 0) || (vertexData == NULL))
        {
            LOGE("VertexBuffer::updateGPU no vertex data yet");
            return false;
        }
        if (mVArrayID == -1)
        {
            GL(glGenVertexArrays(1, &mVArrayID));
            LOGD("VertexBuffer::updateGPU creating vertex array %d", mVArrayID);
        }
        if (ibuf)
        {
            ibuf->updateGPU(renderer);
        }
        if (mVBufferID == -1)
        {
            GL(glGenBuffers(1, &mVBufferID));
            GL(glBindBuffer(GL_ARRAY_BUFFER, mVBufferID));
            GL(glBufferData(GL_ARRAY_BUFFER, getDataSize(), mVertexData, GL_STATIC_DRAW));
            GL(glBindBuffer(GL_ARRAY_BUFFER, 0));
            LOGV("VertexBuffer::updateGPU created vertex buffer %d with %d vertices", mVBufferID, getVertexCount());
            mIsDirty = false;
        }
        else if (mIsDirty)
        {
            GL(glBindBuffer(GL_ARRAY_BUFFER, mVBufferID));
            GL(glBufferData(GL_ARRAY_BUFFER, getDataSize(), NULL, GL_STATIC_DRAW));
            GL(glBufferSubData(GL_ARRAY_BUFFER, 0, getDataSize(), mVertexData));
            GL(glBindBuffer(GL_ARRAY_BUFFER, 0));
            mIsDirty = false;
            LOGV("VertexBuffer::updateGPU updated vertex buffer %d", mVBufferID);
        }
        return true;
    }


} // end gvrf

