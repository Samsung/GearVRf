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
 * JNI
 ***************************************************************************/

#include <engine/renderer/renderer.h>
#include <vulkan/vulkan_shader.h>
#include <gl/gl_shader.h>
#include "shader_manager.h"
#include "shader.h"
#include "util/gvr_jni.h"

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeShaderManager_ctor(JNIEnv* env, jobject obj);

    JNIEXPORT jint JNICALL
    Java_org_gearvrf_NativeShaderManager_addShader(JNIEnv* env, jobject obj, jlong jshader_manager,
                                                    jstring signature,
                                                    jstring uniformDesc,
                                                    jstring textureDesc,
                                                    jstring vertexDesc,
                                                    jstring vertex_shader,
                                                    jstring fragment_shader);

    JNIEXPORT jint JNICALL
    Java_org_gearvrf_NativeShaderManager_getShader(JNIEnv* env, jobject obj, jlong jshader_manager, jstring signature);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeShaderManager_bindCalcMatrix(JNIEnv* env, jobject obj, jlong jshader_manager,
                                                        jint nativeShader, jclass javeShaderClass);

    JNIEXPORT jstring JNICALL
    Java_org_gearvrf_NativeShaderManager_makeLayout(JNIEnv* env, jobject obj,
                                                   jstring descriptor, jstring blockName, jboolean useGPUBuffer);

}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeShaderManager_ctor(JNIEnv* env, jobject obj)
{
    return reinterpret_cast<jlong>(new ShaderManager());
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeShaderManager_addShader(JNIEnv* env, jobject obj,
                                                jlong jshader_manager,
                                                jstring signature,
                                                jstring uniformDesc,
                                                jstring textureDesc,
                                                jstring vertexDesc,
                                                jstring vertex_shader,
                                                jstring fragment_shader)
{
    const char *sig_str = env->GetStringUTFChars(signature, 0);
    const char* uniform_str = env->GetStringUTFChars(uniformDesc, 0);
    const char* texture_str = env->GetStringUTFChars(textureDesc, 0);
    const char* vdesc_str = env->GetStringUTFChars(vertexDesc, 0);
    const char *vertex_str = env->GetStringUTFChars(vertex_shader, 0);
    const char *fragment_str = env->GetStringUTFChars(fragment_shader, 0);
    ShaderManager* shader_manager = reinterpret_cast<ShaderManager*>(jshader_manager);
    long id = shader_manager->addShader(sig_str, uniform_str, texture_str, vdesc_str, vertex_str, fragment_str);
    env->ReleaseStringUTFChars(vertex_shader, vertex_str);
    env->ReleaseStringUTFChars(fragment_shader, fragment_str);
    env->ReleaseStringUTFChars(signature, sig_str);
    env->ReleaseStringUTFChars(uniformDesc, uniform_str);
    env->ReleaseStringUTFChars(textureDesc, texture_str);
    env->ReleaseStringUTFChars(vertexDesc, vdesc_str);
    return id;
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeShaderManager_getShader(JNIEnv * env, jobject obj, jlong jshader_manager, jstring signature)
{
    ShaderManager* shader_manager = reinterpret_cast<ShaderManager*>(jshader_manager);
    const char* sig_str = env->GetStringUTFChars(signature, 0);
    Shader* shader = shader_manager->findShader(sig_str);

    env->ReleaseStringUTFChars(signature, sig_str);
    if (shader != NULL)
    {
        int id = shader->getShaderID();
        return reinterpret_cast<jint>(id);
    }
    return 0;
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeShaderManager_bindCalcMatrix(JNIEnv* env, jobject obj, jlong jshader_manager,
                                                    jint nativeShader, jclass javeShaderClass)
{
    ShaderManager* shader_manager = reinterpret_cast<ShaderManager*>(jshader_manager);
    Shader* shader = shader_manager->getShader(nativeShader);
    if (shader != nullptr)
    {
        JavaVM *jvm;
        env->GetJavaVM(&jvm);
        shader->setJava(javeShaderClass, jvm);
    }
}

JNIEXPORT jstring JNICALL
Java_org_gearvrf_NativeShaderManager_makeLayout(JNIEnv* env, jobject obj,
                                                jstring jdescriptor, jstring jblockName, jboolean useGPUBuffer)
{
    const char* sdesc = env->GetStringUTFChars(jdescriptor, 0);
    const char* block = env->GetStringUTFChars(jblockName, 0);
    Renderer* renderer = Renderer::getInstance();
    DataDescriptor desc(sdesc);
    if (renderer->isVulkanInstance())
    {
        const std::string& layout = VulkanShader::makeLayout(desc, block, useGPUBuffer);
        return env->NewStringUTF(layout.c_str());
    }
    else
    {
        const std::string& layout = GLShader::makeLayout(desc, block, useGPUBuffer);
        return env->NewStringUTF(layout.c_str());
    }
}


}
