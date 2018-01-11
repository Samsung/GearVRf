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
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "shaders/shader.h"
#include "objects/components/render_data.h"

namespace gvr {
/**
 * Constructs a bnse material.
 * The material contains a UniformBlock describing the possible uniforms
 * that can be used by this material. It also maintains the list of
 * possible textures in the order specified by the descriptor.
 * All materials which use the same shader will have the same ordering
 * of uniforms and textures in their descriptors.
 * @param uniform_desc  string describing uniforms used by this material
 * @param texture_desc  string describing textures used by this material
 */
    ShaderData::ShaderData(const char* texture_desc) :
            mNativeShader(0),
            mTextureDesc(texture_desc),
            mLock(),
            mDirty(NONE)
    {
        DataDescriptor texdesc(texture_desc);
        texdesc.forEach([this](const char* name, const char* type, int size) mutable
        {
            mTextureNames.push_back(name);
            mTextures.push_back(nullptr);
        });
    }

    Texture* ShaderData::getTexture(const char* key) const
    {
        for (auto it = mTextureNames.begin(); it < mTextureNames.end(); ++it)
        {
            if (*it == key)
            {
                return mTextures[it - mTextureNames.begin()];
            }
        }
        return NULL;
    }

    void ShaderData::setTexture(const char* key, Texture* texture)
    {
        std::lock_guard<std::mutex> lock(mLock);
        for (int i = 0; i < mTextureNames.size(); ++i)
        {
            const std::string& temp = mTextureNames[i];
            if (temp.compare(key) == 0)
            {
                Texture* oldtex = mTextures[i];
                makeDirty(oldtex ? MOD_TEXTURE : NEW_TEXTURE);
                mTextures[i] = texture;
                return;
            }
        }
    }
/**
 * Visits each texture in the material and calls the given function.
 */
void ShaderData::forEachTexture(std::function< void(const char* texname, Texture* tex) > func) const
{
    std::lock_guard<std::mutex> lock(mLock);
    for (int i = 0; i < mTextureNames.size(); ++i)
    {
        Texture* tex = mTextures[i];
        const std::string& name = mTextureNames[i];
        func(name.c_str(), tex);
    }
}


std::string ShaderData::makeShaderLayout()
{
    return uniforms().makeShaderLayout();
}

int ShaderData::getByteSize(const char* name) const
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().getByteSize(name);
}

const char* ShaderData::getUniformDescriptor() const
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().getDescriptor();
}

const char* ShaderData::getTextureDescriptor() const
{
    return mTextureDesc.c_str();
}

bool ShaderData::getFloat(const char* name, float& v) const
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().getFloat(name, v);
}

bool   ShaderData::getInt(const char* name, int& v) const
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().getInt(name, v);
}

bool  ShaderData::setInt(const char* name, int val)
{
    std::lock_guard<std::mutex> lock(mLock);
    makeDirty(MAT_DATA);
    return uniforms().setInt(name, val);
}

bool  ShaderData::setFloat(const char* name, float val)
{
    std::lock_guard<std::mutex> lock(mLock);
    makeDirty(MAT_DATA);
    return uniforms().setFloat(name, val);
}

bool  ShaderData::setIntVec(const char* name, const int* val, int n)
{
    std::lock_guard<std::mutex> lock(mLock);
    makeDirty(MAT_DATA);
    return uniforms().setIntVec(name, val, n);
}

bool  ShaderData::setFloatVec(const char* name, const float* val, int n)
{
    std::lock_guard<std::mutex> lock(mLock);
    makeDirty(MAT_DATA);
    return uniforms().setFloatVec(name, val, n);
}

bool  ShaderData::getFloatVec(const char* name, float* val, int n)
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().getFloatVec(name, val, n);
}

bool  ShaderData::getIntVec(const char* name, int* val, int n)
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().getIntVec(name, val, n);
}

bool  ShaderData::setVec2(const char* name, const glm::vec2& v)
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().setVec2(name, v);
}

bool  ShaderData::setVec3(const char* name, const glm::vec3& v)
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().setVec3(name, v);
}

bool  ShaderData::setVec4(const char* name, const glm::vec4& v)
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().setVec4(name, v);
}

bool  ShaderData::setMat4(const char* name, const glm::mat4& m)
{
    std::lock_guard<std::mutex> lock(mLock);
    return uniforms().setMat4(name, m);
}

void ShaderData::makeDirty(DIRTY_BITS bits)
{
    int temp = mDirty;
    temp |= bits;
    mDirty = static_cast<DIRTY_BITS>(temp);
}

bool ShaderData::isDirty(DIRTY_BITS bits)
{
    return (bits & mDirty) != 0;
}

void ShaderData::clearDirty()
{
    mDirty = NONE;
}

bool ShaderData::hasTexture(const char* key) const
{
    std::lock_guard<std::mutex> lock(mLock);
    for (auto it = mTextureNames.begin(); it < mTextureNames.end(); ++it)
    {
        if (*it == key && getTexture(key))
        {
            return true;
        }
    }
    return false;
}

bool ShaderData::hasUniform(const char* key) const
{
    return (uniforms().getByteSize(key) > 0);
}

bool ShaderData::copyUniforms(const ShaderData* src)
{
    const UniformBlock* srcBlock = &src->uniforms();
    UniformBlock*dstBlock = &uniforms();
    bool rc = true;

    srcBlock->forEachEntry([dstBlock, srcBlock, rc, this](const DataDescriptor::DataEntry& entry) mutable
    {
        if (entry.NotUsed || !hasUniform(entry.Name))
        {
            return;
        }
        if (entry.IsInt)
        {
            int n = entry.Size / sizeof(int);
            int v[n];
            if (srcBlock->getIntVec(entry.Name, v, n))
            {
                dstBlock->setIntVec(entry.Name, v, n);
                return;
            }
        }
        else
        {
            int n = entry.Size / sizeof(int);
            float v[n];
            if (srcBlock->getFloatVec(entry.Name, v, n))
            {
                dstBlock->setFloatVec(entry.Name, v, n);
                return;
            }
        }
        LOGE("ERROR: ShaderData::copyUniforms failed to copy uniform %s", entry.Name);
        rc = false;
    });
    return rc;
}

/**
 * Updates the values of the uniforms and textures
 * by copying the relevant data from the CPU to the GPU.
 * This function operates independently of the shader,
 * so it cannot tell if a texture the shader requires
 * is missing.
 * @param renderer
 * @return 1 = success, -1 texture not ready, 0 uniforms failed to load
 */
int ShaderData::updateGPU(Renderer* renderer, RenderData* rdata)
{
    std::lock_guard<std::mutex> lock(mLock);
    for (int texIndex = 0; texIndex < mTextures.size(); ++texIndex)
    {
        Texture *tex = mTextures[texIndex];

        if (tex != NULL)
        {
            bool ready = tex->isReady();
            if (!ready)
            {
                return -1;
            }
            if (rdata && (texIndex == 0))
            {
                rdata->adjustRenderingOrderForTransparency(tex->transparency());
            }
        }
    }
    clearDirty();
    return (uniforms().updateGPU(renderer) ? 1 : 0);
}

}

