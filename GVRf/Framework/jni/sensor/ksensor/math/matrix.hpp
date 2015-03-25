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


#pragma once
#include "vector.hpp"

template<typename T>
struct Matrix2 {
    Matrix2() {
        x.x = 1;
        x.y = 0;
        y.x = 0;
        y.y = 1;
    }
    Matrix2(const T* m) {
        x.x = m[0];
        x.y = m[1];
        y.x = m[2];
        y.y = m[3];
    }
    vec2 x;
    vec2 y;
};

template<typename T>
struct Matrix3 {
    Matrix3() {
        x.x = 1;
        x.y = 0;
        x.z = 0;
        y.x = 0;
        y.y = 1;
        y.z = 0;
        z.x = 0;
        z.y = 0;
        z.z = 1;
    }
    Matrix3(const T* m) {
        x.x = m[0];
        x.y = m[1];
        x.z = m[2];
        y.x = m[3];
        y.y = m[4];
        y.z = m[5];
        z.x = m[6];
        z.y = m[7];
        z.z = m[8];
    }
    Matrix3(vec3 x, vec3 y, vec3 z) :
            x(x), y(y), z(z) {
    }
    Matrix3 Transposed() const {
        Matrix3 m;
        m.x.x = x.x;
        m.x.y = y.x;
        m.x.z = z.x;
        m.y.x = x.y;
        m.y.y = y.y;
        m.y.z = z.y;
        m.z.x = x.z;
        m.z.y = y.z;
        m.z.z = z.z;
        return m;
    }
    const T* Pointer() const {
        return &x.x;
    }
    vec3 x;
    vec3 y;
    vec3 z;
};

