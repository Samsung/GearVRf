/***************************************************************************
 * JNI
 ***************************************************************************/

#include "objects/components/render_target.h"
#include "util/gvr_jni.h"

namespace gvr {

extern "C" {
    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeRenderTarget_ctorMultiview(JNIEnv *env, jobject obj, jlong jtexture, jboolean isMultiview);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeRenderTarget_ctor(JNIEnv *env, jobject obj, jlong jtexture,  jlong ptr);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeRenderTarget_getComponentType(JNIEnv *env, jobject obj);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderTarget_setTexture(JNIEnv *env, jobject obj, jlong ptr, jlong texture);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderTarget_setMainScene(JNIEnv *env, jobject obj, jlong ptr, jlong Sceneptr);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderTarget_setCamera(JNIEnv *env, jobject obj, jlong ptr, jlong camera);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderTarget_beginRendering(JNIEnv *env, jobject obj, jlong ptr, jlong camera);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderTarget_endRendering(JNIEnv *env, jobject obj, jlong ptr);

    JNIEXPORT jlong JNICALL
    Java_org_gearvrf_NativeRenderTarget_defaultCtr(JNIEnv *env, jobject obj, jlong jscene);
    JNIEXPORT void JNICALL
            Java_org_gearvrf_NativeRenderTarget_attachRenderTarget(JNIEnv *env, jobject obj, jlong jrendertarget, jlong jnextrendertarget);
    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderTarget_cullFromCamera(JNIEnv *env, jobject obj, jlong jscene, jobject javaSceneObject, jlong ptr, jlong jcamera, jlong jshaderManager);
    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeRenderTarget_render(JNIEnv *env, jobject obj, jlong renderTarget, jlong camera,
                                               jlong shader_manager, jlong posteffectrenderTextureA, jlong posteffectRenderTextureB, jlong jscene,
                                               jobject javaSceneObject);
};

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_render(JNIEnv *env, jobject obj, jlong renderTarget,
                                           jlong camera,
                                           jlong shader_manager, jlong posteffectrenderTextureA,
                                           jlong posteffectRenderTextureB, jlong jscene, jobject javaSceneObject) {
    RenderTarget* target = reinterpret_cast<RenderTarget*>(renderTarget);
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    // Do not remote this: need it for screenshot capturer, center camera rendering
    target->setCamera(reinterpret_cast<Camera*>(camera));
    javaSceneObject = env->NewLocalRef(javaSceneObject);
    gRenderer->getInstance()->renderRenderTarget(scene, javaSceneObject, target, reinterpret_cast<ShaderManager*>(shader_manager),
                                                 reinterpret_cast<RenderTexture*>(posteffectrenderTextureA), reinterpret_cast<RenderTexture*>(posteffectRenderTextureB));
    env->DeleteLocalRef(javaSceneObject);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTarget_defaultCtr(JNIEnv *env, jobject obj, jlong jscene){
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    return reinterpret_cast<jlong>(Renderer::getInstance()->createRenderTarget(scene));

}
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTarget_ctorMultiview(JNIEnv *env, jobject obj, jlong jtexture, jboolean isMultiview)
{

    RenderTexture* texture = reinterpret_cast<RenderTexture*>(jtexture);
    return reinterpret_cast<jlong>(Renderer::getInstance()->createRenderTarget(texture, isMultiview));
}
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTarget_ctor(JNIEnv *env, jobject obj, jlong jtexture, jlong ptr)
{
    RenderTexture* texture = reinterpret_cast<RenderTexture*>(jtexture);
    RenderTarget* sourceRenderTarget = reinterpret_cast<RenderTarget*>(ptr);
    return reinterpret_cast<jlong>(Renderer::getInstance()->createRenderTarget(texture, sourceRenderTarget));
}
JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_setMainScene(JNIEnv *env, jobject obj, jlong ptr, jlong Sceneptr){
    RenderTarget* target = reinterpret_cast<RenderTarget*>(ptr);
    Scene* scene = reinterpret_cast<Scene*>(Sceneptr);
    target->setMainScene(scene);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_attachRenderTarget(JNIEnv *env, jobject obj, jlong jrendertarget, jlong jnextrendertarget){
    RenderTarget* target = reinterpret_cast<RenderTarget*>(jrendertarget);
    RenderTarget* nextrendertarget = reinterpret_cast<RenderTarget*>(jnextrendertarget);
    target->attachNextRenderTarget(nextrendertarget);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_beginRendering(JNIEnv *env, jobject obj, jlong ptr, jlong jcamera){
    RenderTarget* target = reinterpret_cast<RenderTarget*>(ptr);
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    target->setCamera(camera);
    target->beginRendering(gRenderer->getInstance());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_endRendering(JNIEnv *env, jobject obj, jlong ptr){
    RenderTarget* target = reinterpret_cast<RenderTarget*>(ptr);
    target->endRendering(gRenderer->getInstance());
}


JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_cullFromCamera(JNIEnv *env, jobject obj, jlong jscene, jobject javaSceneObject, jlong ptr, jlong jcamera, jlong jshaderManager){

    RenderTarget* target = reinterpret_cast<RenderTarget*>(ptr);
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    Scene* scene = reinterpret_cast<Scene*>(jscene);
    ShaderManager* shaderManager = reinterpret_cast<ShaderManager*> (jshaderManager);
    javaSceneObject = env->NewLocalRef(javaSceneObject);
    target->cullFromCamera(scene, javaSceneObject, camera,gRenderer->getInstance(), shaderManager);
    env->DeleteLocalRef(javaSceneObject);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_setTexture(JNIEnv *env, jobject obj, jlong ptr, jlong jtexture)
{
    RenderTarget* target = reinterpret_cast<RenderTarget*>(ptr);
    RenderTexture* texture = reinterpret_cast<RenderTexture*>(jtexture);
    target->setTexture(texture);
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderTarget_setCamera(JNIEnv *env, jobject obj, jlong ptr, jlong jcamera)
{
    RenderTarget* target = reinterpret_cast<RenderTarget*>(ptr);
    Camera* camera = reinterpret_cast<Camera*>(jcamera);
    target->setCamera(camera);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderTarget_getComponentType(JNIEnv * env, jobject obj)
{
    return RenderTarget::getComponentType();
}

}