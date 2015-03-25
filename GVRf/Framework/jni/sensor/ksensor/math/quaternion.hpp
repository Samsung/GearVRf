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
#include "matrix.hpp"

template<typename T>
struct QuaternionT {
    T x;
    T y;
    T z;
    T w;

    QuaternionT();
    QuaternionT(T x, T y, T z, T w);
    QuaternionT(T w, Vector3<T> v);

    Vector3<T> Imag() const;
    QuaternionT<T> Slerp(T mu, const QuaternionT<T>& q) const;
    QuaternionT<T> Rotated(const QuaternionT<T>& b) const;
    QuaternionT<T> Multiplied(const QuaternionT<T>& q2) const;
    QuaternionT<T> Scaled(T scale) const;
    QuaternionT<T> Inverted() const;
    T Dot(const QuaternionT<T>& q) const;
    Matrix3<T> ToMatrix() const;
    Vector4<T> ToVector() const;
    QuaternionT<T> operator=(const QuaternionT<T>& q);
    QuaternionT<T> operator-(const QuaternionT<T>& q) const;
    QuaternionT<T> operator+(const QuaternionT<T>& q) const;
    QuaternionT<T> operator*(const QuaternionT<T>& q) const;
    bool operator==(const QuaternionT<T>& q) const;
    bool operator!=(const QuaternionT<T>& q) const;

    void Normalize();
    void Rotate(const QuaternionT<T>& q);
    void Conjugate();
    Vector3<T> Rotate(const Vector3<T>& v) const;
    Vector3<T> ToEulerAngle() const;

    static QuaternionT<T> CreateFromVectors(const Vector3<T>& v0,
            const Vector3<T>& v1);
    static QuaternionT<T> CreateFromAxisAngle(const Vector3<T>& axis,
            float radians);
    static QuaternionT<T> CreateFromEulerAngle(const Vector3<T>& axis);
};

template<typename T>
inline QuaternionT<T>::QuaternionT() :
        x(0), y(0), z(0), w(1) {
}

template<typename T>
inline QuaternionT<T>::QuaternionT(T x, T y, T z, T w) :
        x(x), y(y), z(z), w(w) {
}

template<typename T>
inline QuaternionT<T>::QuaternionT(T w, Vector3<T> v) :
        x(v.x), y(v.y), z(v.z), w(w) {
}

template<typename T>
inline Vector3<T> QuaternionT<T>::Imag() const {
    return Vector3<T>(x, y, z);
}

// Ken Shoemake's famous method.
template<typename T>
inline QuaternionT<T> QuaternionT<T>::Slerp(T t,
        const QuaternionT<T>& v1) const {
    const T epsilon = 0.0005f;
    T dot = Dot(v1);

    if (dot > 1 - epsilon) {
        QuaternionT<T> result = v1 + (*this - v1).Scaled(t);
        result.Normalize();
        return result;
    }

    if (dot < 0)
        dot = 0;

    if (dot > 1)
        dot = 1;

    T theta0 = std::acos(dot);
    T theta = theta0 * t;

    QuaternionT<T> v2 = (v1 - Scaled(dot));
    v2.Normalize();

    QuaternionT<T> q = Scaled(std::cos(theta)) + v2.Scaled(std::sin(theta));
    q.Normalize();
    return q;
}

template<typename T>
inline QuaternionT<T> QuaternionT<T>::Rotated(const QuaternionT<T>& b) const {
    QuaternionT<T> q;
    q.w = w * b.w - x * b.x - y * b.y - z * b.z;
    q.x = w * b.x + x * b.w + y * b.z - z * b.y;
    q.y = w * b.y + y * b.w + z * b.x - x * b.z;
    q.z = w * b.z + z * b.w + x * b.y - y * b.x;
    q.Normalize();
    return q;
}

template<typename T>
inline QuaternionT<T> QuaternionT<T>::Multiplied(
        const QuaternionT<T>& q2) const {
    QuaternionT<T> res;
    T A, B, C, D, E, F, G, H;

    A = (w + x) * (q2.w + q2.x);

    B = (z - y) * (q2.y - q2.z);

    C = (w - x) * (q2.y + q2.z);

    D = (y + z) * (q2.w - q2.x);

    E = (x + z) * (q2.x + q2.y);

    F = (x - z) * (q2.x - q2.y);

    G = (w + y) * (q2.w - q2.z);

    H = (w - y) * (q2.w + q2.z);

    res.w = B + (-E - F + G + H) / 2;

    res.x = A - (E + F + G + H) / 2;

    res.y = C + (E - F + G - H) / 2;

    res.z = D + (E - F - G + H) / 2;

    return res;
}

