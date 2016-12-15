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

#include "objects/hybrid_object.h"
#include "objects/components/render_data.h"
#include "objects/render_pass.h"

namespace gvr {

void RenderPass::add_listener(RenderData* render_data){
    if(render_data){
        listener_->add_listener(render_data);
        if(material_)
            material_->add_listener(render_data);
    }
}

void RenderPass::set_material(Material* material) {
    // if renderData changes its material in-between remove the owners from previous material and add the owners into new
    if(material_)
        material_->remove_listener(listener_);

    material_ = material;

    material->add_listener(listener_);
    listener_->notify_listeners(true);
}
}