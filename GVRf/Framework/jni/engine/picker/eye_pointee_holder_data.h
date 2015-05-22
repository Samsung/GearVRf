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
 * Picker's helper class.
 ***************************************************************************/

#ifndef EYE_POINTEE_HOLDER_DATA_H_
#define EYE_POINTEE_HOLDER_DATA_H_

namespace gvr {

class EyePointeeHolderData {
public:
    EyePointeeHolderData(EyePointeeHolder* holder, float distance) :
            eye_pointee_holder_(holder), distance_(distance) {
    }

    EyePointeeHolderData(EyePointeeHolderData&& eye_pointee_holder_data) :
            eye_pointee_holder_(
                    std::move(eye_pointee_holder_data.eye_pointee_holder_)), distance_(
                    eye_pointee_holder_data.distance_) {
    }

    ~EyePointeeHolderData() {
    }

    EyePointeeHolder* eye_pointee_holder() const {
        return eye_pointee_holder_;
    }

    float distance() const {
        return distance_;
    }

    EyePointeeHolderData& operator=(
            EyePointeeHolderData&& eye_pointee_holder_data) {
        eye_pointee_holder_ = eye_pointee_holder_data.eye_pointee_holder_;
        distance_ = eye_pointee_holder_data.distance_;

        return *this;
    }

private:
    EyePointeeHolderData(const EyePointeeHolderData& eye_pointee_holder_data);
    EyePointeeHolderData& operator=(
            const EyePointeeHolderData& eye_pointee_holder_data);

private:
    EyePointeeHolder* eye_pointee_holder_;
    float distance_;
};

inline bool compareEyePointeeHolderData(const EyePointeeHolderData& i,
        const EyePointeeHolderData& j) {
    return i.distance() < j.distance();
}

}

#endif