template<typename T>
inline QuaternionT<T> QuaternionT<T>::Scaled(T s) const {
    return QuaternionT<T>(x * s, y * s, z * s, w * s);
}

template<typename T>
inline QuaternionT<T> QuaternionT<T>::Inverted() const {
    return QuaternionT<T>(-x, -y, -z, w);
}

template<typename T>
inline T QuaternionT<T>::Dot(const QuaternionT<T>& q) const {
    return x * q.x + y * q.y + z * q.z + w * q.w;
}

template<typename T>
inline Matrix3<T> QuaternionT<T>::ToMatrix() const {
    const T s = 2;
    T xs, ys, zs;
    T wx, wy, wz;
    T xx, xy, xz;
    T yy, yz, zz;
    xs = x * s;
    ys = y * s;
    zs = z * s;
    wx = w * xs;
    wy = w * ys;
    wz = w * zs;
    xx = x * xs;
    xy = x * ys;
    xz = x * zs;
    yy = y * ys;
    yz = y * zs;
    zz = z * zs;
    Matrix3<T> m;
    m.x.x = 1 - (yy + zz);
    m.y.x = xy - wz;
    m.z.x = xz + wy;
    m.x.y = xy + wz;
    m.y.y = 1 - (xx + zz);
    m.z.y = yz - wx;
    m.x.z = xz - wy;
    m.y.z = yz + wx;
    m.z.z = 1 - (xx + yy);
    return m;
}

template<typename T>
inline Vector4<T> QuaternionT<T>::ToVector() const {
    return Vector4<T>(x, y, z, w);
}
template<typename T>
QuaternionT<T> QuaternionT<T>::operator=(const QuaternionT<T>& q) {
    this->x = q.x;
    this->y = q.y;
    this->z = q.z;
    this->w = q.w;
    return *this;
}

template<typename T>
QuaternionT<T> QuaternionT<T>::operator-(const QuaternionT<T>& q) const {
    return QuaternionT<T>(x - q.x, y - q.y, z - q.z, w - q.w);
}

template<typename T>
QuaternionT<T> QuaternionT<T>::operator+(const QuaternionT<T>& q) const {
    return QuaternionT<T>(x + q.x, y + q.y, z + q.z, w + q.w);
}

template<typename T>
QuaternionT<T> QuaternionT<T>::operator*(const QuaternionT<T>& q) const {
    return QuaternionT<T>(w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y - x * q.z + y * q.w + z * q.x,
            w * q.z + x * q.y - y * q.x + z * q.w,
            w * q.w - x * q.x - y * q.y - z * q.z);
}

template<typename T>
bool QuaternionT<T>::operator==(const QuaternionT<T>& q) const {
    return x == q.x && y == q.y && z == q.z && w == q.w;
}

template<typename T>
bool QuaternionT<T>::operator!=(const QuaternionT<T>& q) const {
    return !(*this == q);
}

// Compute the quaternion that rotates from a to b, avoiding numerical instability.
// Taken from "The Shortest Arc Quaternion" by Stan Melax in "Game Programming Gems".
template<typename T>
inline QuaternionT<T> QuaternionT<T>::CreateFromVectors(const Vector3<T>& v0,
        const Vector3<T>& v1) {
    if (v0 == -v1)
        return QuaternionT<T>::CreateFromAxisAngle(vec3(1, 0, 0), Pi);

    Vector3<T> c = v0.Cross(v1);
    T d = v0.Dot(v1);
    T s = std::sqrt((1 + d) * 2);

    QuaternionT<T> q;
    q.x = c.x / s;
    q.y = c.y / s;
    q.z = c.z / s;
    q.w = s / 2.0f;
    return q;
}

