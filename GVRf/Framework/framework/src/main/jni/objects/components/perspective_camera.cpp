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
 * Perspective camera for scene rendering.
 ***************************************************************************/

#include "perspective_camera.h"

#include "glm/gtc/matrix_transform.hpp"

namespace gvr {
float PerspectiveCamera::default_fov_y_ = glm::radians(95.0f);
float PerspectiveCamera::default_aspect_ratio_ = 1.0f;

glm::mat4 PerspectiveCamera::getProjectionMatrix() const {
    return glm::perspective(fov_y_, aspect_ratio(), near_clipping_distance(),
            far_clipping_distance());
}
}
