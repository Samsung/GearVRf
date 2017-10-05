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
 * Camera for scene rendering.
 ***************************************************************************/

#include "camera.h"

#include "glm/gtc/matrix_inverse.hpp"

#include "objects/scene_object.h"
#include "util/gvr_log.h"

namespace gvr {

Camera::Camera() :
        Component(Camera::getComponentType()),
        background_color_r_(0.0f),
        background_color_g_(0.0f),
        background_color_b_(0.0f),
        background_color_a_(1.0f),
        render_mask_(3),
        post_effect_data_(NULL)
{
}

Camera::~Camera() {
    if (post_effect_data_)
    {
        delete post_effect_data_;
        post_effect_data_ = NULL;
    }

}

void Camera::setPostEffect(RenderData* post_effects)
{
    post_effect_data_ = post_effects;
    if (post_effects)
    {
        Mesh* mesh = Renderer::getInstance()->getPostEffectMesh();
        post_effects->set_mesh(mesh);
    }
}


const glm::mat4& Camera::getViewMatrix() {
    if (owner_object() != nullptr) {
        Transform *const t = owner_object()->transform();
        if (t != nullptr) {
            view_matrix_ = glm::affineInverse(t->getModelMatrix(true));
        }
    }
    return view_matrix_;
}

void Camera::setViewMatrix(const glm::mat4& viewMtx) {
    view_matrix_ = viewMtx;
}
}
