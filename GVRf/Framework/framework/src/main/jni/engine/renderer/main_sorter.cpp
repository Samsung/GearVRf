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

#include "objects/scene.h"
#include "objects/components/render_data.h"
#include "shaders/shader.h"
#include "main_sorter.h"

namespace gvr {

MainSceneSorter::MainSceneSorter(Renderer& renderer, int numMatrices, bool forceTransformBlock)
: RenderSorter(renderer, "MainSorter", numMatrices, forceTransformBlock)
{
    mAllMergeFunctions[0] = &MainSceneSorter::mergeByOrder;
    mAllMergeFunctions[1] = &MainSceneSorter::mergeByDistance;
    mAllMergeFunctions[2] = &MainSceneSorter::mergeByShader;
    mAllMergeFunctions[3] = &MainSceneSorter::mergeByMesh;
    mAllMergeFunctions[4] = &MainSceneSorter::mergeByMaterial;
    mAllMergeFunctions[5] = &MainSceneSorter::mergeByMode;
    setSortOptions({ RENDER_ORDER, DISTANCE, SHADER, MESH, MATERIAL });
}

void MainSceneSorter::setSortOptions(std::initializer_list<SortOption> list)
{
    int level = 0;
    for (SortOption o : list)
    {
        mMergeFunctions[level] = mAllMergeFunctions[o];
        if (++level >= 8)
        {
            break;
        }
    }
    mMergeFunctions[level] = nullptr;
}

void MainSceneSorter::cull(RenderState& rstate)
{
    if (rstate.is_stereo)
    {
        Scene* scene = rstate.scene;
        scene->lockColliders();
        scene->clearVisibleColliders();
        RenderSorter::cull(rstate);
        scene->unlockColliders();
    }
    else
    {
        RenderSorter::cull(rstate);
    }
}

void MainSceneSorter::add(RenderState& rstate, SceneObject* object)
{
    RenderData* rdata = object->render_data();

    rstate.scene->pick(object);
    if (rdata == nullptr)
    {
        return;
    }
    Mesh* geometry = rdata->mesh();

    if (geometry == nullptr)
    {
        return;
    }
    BoundingVolume bounding_volume_ = object->getBoundingVolume();
    glm::vec3 center(bounding_volume_.center());
    glm::vec3 diff(center - rstate.camera_position);
    float d = (diff.x * diff.x) + (diff.y * diff.y) + (diff.z * diff.z);
    d = sqrtf(d);
    for (int i = 0; i < rdata->pass_count(); ++i)
    {
        RenderPass* rpass = rdata->pass(i);
        Renderable* r = alloc();

        r->mesh = geometry;
        r->mvp = object->transform()->getModelMatrix();
        r->renderData = rdata;
        r->renderPass = rpass;
        r->material = rpass->material();
        r->renderModes = rpass->render_modes();
        r->nextLevel = nullptr;
        r->nextSibling = nullptr;
        r->transformBlock = nullptr;
        r->distanceFromCamera = d;
        r->shader = selectShader(rstate, *r);
#ifdef DEBUG_RENDER
        SceneObject* owner = r->renderData->owner_object();
        const char* name = (owner ? owner->name().c_str() : "");

        LOGV("RENDER: Add %s [%p] order = %d shader = %d material = %p",
             name, r, r->renderModes.getRenderOrder(), r->shader ? r->shader->getShaderID() : -1, r->material);
#endif
        ++mVisibleElems;
    }
}

void MainSceneSorter::validate(RenderState& rstate)
{
    rstate.shadow_map = rstate.scene->getLights().updateLights(&mRenderer);
    RenderSorter::validate(rstate);
}

bool MainSceneSorter::isValid(RenderState& rstate, Renderable& r)
{
    if (r.shader)
    {
        const char* lightsig = rstate.scene->getLights().getDescriptor();

        if (r.shader->useLights() != (*lightsig != 0))
        {
            r.renderPass->markDirty();
        }
        else
        {
            const char* oldsig = r.shader->signature();
            int nnew = strlen(lightsig);
            int nold = strlen(oldsig);
            oldsig += nold - nnew;
            if ((nold > nnew) && (strcmp(oldsig, lightsig) != 0))
            {
                r.renderPass->markDirty();
            }
        }
        if (r.material->isTransparent())
        {
            if (r.renderModes.getRenderOrder() == RenderModes::Geometry)
            {
                r.renderModes.setRenderOrder(RenderModes::Transparent);
            }
        }
    }
    return RenderSorter::isValid(rstate, r);
}

void MainSceneSorter::merge(Renderable* item)
{
    MERGEFUNC f = mMergeFunctions[0];
    (this->*f)(&mRenderList, item, 0);
}

void MainSceneSorter::mergeByOrder(Renderable* prev, Renderable* item, int level)
{
    Renderable* cur = prev->nextLevel;
    int itemOrder = item->renderModes.getRenderOrder();
#ifdef DEBUG_RENDER
    SceneObject* owner = item->renderData->owner_object();
    const char* name = (owner ? owner->name().c_str() : "");
#endif
    if (itemOrder >= RenderData::Transparent)
    {
        item->renderModes.setAlphaBlend(true);
        item->renderModes.setDepthTest(false);
    }
    /*
     * Add this item at the front of the list?
     */
    if ((cur == nullptr) || (itemOrder < cur->renderModes.getRenderOrder()))
    {
        item->nextSibling = cur;
        prev->nextLevel = item;
#ifdef DEBUG_RENDER
        LOGV("RENDER: Front order: %s [%p] order = %d shader = %d material = %p",
             name, item, itemOrder, item->shader->getShaderID(), item->material);
#endif
        return;
    }
    /*
     * Scan the list to see where it fits
     */
    ++level;
    while (1)
    {
        if (itemOrder == cur->renderModes.getRenderOrder())
        {
            if (cur->nextLevel == nullptr)
            {
                addListhead(cur);
            }
            int l = level;
            if (itemOrder < RenderData::Transparent)
            {
                ++l;
            }
            MERGEFUNC f = mMergeFunctions[l];
            (this->*f)(cur, item, l);
            return;
        }
        prev = cur;
        cur = cur->nextSibling;
        if (cur == nullptr)
        {
            break;
        }
        if (itemOrder <= cur->renderModes.getRenderOrder())
        {
            item->nextSibling = cur;
            prev->nextSibling = item;
#ifdef DEBUG_RENDER
            LOGV("RENDER: Middle order: %s [%p] order = %d shader = %d material = %p",
                 name, item, itemOrder, item->shader->getShaderID(), item->material);
#endif
            return;
        }
    }
    prev->nextSibling = item;
    item->nextSibling = nullptr;
#ifdef DEBUG_RENDER
    LOGV("RENDER: End order: %s [%p] order = %d shader = %d material = %p",
         name, item, itemOrder, item->shader->getShaderID(), item->material);
#endif
}


void MainSceneSorter::mergeByMesh(Renderable* prev, Renderable* item, int level)
{
    Renderable* cur = prev->nextLevel;
    Mesh* itemMesh = item->mesh;
    float itemDist = item->distanceFromCamera;
#ifdef DEBUG_RENDER
    SceneObject* owner = item->renderData->owner_object();
    int itemOrder = item->renderModes.getRenderOrder();
    int itemShader = item->shader->getShaderID();
    const char* name = (owner ? owner->name().c_str() : "");
#endif

/*
* Add this item at the front of the list?
*/
    if ((cur == nullptr) || (itemMesh < cur->mesh))
    {
        item->nextSibling = cur;
        prev->nextLevel = item;
#ifdef DEBUG_RENDER
        LOGV("RENDER: Front mesh: %s [%p] dist = %f order = %d shader = %d material = %p",
             name, item, itemDist, itemOrder, itemShader, item->material);
#endif
        return;
    }
    /*
     * Scan the list to see where it fits
     */
    ++level;
    while (1)
    {
        if (itemMesh == cur->mesh)              // mesh the same?
        {
            MERGEFUNC f = mMergeFunctions[level];

            if (f != nullptr)
            {
                if (cur->nextLevel == nullptr)  // no? add a listhead
                {
                    addListhead(cur);
                }
                (this->*f)(cur, item, level);  // add at next level
                return;
            }
        }
        prev = cur;
        cur = cur->nextSibling;
        if (cur == nullptr)
        {
            break;
        }
        if (itemMesh <= cur->mesh)
        {
            prev->nextSibling = item;
            item->nextSibling = cur;
#ifdef DEBUG_RENDER
            LOGV("RENDER: Middle mesh: %s [%p] dist = %f order = %d shader = %d material = %p",
                 name, item, itemDist, itemOrder, itemShader, item->material);
#endif
            return;
        }
    }
    prev->nextSibling = item;
    item->nextSibling = nullptr;
#ifdef DEBUG_RENDER
    LOGV("RENDER: End mesh: %s [%p] dist = %f order = %d shader = %d material = %p",
         name, item, itemDist, itemOrder, itemShader, item->material);
#endif
}

void MainSceneSorter::mergeByShader(Renderable* prev, Renderable* item, int level)
{
    int itemShader = item->shader->getShaderID();
    Renderable* cur = prev->nextLevel;

#ifdef DEBUG_RENDER
    SceneObject* owner = item->renderData->owner_object();
    int itemOrder = item->renderModes.getRenderOrder();
    float itemDist = item->distanceFromCamera;
    const char* name = (owner ? owner->name().c_str() : "");
#endif
/*
 * Add this item at the front of the list?
 */
    if ((cur == nullptr) || (itemShader < cur->shader->getShaderID()))
    {
        item->nextSibling = cur;
        prev->nextLevel = item;
#ifdef DEBUG_RENDER
        LOGV("RENDER: Front shader: %s [%p] dist = %f order = %d shader = %d material = %p",
             name, item, itemDist, itemOrder, itemShader, item->material);
#endif
        return;
    }
    /*
     * Scan the list to see where it fits
     */
    ++level;
    while (1)
    {
        if (itemShader == cur->shader->getShaderID())
        {
            MERGEFUNC f = mMergeFunctions[level];

            if (f != nullptr)
            {
                if (cur->nextLevel == nullptr)  // no? add a listhead
                {
                    addListhead(cur);
                }
                (this->*f)(cur, item, level);   // add at next level
                return;
            }
        }
        prev = cur;
        cur = cur->nextSibling;
        if (cur == nullptr)
        {
            break;
        }
        if (itemShader <= cur->shader->getShaderID())
        {
            item->nextSibling = cur;
            prev->nextSibling = item;
#ifdef DEBUG_RENDER
            LOGV("RENDER: Middle shader: %s [%p] dist = %f order = %d shader = %d material = %p",
                 name, item, itemDist, itemOrder, itemShader, item->material);
#endif
            return;
        }
    }
    prev->nextSibling = item;
    item->nextSibling = nullptr;
#ifdef DEBUG_RENDER
    LOGV("RENDER: End shader: %s [%p] dist = %f order = %d shader = %d material = %p",
         name, item, itemDist, itemOrder, itemShader, item->material);
#endif
}

void MainSceneSorter::mergeByMaterial(Renderable* prev, Renderable* item, int level)
{
    ShaderData* itemMtl = item->material;
    Renderable* cur = prev->nextLevel;

#ifdef DEBUG_RENDER
    SceneObject* owner = item->renderData->owner_object();
    int itemOrder = item->renderModes.getRenderOrder();
    int itemShader = item->shader->getShaderID();
    const char* name = (owner ? owner->name().c_str() : "");
#endif
/*
 * Add this item at the front of the list?
 */
    if ((cur == nullptr) || (itemMtl <= cur->material))
    {
        item->nextSibling = cur;
        prev->nextLevel = item;
#ifdef DEBUG_RENDER
        LOGV("RENDER: Front material: %s [%p] order = %d shader = %d material = %p",
             name, item, itemOrder, itemShader, item->material);
#endif
        return;
    }
    /*
     * Scan the list to see where it fits
     */
    ++level;
    while (cur)
    {
        if (itemMtl == cur->material)
        {
            MERGEFUNC f = mMergeFunctions[level];

            if (f != nullptr)
            {
                if (cur->nextLevel == nullptr)  // no? add a listhead
                {
                    addListhead(cur);
                }
                (this->*f)(cur, item, level);   // add at next level
                return;
            }
        }
        prev = cur;
        cur = cur->nextSibling;
        if (cur == nullptr)
        {
            break;
        }
        if (itemMtl <= cur->material)
        {
            item->nextSibling = cur;
            prev->nextSibling = item;
#ifdef DEBUG_RENDER
            LOGV("RENDER: Middle material: %s [%p] order = %d shader = %d material = %p",
                 name, item, itemOrder, itemShader, item->material);
#endif
            return;
        }
        prev = cur;
        cur = cur->nextSibling;
    }
    prev->nextSibling = item;
    item->nextSibling = nullptr;
#ifdef DEBUG_RENDER
    LOGV("RENDER: End material: %s [%p] order = %d shader = %d material = %p",
         name, item, itemOrder, itemShader, item->material);
#endif
}

void MainSceneSorter::mergeByDistance(Renderable* prev, Renderable* item, int level)
{
    float itemDist = item->distanceFromCamera;
    Renderable* cur = prev->nextLevel;

#ifdef DEBUG_RENDER
    SceneObject* owner = item->renderData->owner_object();
    int itemOrder = item->renderModes.getRenderOrder();
    int itemShader = item->shader->getShaderID();
    const char* name = (owner ? owner->name().c_str() : "");
#endif

/*
* Add this item at the front of the list?
*/
    if ((cur == nullptr) || (itemDist > cur->distanceFromCamera))
    {
        item->nextSibling = cur;
        prev->nextLevel = item;
#ifdef DEBUG_RENDER
        LOGV("RENDER: Front distance: %s [%p] dist = %f order = %d shader = %d material = %p",
             name, item, itemDist, itemOrder, itemShader, item->material);
#endif
        return;
    }
    /*
     * Scan the list to see where it fits
     */
    ++level;
    while (1)
    {
        if (itemDist == cur->distanceFromCamera) // same distance from camera?
        {
            MERGEFUNC f = mMergeFunctions[level];

            if (f != nullptr)
            {
                if (cur->nextLevel == nullptr)  // no? add a listhead
                {
                    addListhead(cur);
                }
                (this->*f)(cur, item, level);   // add at next level
                return;
            }
        }
        prev = cur;
        cur = cur->nextSibling;
        if (cur == nullptr)
        {
            break;
        }
        if (itemDist >= cur->distanceFromCamera)
        {
            item->nextSibling = cur;
            prev->nextSibling = item;
#ifdef DEBUG_RENDER
            LOGV("RENDER: Middle distance: %s [%p] dist = %f order = %d shader = %d material = %p",
                 name, item, itemDist, itemOrder, itemShader, item->material);
#endif
            return;
        }
    }
    prev->nextSibling = item;
    item->nextSibling = nullptr;
#ifdef DEBUG_RENDER
    LOGV("RENDER: End distance: %s [%p] dist = %f order = %d shader = %d material = %p",
         name, item, itemDist, itemOrder, itemShader, item->material);
#endif
}

void MainSceneSorter::mergeByMode(Renderable* prev, Renderable* item, int level)
{
    Renderable* cur = prev->nextLevel;
    uint64_t itemFlags = item->renderModes.getRenderFlags();

#ifdef DEBUG_RENDER
    SceneObject* owner = item->renderData->owner_object();
    int itemOrder = item->renderModes.getRenderOrder();
    int itemShader = item->shader->getShaderID();
    const char* name = (owner ? owner->name().c_str() : "");
#endif

/*
* Add this item at the front of the list?
*/
    if ((cur == nullptr) || (itemFlags > cur->renderModes.getRenderFlags()))
    {
        item->nextSibling = cur;
        prev->nextLevel = item;
#ifdef DEBUG_RENDER
        LOGV("RENDER: Front distance: %s [%p] dist = %f order = %d shader = %d material = %p",
              name, item, item->distanceFromCamera, itemOrder, itemShader, item->material);
#endif
        return;
    }
    /*
     * Scan the list to see where it fits
     */
    ++level;
    while (1)
    {
        if (itemFlags == cur->renderModes.getRenderFlags()) // same render modes?
        {
            MERGEFUNC f = mMergeFunctions[level];

            if (f != nullptr)
            {
                if (cur->nextLevel == nullptr)  // no? add a listhead
                {
                    addListhead(cur);
                }
                (this->*f)(cur, item, level);   // add at next level
                return;
            }
        }
        prev = cur;
        cur = cur->nextSibling;
        if (cur == nullptr)
        {
            break;
        }
        if (itemFlags > cur->renderModes.getRenderFlags())
        {
            item->nextSibling = cur;
            prev->nextSibling = item;
#ifdef DEBUG_RENDER
            LOGV("RENDER: Middle distance: %s [%p] dist = %f order = %d shader = %d material = %p",
                 name, item, item->distanceFromCamera, itemOrder, itemShader, item->material);
#endif
            return;
        }
    }
    prev->nextSibling = item;
    item->nextSibling = nullptr;
#ifdef DEBUG_RENDER
    LOGV("RENDER: End distance: %s [%p] dist = %f order = %d shader = %d material = %p",
         name, item, item->distanceFromCamera, itemOrder, itemShader, item->material);
#endif
}
}
