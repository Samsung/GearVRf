/***************************************************************************
 * JNI
 ***************************************************************************/

#include <jni.h>
#include "component_types.h"

namespace gvr {
extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_animation_NativeBodyTracker_getComponentType(JNIEnv* env, jobject clz);
} // extern "C"



JNIEXPORT jlong JNICALL
Java_org_gearvrf_animation_NativeBodyTracker_getComponentType(JNIEnv* env, jobject clz)
{
    return (jlong) COMPONENT_TYPE_BODYTRACKER;
}

} // namespace gvr
