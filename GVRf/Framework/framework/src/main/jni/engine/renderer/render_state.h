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
 * Containing data about how to render an object.
 ***************************************************************************/

#ifndef RENDER_STATE_H_
#define RENDER_STATE_H_

#include "glm/glm.hpp"
#include "util/gvr_jni.h"

namespace gvr
{
    class Scene;
    class ShaderManager;
    class UniformBlock;
    class ShadowMap;
    class Camera;

    /**
     * Defines offsets into RenderState.u_matrices for the
     * globally computed matrices in the RenderState.
     */
    enum MATRIXTYPE
    {
        VIEW_PROJ = 0,
        PROJECTION = 2,
        VIEW = 3,
        VIEW_INVERSE = 5,
        MODEL = 7,
        MVP = 8,
        MAX_MATRIX = 12
    };

    /**
     * Contains (nmostly) global render state that is exchanged
     * between the Renderer and the RenderSorter.
     * Each RenderTarget has its own RenderState.
     */
    struct RenderState
    {
        JNIEnv*                 javaEnv;
        jobject                 javaSceneObject = nullptr;
        Scene*                  scene;
        Camera*                 camera;
        ShaderManager*          shader_manager;
        UniformBlock*           transform_block;
        ShadowMap*              shadow_map;
        glm::vec3               camera_position;
        unsigned short int      u_render_mask : 2;    // 1 = left, 2 = right, 3 = both
        bool                    is_shadow : 1;
        bool                    is_multiview : 1;
        bool                    is_stereo : 1;
        unsigned short int      u_right : 1;          // 1 = right eye, 0 = left
        unsigned short int      pad : 2;
        unsigned char           sampleCount;
        unsigned char           u_matrix_offset;      // offset of model matrix
        glm::mat4               u_matrices[MAX_MATRIX];
    };

    /**
     * Contains all of the information about how to render meshes.
     * This includes rendering order, enables for depth, stencil,
     * polygon offset, lighting, shadows as well as configuration
     * options for these features. These correspond to the
     * user-accessible render modes in RenderPass.
     *
     * This class packs all the render modes into a few integers
     * and tries to efficiently implement compare and copy.
     * There is a dirty flag which is set whenever one of the
     * render modes is changed.
     */
    class RenderModes
    {
    public:
        enum RenderOrder : short int
        {
            Stencil = -1000, Background = 1000, Geometry = 2000, Transparent = 3000, Overlay = 4000
        };

        enum CullFace :  char
        {
            CullBack = 0, CullFront, CullNone
        };

    private:
        enum BlendMode :  char
        {
            Zero = 0,
            One = 1,
            SourceColor,
            OneMinusSourceColor,
            SourceAlpha,
            OneMinusSourceAlpha,
            DestAlpha,
            OneMinusDestAlpha,
            DestColor,
            OneMinusDestColor,
            SourceAlphaSaturate
        };

        enum StencilOp :  char
        {
            Keep = 1,   // GL_KEEP
            Replace,    // GL_REPLACE
            Increment,  // GL_INCR
            Decrement,  // GL_DECR
            Invert,     // GL_INVERT
            IncrWrap,   // GL_INCR_WRAP
            DecrWrap,   // GL_DECR_WRAP
        };

        enum StencilFunc : char
        {
            Never,          // GL_NEVER
            Less,           // GL_LESS
            Equal,          // GL_EQUAL
            LessOrEqual,    // GL_LEQUAL,
            Greater,        // GL_GREATER
            NotEqual,       // GL_NOTEQUAL
            GreaterOrEqual, // GL_GEQUAL
            Always,         // GL_ALWAYS
        };


        /**
         * Contains information about how to render meshes
         * (blending, depth testing, lighting).
         * The render flags into one 32 bit integer.
         * THERE ARE ONLY SIX BITS LEFT.
         * DO NOT ADD MORE THAN SIX BITS TO THIS STRUCTURE!
         */
        struct RenderFlags
        {
            unsigned short int source_blend : 4;
            unsigned short int dest_blend : 4;

