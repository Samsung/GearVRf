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
 * Containing data about material and per pass configurations.             *
 ***************************************************************************/

#ifndef RENDER_PASS_H_
#define RENDER_PASS_H_

#include <memory>
#include <unordered_set>
#include "objects/hybrid_object.h"
#include "engine/renderer/render_state.h"

namespace gvr {

class ShaderData;
struct RenderState;

/**
 * Contains the material and render modes describing
 * how to render a mesh. A RenderPass exists as part
 * of the RenderData class. Each RenderData may have
 * one or more passes. The mesh is rendered differently
 * in each pass.
 *
 * @see RenderData
 */
class RenderPass : public HybridObject
{
public:
    RenderPass();
    virtual ~RenderPass() {}

    ShaderData* material() const {
        return material_;
    }

    /**
     * Set the material to use for rendering the mesh
     * in this pass.
     * @param material ShaderData uniform and texture data for rendering
     */
    void set_material(ShaderData* material);

    /**
     * Enable lighting for this pass.
     */
    void enable_light()
    {
        if (render_modes_.setUseLights(true))
        {
            markDirty();
        }
    }

    /**
     * Disable lighting for this pass.
     */
    void disable_light()
    {
        if (render_modes_.setUseLights(false))
        {
            markDirty();
        }
    }

    /**
     * Determine whether lighting is enabled or not.
     * @return true if lighting enabled for this pass, false if not
     */
    bool light_enabled()
    {
        return render_modes_.useLights();
    }

    /*
     * Enable light mapping for this pass.
     */
    void enable_lightmap()
    {
        render_modes_.setUseLightmap(true);
    }

    /*
     * Disable light mapping for this pass.
     */
    void disable_lightmap()
    {
        render_modes_.setUseLightmap(false);
    }

    /*
     * Get the rendering order for this pass.
     */
    int rendering_order() const
    {
        return render_modes_.getRenderOrder();
    }

    /**
     * Set the rendering order for this pass.
     */
    void set_rendering_order(int ro)
    {
        render_modes_.setRenderOrder(ro);
    }

    /**
     * Get the draw mode for this pass.
     * @return OpenGL draw mode
     */
    int draw_mode() const
    {
        return render_modes_.getDrawMode();
    }

    /**
     * Set the draw mode for this pass.
     * This is the OpenGL draw mode.
     * @param draw_mode
     */
    void set_draw_mode(int draw_mode)
    {
        render_modes_.setDrawMode(draw_mode);
    }

    /*
     * Determine whether stencil test is enabled.
     * @returns true if enabled, else false.
     */
    bool stencil_test() const
    {
        return render_modes_.isStencilTestEnabled();
    }

    /**
     * Enable or disable the stencil test
     * @param stencil_test true to enable, false to disable.
     */
    void set_stencil_test(int stencil_test)
    {
        render_modes_.setStencilTest(stencil_test);
    }

    /**
     * Get the eye render mask (enable or disable eyes independently).
     * @return render mask
     */
    int render_mask() const
    {
        return render_modes_.getRenderMask();
    }

    /**
     * Set the eye render mask (enable or disable eyes independently).
     * @param render_mask   new render mask
     */
    void set_render_mask(int render_mask)
    {
        render_modes_.setRenderMask(render_mask);
    }

    /**
     * Determine if shadows are enabled.
     * @return true if this pass casts shadows, else false.
     */
    bool cast_shadows()
    {
        return render_modes_.castShadows();
    }

    /**
     * Enable or disable shadow casting.
     * @param cast_shadows  true to make this pass cast shadows, false to disable.
     */
    void set_cast_shadows(bool cast_shadows)
    {
        if (render_modes_.setCastShadows(cast_shadows))
        {
            markDirty();
        }
    }

    /**
     * Determine if alpha blending is enabled.
     * @return true if enabled, false if disabled.
     */
    bool alpha_blend() const
    {
        return render_modes_.isAlphaBlendEnabled();
    }

    /**
     * Enable or disable alpha blending.
     * @param alpha_blend true to enable, false to disable.
     */
    void set_alpha_blend(bool alpha_blend)
    {
        render_modes_.setAlphaBlend(alpha_blend);
    }

