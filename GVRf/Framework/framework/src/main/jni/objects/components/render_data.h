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

#include "gl/gl_program.h"
#include "glm/glm.hpp"
#include "objects/mesh.h"
#include "objects/components/component.h"
#include "objects/render_pass.h"
#include "objects/material.h"
#include<sstream>
typedef unsigned long Long;
namespace gvr {
class Mesh;
class Material;
class Light;
class Batch;
class TextureCapturer;
template<typename T>
std::string to_string(T value) {
    //create an output string stream
    std::ostringstream os;

    //throw the value into the string stream
    os << value;

    //convert the string stream into a string and return
    return os.str();
}
class RenderData: public Component {
public:
    enum Queue {
        Background = 1000, Geometry = 2000, Transparent = 3000, Overlay = 4000
    };

    enum RenderMaskBit {
        Left = 0x1, Right = 0x2
    };

    enum CullFace {
        CullBack = 0, CullFront, CullNone
    };

    RenderData() :
            Component(RenderData::getComponentType()), mesh_(0), light_(0), use_light_(
                    false), use_lightmap_(false), batching_(false), render_mask_(
                    DEFAULT_RENDER_MASK), batch_(nullptr), rendering_order_(
                    DEFAULT_RENDERING_ORDER), hash_code_dirty_(true), offset_(
                    false), offset_factor_(0.0f), offset_units_(0.0f), depth_test_(
                    true), alpha_blend_(true), alpha_to_coverage_(false), sample_coverage_(
                    1.0f), invert_coverage_mask_(GL_FALSE), draw_mode_(
                    GL_TRIANGLES), texture_capturer(0) {
    }

    void copy(const RenderData& rdata) {
        Component(rdata.getComponentType());
        hash_code = rdata.hash_code;
        mesh_ = rdata.mesh_;
        light_ = rdata.light_;
        use_light_ = rdata.use_light_;
        use_lightmap_ = rdata.use_lightmap_;
        batching_ = rdata.batching_;
        render_mask_ = rdata.render_mask_;
        batch_ = rdata.batch_;
        rendering_order_ = rdata.rendering_order_;
        hash_code_dirty_ = rdata.hash_code_dirty_;
        offset_ = rdata.offset_;
        offset_factor_ = rdata.offset_factor_;
        offset_units_ = rdata.offset_units_;
        depth_test_ = rdata.depth_test_;
        alpha_blend_ = rdata.alpha_blend_;
        alpha_to_coverage_ = rdata.alpha_to_coverage_;
        sample_coverage_ = rdata.sample_coverage_;
        invert_coverage_mask_ = rdata.invert_coverage_mask_;
        draw_mode_ = rdata.draw_mode_;
        texture_capturer = rdata.texture_capturer;
    }

    RenderData(const RenderData& rdata) {
        copy(rdata);
    }

    ~RenderData() {
        render_pass_list_.clear();
    }

    static long long getComponentType() {
        return (long long) &getComponentType;
    }

    Mesh* mesh() const {
        return mesh_;
    }

    void set_mesh(Mesh* mesh) {
        mesh_ = mesh;
    }

    void add_pass(RenderPass* render_pass) {
        render_pass_list_.push_back(render_pass);
    }

    const RenderPass* pass(int pass) const {
        if (pass >= 0 && pass < render_pass_list_.size()) {
            return render_pass_list_[pass];
        }

        return nullptr;
    }

    const int pass_count() const {
        return render_pass_list_.size();
    }

    Material* material(int pass) const {
        if (pass >= 0 && pass < render_pass_list_.size()) {
            return render_pass_list_[pass]->material();
        }

        return nullptr;
    }

    void set_material(Material* material, int pass) {
        if (pass >= 0 && pass < render_pass_list_.size()) {
            render_pass_list_[pass]->set_material(material);
        }
    }

    Light* light() const {
        return light_;
    }

    void set_light(Light* light) {
        light_ = light;
        use_light_ = true;
    }

    void enable_light() {
        use_light_ = true;
        hash_code_dirty_ = true;
    }

    void disable_light() {
        use_light_ = false;
        hash_code_dirty_ = true;
    }

    bool light_enabled() {
        return use_light_;
    }

