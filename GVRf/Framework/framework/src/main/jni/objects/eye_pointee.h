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
 * Can be picked by the picker.
 ***************************************************************************/

#ifndef EYE_POINTEE_H_
#define EYE_POINTEE_H_

#include "glm/glm.hpp"

#include "engine/picker/eye_point_data.h"
#include "objects/hybrid_object.h"

namespace gvr {

class EyePointee: public HybridObject {
public:
    EyePointee() {
    }

    virtual ~EyePointee() {
    }

    virtual EyePointData isPointed(const glm::mat4& mv_matrix) = 0;
    virtual EyePointData isPointed(const glm::mat4& mv_matrix, float ox,
            float oy, float oz, float dx, float dy, float dz) = 0;

private:
    EyePointee(const EyePointee& eye_pointee);
    EyePointee(EyePointee&& eye_pointee);
    EyePointee& operator=(const EyePointee& eye_pointee);
    EyePointee& operator=(EyePointee&& eye_pointee);
};
}
#endif
