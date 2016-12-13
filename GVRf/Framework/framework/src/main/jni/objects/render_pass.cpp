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

#include "objects/render_pass.h"
#include "objects/material.h"

namespace gvr {

void RenderPass::set_material(Material* material) {
    material_ = material;
    material->add_dirty_flags(dirty_flags_);
    dirty();
}

void RenderPass::add_dirty_flag(const std::shared_ptr<bool>& dirty_flag) {
    dirty_flags_.insert(dirty_flag);
    if (nullptr != material_) {
        material_->add_dirty_flag(dirty_flag);
    }
}

}