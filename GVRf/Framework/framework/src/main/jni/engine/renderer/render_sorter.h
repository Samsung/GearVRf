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

#ifndef RENDER_SORTER_H_
#define RENDER_SORTER_H_

//#define DEBUG_CULL 1
//#define DEBUG_RENDER 1
//#define DEBUG_TRANSFORM 1

#include <vector>
#include <memory>
#include "glm/glm.hpp"
#include "objects/scene_object.h"
#include "objects/bounding_volume.h"
#include "util/gvr_jni.h"

namespace gvr {

class Camera;
class Scene;
class ShaderData;
class UniformBlock;
class RenderPass;
class RenderTarget;
class RenderState;
class RenderData;
class Mesh;
class Shader;
class Renderer;

/**
 * The RenderSorter performs view frustum culling, state sorting and controls
 * the order in which objects are rendered.
 *
 * During culling, the RenderSorter creates Renderable instances for
 * each visible SceneObject. The Renderables are kept in a memory pool
 * and used over and over each frame. After culling the Renderables
 * are sorted into a set of lists. The sorting step depends on what
 * type of RenderTarget is being rendered.
 * When sorting is complete, the lists are traversed and Renderables
 * are submitted to the Renderer to be drawn.
 *
 * Transforms may be accumulated in a UniformBlock or updated
 * with direct GL calls. The Shader class calculates the
 * transforms for the shader.
 *
 * @see MainSorter
 * @see Renderer
 * @see ShadowSorter
 */
class RenderSorter
{
public:
    /**
     * Renderable entity - typically corresponds to a RenderPass.
     * Some RenderTargets (like shadow maps) do not use the materials
     * and render modes from the RenderPass so we store these as well.
     * Renderables make up a tree which is sorted based on optimal
     * rendering order for the particular type of RenderTarget.
     */
    class Renderable
    {
    public:
        int             matrixOffset;           // offset of matrices in transform block
        float           distanceFromCamera;     // distance of object from camera
        RenderModes     renderModes;            // render modes to use
        RenderData*     renderData;             // original RenderData
        RenderPass*     renderPass;             // original RenderPass
        ShaderData*     material;               // material to use
        Shader*         shader;                 // shader to use
        Mesh*           mesh;                   // mesh to render
        glm::mat4       mvp;                    // model, view, projection
        UniformBlock*   transformBlock;         // uniform block with transforms
        Renderable*     nextLevel;              // next descendant
        Renderable*     nextSibling;            // next sibling

        Renderable()
        : nextLevel(nullptr),
          nextSibling(nullptr),
          renderData(nullptr),
          renderPass(nullptr),
          shader(nullptr),
          mesh(nullptr),
          material(nullptr),
          distanceFromCamera(0),
          matrixOffset(-1),
          transformBlock(nullptr)
        {
        }

        void reset()
        {
            renderData = nullptr;
            renderPass = nullptr;
            shader = nullptr;
            mesh = nullptr;
            material = nullptr;
            nextLevel = nullptr;
            nextSibling = nullptr;
            distanceFromCamera = 0;
            matrixOffset = -1;
            transformBlock = nullptr;
            renderModes.init();
        }
    };

    /**
     * Create a render sorter
     * @param renderer      Renderer to use for rendering
     * @param name          name of sorter
     * @param numMatrices   maximum number of matrices per transform block
     * @param forceTransformBlock use transform block for all shaders
     */
    RenderSorter(Renderer& renderer,
                 const char* name = "RenderSorter",
                 int numMatrices = 0,
                 bool forceTransformBlock = false);

    /**
     * Perform view frustum culling and accumulate the visible Renderables.
     * @param rstate    RenderState
     */
    virtual void cull(RenderState& rstate);

    /**
     * Initialize before sorting.
     * @param rstate    RenderState
     */
    virtual void init(RenderState& rstate);

    /**
     * Sort the list of Renderables optimally for rendering.
     * The default sort puts them all in a single list in
     * the order they were submitted. Subclasses can perform
     * more complex sorting.
     * @param rstate RenderState
     */
    virtual void sort(RenderState& rstate);

    /**
     * Submit the sorted Renderables to the renderer.
     * @param rstate RenderState
     */
    virtual void render(RenderState& rstate);

    /**
     * Clear the visible and render lists.
     * This is called every frame before culling.
     */
    virtual void clear();

    /**
     * Dump the objects to be rendered to the log.
     */
    void dump();

    /**
     * Get the Renderer used by this sorter
     * @return Renderer
     */
    Renderer& getRenderer()
    {
        return mRenderer;
    }

    virtual ~RenderSorter();

