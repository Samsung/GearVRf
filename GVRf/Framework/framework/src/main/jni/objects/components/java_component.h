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
 * Things which can be attached to a scene object.
 ***************************************************************************/

#ifndef JAVA_COMPONENT_H_
#define JAVA_COMPONENT_H_

#include "component.h"
#include "util/jni_utils.h"
#include "util/gvr_log.h"

namespace gvr {
    class SceneObject;

    class Scene;

    class JavaComponent : public Component {
    public:
        JavaComponent(long long type) : Component(type), javaObj_(0L), javaVM_(NULL) { }
        JavaComponent() : Component(), javaObj_(0L), javaVM_(NULL) { }

        virtual ~JavaComponent();
        virtual JNIEnv* set_java(jobject javaObj, JavaVM *javaVM);
        jobject get_java() { return javaObj_; }
        void free_java();

    private:
        JavaComponent(const JavaComponent &component);
        JavaComponent(JavaComponent &&component);
        JavaComponent &operator=(const JavaComponent &component);
        JavaComponent &operator=(JavaComponent &&component);

    protected:
        jobject javaObj_;
        JavaVM *javaVM_;
    };


inline JNIEnv* JavaComponent::set_java(jobject javaObj, JavaVM *javaVM)
{
    JNIEnv *env = getCurrentEnv(javaVM);
    javaVM_ = javaVM;
    if (env)
    {
        javaObj_ = env->NewGlobalRef(javaObj);
        return env;
    }
    return NULL;
}

inline void JavaComponent::free_java()
{
    if (javaVM_ && javaObj_)
    {
        JNIEnv* env;
        jint rs = javaVM_->AttachCurrentThread(&env, NULL);
        if (rs == JNI_OK) {
            env->DeleteGlobalRef(javaObj_);
        }
    }
}

inline JavaComponent::~JavaComponent()
{
    free_java();
}
}
#endif


