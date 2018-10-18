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
 * Renders a scene, a screen.
 ***************************************************************************/

#ifndef RENDERER_H_
#define RENDERER_H_

#include <unordered_map>
#include "render_sorter.h"
#include "shaders/shader_manager.h"
#include "render_state.h"

namespace gvr {
extern bool use_multiview;
struct RenderTextureInfo;
class Camera;
class Scene;
class SceneObject;
class ShaderData;
class RenderData;
class RenderTarget;
class RenderTexture;
class Light;
class BitmapImage;
class CubemapImage;
class CompressedImage;
class FloatImage;
class VertexBuffer;
class IndexBuffer;
class UniformBlock;
class Image;
class RenderPass;
class Texture;
class RenderTarget;
class ShadowRenderSorter;
class ShadowMap;
class Mesh;
class TextureParameters;

extern uint8_t* oculusTexData;



enum EYE
{
    LEFT, RIGHT, MULTIVIEW
};

#include "render_state.h"

class Renderer {
public:
    void resetStats() {
        numberDrawCalls = 0;
        numberTriangles = 0;
    }
    bool isVulkanInstance(){
        return isVulkan_;
    }
    int getNumberDrawCalls() {
        return numberDrawCalls;
    }

    int getNumberTriangles() {
        return numberTriangles;
    }
    int incrementTriangles(int number=1){
        return numberTriangles += number;
    }
    int incrementDrawCalls(){
        return ++numberDrawCalls;
    }
    JavaVM* getJavaVM() const { return mJavaVM; }
    void setJavaVM(JavaVM* java) { mJavaVM = java; }

    int getJavaEnv(JNIEnv** envptr);
    void detachJavaEnv();
    static Renderer* getInstance(std::string type =  " ");
    static void resetInstance()
    {
        //@todo fix for vulkan
        if (!isVulkan_) {
            delete instance;
            instance = NULL;
        }
    }
    static bool useVulkanInstance();
    virtual UniformBlock* createTransformBlock(int numMatrices) = 0;
    virtual ShaderData* createMaterial(const char* uniform_desc, const char* texture_desc) = 0;
    virtual RenderData* createRenderData() = 0;
    virtual RenderData* createRenderData(RenderData*) = 0;
    virtual UniformBlock* createUniformBlock(const char* desc, int bindingPoint, const char* name, int numElems) = 0;
    virtual Image* createImage(int type, int format) = 0;
    virtual RenderPass* createRenderPass() = 0;
    virtual Texture* createTexture(int target = GL_TEXTURE_2D) = 0;
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count,
                                               int jcolor_format, int jdepth_format, bool resolve_depth,
                                               const TextureParameters* texture_parameters, int number_views) = 0;
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count, int layers, int jdepth_format) = 0;
    virtual RenderTexture* createRenderTexture(const RenderTextureInfo&)=0;
    virtual ShadowMap* createShadowMap(ShaderData*) = 0;
    virtual Shader* createShader(int id, const char* signature,
                                 const char* uniformDescriptor, const char* textureDescriptor,
                                 const char* vertexDescriptor, const char* vertexShader,
                                 const char* fragmentShader, const char* matrixCalc) = 0;
    virtual VertexBuffer* createVertexBuffer(const char* descriptor, int vcount) = 0;
    virtual IndexBuffer* createIndexBuffer(int bytesPerIndex, int icount) = 0;
    virtual RenderTarget* createRenderTarget(Scene*, bool stereo) = 0;
    virtual RenderTarget* createRenderTarget(RenderTexture*, bool multiview, bool stereo) = 0;
    virtual RenderTarget* createRenderTarget(RenderTexture*, const RenderTarget*) = 0;
    virtual void renderRenderTarget(Scene*, jobject javaSceneObject, RenderTarget* renderTarget, ShaderManager* shader_manager,
                                    RenderTexture* post_effect_render_texture_a, RenderTexture* post_effect_render_texture_b)=0;
    virtual Texture* createSharedTexture(int id) = 0;
    virtual void makeShadowMaps(Scene* scene, jobject javaSceneObject, ShaderManager* shader_manager) = 0;
    virtual Light* createLight(const char* uniformDescriptor, const char* textureDescriptor) = 0;
    virtual void updatePostEffectMesh(Mesh*) = 0;
    virtual int getMaxArraySize(int elemSize) const;
    void addRenderTarget(RenderTarget* renderTarget, EYE eye, int index){
        switch (eye) {
            case LEFT:
                mLeftRenderTarget[index] = renderTarget;
                break;
            case RIGHT:
                mRightRenderTarget[index] = renderTarget;
                break;
            case MULTIVIEW:
                mMultiviewRenderTarget[index] = renderTarget;
                break;
            default:
                LOGE("invalid Eye");
        }
    }
    RenderTarget* getRenderTarget(int index, int eye){
        switch (eye) {
            case LEFT:
                return mLeftRenderTarget[index];
            case RIGHT:
                return mRightRenderTarget[index];
            case MULTIVIEW:
                return mMultiviewRenderTarget[index];
            default:
                FAIL("invalid eye");
        }
        return nullptr;
    }
    virtual void validate(RenderSorter::Renderable& r) = 0;
    virtual void render(const RenderState&, const RenderSorter::Renderable&) = 0;
private:
    RenderTarget* mLeftRenderTarget[3];
    RenderTarget* mRightRenderTarget[3];
    RenderTarget* mMultiviewRenderTarget[3];
    static bool isVulkan_;
    Renderer(const Renderer& render_engine) = delete;
    Renderer(Renderer&& render_engine) = delete;
    Renderer& operator=(const Renderer& render_engine) = delete;
    Renderer& operator=(Renderer&& render_engine) = delete;
    static Renderer* instance;

protected:
    Renderer();
    virtual ~Renderer() { }
    virtual bool renderPostEffectData(RenderState& rstate, RenderTexture* input_texture, RenderData* post_effect, int pass);

    int mMaxUniformBlockSize;
    int mMaxArrayFloats;
    int numberDrawCalls;
    int numberTriangles;
    bool useStencilBuffer_ = false;
    RenderSorter* mPostEffectSorter;
    ShadowRenderSorter* mShadowSorter;
    JavaVM* mJavaVM;

public:
    int numLights;
    void setUseStencilBuffer(bool enable) { useStencilBuffer_ = enable; }
    bool useStencilBuffer(){
        return  useStencilBuffer_;
    }
};
extern Renderer* gRenderer;
}
#endif
