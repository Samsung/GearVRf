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
#ifndef COMPONENT_TYPES_H
#define COMPONENT_TYPES_H

namespace gvr {
    static const long long COMPONENT_TYPE_TRANSFORM          = 10001;
    static const long long COMPONENT_TYPE_LIGHT              = 10002;
    static const long long COMPONENT_TYPE_BONE               = 10003;
    static const long long COMPONENT_TYPE_BONE_WEIGHT        = 10004;
    static const long long COMPONENT_TYPE_CAMERA             = 10005;
    static const long long COMPONENT_TYPE_CAMERA_RIG         = 10006;
    static const long long COMPONENT_TYPE_COLLIDER           = 10007;
    static const long long COMPONENT_TYPE_RENDER_DATA        = 10008;
    static const long long COMPONENT_TYPE_TEXTURE_CAPTURER   = 10009;
    static const long long COMPONENT_TYPE_PHYSICS_RIGID_BODY = 10010;
    static const long long COMPONENT_TYPE_PHYSICS_WORLD      = 10011;
}

#endif
