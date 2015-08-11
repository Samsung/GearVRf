
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
 * The bounding_volume for rendering.
 ***************************************************************************/

#ifndef BOUNDING_VOLUME_H_
#define BOUNDING_VOLUME_H_

#include <map>
#include <memory>
#include <vector>
#include <string>

#include "glm/glm.hpp"


namespace gvr {
class BoundingVolume: {
public:
    BoundingVolume() {
    }

    ~BoundingVolume() {
    }

    void expand(const glm::vec3 point) {
        if(min_corner_[0] > point[0]) {
            min_corner_[0] = point[0];
        }
        if(min_corner_[1] > point[1]) {
            min_corner_[1] = point[1];
        }
        if(min_corner_[2] > point[2]) {
            min_corner_[2] = point[2];
        }

        if(max_corner_[0] < point[0]) {
            max_corner_[0] = point[0];
        }
        if(max_corner_[1] < point[1]) {
            max_corner_[1] = point[1];
        }
        if(max_corner_[2] < point[2]) {
            max_corner_[2] = point[2];
        }

        center_ = (min_corner_ + max_corner_)*0.5f;
        radius_ = (max_corner_ - center_).length();
    }

    void expand(const BoundingVolume &volume) {
        float in_center = volume.center();
        float in_radius = volume.radius();

        glm::vec3 v = in_center - center_;
        float length = v.length();

        if(length == 0 && in_radius > radius_){
            radius_ = in_radius;
        } else if((length + in_radius)> radius_) {
            v.normalize();
            glm::vec3 c1 = in_center + (v * in_radius);
            glm::vec3 c0 = center_ - (v * radius_);
            center_ = (c0 + c1) * 0.5f;
            radius_ = (c1 - c0).length() * 0.5f;
        }

        float s = (float) sqrt((radius/3.0f));
        leftBottomNear.set(center_[0] - s,
                           center_[0] - s,
                           center_[0] - s);

        max_corner_.set(center_[0] + s,
                        center_[0] + s,
                        center_[0] + s);
    }

    glm::vec3& center() { return center_; }
    glm::vec3& radius() { return radius_; }
    glm::vec3& min_corner() { return min_corner_; }
    glm::vec3& max_corner() { return max_corner_; }

private:
    // bounding volume info
    bool dirty = true;

    glm::vec3 center_(0.0f, 0.0f, 0.0f);
    float radius_ = 0.0f;
    glm::vec3 min_corner_(0.0f, 0.0f, 0.0f);
    glm::vec3 max_corner_(0.0f, 0.0f, 0.0f);
};
}
#endif