            unsigned short int render_mask : 2;
            unsigned short int cull_face : 2;
            unsigned short int draw_mode: 3;
            bool dirty : 1;

            bool depth_test : 1;
            bool alpha_blend : 1;
            bool use_light : 1;
            bool cast_shadows : 1;
            bool depth_mask : 1;
            bool alpha_to_coverage : 1;
            bool use_lightmap : 1;
            bool offset : 1;

            bool invert_coverage_mask : 1;
            bool stencil_test : 1;
            unsigned short int stencil_func : 3;
            unsigned short int stencil_fail : 3;

            unsigned short int depth_fail : 3;
            unsigned short int stencil_pass : 3;
            unsigned short int pad : 2;
            unsigned char stencil_func_mask;
            unsigned char stencil_mask;
            unsigned char stencil_ref;

            RenderFlags& operator=(const RenderFlags &srcFlags)
            {
                const uint64_t *srcPtr = (const uint64_t *) &srcFlags;
                uint64_t *dstPtr = (uint64_t*) this;

                *dstPtr = *srcPtr;
                return *this;
            }

            bool operator==(const RenderFlags &srcFlags) const
            {
                const uint64_t *srcPtr = (const uint64_t *) &srcFlags;
                uint64_t *dstPtr = (uint64_t *) this;

                return *dstPtr == *srcPtr;
            }

            bool operator!=(const RenderFlags &srcFlags) const
            {
                const uint64_t *srcPtr = (const uint64_t *) &srcFlags;
                uint64_t *dstPtr = (uint64_t *) this;

                return *dstPtr != *srcPtr;
            }

            void init()
            {
                use_light = true;
                use_lightmap = false;
                offset = false;
                depth_test = true;
                depth_mask = true;
                alpha_blend = false;
                alpha_to_coverage = false;
                cast_shadows = true;
                dirty = false;
                invert_coverage_mask = false;
                stencil_test = false;
                draw_mode = GL_TRIANGLES;
                source_blend = One;
                cull_face = CullBack;
                render_mask = 3;
                dest_blend = OneMinusSourceAlpha;
                stencil_fail = Keep;
                depth_fail = Keep;
                stencil_pass = Keep;
                stencil_func = Always;
                stencil_func_mask = ~0;
                stencil_mask = ~0;
                stencil_ref = 0;
                pad = 0;
            }

            int GLBlendFunc(int GVRblendFunc) const
            {
                if (GVRblendFunc >= SourceColor)
                {
                    return GVRblendFunc + GL_SRC_COLOR - SourceColor;
                }
                else
                {
                    return GVRblendFunc;
                }
            }

            int GVRBlendFunc(int glBlendFunc)
            {
                if (glBlendFunc >= GL_SRC_COLOR)
                {
                    return glBlendFunc - GL_SRC_COLOR + SourceColor;
                }
                else
                {
                    return glBlendFunc;
                }
            }
            int GLStencilOp(int GVRstencilOp) const
            {
                static int gl_values[8] =
                {
                        GL_ZERO, GL_KEEP, GL_REPLACE, GL_INCR,
                        GL_DECR, GL_INVERT, GL_INCR_WRAP, GL_DECR_WRAP
                };
                return gl_values[GVRstencilOp];
            }

            int GLStencilFunc() const
            {
                return stencil_func + GL_NEVER;
            }

            int GVRStencilFunc(int GLstencilFunc) const
            {
                return GLstencilFunc - GL_NEVER;
            }

            int GVRStencilOp(int GLstencilOp) const
            {
                switch (GLstencilOp)
                {
                    case GL_ZERO: return Zero;
                    case GL_REPLACE: return Replace;
                    case GL_INCR: return Increment;
                    case GL_DECR: return Decrement;
                    case GL_INVERT: return Invert;
                    case GL_INCR_WRAP: return IncrWrap;
                    case GL_DECR_WRAP: return DecrWrap;
                    case GL_KEEP: return Keep;
                    default: return Keep;
                };
            }
        };

        RenderFlags render_flags;
        int render_order;
        float offset_factor;
        float offset_units;
        float sample_coverage;

