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
#include <cmath>

const float Pi = 4 * std::atan(1.0f);
const float TwoPi = 2 * Pi;

template<typename T>
struct Vector2 {
    Vector2() {
    }
    Vector2(T x, T y) :
            x(x), y(y) {
    }
    T Dot(const Vector2& v) const {
        return x * v.x + y * v.y;
    }
    Vector2 operator+(const Vector2& v) const {
        return Vector2(x + v.x, y + v.y);
    }
    Vector2 operator-(const Vector2& v) const {
        return Vector2(x - v.x, y - v.y);
    }
    void operator+=(const Vector2& v) {
        *this = Vector2(x + v.x, y + v.y);
    }
    void operator-=(const Vector2& v) {
        *this = Vector2(x - v.x, y - v.y);
    }
    Vector2 operator/(float s) const {
        return Vector2(x / s, y / s);
    }
    Vector2 operator*(float s) const {
        return Vector2(x * s, y * s);
    }
    void operator/=(float s) {
        *this = Vector2(x / s, y / s);
    }
    void operator*=(float s) {
        *this = Vector2(x * s, y * s);
    }
    void Normalize() {
        float s = 1.0f / Length();
        x *= s;
        y *= s;
    }
    Vector2 Normalized() const {
        Vector2 v = *this;
        v.Normalize();
        return v;
    }
    T LengthSquared() const {
        return x * x + y * y;
    }
    T Length() const {
        return sqrt(LengthSquared());
    }
    const T* Pointer() const {
        return &x;
    }
    operator Vector2<float>() const {
        return Vector2<float>(x, y);
    }
    bool operator==(const Vector2& v) const {
        return x == v.x && y == v.y;
    }
    Vector2 Lerp(float t, const Vector2& v) const {
        return Vector2(x * (1 - t) + v.x * t, y * (1 - t) + v.y * t);
    }
    template<typename P>
    P* Write(P* pData) {
        Vector2* pVector = (Vector2*) pData;
        *pVector++ = *this;
        return (P*) pVector;
    }
    T x;
    T y;
};

template<typename T>
struct Vector3 {
    Vector3() {
    }
    Vector3(T x, T y, T z) :
            x(x), y(y), z(z) {
    }
    Vector3(const Vector2<T>& v, T k) :
            x(v.x), y(v.y), z(k) {
    }
    T Length() const {
        return std::sqrt(x * x + y * y + z * z);
    }
    void Normalize() {
        float s = 1.0f / Length();
        x *= s;
        y *= s;
        z *= s;
    }
    Vector3 Normalized() const {
        Vector3 v = *this;
        v.Normalize();
        return v;
    }
    Vector3 Cross(const Vector3& v) const {
        return Vector3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }
    T Dot(const Vector3& v) const {
        return x * v.x + y * v.y + z * v.z;
    }

    T Angle(const Vector3& v) const {
        T div = Length() * v.Length();
        T result = acos((this->Dot(v)) / div);
        return result;
    }

    Vector3 operator+(const Vector3& v) const {
        return Vector3(x + v.x, y + v.y, z + v.z);
    }
    void operator+=(const Vector3& v) {
        x += v.x;
        y += v.y;
        z += v.z;
    }
    void operator-=(const Vector3& v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
    }
    void operator/=(T s) {
        x /= s;
        y /= s;
        z /= s;
    }
    Vector3 operator-(const Vector3& v) const {
        return Vector3(x - v.x, y - v.y, z - v.z);
    }
    Vector3 operator-() const {
        return Vector3(-x, -y, -z);
    }
    Vector3 operator*(T s) const {
        return Vector3(x * s, y * s, z * s);
    }
    Vector3 operator/(T s) const {
        return Vector3(x / s, y / s, z / s);
    }
    bool operator==(const Vector3& v) const {
        return x == v.x && y == v.y && z == v.z;
    }
    Vector3 Lerp(float t, const Vector3& v) const {
        return Vector3(x * (1 - t) + v.x * t, y * (1 - t) + v.y * t,
                z * (1 - t) + v.z * t);
    }
    const T* Pointer() const {
        return &x;
    }
    template<typename P>
    P* Write(P* pData) {
        Vector3<T>* pVector = (Vector3<T>*) pData;
        *pVector++ = *this;
        return (P*) pVector;
    }
    T x;
    T y;
    T z;
};

template<typename T>
struct Vector4 {
    Vector4() {
    }
    Vector4(T x, T y, T z, T w) :
            x(x), y(y), z(z), w(w) {
    }
    Vector4(const Vector2<T>& v, T k, T w) :
            x(v.x), y(v.y), z(k), w(w) {
    }
    Vector4(const Vector3<T>& v, T w) :
            x(v.x), y(v.y), z(v.z), w(w) {
    }
    T Dot(const Vector4& v) const {
        return x * v.x + y * v.y + z * v.z + w * v.w;
    }
    Vector4 operator+(const Vector4& v) const {
        return Vector4(x + v.x, y + v.y, z + v.z, w + v.w);
    }
    Vector4 operator*(T s) const {
        return Vector4(x * s, y * s, z * s, w * s);
    }
    Vector4 Lerp(float t, const Vector4& v) const {
        return Vector4(x * (1 - t) + v.x * t, y * (1 - t) + v.y * t,
                z * (1 - t) + v.z * t, w * (1 - t) + v.w * t);
    }
    Vector3<T> ToVector3() const {
        return Vector3<T>(x, y, z);
    }
    const T* Pointer() const {
        return &x;
    }
    T x;
    T y;
    T z;
    T w;
};

typedef Vector2<bool> bvec2;

typedef Vector2<int> ivec2;
typedef Vector3<int> ivec3;
typedef Vector4<int> ivec4;

typedef Vector2<float> vec2;
typedef Vector3<float> vec3;
typedef Vector4<float> vec4;
