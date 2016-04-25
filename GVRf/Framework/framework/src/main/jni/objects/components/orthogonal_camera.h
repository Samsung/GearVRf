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
 * Orthogonal camera for scene rendering.
 ***************************************************************************/

#ifndef ORTHOGONAL_CAMERA_H_
#define ORTHOGONAL_CAMERA_H_

#include "objects/components/camera.h"

namespace gvr {

class OrthogonalCamera: public Camera {
public:
    OrthogonalCamera() :
            Camera(), left_clipping_distance_(-1.0f), right_clipping_distance_(
                    1.0f), bottom_clipping_distance_(-1.0f), top_clipping_distance_(
                    1.0f), near_clipping_distance_(0.0f), far_clipping_distance_(
                    1.0f) {
    }

    ~OrthogonalCamera() {
    }

    float left_clipping_distance() const {
        return left_clipping_distance_;
    }

    void set_left_clipping_distance(float left_clipping_distance) {
        left_clipping_distance_ = left_clipping_distance;
    }

    float right_clipping_distance() const {
        return right_clipping_distance_;
    }

    void set_right_clipping_distance(float right_clipping_distance) {
        right_clipping_distance_ = right_clipping_distance;
    }

    float bottom_clipping_distance() const {
        return bottom_clipping_distance_;
    }

    void set_bottom_clipping_distance(float bottom_clipping_distance) {
        bottom_clipping_distance_ = bottom_clipping_distance;
    }

    float top_clipping_distance() const {
        return top_clipping_distance_;
    }

    void set_top_clipping_distance(float top_clipping_distance) {
        top_clipping_distance_ = top_clipping_distance;
    }

    float near_clipping_distance() const {
        return near_clipping_distance_;
    }

    void set_near_clipping_distance(float near_clipping_distance) {
        near_clipping_distance_ = near_clipping_distance;
    }

    float far_clipping_distance() const {
        return far_clipping_distance_;
    }

    void set_far_clipping_distance(float far_clipping_distance) {
        far_clipping_distance_ = far_clipping_distance;
    }

    glm::mat4 getProjectionMatrix() const;

private:
    OrthogonalCamera(const OrthogonalCamera& camera);
    OrthogonalCamera(OrthogonalCamera&& camera);
    OrthogonalCamera& operator=(const OrthogonalCamera& camera);
    OrthogonalCamera& operator=(OrthogonalCamera&& camera);

private:
    float left_clipping_distance_;
    float right_clipping_distance_;
    float bottom_clipping_distance_;
    float top_clipping_distance_;
    float near_clipping_distance_;
    float far_clipping_distance_;
};
}
#endif
