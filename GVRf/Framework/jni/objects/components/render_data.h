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

#include "objects/components/component.h"

namespace gvr {
class Mesh;
class Material;

class RenderData: public Component {
public:
    enum Queue {
        Background = 1000, Geometry = 2000, Transparent = 3000, Overlay = 4000
    };

    enum RenderMaskBit {
        Left = 0x1, Right = 0x2
    };

    RenderData() :
            Component(), mesh_(0), material_(0), render_mask_(
                    DEFAULT_RENDER_MASK), rendering_order_(
                    DEFAULT_RENDERING_ORDER), cull_test_(true), offset_(false), offset_factor_(
                    0.0f), offset_units_(0.0f), depth_test_(true), alpha_blend_(
                    true), draw_mode_(GL_TRIANGLES) {
    }

    ~RenderData() {
    }

    Mesh* mesh() const {
        return mesh_;
    }

    void set_mesh(Mesh* mesh) {
        mesh_ = mesh;
    }

    Material* material() const {
        return material_;
    }

    void set_material(Material* material) {
        material_ = material;
    }

    int render_mask() const {
        return render_mask_;
    }

    void set_render_mask(int render_mask) {
        render_mask_ = render_mask;
    }

    int rendering_order() const {
        return rendering_order_;
    }

    void set_rendering_order(int rendering_order) {
        rendering_order_ = rendering_order;
    }

    bool cull_test() const {
        return cull_test_;
    }

    void set_cull_test(bool cull_test) {
        cull_test_ = cull_test;
    }

    bool offset() const {
        return offset_;
    }

    void set_offset(bool offset) {
        offset_ = offset;
    }

    float offset_factor() const {
        return offset_factor_;
    }

    void set_offset_factor(float offset_factor) {
        offset_factor_ = offset_factor;
    }

    float offset_units() const {
        return offset_units_;
    }

    void set_offset_units(float offset_units) {
        offset_units_ = offset_units;
    }

    bool depth_test() const {
        return depth_test_;
    }

    void set_depth_test(bool depth_test) {
        depth_test_ = depth_test;
    }

    bool alpha_blend() const {
        return alpha_blend_;
    }

    void set_alpha_blend(bool alpha_blend) {
        alpha_blend_ = alpha_blend;
    }

    GLenum draw_mode() const {
        return draw_mode_;
    }

    void set_draw_mode(GLenum draw_mode) {
        draw_mode_ = draw_mode;
    }

private:
    RenderData(const RenderData& render_data);
    RenderData(RenderData&& render_data);
    RenderData& operator=(const RenderData& render_data);
    RenderData& operator=(RenderData&& render_data);

private:
    static const int DEFAULT_RENDER_MASK = Left | Right;
    static const int DEFAULT_RENDERING_ORDER = Geometry;
    Mesh* mesh_;
    Material* material_;
    int render_mask_;
    int rendering_order_;
    bool cull_test_;
    bool offset_;
    float offset_factor_;
    float offset_units_;
    bool depth_test_;
    bool alpha_blend_;
    GLenum draw_mode_;
};

inline bool compareRenderData(RenderData* i, RenderData* j) {
    return i->rendering_order() < j->rendering_order();
}

}
#endif