    /**
     * Add a renderable to be sorted.
     * @param r     Renderable to add
     * @return -> actual Renderable added
     */
    Renderable* add(RenderState&, Renderable& r);

private:
    /**
     * Build the view frustum for culling
     * @param frustum       array to be frustum planes
     * @param vp_matrix     view projection matrix
     */
    virtual void build_frustum(float frustum[6][4], const float *vp_matrix);

    /**
     * Cull a scene object and its descendants against the frustum planes.
     * @param rstate            RenderState
     * @param camera_position   position of camera
     * @param object            SceneObject to cull
     * @param frustum           view frustum planes
     * @param continue_cull     true to cull descendants
     * @param planeMask         cull plane mask
     */
    virtual void frustum_cull(RenderState& rstate,
                              glm::vec3 camera_position,
                              SceneObject *object,
                              float frustum[6][4],
                              bool continue_cull,
                              int planeMask);

    RenderSorter(RenderSorter&&) = delete;
    RenderSorter& operator=(const RenderSorter&) = delete;
    RenderSorter& operator=(RenderSorter&&) = delete;

protected:
    /**
     * Memory pool block header.
     * Renderables are allocated from a memory pool of
     * fixed size blocks which grow as needed.
     */
    struct BlockHeader
    {
        BlockHeader*    nextBlock;
        int             numElems;
    };

    /**
     * Allocate a new Renderable
     * @return -> Renderable instance
     */
    Renderable*     alloc();

    /**
     * Validate the accumulated renderables and regenerate
     * shaders if necessary
     * @param rstate RenderStatae
     */
    virtual void    validate(RenderState& rstate);

    /**
     * Merge sort this Renderable into the proper location.
     * @param item Renderable to sort
     */
    virtual void    merge(Renderable* item);

    /**
     * Add a visible SceneObject to the render list.
     * @param rstate    RenderState
     * @param object    SceneObject to add
     */
    virtual void    add(RenderState& rstate, SceneObject* object);

    /**
     * Select the native shader for a Renderable.
     * @param rstate    RenderState
     * @param r         Renderable
     * @return -> Shader or null if no shader generated yet
     */
    virtual Shader* selectShader(const RenderState& rstate, Renderable& r);

    /**
     * Determine if a Renderable is valid. If it does not have a proper
     * shader, mesh and material, it will not be rendered.
     * @param rstate    RenderState
     * @param r         Renderable
     * @return true if object should be rendered, else false
     */
    virtual bool    isValid(RenderState& rstate, Renderable& r);

    /**
     * Update the transform matrices for a Renderable.
     *
     * @param rstate    RenderState
     * @param r         Renderable
     */
    virtual void    updateTransform(RenderState& rstate, Renderable& r);

    /**
     * Submit a Renderable to be rendered.
     * @param rstate    RenderState
     * @param r         Renderable to render
     */
    virtual void    render(RenderState& rstate, const Renderable& r);

    /**
     * Dump a Renderable to the log.
     */
    virtual void    dump(const Renderable&, const std::string& pad) const;

    /**
     * Merge sort Renderable based on native shader
     * @param list  Renderable list to add to
     * @param item  Renderable item to add
     */
    virtual void    mergeByShader(Renderable* list, Renderable* item);

    /**
     * Copy new matrices to the current transform block.
     * If that block is exhausted, a new one is obtained.
     * @param r             Renderable
     * @param numMatrices   number of matrices to copy
     * @param matrixdata    matrix data to copy
     * @return -> UniformBlock used for transforms
     */
    UniformBlock*   updateTransformBlock(Renderable& r, int numMatrices, const float* matrixdata);

    /**
     * Add a listhead to the Renderable tree
     * @param cur Renderable to become the first list item
     */
    void            addListhead(Renderable* cur);

    /**
     * Find a Renderable in the render tree
     * @param root      root of render tree
     * @param findme    Renderable to find
     * @return true if found, false if not.
     */
    bool            findRenderable(const Renderable* root, const Renderable* findme) const;

    std::string     mName;                  // name of RenderSorter (used in logging)
    Renderable      mRenderList;            // root of render tree
    Renderer&       mRenderer;              // Renderer to use for rendering
    int             mMaxMatricesPerBlock;   // max size of transform block
    int             mTransBlockIndex;       // transform block index
    int             mNumMatricesInBlock;    // size of current transform block
    BlockHeader*    mMemoryPool;            // first block in Renderable memory pool
    BlockHeader*    mCurBlock;              // current block in memory pool
    int             mMaxElems;              // maximum number of elements in memory pool block
    int             mVisibleElems;          // current number of visible elements
    bool            mForceTransformBlock;   // force all shaders to use the transform block
    glm::mat4       mOutputMatrices[10];    // temporary output matrix storage
    std::vector<UniformBlock*> mTransformBlocks; // list of transform blocks
};


}
#endif