    void enable_lightmap() {
        use_lightmap_ = true;
        hash_code_dirty_ = true;
    }

    void disable_lightmap() {
        use_lightmap_ = false;
        hash_code_dirty_ = true;
    }

    bool lightmap_enabled() {
        return use_lightmap_;
    }

    int render_mask() const {
        return render_mask_;
    }

    void set_render_mask(int render_mask) {
        render_mask_ = render_mask;
        hash_code_dirty_ = true;
    }

    int rendering_order() const {
        return rendering_order_;
    }

    void set_rendering_order(int rendering_order) {
        rendering_order_ = rendering_order;
    }

    Batch* getBatch() {
        return batch_;
    }

    void set_batching(bool status) {
        batching_ = status;
    }

    bool batching() {
        return batching_;
    }

    void setBatch(Batch* batch) {
        this->batch_ = batch;
    }

    void setBatchNull() {
        batch_ = nullptr;
    }

    bool cull_face(int pass = 0) const {
        if (pass >= 0 && pass < render_pass_list_.size()) {
            return render_pass_list_[pass]->cull_face();
        }

        return nullptr;
    }

    void set_cull_face(int cull_face, int pass) {
        if (pass >= 0 && pass < render_pass_list_.size()) {
            render_pass_list_[pass]->set_cull_face(cull_face);
        }
    }

    bool offset() const {
        return offset_;
    }

    void set_offset(bool offset) {
        offset_ = offset;
        hash_code_dirty_ = true;
    }

    float offset_factor() const {
        return offset_factor_;
    }

    void set_offset_factor(float offset_factor) {
        offset_factor_ = offset_factor;
        hash_code_dirty_ = true;
    }

    float offset_units() const {
        return offset_units_;
    }

    void set_offset_units(float offset_units) {
        offset_units_ = offset_units;
        hash_code_dirty_ = true;
    }

    bool depth_test() const {
        return depth_test_;
    }

    void set_depth_test(bool depth_test) {
        depth_test_ = depth_test;
        hash_code_dirty_ = true;
    }

    bool alpha_blend() const {
        return alpha_blend_;
    }

    void set_alpha_blend(bool alpha_blend) {
        alpha_blend_ = alpha_blend;
        hash_code_dirty_ = true;
    }

    bool alpha_to_coverage() const {
    	return alpha_to_coverage_;
    }

    void set_alpha_to_coverage(bool alpha_to_coverage) {
        alpha_to_coverage_ = alpha_to_coverage;
        hash_code_dirty_ = true;
    }

    void set_sample_coverage(float sample_coverage) {
        sample_coverage_ = sample_coverage;
        hash_code_dirty_ = true;
    }
   
    float sample_coverage() const {
    	return sample_coverage_;
    }

    void set_invert_coverage_mask(GLboolean invert_coverage_mask) {
        invert_coverage_mask_ = invert_coverage_mask;
        hash_code_dirty_ = true;
    }

    GLboolean invert_coverage_mask() const {
    	return invert_coverage_mask_;
    }

    GLenum draw_mode() const {
        return draw_mode_;
    }

    void set_camera_distance(float distance) {
        camera_distance_ = distance;
    }

    float camera_distance() const {
        return camera_distance_;
    }

    void set_draw_mode(GLenum draw_mode) {
        draw_mode_ = draw_mode;
        hash_code_dirty_ = true;
    }

    void set_texture_capturer(TextureCapturer *capturer) {
        texture_capturer = capturer;
    }
    // TODO: need to consider texture_capturer in hash_code ?
    TextureCapturer *get_texture_capturer() {
        return texture_capturer;
    }

