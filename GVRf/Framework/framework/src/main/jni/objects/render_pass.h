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
struct RenderState;

class RenderPass : public HybridObject {
public:

    enum CullFace : int {
        CullBack = 0, CullFront, CullNone
    };

    RenderPass();

    ShaderData* material() const {
        return material_;
    }

    void set_material(ShaderData* material);

    int cull_face() const {
        return cull_face_;
    }

    void set_cull_face(int cull_face);

    void set_shader(int shaderid, bool useMultiview);

    int get_shader(bool useMultiview) const { return shaderID_[useMultiview]; }

    void markDirty() {
        dirty_ = true;
    }

    void clearDirty() {
        dirty_ = false;
    }

    /**
     * Determine whether this RenderPass is dirty and might
     * need a different shader. A RenderPass is marked dirty
     * when its material is changed.
     * @param renderer  Renderer used to render this RenderData
     * @param rstate    current rendering state
     * @param rdata     RenderData this RenderPass belongs to
     * @returns -1 = material not ready, 0 = dirty, 1 = clean
     */
    int isValid(Renderer* renderer, const RenderState& rstate, RenderData* rdata);

private:
    static const int DEFAULT_CULL_FACE = CullBack;
    ShaderData* material_;
    int shaderID_[2];
    int cull_face_;
    bool dirty_;
};

}

#endif