    /**
     * Get the source alpha blend function.
     * This is the OpenGL value for the blend function.
     * @return OpenGL blend function
     */
    int source_alpha_blend_func() const
    {
        return render_modes_.getSourceBlendFunc();
    }

    /**
     * Get the destination alpha blend function.
     * This is the OpenGL value for the blend function.
     * @return OpenGL blend function
     */
    int dest_alpha_blend_func() const
    {
        return render_modes_.getDestBlendFunc();
    }

    /**
     * Set the source and destination alpha blend functions.
     *
     * @param sourceblend OpenGL source blend function.
     * @param destblend   OpenGL destination blend function.
     */
    void set_alpha_blend_func(int sourceblend, int destblend)
    {
        render_modes_.setSourceBlendFunc(sourceblend);
        render_modes_.setDestBlendFunc(destblend);
    }

    /**
     * Determine whether alpha to coverage is enabled.
     * @return true if enabled, false if disabled.
     */
    bool alpha_to_coverage() const
    {
        return render_modes_.isAlphaToCoverageEnabled();
    }

    /**
     * Enable or disable alpha to coverage,
     * @param alpha_to_coverage true to enable, false to disable.
     */
    void set_alpha_to_coverage(bool alpha_to_coverage)
    {
        render_modes_.setAlphaToCoverage(alpha_to_coverage);
    }

    /**
     * Determine whether coverage mask is inverted.
     * @return true if using inverted mask, false if not.
     */
    bool invert_coverage_mask() const
    {
        return render_modes_.invertCoverageMask();
    }

    /**
     * Designate whether or not to invert the coverage mask.
     * @param invert_coverage_mask true to invert, false to not.
     */
    void set_invert_coverage_mask(bool invert_coverage_mask)
    {
        render_modes_.setInvertCoverageMask(invert_coverage_mask);
    }

    /**
     * Get the face culling mode.
     * @return CullFace::CullBack, CullFace::CullFront or CullFace::CullNone
     */
    int cull_face() const
    {
        return render_modes_.getCullFace();
    }

    /**
     * Set face culling mode.
     * @param cull_face one of CullFace::CullBack, CullFace::CullFront or CullFace::CullNone
     */
    void set_cull_face(int cull_face)
    {
        render_modes_.setCullFace(cull_face);
    }

    /**
     * Determine whether depth testing is enabled.
     * @return true if enabled, else false.
     */
    bool depth_test() const
    {
        return render_modes_.isDepthTestEnabled();
    }

    /**
     * Enable or disable depth testing.
     * @param depth_test true to enable, false to disable.
     */
    void set_depth_test(bool depth_test)
    {
        render_modes_.setDepthTest(depth_test);
    }

    /**
     * Enable or disable the depth mask.
     * @param depth_mask true to enable, false to disable.
     */
    void set_depth_mask(bool depth_mask)
    {
        render_modes_.setDepthMask(depth_mask);
    }

    /**
     * Determine whether depth mask is enabled.
     * @return true if enabled, false if not.
     */
    bool depth_mask() const
    {
        return render_modes_.isDepthMaskEnabled();
    }

    /**
     * Determine whether polygon offset is enabled.
     * @return true if enabled, false if not.
     */
    bool offset() const
    {
        return render_modes_.isOffsetEnabled();
    }

    /**
     * Enable or disable the polygon offset.
     * @param offset true to enable, false to disable.
     */
    void set_offset(bool offset)
    {
        render_modes_.setOffset(offset);
    }

    /**
     * Get the polygon offset units.
     * This scales the minimum resolvable depth buffer value.
     * @return polygon offset units
     */
    float offset_units() const
    {
        return render_modes_.getOffsetUnits();
    }

    /**
     * Set the polygon offset units.
     * This scales the minimum resolvable depth buffer value.
     * @param units new polygon offset units
     */
    void set_offset_units(float units)
    {
        render_modes_.setOffsetUnits(units);
    }

    /**
     * Gets the polygon offset factor.
     * This scales the maximum Z slope, with respect to X or Y of the polygon.
     * @return polygon offset factor
     */
    float offset_factor() const
    {
        return render_modes_.getOffsetFactor();
    }

    /**
    * Sets the polygon offset factor.
    * This scales the maximum Z slope, with respect to X or Y of the polygon.
    * @param factor new polygon offset factor
    */
    void set_offset_factor(float factor)
    {
        render_modes_.setOffsetFactor(factor);
    }