template<typename T>
inline QuaternionT<T> QuaternionT<T>::CreateFromAxisAngle(
        const Vector3<T>& axis, float radians) {
    QuaternionT<T> q;
    q.w = std::cos(radians / 2);
    q.x = q.y = q.z = std::sin(radians / 2);
    q.x *= axis.x;
    q.y *= axis.y;
    q.z *= axis.z;
//    q.Normalize();
    return q;
}

template<typename T>
inline void QuaternionT<T>::Normalize() {
    *this = Scaled(1 / std::sqrt(Dot(*this)));
}

template<typename T>
inline void QuaternionT<T>::Rotate(const QuaternionT<T>& q2) {
    QuaternionT<T> q;
    QuaternionT<T>& q1 = *this;

    q.w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;
    q.x = q1.w * q2.x + q1.x * q2.w + q1.y * q2.z - q1.z * q2.y;
    q.y = q1.w * q2.y + q1.y * q2.w + q1.z * q2.x - q1.x * q2.z;
    q.z = q1.w * q2.z + q1.z * q2.w + q1.x * q2.y - q1.y * q2.x;

    q.Normalize();
    *this = q;
}

template<typename T>
inline Vector3<T> QuaternionT<T>::ToEulerAngle() const {
    Vector3<T> v;

    T Q[3] = { x, y, z };  //Quaternion components x,y,z

    int A1 = 0;
    int A2 = 1;
    int A3 = 2;

    T ww = w * w;
    T Q11 = Q[A1] * Q[A1];
    T Q22 = Q[A2] * Q[A2];
    T Q33 = Q[A3] * Q[A3];

    T psign = T(-1);
    // Determine whether even permutation
    if (((A1 + 1) % 3 == A2) && ((A2 + 1) % 3 == A3))
        psign = T(1);

    T s2 = psign * T(2) * (psign * w * Q[A2] + Q[A1] * Q[A3]);

    if (s2 < T(-1) + 0.000000000001) { // South pole singularity
        v.x = T(0);
        v.y = -atan(1.0) * 2;
        v.z = atan2(T(2) * (psign * Q[A1] * Q[A2] + w * Q[A3]),
                ww + Q22 - Q11 - Q33);
    } else if (s2 > T(1) - 0.000000000001) {  // North pole singularity
        v.x = T(0);
        v.y = atan(1.0) * 2;
        v.z = atan2(T(2) * (psign * Q[A1] * Q[A2] + w * Q[A3]),
                ww + Q22 - Q11 - Q33);
    } else {
        v.x = -atan2(T(-2) * (w * Q[A1] - psign * Q[A2] * Q[A3]),
                ww + Q33 - Q11 - Q22);
        v.y = asin(s2);
        v.z = atan2(T(2) * (w * Q[A3] - psign * Q[A1] * Q[A2]),
                ww + Q11 - Q22 - Q33);
    }
    return v;
}

template<typename T>
inline Vector3<T> QuaternionT<T>::Rotate(const Vector3<T>& v) const {
    return ((*this * QuaternionT<T>(v.x, v.y, v.z, T(0))) * Inverted()).Imag();
}

template<typename T>
inline void QuaternionT<T>::Conjugate() {
    QuaternionT<T> q;
    QuaternionT<T>& q1 = *this;

    q.w = q1.w;
    q.x = -q1.x;
    q.y = -q1.y;
    q.z = -q1.z;

    *this = q;
}

typedef QuaternionT<float> Quaternion;

//@ref http://gamedev.stackexchange.com/a/13439
template<typename T>
inline QuaternionT<T> QuaternionT<T>::CreateFromEulerAngle(
        const Vector3<T>& euler_angle) {

    QuaternionT<T> qAroundX = QuaternionT<T>::CreateFromAxisAngle(
            Vector3<T>(1.0f, 0.0f, 0.0f), DEGTORAD(euler_angle.x));
    QuaternionT<T> qAroundY = QuaternionT<T>::CreateFromAxisAngle(
            Vector3<T>(0.0f, 1.0f, 0.0f), DEGTORAD(euler_angle.y));
    QuaternionT<T> qAroundZ = QuaternionT<T>::CreateFromAxisAngle(
            Vector3<T>(0.0f, 0.0f, 1.0f), DEGTORAD(euler_angle.z));
    Quaternion qRes = qAroundZ.Multiplied(qAroundX).Multiplied(qAroundY); //1108

    qRes.Normalize();

    return qRes;
}