template<typename T>
struct Matrix4 {
    Matrix4() {
        x.x = 1;
        x.y = 0;
        x.z = 0;
        x.w = 0;
        y.x = 0;
        y.y = 1;
        y.z = 0;
        y.w = 0;
        z.x = 0;
        z.y = 0;
        z.z = 1;
        z.w = 0;
        w.x = 0;
        w.y = 0;
        w.z = 0;
        w.w = 1;
    }
    Matrix4(const Matrix3<T>& m) {
        x.x = m.x.x;
        x.y = m.x.y;
        x.z = m.x.z;
        x.w = 0;
        y.x = m.y.x;
        y.y = m.y.y;
        y.z = m.y.z;
        y.w = 0;
        z.x = m.z.x;
        z.y = m.z.y;
        z.z = m.z.z;
        z.w = 0;
        w.x = 0;
        w.y = 0;
        w.z = 0;
        w.w = 1;
    }
    Matrix4(const T* m) {
        x.x = m[0];
        x.y = m[1];
        x.z = m[2];
        x.w = m[3];
        y.x = m[4];
        y.y = m[5];
        y.z = m[6];
        y.w = m[7];
        z.x = m[8];
        z.y = m[9];
        z.z = m[10];
        z.w = m[11];
        w.x = m[12];
        w.y = m[13];
        w.z = m[14];
        w.w = m[15];
    }
    Matrix4 operator *(const Matrix4& b) const {
        Matrix4 m;
        m.x.x = x.x * b.x.x + x.y * b.y.x + x.z * b.z.x + x.w * b.w.x;
        m.x.y = x.x * b.x.y + x.y * b.y.y + x.z * b.z.y + x.w * b.w.y;
        m.x.z = x.x * b.x.z + x.y * b.y.z + x.z * b.z.z + x.w * b.w.z;
        m.x.w = x.x * b.x.w + x.y * b.y.w + x.z * b.z.w + x.w * b.w.w;
        m.y.x = y.x * b.x.x + y.y * b.y.x + y.z * b.z.x + y.w * b.w.x;
        m.y.y = y.x * b.x.y + y.y * b.y.y + y.z * b.z.y + y.w * b.w.y;
        m.y.z = y.x * b.x.z + y.y * b.y.z + y.z * b.z.z + y.w * b.w.z;
        m.y.w = y.x * b.x.w + y.y * b.y.w + y.z * b.z.w + y.w * b.w.w;
        m.z.x = z.x * b.x.x + z.y * b.y.x + z.z * b.z.x + z.w * b.w.x;
        m.z.y = z.x * b.x.y + z.y * b.y.y + z.z * b.z.y + z.w * b.w.y;
        m.z.z = z.x * b.x.z + z.y * b.y.z + z.z * b.z.z + z.w * b.w.z;
        m.z.w = z.x * b.x.w + z.y * b.y.w + z.z * b.z.w + z.w * b.w.w;
        m.w.x = w.x * b.x.x + w.y * b.y.x + w.z * b.z.x + w.w * b.w.x;
        m.w.y = w.x * b.x.y + w.y * b.y.y + w.z * b.z.y + w.w * b.w.y;
        m.w.z = w.x * b.x.z + w.y * b.y.z + w.z * b.z.z + w.w * b.w.z;
        m.w.w = w.x * b.x.w + w.y * b.y.w + w.z * b.z.w + w.w * b.w.w;
        return m;
    }
    Vector4<T> operator *(const Vector4<T>& b) const {
        Vector4<T> v;
        v.x = x.x * b.x + x.y * b.y + x.z * b.z + x.w * b.w;
        v.y = y.x * b.x + y.y * b.y + y.z * b.z + y.w * b.w;
        v.z = z.x * b.x + z.y * b.y + z.z * b.z + z.w * b.w;
        v.w = w.x * b.x + w.y * b.y + w.z * b.z + w.w * b.w;
        return v;
    }
    Matrix4& operator *=(const Matrix4& b) {
        Matrix4 m = *this * b;
        return (*this = m);
    }
    bool operator ==(const Matrix4& b) const {
        return (x.x == b.x.x && x.y == b.x.y && x.z == b.x.z && x.w == b.x.w
                && y.x == b.y.x && y.y == b.y.y && y.z == b.y.z && y.w == b.y.w
                && z.x == b.z.x && z.y == b.z.y && z.z == b.z.z && z.w == b.z.w
                && w.x == b.w.x && w.y == b.w.y && w.z == b.w.z && w.w == b.w.w);
    }
    Matrix4 Transposed() const {
        Matrix4 m;
        m.x.x = x.x;
        m.x.y = y.x;
        m.x.z = z.x;
        m.x.w = w.x;
        m.y.x = x.y;
        m.y.y = y.y;
        m.y.z = z.y;
        m.y.w = w.y;
        m.z.x = x.z;
        m.z.y = y.z;
        m.z.z = z.z;
        m.z.w = w.z;
        m.w.x = x.w;
        m.w.y = y.w;
        m.w.z = z.w;
        m.w.w = w.w;
        return m;
    }
    Matrix3<T> ToMat3() const {
        Matrix3<T> m;
        m.x.x = x.x;
        m.y.x = y.x;
        m.z.x = z.x;
        m.x.y = x.y;
        m.y.y = y.y;
        m.z.y = z.y;
        m.x.z = x.z;
        m.y.z = y.z;
        m.z.z = z.z;
        return m;
    }

