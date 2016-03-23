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

#include "objects/components/transform.h"

#include "glm/glm.hpp"
#include "glm/geometric.hpp"

namespace gvr {
class BoundingVolume {
public:
    BoundingVolume();

    ~BoundingVolume() {
    }

    const glm::vec3& center() const {
        return center_;
    }
    float radius() const {
        return radius_;
    }
    const glm::vec3& min_corner() const {
        return min_corner_;
    }
    const glm::vec3& max_corner() const {
        return max_corner_;
    }

    void reset();
    void expand(const glm::vec3 point);
    void expand(const BoundingVolume &volume);
    void expand(const glm::vec3 &in_center, float in_radius);
    void transform(const BoundingVolume &volume, glm::mat4 matrix);

private:
    glm::vec3 center_;
    float radius_ = 0.0f;
    glm::vec3 min_corner_;
    glm::vec3 max_corner_;
};
}
#endif
