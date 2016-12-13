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

namespace gvr {

RenderData::~RenderData() {
}

void RenderData::add_pass(RenderPass* render_pass) {
    render_pass_list_.push_back(render_pass);
    render_pass->add_dirty_flag(dirty_flag_);
    *dirty_flag_ = true;
}

const RenderPass* RenderData::pass(int pass) const {
    if (pass >= 0 && pass < render_pass_list_.size()) {
        return render_pass_list_[pass];
    }
    return nullptr;
}

void RenderData::set_mesh(Mesh* mesh) {
    mesh_ = mesh;
    mesh->add_dirty_flag(dirty_flag_);
    *dirty_flag_ = true;
}

void RenderData::setDirty(bool dirty){
    *dirty_flag_ = dirty;
}

bool RenderData::cull_face(int pass) const {
    if (pass >= 0 && pass < render_pass_list_.size()) {
        return render_pass_list_[pass]->cull_face();
    }
    return nullptr;
}

Material* RenderData::material(int pass) const {
    if (pass >= 0 && pass < render_pass_list_.size()) {
        return render_pass_list_[pass]->material();
    }
    return nullptr;
}

void RenderData::setCameraDistanceLambda(std::function<float()> func) {
    cameraDistanceLambda_ = func;
}

bool compareRenderDataByOrderShaderDistance(RenderData *i, RenderData *j) {
    //1. rendering order needs to be sorted first to guarantee specified correct order
    if (i->rendering_order() == j->rendering_order()) {

        if (i->material(0)->shader_type() == j->material(0)->shader_type()) {

            // if it is a transparent object, sort by camera distance from back to front
            if (i->rendering_order() >= RenderData::Transparent
                && i->rendering_order() < RenderData::Overlay) {
                return i->camera_distance() > j->camera_distance();
            }

            int no_passes1 = i->pass_count();
            int no_passes2 = j->pass_count();

            if (no_passes1 == no_passes2) {

                //@todo what about the other passes

                //this is pointer comparison; assumes batching is on; if the materials are not
                //the same then comparing the pointers further is an arbitrary decision; hence
                //falling back to camera distance.
                if (i->material(0) == j->material(0)) {
                    if (i->cull_face(0) == j->cull_face(0)) {
                        if (i->getHashCode().compare(j->getHashCode()) == 0) {
                            // otherwise sort from front to back
                            return i->camera_distance() < j->camera_distance();
                        }
                        return i->getHashCode() < j->getHashCode();
                    }
                    return i->cull_face(0) < j->cull_face(0);
                }
                return i->material(0) < j->material(0);
            }
            return no_passes1 < no_passes2;
        }
        return i->material(0)->shader_type() < j->material(0)->shader_type();
    }
    return i->rendering_order() < j->rendering_order();
}
}