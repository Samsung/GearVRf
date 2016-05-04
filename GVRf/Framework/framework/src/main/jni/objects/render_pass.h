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

#include "objects/hybrid_object.h"

namespace gvr {
class Material;

class RenderPass : public HybridObject {
public:

    enum CullFace : int {
        CullBack = 0, CullFront, CullNone
    };

    RenderPass() :
            material_(0), cull_face_(DEFAULT_CULL_FACE) {
    }

    Material* material() const {
        return material_;
    }

    void set_material(Material* material) {
        material_ = material;
    }

    int cull_face() const {
        return cull_face_;
    }

    void set_cull_face(int cull_face) {
        cull_face_ = cull_face;
    }

private:

    static const int DEFAULT_CULL_FACE = CullBack;
    Material* material_;
    int cull_face_;
};

}

#endif
