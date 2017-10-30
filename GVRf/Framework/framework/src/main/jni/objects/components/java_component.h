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
        explicit JavaComponent(long long type) : Component(type), javaObj_(0L), javaVM_(NULL) { }
        JavaComponent() : Component(), javaObj_(0L), javaVM_(NULL) { }

        virtual ~JavaComponent();
        virtual JNIEnv* set_java(jobject javaObj, JavaVM *javaVM);

        /**
         * @return nullptr if the Java object is not around anymore or a local reference to it
         */
        jobject get_java(JNIEnv* env) {
            return env->NewLocalRef(javaObj_);
        }

    private:
        JavaComponent(const JavaComponent &component) = delete;
        JavaComponent(JavaComponent &&component) = delete;
        JavaComponent &operator=(const JavaComponent &component) = delete;
        JavaComponent &operator=(JavaComponent &&component) = delete;

    protected:
        jobject javaObj_;
        JavaVM *javaVM_;
    };


inline JNIEnv* JavaComponent::set_java(jobject javaObj, JavaVM *javaVM)
{
    JNIEnv *env = getCurrentEnv(javaVM);
    javaVM_ = javaVM;
    javaObj_ = env->NewWeakGlobalRef(javaObj);
    return env;
}

inline JavaComponent::~JavaComponent()
{
    if (javaVM_ && javaObj_)
    {
        JNIEnv* env;
        jint rs = javaVM_->AttachCurrentThread(&env, NULL);
        if (rs == JNI_OK) {
            env->DeleteWeakGlobalRef(javaObj_);
        }
    }
}

}
#endif


