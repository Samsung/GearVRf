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
 * Camera with a custom projection matrix for scene rendering.
 ***************************************************************************/

#ifndef CUSTOM_CAMERA_H_
#define CUSTOM_CAMERA_H_

#include "objects/components/camera.h"

namespace gvr {

class CustomCamera: public Camera {
public:
    CustomCamera() :
            Camera(), projection_matrix_() {
    }

    glm::mat4 projection_matrix() {
        return projection_matrix_;
    }

    void set_projection_matrix(glm::mat4 projection_matrix) {
        projection_matrix_ = projection_matrix;
    }

    glm::mat4 getProjectionMatrix() const {
        return projection_matrix_;
    }

private:
    CustomCamera(const CustomCamera& camera);
    CustomCamera(CustomCamera&& camera);
    CustomCamera& operator=(const CustomCamera& camera);
    CustomCamera& operator=(CustomCamera&& camera);

private:
    glm::mat4 projection_matrix_;
};
}
#endif
