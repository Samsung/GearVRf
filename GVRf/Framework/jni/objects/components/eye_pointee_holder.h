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
 * Can hold eye pointees and attached to a scene object.
 ***************************************************************************/

#ifndef EYE_POINTEE_HOLDER_H_
#define EYE_POINTEE_HOLDER_H_

#include <algorithm>
#include <memory>
#include <vector>

#include "glm/glm.hpp"

#include "engine/picker/eye_point_data.h"
#include "objects/components/component.h"

namespace gvr {
class EyePointee;

class EyePointeeHolder: public Component {
public:
    EyePointeeHolder();
    ~EyePointeeHolder();

    bool enable() const {
        return enable_;
    }

    void set_enable(bool enable) {
        enable_ = enable;
    }

    const glm::vec3& hit() const {
        return hit_;
    }

    void set_hit(const glm::vec3& hit) {
        hit_ = hit;
    }

    void addPointee(const std::shared_ptr<EyePointee>& pointee);
    void removePointee(const std::shared_ptr<EyePointee>& pointee);
    EyePointData isPointed(const glm::mat4& view_matrix);
    EyePointData isPointed(const glm::mat4& view_matrix, float ox, float oy,
            float oz, float dx, float dy, float dz);

private:
    EyePointeeHolder(const EyePointeeHolder& eye_pointee_holder);
    EyePointeeHolder(EyePointeeHolder&& eye_pointee_holder);
    EyePointeeHolder& operator=(const EyePointeeHolder& eye_pointee_holder);
    EyePointeeHolder& operator=(EyePointeeHolder&& eye_pointee_holder);

private:
    bool enable_;
    glm::vec3 hit_;
    std::vector<std::shared_ptr<EyePointee>> pointees_;
};

}

#endif
