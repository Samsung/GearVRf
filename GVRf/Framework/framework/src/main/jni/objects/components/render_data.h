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

#ifndef RENDER_DATA_H_
#define RENDER_DATA_H_

#include <memory>
#include <vector>
#include <sstream>
#include "gl/gl_program.h"

#include "objects/components/component.h"
#include "objects/shader_data.h"
#include "objects/render_pass.h"
#include "engine/renderer/render_state.h"

namespace gvr
{
    class Mesh;
    class Light;
    struct RenderState;

    class RenderData : public Component
    {
    public:
        enum Queue
        {
            Stencil = RenderModes::Stencil,
            Background = RenderModes::Background,
            Geometry = RenderModes::Geometry,
            Transparent = RenderModes::Transparent,
            Overlay = RenderModes::Overlay
        };

        enum RenderMaskBit
        {
            Left = 0x1, Right = 0x2
        };

        enum CullFace
        {
            CullBack = RenderModes::CullBack,
            CullFront = RenderModes::CullFront,
            CullNone = RenderModes::CullNone
        };

        RenderData() :
                Component(RenderData::getComponentType()), mesh_(0),
                bones_ubo_(nullptr)
        {
        }

        RenderData(const RenderData& rdata) : Component(rdata.getComponentType())
        {
            hash_code = rdata.hash_code;
            mesh_ = rdata.mesh_;
            bones_ubo_ = rdata.bones_ubo_;
            for (int i = 0; i < rdata.render_pass_list_.size(); i++)
            {
                render_pass_list_.push_back((rdata.render_pass_list_)[i]);
            }
        }

        virtual ~RenderData();

        static long long getComponentType()
        {
            return COMPONENT_TYPE_RENDER_DATA;
        }

        Mesh* mesh() const
        {
            return mesh_;
        }

        virtual bool updateGPU(Renderer*, Shader*);

        void set_mesh(Mesh* mesh);

        void add_pass(RenderPass* render_pass);

        void remove_pass(int pass);

        RenderPass* pass(int pass);

        const RenderPass* pass(int pass) const;

        const int pass_count() const
        {
            return render_pass_list_.size();
        }

        ShaderData* material(int pass) const;

        /**
         * Select or generate a shader for this render data.
         * This function executes a Java task on the Framework thread.
         */
        void bindShader(JNIEnv* env, jobject localSceneObject, bool);

        void markDirty()
        {
            dirty_ = true;
        }

        bool isDirty() const
        {
            return dirty_;
        }

        void clearDirty()
        {
            dirty_ = false;
        }

        void enable_light()
        {
            render_pass_list_[0]->enable_light();
        }

        void disable_light()
        {
            render_pass_list_[0]->disable_light();
        }

        bool light_enabled()
        {
            return render_pass_list_[0]->light_enabled();
        }

        void enable_lightmap()
        {
            render_pass_list_[0]->enable_lightmap();
        }

        void disable_lightmap()
        {
            render_pass_list_[0]->disable_lightmap();
        }

        int render_mask() const
        {
            return render_pass_list_[0]->render_mask();
        }

        void set_render_mask(int render_mask)
        {
            render_pass_list_[0]->set_render_mask(render_mask);
        }

        int rendering_order() const
        {
            return render_pass_list_[0]->rendering_order();
        }

        void set_rendering_order(int rendering_order)
        {
            render_pass_list_[0]->set_rendering_order(rendering_order);
        }

        bool cast_shadows()
        {
            return render_pass_list_[0]->cast_shadows();
        }

        void set_cast_shadows(bool cast_shadows)
        {
            render_pass_list_[0]->set_cast_shadows(cast_shadows);
        }

        int cull_face(int pass = 0) const;

        bool offset() const
        {
            return render_pass_list_[0]->offset();
        }

        void set_offset(bool offset)
        {
            render_pass_list_[0]->set_offset(offset);
        }

        float offset_factor() const
        {
            return render_pass_list_[0]->offset_factor();
        }

        void set_offset_factor(float offset_factor)
        {
            render_pass_list_[0]->set_offset_factor(offset_factor);
        }

        float offset_units() const
        {
            return render_pass_list_[0]->offset_units();
        }

        void set_offset_units(float offset_units)
        {
            render_pass_list_[0]->set_offset_units(offset_units);
        }

        bool depth_test() const
        {
            return render_pass_list_[0]->depth_test();
        }