    Matrix4 Inverse() const {

        double Result[4][4];
        float const* src; /* array of transpose source matrix */
        double det, invDet; /* determinant */

        // set src
        src = Transposed().Pointer();

        // computing
        double subfactor01 = src[10] * src[15] - src[11] * src[14];
        double subfactor23 = src[9] * src[15] - src[11] * src[13];
        double subfactor45 = src[9] * src[14] - src[10] * src[13];
        double subfactor67 = src[8] * src[15] - src[11] * src[12];
        double subfactor89 = src[8] * src[14] - src[10] * src[12];
        double subfactor1011 = src[8] * src[13] - src[9] * src[12];

        Result[0][0] = subfactor01 * src[5] - subfactor23 * src[6] + subfactor45 * src[7];
        Result[0][1] = -subfactor01 * src[4] + subfactor67 * src[6] - subfactor89 * src[7];
        Result[0][2] = subfactor23 * src[4] - subfactor67 * src[5] + subfactor1011 * src[7];
        Result[0][3] = -subfactor45 * src[4] + subfactor89 * src[5] - subfactor1011 * src[6];
        Result[1][0] = -subfactor01 * src[1] + subfactor23 * src[2] - subfactor45 * src[3];
        Result[1][1] = subfactor01 * src[0] - subfactor67 * src[2] + subfactor89 * src[3];
        Result[1][2] = -subfactor23 * src[0] + subfactor67 * src[1] - subfactor1011 * src[3];
        Result[1][3] = subfactor45 * src[0] - subfactor89 * src[1] + subfactor1011 * src[2];


        // computing
        subfactor01 = src[2] * src[7] - src[3] * src[6];
        subfactor23 = src[1] * src[7] - src[3] * src[5];
        subfactor45 = src[1] * src[6] - src[2] * src[5];
        subfactor67 = src[0] * src[7] - src[3] * src[4];
        subfactor89 = src[0] * src[6] - src[2] * src[4];
        subfactor1011 = src[0] * src[5] - src[1] * src[4];

        Result[2][0] = subfactor01 * src[13] - subfactor23 * src[14] + subfactor45 * src[15];
        Result[2][1] = -subfactor01 * src[12] + subfactor67 * src[14] - subfactor89 * src[15];
        Result[2][2] = subfactor23 * src[12] - subfactor67 * src[13] + subfactor1011 * src[15];
        Result[2][3] = -subfactor45 * src[12] + subfactor89 * src[13] - subfactor1011 * src[14];
        Result[3][0] =  - subfactor01 * src[9] + subfactor23 * src[10] - subfactor45 * src[11];
        Result[3][1] = subfactor01 * src[8] - subfactor67 * src[10] + subfactor89 * src[11];
        Result[3][2] = - subfactor23 * src[8] + subfactor67 * src[9] - subfactor1011 * src[11];
        Result[3][3] = subfactor45 * src[8] - subfactor89 * src[9] + subfactor1011 * src[10];

        // to calculate final inverse matrix
        det = Result[0][0] * src[0] + Result[0][1] * src[1] +
              Result[0][2] * src[2] + Result[0][3] * src[3];
        invDet = 1.0f / det;

        Matrix4 inverse;
        float* fmat = (float*) inverse.Pointer();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                fmat[i * 4 + j] = float(Result[i][j] * invDet);
            }
        }

        return inverse;
    }

    const T* Pointer() const {
        return &x.x;
    }

    static Matrix4<T> Identity() {
        return Matrix4();
    }

    static Matrix4<T> Zeros() {
        Matrix4 m;
        m.x.x = 0;
        m.x.y = 0;
        m.x.z = 0;
        m.x.w = 0;
        m.y.x = 0;
        m.y.y = 0;
        m.y.z = 0;
        m.y.w = 0;
        m.z.x = 0;
        m.z.y = 0;
        m.z.z = 0;
        m.z.w = 0;
        m.w.x = 0;
        m.w.y = 0;
        m.w.z = 0;
        m.w.w = 0;
        return m;
    }
    static Matrix4<T> Translate(const Vector3<T>& v) {
        Matrix4 m;
        m.x.x = 1;
        m.x.y = 0;
        m.x.z = 0;
        m.x.w = 0;
        m.y.x = 0;
        m.y.y = 1;
        m.y.z = 0;
        m.y.w = 0;
        m.z.x = 0;
        m.z.y = 0;
        m.z.z = 1;
        m.z.w = 0;
        m.w.x = v.x;
        m.w.y = v.y;
        m.w.z = v.z;
        m.w.w = 1;
        return m.Transposed();
    }
    static Matrix4<T> Translate(T x, T y, T z) {
        Matrix4 m;
        m.x.x = 1;
        m.x.y = 0;
        m.x.z = 0;
        m.x.w = 0;
        m.y.x = 0;
        m.y.y = 1;
        m.y.z = 0;
        m.y.w = 0;
        m.z.x = 0;
        m.z.y = 0;
        m.z.z = 1;
        m.z.w = 0;
        m.w.x = x;
        m.w.y = y;
        m.w.z = z;
        m.w.w = 1;
        return m.Transposed();
    }
    static Matrix4<T> Scale(T s) {
        Matrix4 m;
        m.x.x = s;
        m.x.y = 0;
        m.x.z = 0;
        m.x.w = 0;
        m.y.x = 0;
        m.y.y = s;
        m.y.z = 0;
        m.y.w = 0;
        m.z.x = 0;
        m.z.y = 0;
        m.z.z = s;
        m.z.w = 0;
        m.w.x = 0;
        m.w.y = 0;
        m.w.z = 0;
        m.w.w = 1;
        return m;
    }
    static Matrix4<T> Scale(T x, T y, T z) {
        Matrix4 m;
        m.x.x = x;
        m.x.y = 0;
        m.x.z = 0;
        m.x.w = 0;
        m.y.x = 0;
        m.y.y = y;
        m.y.z = 0;
        m.y.w = 0;
        m.z.x = 0;
        m.z.y = 0;
        m.z.z = z;
        m.z.w = 0;
        m.w.x = 0;
        m.w.y = 0;
        m.w.z = 0;
        m.w.w = 1;
        return m;
    }
    static Matrix4<T> Scale(const Vector3<T>& v) {
        Matrix4 m;
        m.x.x = v.x;
        m.x.y = 0;
        m.x.z = 0;
        m.x.w = 0;
        m.y.x = 0;
        m.y.y = v.y;
        m.y.z = 0;
        m.y.w = 0;
        m.z.x = 0;
        m.z.y = 0;
        m.z.z = v.z;
        m.z.w = 0;
        m.w.x = 0;
        m.w.y = 0;
        m.w.z = 0;
        m.w.w = 1;
        return m;
    }
    static Matrix4<T> Rotate(T degrees) {
        T radians = degrees * 3.14159f / 180.0f;
        T s = std::sin(radians);
        T c = std::cos(radians);

        Matrix4 m = Identity();
        m.x.x = c;
        m.x.y = s;
        m.y.x = -s;
        m.y.y = c;
        return m;
    }
    static Matrix4<T> Rotate(T radians, const vec3& axis) {
        T s = std::sin(radians);
        T c = std::cos(radians);

        Matrix4 m = Identity();
        m.x.x = c + (1 - c) * axis.x * axis.x;
        m.x.y = (1 - c) * axis.x * axis.y - axis.z * s;
        m.x.z = (1 - c) * axis.x * axis.z + axis.y * s;
        m.y.x = (1 - c) * axis.x * axis.y + axis.z * s;
        m.y.y = c + (1 - c) * axis.y * axis.y;
        m.y.z = (1 - c) * axis.y * axis.z - axis.x * s;
        m.z.x = (1 - c) * axis.x * axis.z - axis.y * s;
        m.z.y = (1 - c) * axis.y * axis.z + axis.x * s;
        m.z.z = c + (1 - c) * axis.z * axis.z;
        return m;
    }
    static Matrix4<T> RotateX(T degrees) {
        T radians = degrees * 3.14159f / 180.0f;
        T s = std::sin(radians);
        T c = std::cos(radians);
        Matrix4 m;
        m.x.x = 1;
        m.x.y = 0;
        m.x.z = 0;
        m.x.w = 0;
        m.y.x = 0;
        m.y.y = c;
        m.y.z = s;
        m.y.w = 0;
        m.z.x = 0;
        m.z.y = -s;
        m.z.z = c;
        m.z.w = 0;
        m.w.x = 0;
        m.w.y = 0;
        m.w.z = 0;
        m.w.w = 1;
        return m;
    }
    static Matrix4<T> RotateY(T degrees) {
        T radians = degrees * 3.14159f / 180.0f;
        T s = std::sin(radians);
        T c = std::cos(radians);
        Matrix4 m;
        m.x.x = c;
        m.x.y = 0;
        m.x.z = -s;
        m.x.w = 0;
        m.y.x = 0;
        m.y.y = 1;
        m.y.z = 0;
        m.y.w = 0;
        m.z.x = s;
        m.z.y = 0;
        m.z.z = c;
        m.z.w = 0;
        m.w.x = 0;
        m.w.y = 0;
        m.w.z = 0;
        m.w.w = 1;
        return m;
    }
    static Matrix4<T> RotateZ(T degrees) {
        T radians = degrees * 3.14159f / 180.0f;
        T s = std::sin(radians);
        T c = std::cos(radians);
        Matrix4 m;
        m.x.x = c;
        m.x.y = s;
        m.x.z = 0;
        m.x.w = 0;
        m.y.x = -s;
        m.y.y = c;
        m.y.z = 0;
        m.y.w = 0;
        m.z.x = 0;
        m.z.y = 0;
        m.z.z = 1;
        m.z.w = 0;
        m.w.x = 0;
        m.w.y = 0;
        m.w.z = 0;
        m.w.w = 1;
        return m;
    }
    static Matrix4<T> Ortho(T left, T right, T bottom, T top, T near, T far) {
        T a = 2.0f / (right - left);
        T b = 2.0f / (top - bottom);
        T c = -2.0f / (far - near);
        T tx = (right + left) / (right - left);
        T ty = (top + bottom) / (top - bottom);
        T tz = (far + near) / (far - near);
        Matrix4 m;
        m.x.x = a;
        m.x.y = 0;
        m.x.z = 0;
        m.x.w = tx;
        m.y.x = 0;
        m.y.y = b;
        m.y.z = 0;
        m.y.w = ty;
        m.z.x = 0;
        m.z.y = 0;
        m.z.z = c;
        m.z.w = tz;
        m.w.x = 0;
        m.w.y = 0;
        m.w.z = 0;
        m.w.w = 1;
        return m;
    }

    static Matrix4<T> LookAt(const Vector3<T>& eye, const Vector3<T>& target,
            const Vector3<T>& up) {
        Vector3<T> z = (eye - target).Normalized();
        Vector3<T> x = up.Cross(z).Normalized();
        Vector3<T> y = z.Cross(x).Normalized();

        Matrix4<T> m;
        m.x = Vector4<T>(x, 0);
        m.y = Vector4<T>(y, 0);
        m.z = Vector4<T>(z, 0);
        m.w = Vector4<T>(0, 0, 0, 1);

        Vector4<T> eyePrime = m * Vector4<T>(-eye, 1);
        m = m.Transposed();
        m.w = eyePrime;

        return m;
    }
    static Matrix4<T> LookAtWithVector(const Vector3<T>& eye,
            const Vector3<T>& lookVector, const Vector3<T>& up) {
        Vector3<T> z = lookVector.Normalized();
        Vector3<T> x = up.Cross(z).Normalized();
        Vector3<T> y = z.Cross(x).Normalized();

        Matrix4<T> m;
        m.x = Vector4<T>(x, 0);
        m.y = Vector4<T>(y, 0);
        m.z = Vector4<T>(z, 0);
        m.w = Vector4<T>(0, 0, 0, 1);

        Vector4<T> eyePrime = m * Vector4<T>(-eye, 1);
        m = m.Transposed();
        m.w = eyePrime;

        return m;
    }
    vec4 x;
    vec4 y;
    vec4 z;
    vec4 w;
};

typedef Matrix2<float> mat2;
typedef Matrix3<float> mat3;
typedef Matrix4<float> mat4;
