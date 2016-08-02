#ifndef GVR_GL_EXT_H_
#define GVR_GL_EXT_H_

#define __gl2_h_
#include <GLES3/gl3.h>
#include <GLES2/gl2ext.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>

namespace gvr {
typedef void (GL_APIENTRYP PFNGLINVALIDATEFRAMEBUFFER_)(GLenum target,
        GLsizei numAttachments, const GLenum* attachments);

typedef void (GL_APIENTRYP PFNGLBLITFRAMEBUFFER_)(GLint srcX0, GLint srcY0,
        GLint srcX1, GLint srcY1, GLint dstX0, GLint dstY0, GLint dstX1,
        GLint dstY1, GLbitfield mask, GLenum filter);

static PFNGLINVALIDATEFRAMEBUFFER_ glInvalidateFramebuffer_ = NULL;
static PFNGLBLITFRAMEBUFFER_ glBlitFramebuffer_ = NULL;

static void glInvalidatebuffer(GLenum target, GLsizei numAttachments,
        const GLenum* attachments) {
    if (!glInvalidateFramebuffer_) {
        glInvalidateFramebuffer_ =
                reinterpret_cast<PFNGLINVALIDATEFRAMEBUFFER_>(eglGetProcAddress(
                        "glInvalidateFramebuffer"));
    }
    glInvalidateFramebuffer_(target, numAttachments, attachments);
}

static void glBlitFramebuffer(GLint srcX0, GLint srcY0, GLint srcX1,
        GLint srcY1, GLint dstX0, GLint dstY0, GLint dstX1, GLint dstY1,
        GLbitfield mask, GLenum filter) {
    if (!glBlitFramebuffer_) {
        glBlitFramebuffer_ =
                reinterpret_cast<PFNGLBLITFRAMEBUFFER_>(eglGetProcAddress(
                        "glBlitFramebuffer"));
    }
    glBlitFramebuffer_(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1,
            mask, filter);
}
}

#endif
