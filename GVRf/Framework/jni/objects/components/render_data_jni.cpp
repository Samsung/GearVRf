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

#include "render_data.h"

#include "util/gvr_jni.h"

#include "objects/mesh.h"
#include "objects/material.h"

namespace gvr {

extern "C" {
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderData_ctor(JNIEnv * env,
        jobject obj);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderData_getMesh(JNIEnv * env,
        jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setMesh(JNIEnv * env,
        jobject obj, jlong jrender_data, jlong jmesh);
JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderData_getMaterial(JNIEnv * env,
        jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setMaterial(JNIEnv * env,
        jobject obj, jlong jrender_data, jlong jmaterial);
JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeRenderData_getRenderMask(JNIEnv * env,
        jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setRenderMask(JNIEnv * env,
        jobject obj, jlong jrender_data, jint render_mask);
JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeRenderData_getRenderingOrder(
        JNIEnv * env, jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setRenderingOrder(
        JNIEnv * env, jobject obj, jlong jrender_data, jint rendering_order);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeRenderData_getCullTest(JNIEnv * env,
        jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setCullTest(JNIEnv * env,
        jobject obj, jlong jrender_data, jboolean cull_test);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeRenderData_getOffset(JNIEnv * env,
        jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setOffset(JNIEnv * env,
        jobject obj, jlong jrender_data, jboolean offset);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeRenderData_getOffsetFactor(JNIEnv * env,
        jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setOffsetFactor(JNIEnv * env,
        jobject obj, jlong jrender_data, jfloat offset_factor);
JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeRenderData_getOffsetUnits(JNIEnv * env,
        jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setOffsetUnits(JNIEnv * env,
        jobject obj, jlong jrender_data, jfloat offset_units);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeRenderData_getDepthTest(JNIEnv * env,
        jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setDepthTest(JNIEnv * env,
        jobject obj, jlong jrender_data, jboolean depth_test);
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeRenderData_getAlphaBlend(JNIEnv * env,
        jobject obj, jlong jrender_data);

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setAlphaBlend(JNIEnv * env,
        jobject obj, jlong jrender_data, jboolean alpha_blend);
}
;

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderData_ctor(JNIEnv * env,
        jobject obj) {
    return reinterpret_cast<jlong>(new std::shared_ptr<RenderData>(
            new RenderData()));
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderData_getMesh(JNIEnv * env,
        jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    std::shared_ptr<Mesh> mesh = render_data->mesh();
    return mesh == NULL ?
            0 : reinterpret_cast<jlong>(new std::shared_ptr<Mesh>(mesh));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setMesh(JNIEnv * env,
        jobject obj, jlong jrender_data, jlong jmesh) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    std::shared_ptr<Mesh> mesh =
            *reinterpret_cast<std::shared_ptr<Mesh>*>(jmesh);
    render_data->set_mesh(mesh);
}

JNIEXPORT jlong JNICALL
Java_org_gearvrf_NativeRenderData_getMaterial(JNIEnv * env,
        jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    std::shared_ptr<Material> material = render_data->material();
    return material.get() == NULL ?
            0 : reinterpret_cast<jlong>(new std::shared_ptr<Material>(material));
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setMaterial(JNIEnv * env,
        jobject obj, jlong jrender_data, jlong jmaterial) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    std::shared_ptr<Material> material = *reinterpret_cast<std::shared_ptr<
            Material>*>(jmaterial);
    render_data->set_material(material);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeRenderData_getRenderMask(JNIEnv * env,
        jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    return render_data->render_mask();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setRenderMask(JNIEnv * env,
        jobject obj, jlong jrender_data, jint render_mask) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    render_data->set_render_mask(render_mask);
}

JNIEXPORT jint JNICALL
Java_org_gearvrf_NativeRenderData_getRenderingOrder(
        JNIEnv * env, jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    return render_data->rendering_order();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setRenderingOrder(
        JNIEnv * env, jobject obj, jlong jrender_data, jint rendering_order) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    render_data->set_rendering_order(rendering_order);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeRenderData_getCullTest(JNIEnv * env,
        jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    return static_cast<jboolean>(render_data->cull_test());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setCullTest(JNIEnv * env,
        jobject obj, jlong jrender_data, jboolean cull_test) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    render_data->set_cull_test(static_cast<bool>(cull_test));
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeRenderData_getOffset(JNIEnv * env,
        jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    return static_cast<jboolean>(render_data->offset());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setOffset(JNIEnv * env,
        jobject obj, jlong jrender_data, jboolean offset) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    render_data->set_offset(static_cast<bool>(offset));
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeRenderData_getOffsetFactor(JNIEnv * env,
        jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    return render_data->offset_factor();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setOffsetFactor(JNIEnv * env,
        jobject obj, jlong jrender_data, jfloat offset_factor) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    render_data->set_offset_factor(offset_factor);
}

JNIEXPORT jfloat JNICALL
Java_org_gearvrf_NativeRenderData_getOffsetUnits(JNIEnv * env,
        jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    return render_data->offset_units();
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setOffsetUnits(JNIEnv * env,
        jobject obj, jlong jrender_data, jfloat offset_units) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    render_data->set_offset_units(offset_units);
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeRenderData_getDepthTest(JNIEnv * env,
        jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    return static_cast<jboolean>(render_data->depth_test());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setDepthTest(JNIEnv * env,
        jobject obj, jlong jrender_data, jboolean depth_test) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    render_data->set_depth_test(static_cast<bool>(depth_test));
}

JNIEXPORT jboolean JNICALL
Java_org_gearvrf_NativeRenderData_getAlphaBlend(JNIEnv * env,
        jobject obj, jlong jrender_data) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    return static_cast<jboolean>(render_data->alpha_blend());
}

JNIEXPORT void JNICALL
Java_org_gearvrf_NativeRenderData_setAlphaBlend(JNIEnv * env,
        jobject obj, jlong jrender_data, jboolean alpha_blend) {
    std::shared_ptr<RenderData> render_data = *reinterpret_cast<std::shared_ptr<
            RenderData>*>(jrender_data);
    render_data->set_alpha_blend(static_cast<bool>(alpha_blend));
}
}