    public:

        void init()
        {
            render_flags.init();
            render_order = Geometry;
            sample_coverage = 1.0f;
            offset_factor = 0;
            offset_units = 0;
        }

        RenderModes& operator=(const RenderModes& src)
        {
            render_flags = src.render_flags;
            offset_factor = src.offset_factor;
            offset_units = src.offset_units;
            render_order = src.render_order;
            sample_coverage = src.sample_coverage;
            return *this;
        }

        bool operator==(const RenderModes& src) const
        {
            if ((render_order != src.render_order) ||
                (render_flags != src.render_flags))
            {
                return false;
            }
            if (isOffsetEnabled() &&
                ((offset_factor != src.offset_factor) ||
                (offset_units != src.offset_units)))
            {
                return false;
            }
            if (isAlphaToCoverageEnabled() &&
                (sample_coverage != src.sample_coverage))
            {
                return false;
            }
            return true;
        }

        bool operator!=(const RenderModes& src) const
        {
            return !(*this == src);
        }

        void markDirty()
        {
            render_flags.dirty = true;
        }

        void clearDirty()
        {
            render_flags.dirty = false;
        }

        bool isDirty()
        {
            return render_flags.dirty;
        }

        int getRenderOrder() const
        {
            return render_order;
        }

        void setRenderOrder(int ro)
        {
            if (render_order != ro)
            {
                markDirty();
                render_order = ro;
            }
        }

        int getCullFace() const
        {
            return render_flags.cull_face;
        }

        void setCullFace(int cf)
        {
            if (render_flags.cull_face != cf)
            {
                markDirty();
                render_flags.cull_face = cf;
            }
        }

        int getDrawMode() const
        {
            return render_flags.draw_mode;
        }

        void setDrawMode(int mode)
        {
            if (render_flags.draw_mode != mode)
            {
                markDirty();
                render_flags.draw_mode = mode;
            }
        }

        unsigned char getRenderMask() const
        {
            return (unsigned char) render_flags.render_mask;
        }

        void setRenderMask(unsigned char mask)
        {
            if (render_flags.render_mask != mask)
            {
                markDirty();
                render_flags.render_mask = mask;
            }
        }

        bool useLights() const
        {
            return render_flags.use_light;
        }

        bool setUseLights(bool flag)
        {
            if (render_flags.use_light != flag)
            {
                markDirty();
                render_flags.use_light = flag;
                return true;
            }
            return false;
        }

        bool useLightMap() const
        {
            return render_flags.use_lightmap;
        }

        void setUseLightmap(bool flag)
        {
            if (render_flags.use_lightmap != flag)
            {
                markDirty();
                render_flags.use_lightmap = flag;
            }
        }

        bool isDepthTestEnabled() const
        {
            return render_flags.depth_test;
        }

        void setDepthTest(bool flag)
        {
            if (render_flags.depth_test != flag)
            {
                markDirty();
                render_flags.depth_test = flag;
            }
        }

        bool isDepthMaskEnabled() const
        {
            return render_flags.depth_mask;
        }

        void setDepthMask(bool flag)
        {
            if (render_flags.depth_mask != flag)
            {
                markDirty();
                render_flags.depth_mask = flag;
            }
        }

        bool isAlphaBlendEnabled() const
        {
            return render_flags.alpha_blend;
        }

        void setAlphaBlend(bool flag)
        {
            if (render_flags.alpha_blend != flag)
            {
                markDirty();
                render_flags.alpha_blend = flag;
            }
        }

        bool isAlphaToCoverageEnabled() const
        {
            return render_flags.alpha_to_coverage;
        }

        void setAlphaToCoverage(bool flag)
        {
            if (render_flags.alpha_to_coverage != flag)
            {
                markDirty();
                render_flags.alpha_to_coverage = flag;
            }
        }

        bool isOffsetEnabled() const
        {
            return render_flags.offset;
        }

        void setOffset(bool flag)
        {
            if (render_flags.offset != flag)
            {
                markDirty();
                render_flags.offset = flag;
            }
        }

