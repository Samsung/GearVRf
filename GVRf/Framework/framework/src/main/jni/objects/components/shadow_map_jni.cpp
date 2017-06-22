#include "objects/components/shadow_map.h"

#include "util/gvr_jni.h"
#include "util/gvr_log.h"
#include "glm/gtc/type_ptr.hpp"

namespace gvr
{
    extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeShadowMap_ctor(JNIEnv *env, jobject obj, jobject jmaterial);
    };

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeShadowMap_ctor(JNIEnv *env, jobject obj, jobject jmaterial)
    {
        ShaderData* material = reinterpret_cast<ShaderData*>(jmaterial);
        return reinterpret_cast<jlong>(new ShadowMap(material));
    }

}
