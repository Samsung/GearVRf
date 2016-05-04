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
 * Result data of eye pointing test.
 ***************************************************************************/

#ifndef EYE_POINT_DATA_H_
#define EYE_POINT_DATA_H_

#include <limits>
#include "glm/gtc/type_ptr.hpp"

namespace gvr {

class EyePointData {
public:
    EyePointData() :
            pointed_(false), distance_(std::numeric_limits<float>::infinity()), hit_(
                    std::numeric_limits<float>::infinity()) {
    }

    EyePointData(EyePointData&& eye_point_data) :
            pointed_(eye_point_data.pointed_), distance_(
                    eye_point_data.distance_), hit_(eye_point_data.hit_) {
    }

    ~EyePointData() {
    }

    bool pointed() const {
        return pointed_;
    }

    float distance() const {
        return distance_;
    }

    const glm::vec3& hit() const {
        return hit_;
    }

    void setDistance(float distance) {
        pointed_ = true;
        distance_ = distance;
    }

    void setHit(const glm::vec3 &hit) {
        hit_ = hit;
    }

    EyePointData& operator=(const EyePointData& eye_point_data) {
        pointed_ = eye_point_data.pointed_;
        distance_ = eye_point_data.distance_;
        hit_ = eye_point_data.hit_;

        return *this;
    }

private:
    EyePointData(const EyePointData& eye_point_data);
    EyePointData& operator=(EyePointData&& eye_point_data);

private:
    bool pointed_;
    float distance_;
    glm::vec3 hit_;
};

}

#endif
