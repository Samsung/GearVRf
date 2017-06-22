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
#include "objects/helpers.h"
namespace gvr {

class ShaderData;

class RenderPass : public HybridObject {
public:

    enum CullFace : int {
        CullBack = 0, CullFront, CullNone
    };

    RenderPass() :
            material_(0), shaderID_(0), cull_face_(DEFAULT_CULL_FACE)
    { }

    ShaderData* material() const {
        return material_;
    }

    void set_material(ShaderData* material);

    int cull_face() const {
        return cull_face_;
    }

    void set_cull_face(int cull_face) {
        cull_face_ = cull_face;
        dirty(MOD_CULL_FACE);
    }

    void set_shader(int shaderid)
    {
        shaderID_ = shaderid;
        dirty(MOD_SHADER_ID);
    }

    int get_shader() const { return shaderID_; }

    void dirty(DIRTY_BITS bit) {
        dirtyImpl(dirty_flags_,bit);
    }

    void add_dirty_flag(const std::shared_ptr<u_short>& dirty_flag);

private:
    static const int DEFAULT_CULL_FACE = CullBack;
    ShaderData* material_;
    int shaderID_;
    int cull_face_;
    std::unordered_set<std::shared_ptr<u_short>> dirty_flags_;
};

}

#endif