        bool invertCoverageMask() const
        {
            return render_flags.invert_coverage_mask;
        }

        void setInvertCoverageMask(bool flag)
        {
            if (render_flags.invert_coverage_mask != flag)
            {
                markDirty();
                render_flags.invert_coverage_mask = flag;
            }
        }

        bool isStencilTestEnabled() const
        {
            return render_flags.stencil_test;
        }

        void setStencilTest(bool flag)
        {
            if (render_flags.stencil_test != flag)
            {
                markDirty();
                render_flags.stencil_test = flag;
            }
        }

        bool castShadows() const
        {
            return render_flags.cast_shadows;
        }

        bool setCastShadows(bool flag)
        {
            if (render_flags.cast_shadows != flag)
            {
                markDirty();
                render_flags.cast_shadows = flag;
                return true;
            }
            return false;
        }

        int getStencilMask() const
        {
            return render_flags.stencil_mask;
        }

        void setStencilFuncMask(int mask)
        {
            if (render_flags.stencil_func_mask != mask)
            {
                markDirty();
                render_flags.stencil_func_mask = mask;
            }
        }

        int getStencilFuncMask() const
        {
            return render_flags.stencil_func_mask;
        }

        void setStencilMask(int mask)
        {
            if (render_flags.stencil_mask != mask)
            {
                markDirty();
                render_flags.stencil_mask = mask;
            }
        }

        int getStencilRef() const
        {
            return render_flags.stencil_ref;
        }

        void setStencilRef(int ref)
        {
            render_flags.stencil_ref = ref;
            markDirty();
        }

        int getStencilFunc() const
        {
            return render_flags.GLStencilFunc();
        }

        void setStencilFunc(int func)
        {
            render_flags.stencil_func = render_flags.GVRStencilFunc(func);
            markDirty();
        }

        int getStencilPass() const
        {
            return render_flags.GLStencilOp(render_flags.stencil_pass);
        }

        void setStencilPass(int glStencilOp)
        {
            render_flags.stencil_pass = render_flags.GVRStencilOp(glStencilOp);
            markDirty();
        }

        int getStencilFail() const
        {
            return render_flags.GLStencilOp(render_flags.stencil_fail);
        }

        void setStencilFail(int glStencilOp)
        {
            render_flags.stencil_fail = render_flags.GVRStencilOp(glStencilOp);
            markDirty();
        }

        int getDepthFail() const
        {
            return render_flags.GLStencilOp(render_flags.depth_fail);
        }

        void setDepthFail(int glStencilOp)
        {
            render_flags.depth_fail = render_flags.GVRStencilOp(glStencilOp);
            markDirty();
        }

        int getSourceBlendFunc() const
        {
            return render_flags.GLBlendFunc(render_flags.source_blend);
        }

        void setSourceBlendFunc(int glBlendFunc)
        {
            render_flags.source_blend = render_flags.GVRBlendFunc(glBlendFunc);
            markDirty();
        }

        void setDestBlendFunc(int glBlendFunc)
        {
            render_flags.dest_blend = render_flags.GVRBlendFunc(glBlendFunc);
            markDirty();
        }

        int getDestBlendFunc() const
        {
            return render_flags.GLBlendFunc(render_flags.dest_blend);
        }

        float getOffsetUnits() const
        {
            return offset_units;
        }

        void setOffsetUnits(float units)
        {
            if (offset_units != units)
            {
                markDirty();
                offset_units = units;
            }
        }

        float getOffsetFactor() const
        {
            return offset_factor;
        }

        void setOffsetFactor(float f)
        {
            if (offset_factor!= f)
            {
                markDirty();
                offset_factor = f;
            }
        }

        float getSampleCoverage() const
        {
            return sample_coverage;
        }

        void setSampleCoverage(float f)
        {
            if (sample_coverage!= f)
            {
                markDirty();
                sample_coverage = f;
            }
        }

        uint64_t getRenderFlags() const
        {
            return *(reinterpret_cast<const uint64_t*>(this));
        }
    };
}

#endif
