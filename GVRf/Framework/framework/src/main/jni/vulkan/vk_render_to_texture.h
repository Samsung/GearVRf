#ifndef FRAMEWORK_VK_RENDER_TO_TEXTURE_H
#define FRAMEWORK_VK_RENDER_TO_TEXTURE_H



#include "objects/textures/render_texture.h"
#include "vk_framebuffer.h"


namespace gvr{
class VkRenderTexture : public RenderTexture
{
    VKFramebuffer* fbo;
    void createRenderPass();
    int mWidth, mHeight;
    std::vector <VkClearValue> clear_values;


public:
    VkRenderPassBeginInfo getRenderPassBeginInfo();
    VKFramebuffer* getFBO(){
        return fbo;
    }
    virtual int width() const {
        return mWidth;
    }
    virtual int height() const {
        return mHeight;
    }
    virtual ~VkRenderTexture(){
        delete fbo;
    }
    VkRenderTexture(int width, int height, int sample_count = 1):RenderTexture(sample_count), fbo(nullptr),mWidth(width), mHeight(height){}
    virtual unsigned int getFrameBufferId() const {

    }

    virtual unsigned int getDepthBufferId() const {

    }
    virtual void bind();
    virtual void beginRendering(Renderer* renderer);
    virtual void beginRenderingPE(Renderer* renderer);
    virtual void endRendering(Renderer*){}
    virtual void endRenderingPE(Renderer* renderer);
    // Start to read back texture in the background. It can be optionally called before
    // readRenderResult() to read pixels asynchronously. This function returns immediately.
    virtual void startReadBack() {

    }

    // Copy data in pixel buffer to client memory. This function is synchronous. When
    // it returns, the pixels have been copied to PBO and then to the client memory.
    virtual bool readRenderResult(uint8_t *readback_buffer) {}
    virtual void setLayerIndex(int layer_index) {}
    // Copy data in pixel buffer to client memory. This function is synchronous. When
    // it returns, the pixels have been copied to PBO and then to the client memory.
    virtual bool readRenderResult(uint8_t *readback_buffer, long capacity) {
    }
    bool readVkRenderResult(uint8_t **readback_buffer, VkCommandBuffer& cmd_buffer,VkFence& fence);
    VkRenderPass getRenderPass(){
        bind();
        return fbo->getRenderPass();
    }
};

}
#endif //FRAMEWORK_VK_RENDER_TO_TEXTURE_H