        bool depth_mask() const
        {
            return render_pass_list_[0]->depth_mask();
        }

        void set_depth_test(bool depth_test)
        {
            render_pass_list_[0]->set_depth_test(depth_test);
        }

        void set_depth_mask(bool depth_mask)
        {
            render_pass_list_[0]->set_depth_mask(depth_mask);
        }

        void set_alpha_blend_func(int sourceblend, int destblend)
        {
            render_pass_list_[0]->set_alpha_blend_func(sourceblend, destblend);
        }

        int source_alpha_blend_func() const
        {
            return render_pass_list_[0]->source_alpha_blend_func();
        }

        int dest_alpha_blend_func() const
        {
            return render_pass_list_[0]->dest_alpha_blend_func();
        }

        bool alpha_blend() const
        {
            return render_pass_list_[0]->alpha_blend();
        }

        void set_alpha_blend(bool alpha_blend)
        {
            render_pass_list_[0]->set_alpha_blend(alpha_blend);
        }

        bool alpha_to_coverage() const
        {
            return render_pass_list_[0]->alpha_to_coverage();
        }

        void set_alpha_to_coverage(bool alpha_to_coverage)
        {
            render_pass_list_[0]->set_alpha_to_coverage(alpha_to_coverage);
        }

        void set_sample_coverage(float sample_coverage)
        {
            render_pass_list_[0]->set_sample_coverage(sample_coverage);
        }

        float sample_coverage() const
        {
            return render_pass_list_[0]->sample_coverage();
        }

        void set_invert_coverage_mask(GLboolean invert_coverage_mask)
        {
            render_pass_list_[0]->set_invert_coverage_mask(invert_coverage_mask);
        }

        bool invert_coverage_mask() const
        {
            return render_pass_list_[0]->invert_coverage_mask();
        }

        int draw_mode() const
        {
            return render_pass_list_[0]->draw_mode();
        }

        void set_draw_mode(GLenum draw_mode)
        {
            render_pass_list_[0]->set_draw_mode(draw_mode);
        }

        void setStencilTest(bool flag)
        {
            render_pass_list_[0]->setStencilTest(flag);
        }

        void setStencilFunc(int func, int ref, int mask)
        {
            render_pass_list_[0]->setStencilFunc(func, ref, mask);
        }

        void setStencilOp(int sfail, int dpfail, int dppass)
        {
            render_pass_list_[0]->setStencilOp(sfail, dpfail, dppass);
        }

        void setStencilMask(unsigned int mask)
        {
            render_pass_list_[0]->setStencilMask(mask);
        }

        unsigned int getStencilMask()
        { return render_pass_list_[0]->getStencilMask(); }

        bool stencil_test()
        { return render_pass_list_[0]->stencil_test(); }

        int stencil_func_func()
        { return render_pass_list_[0]->stencil_func_func(); }

        int stencil_func_ref()
        { return render_pass_list_[0]->stencil_func_ref(); }

        int stencil_func_mask()
        { return render_pass_list_[0]->stencil_func_mask(); }

        int stencil_op_sfail()
        { return render_pass_list_[0]->stencil_op_sfail(); }

        int stencil_op_dpfail()
        { return render_pass_list_[0]->stencil_op_dpfail(); }

        int stencil_op_dppass()
        { return render_pass_list_[0]->stencil_op_dppass(); }

        UniformBlock* getBonesUbo()
        {
            return bones_ubo_;
        }

        bool isHashCodeDirty()
        { return render_pass_list_[0]->render_modes().isDirty(); }

        int get_shader(bool useMultiview = false, int pass = 0) const
        { return render_pass_list_[pass]->get_shader(useMultiview); }

        const std::string& getHashCode();

        unsigned short getRenderDataFlagsHashCode()
        {
            return render_pass_list_[0]->render_modes().getRenderFlags();
        }

        void setBindShaderObject(JNIEnv* env, jobject bindShaderObject);

    private:
        RenderData(RenderData&& render_data) = delete;

        RenderData& operator=(const RenderData& render_data) = delete;

        RenderData& operator=(RenderData&& render_data) = delete;

    protected:
        jmethodID bindShaderMethod_;
        Mesh* mesh_;
        UniformBlock* bones_ubo_;
        std::string hash_code;
        std::vector<RenderPass*> render_pass_list_;
        bool dirty_ = false;
        jobject bindShaderObject_ = nullptr;
        JavaVM* javaVm_ = nullptr;
    };
}

#endif