    /**
     * Get sample coverage factor.
     * @return sample coverage factor.
     */
    float sample_coverage() const
    {
        return render_modes_.getSampleCoverage();
    }

    /**
     * Set sample coverage factor.
     * @param f new sample coverage factor.
     */
    void set_sample_coverage(float f)
    {
        render_modes_.setSampleCoverage(f);
    }

    /**
     * Set stencil function and parameters.
     * @param func  stencil function
     * @param ref   stencil reference
     * @param mask  stencil mask
     */
    void setStencilFunc(int func, int ref, int mask)
    {
        render_modes_.setStencilFunc(func);
        render_modes_.setStencilRef(ref);
        render_modes_.setStencilFuncMask(mask);
    }

    /**
     * Set stencil operations.
     * @param sfail     when stencil test fails, do this
     * @param dpfail    when depth test fails, do this
     * @param dppass    when stencil and depth pass, do this
     */
    void setStencilOp(int sfail, int dpfail, int dppass)
    {
        render_modes_.setStencilFail(sfail);
        render_modes_.setDepthFail(dpfail);
        render_modes_.setStencilPass(dppass);
    }

    /**
     * Enable or disable stencil test.
     * @param flag true to enable, false to disable.
     */
    void setStencilTest(bool flag)
    {
        render_modes_.setStencilTest(flag);
    }

    /**
     * Set the stencil mask.
     * @param mask new stencil mask.
     */
    void setStencilMask(unsigned int mask)
    {
        render_modes_.setStencilMask(mask);
    }

    /**
     * Get the current stencil mask.
     * @return stencil mask value.
     */
    unsigned int getStencilMask() const
    { return render_modes_.getStencilMask(); }

    /**
     * Get the stencil function.
     * @return OpenGL stencil function.
     */
    int stencil_func_func() const
    { return render_modes_.getStencilFunc(); }

    /**
     * Get the stencil reference.
     * @return OpenGL stencil reference.
     */
    int stencil_func_ref() const
    { return render_modes_.getStencilRef(); }

    /**
     * Get the stencil mask.
     * @return integer stencil mask.
     */
    int stencil_func_mask() const
    { return render_modes_.getStencilFuncMask(); }

    /**
     * Get the stencil test fail operation.
     * @return GL stencil operation for stencil test failure.
     */
    int stencil_op_sfail() const
    { return render_modes_.getStencilFail(); }

    /**
     * Get the depth test fail operation.
     * @return GL stencil operation for depth test failure.
     */
    int stencil_op_dpfail() const
    { return render_modes_.getDepthFail(); }

    /**
     * Get stencil, depth test pass operation.
     * @return GL stencil operator for stencil & depth test pass.
     */
    int stencil_op_dppass()
    { return render_modes_.getStencilPass(); }

    /**
     * Set the native shader for this pass.
     * @param shaderid      native shader ID
     * @param useMultiview  true if multiview shader, else false.
     */
    void set_shader(int shaderid, bool useMultiview);

    /**
     * Get the native shader ID.
     * @param useMultiview true if using multiview
     * @return shader ID, -1 if shader not generated yet.
     */
    int get_shader(bool useMultiview) const { return shaderID_[useMultiview]; }

    /**
     * Mark this render pass as dirty.
     * A pass is marked dirty if something changed which
     * would cause the shader used by the pass to
     * be changed in some way.
     */
    void markDirty()
    {
        shader_dirty_ = true;
    }

    /**
     * Determine if render pass is dirty or not.
     * @return true if dirty, false if not.
     */
    bool isDirty()
    {
        return shader_dirty_;
    }

    /**
     * Mark this render pass as clean.
     * Usually this is called after the shader has been regenerated.
     */
    void clearDirty()
    {
        shader_dirty_ = false;
    }

    /**
     * Get the render modes for this pass.
     * @return render modes.
     */
    const RenderModes& render_modes() const
    {
        return render_modes_;
    }

    /**
     * Get the render modes for this pass.
     * @return render modes.
     */
    RenderModes& render_modes()
    {
        return render_modes_;
    }

private:
    ShaderData* material_;
    int shaderID_[2];
    RenderModes render_modes_;
    bool shader_dirty_;
};

}

#endif