    std::string getHashCode() {
        if (!hash_code_dirty_) {
            std::string render_data_string;

            render_data_string.append(to_string(use_light_));
            render_data_string.append(to_string(light_));
            render_data_string.append(to_string(getComponentType()));
            render_data_string.append(to_string(use_lightmap_));
            render_data_string.append(to_string(render_mask_));
            render_data_string.append(to_string(offset_));
            render_data_string.append(to_string(offset_factor_));
            render_data_string.append(to_string(offset_units_));
            render_data_string.append(to_string(depth_test_));
            render_data_string.append(to_string(alpha_blend_));
            render_data_string.append(to_string(alpha_to_coverage_));
            render_data_string.append(to_string(sample_coverage_));
            render_data_string.append(to_string(invert_coverage_mask_));
            render_data_string.append(to_string(draw_mode_));

            hash_code = render_data_string;
            hash_code_dirty_ = false;

        }
        return hash_code;
    }
private:
    //  RenderData(const RenderData& render_data);
    RenderData(RenderData&& render_data);
    RenderData& operator=(const RenderData& render_data);
    RenderData& operator=(RenderData&& render_data);

private:
    static const int DEFAULT_RENDER_MASK = Left | Right;
    static const int DEFAULT_RENDERING_ORDER = Geometry;
    Mesh* mesh_;
    Batch* batch_;
    bool hash_code_dirty_;
    std::string hash_code;
    std::vector<RenderPass*> render_pass_list_;
    Light* light_;
    bool use_light_;
    bool batching_;
    bool use_lightmap_;
    int render_mask_;
    int rendering_order_;
    bool offset_;
    float offset_factor_;
    float offset_units_;
    bool depth_test_;
    bool alpha_blend_;
    bool alpha_to_coverage_;
    float sample_coverage_;
    GLboolean invert_coverage_mask_;
    GLenum draw_mode_;
    float camera_distance_;
    TextureCapturer *texture_capturer;
};

static inline bool compareRenderDataWithFrustumCulling(RenderData* i, RenderData* j) {
    // if either i or j is a transparent object or an overlay object
    if (i->rendering_order() >= RenderData::Transparent
            || j->rendering_order() >= RenderData::Transparent) {
        if (i->rendering_order() == j->rendering_order()) {
            // if both are either transparent or both are overlays
            // place them in reverse camera order from back to front
            return i->camera_distance() > j->camera_distance();
        } else {
            // if one of them is a transparent or an overlay draw by rendering order
            return i->rendering_order() < j->rendering_order();
        }
    }

    // if both are neither transparent nor overlays, place them in camera order front to back
    return i->camera_distance() < j->camera_distance();
}

static inline bool compareRenderDataByOrder(RenderData* i, RenderData* j) {
    return i->rendering_order() < j->rendering_order();
}

static inline bool compareRenderDataByShader(RenderData* i, RenderData* j) {
    // Compare renderData by their material's shader type
    // Note: multi-pass renderData is skipped for now and put to later position,
    // since each of the passes has a separate material (and shader as well).
    // An advanced sorting may be added later to take multi-pass into account
    if (j->pass_count() > 1) {
        return true;
    }

    if (i->pass_count() > 1) {
        return false;
    }

    return i->material(0)->shader_type() < j->material(0)->shader_type();
}

static inline bool compareRenderDataByOrderDistance(RenderData* i, RenderData* j) {
    // if it is a transparent object, sort by camera distance.
    if (i->rendering_order() == j->rendering_order()
            && i->rendering_order() >= RenderData::Transparent
            && i->rendering_order() < RenderData::Overlay) {
        return i->camera_distance() > j->camera_distance();
    }

    return i->rendering_order() < j->rendering_order();
}

static inline bool compareRenderDataByOrderShaderDistance(RenderData* i,
        RenderData* j) {
    //1. rendering order needs to be sorted first to guarantee specified correct order
    if (i->rendering_order() == j->rendering_order()) {

        if (i->material(0)->shader_type() == j->material(0)->shader_type()) {

            if (i->material(0) == j->material(0)) {

                if (i->getHashCode().compare(j->getHashCode()) == 0) {

                    if (i->mesh()->isDynamic() == j->mesh()->isDynamic()) {
                        // if it is a transparent object, sort by camera distance from back to front
                        if (i->rendering_order() >= RenderData::Transparent
                                && i->rendering_order() < RenderData::Overlay) {
                            return i->camera_distance() > j->camera_distance();
                        }
                        // otherwise sort from front to back
                        return i->camera_distance() < j->camera_distance();
                    }
                    return true;
                }
                return i->getHashCode() < j->getHashCode();
            }
            return i->material(0) < j->material(0);
        }
        return i->material(0)->shader_type() < j->material(0)->shader_type();
    }
    return i->rendering_order() < j->rendering_order();
}
}
#endif
