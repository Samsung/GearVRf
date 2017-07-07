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


#ifndef BULLET_OBJECT_H
#define BULLET_OBJECT_H

class SceneObject;
namespace gvr {

    class BulletObject {
    public:
        virtual void set_owner_object(SceneObject* obj) = 0;
        virtual void onAttach(SceneObject* owner) = 0;
    };

}
#endif //BULLET_OBJECT_H
