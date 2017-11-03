#pragma once

#include <string>

#ifndef GL_ES_VERSION_3_0
#include "GLES3/gl3.h"
#endif
#include "gl/gl_program.h"
#include "util/gvr_gl.h"

#include "../objects/index_buffer.h"


namespace gvr {
    class GlDelete;
    class Shader;
    class Renderer;

 /**
  * Mesh index storage for OpenGL
  *
  * @see IndexBuffer
  */
    class GLIndexBuffer : public IndexBuffer
    {
    public:
        explicit GLIndexBuffer(int bytesPerIndex, int vertexCount);
        virtual ~GLIndexBuffer();

        virtual bool    bindBuffer(Shader*);
        virtual bool    updateGPU(Renderer* renderer);

    protected:
        GLuint      mIBufferID;
    };

} // end gvrf

