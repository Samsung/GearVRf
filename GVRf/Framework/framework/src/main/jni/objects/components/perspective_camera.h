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

#ifndef PERSPECTIVE_CAMERA_H_
#define PERSPECTIVE_CAMERA_H_

#include "objects/components/camera.h"

namespace gvr {

class PerspectiveCamera: public Camera {
public:
    PerspectiveCamera() :
            Camera(), near_clipping_distance_(0.1f), far_clipping_distance_(
                    1000.0f), fov_y_(default_fov_y_), aspect_ratio_(
                    default_aspect_ratio_) {
    }

    ~PerspectiveCamera() {
    }

    static float default_aspect_ratio() {
        return default_aspect_ratio_;
    }

    static void set_default_aspect_ratio(float aspect_ratio) {
        default_aspect_ratio_ = aspect_ratio;
    }

    // in radians
    static float default_fov_y() {
        return default_fov_y_;
    }

    // in radians
    static void set_default_fov_y(float fov_y) {
        default_fov_y_ = fov_y;
    }

    float aspect_ratio() const {
        return aspect_ratio_;
    }

    void set_aspect_ratio(float aspect_ratio) {
        aspect_ratio_ = aspect_ratio;
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

    // in radians
    float fov_y() const {
        return fov_y_;
    }

    // in radians
    void set_fov_y(float fov_y) {
        fov_y_ = fov_y;
    }

    glm::mat4 getProjectionMatrix() const;

private:
    PerspectiveCamera(const PerspectiveCamera& camera);
    PerspectiveCamera(PerspectiveCamera&& camera);
    PerspectiveCamera& operator=(const PerspectiveCamera& camera);
    PerspectiveCamera& operator=(PerspectiveCamera&& camera);

private:
    static float default_fov_y_; // in radians
    static float default_aspect_ratio_;
    float near_clipping_distance_;
    float far_clipping_distance_;
    float fov_y_; // in radians
    float aspect_ratio_;
};
}
#endif
