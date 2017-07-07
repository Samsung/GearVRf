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
 * Represents a physics 2D or 3D world
 ***************************************************************************/

#ifndef PHYSICS_COMMON_H_
#define PHYSICS_COMMON_H_

namespace gvr {

    union PhysicsVec3 {
        float vec[3];
        struct {
            float x, y, z;
        };

        void set(float const v[]) {
            x = v[0];
            y = v[1];
            z = v[2];
        }

        void set(float x, float y, float z) {
            this->x = x;
            this->y = y;
            this->z = z;
        }

        PhysicsVec3() : x(0), y(0), z(0) {}
        PhysicsVec3(float _x, float _y, float _z) : x(_x), y(_y), z(_z) {}
        PhysicsVec3(float const v[]) : x(v[0]), y(v[1]), z(v[2]) {}
    };

    union PhysicsQuat {
        float q[4];
        struct {
            float x, y, z, w;
        };

        void set(float const q[]) {
            x = q[0];
            y = q[1];
            z = q[2];
            w = q[3];
        }

        void set(float x, float y, float z, float w) {
            this->x = x;
            this->y = y;
            this->z = z;
            this->w = w;
        }

        PhysicsQuat() : x(0), y(0), z(0), w(0) {}
        PhysicsQuat(float _x, float _y, float _z, float _w) : x(_x), y(_y), z(_z), w(_w) {}
        PhysicsQuat(float const q[]) : x(q[0]), y(q[1]), z(q[2]), w(q[3]) {}
    };

    union PhysicsMat3x3 {
        float vec[9];
        float mat[3][3];

        void set(float const v[]) {
            vec[0] = v[0];
            vec[1] = v[1];
            vec[2] = v[2];
            vec[3] = v[3];
            vec[4] = v[4];
            vec[5] = v[5];
            vec[6] = v[6];
            vec[7] = v[7];
            vec[8] = v[8];
        }

        PhysicsMat3x3() {}
        PhysicsMat3x3(float const m[]) {
            vec[0] = m[0];
            vec[1] = m[1];
            vec[2] = m[2];
            vec[3] = m[3];
            vec[4] = m[4];
            vec[5] = m[5];
            vec[6] = m[6];
            vec[7] = m[7];
            vec[8] = m[8];
        }
    };
}

#endif /* PHYSICS_COMMON_H_ */
