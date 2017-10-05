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

package org.gearvrf;

import org.gearvrf.GVRAndroidResource.MeshCallback;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader.FutureResource;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.Threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.opengl.GLES30.GL_LINES;
import static android.opengl.GLES30.GL_LINE_LOOP;
import static android.opengl.GLES30.GL_LINE_STRIP;
import static android.opengl.GLES30.GL_POINTS;
import static android.opengl.GLES30.GL_TRIANGLES;
import static android.opengl.GLES30.GL_TRIANGLE_FAN;
import static android.opengl.GLES30.GL_TRIANGLE_STRIP;

/**
 * Encapsulates the data associated with rendering a mesh.
 *
 * This includes the {@link GVRMesh mesh} itself, the mesh's {@link GVRMaterial
 * material}, camera association, rendering order, and various other parameters.
 */
public class GVRRenderData extends GVRJavaComponent implements IRenderable, PrettyPrint {

    private GVRMesh mMesh;
    private ArrayList<GVRRenderPass> mRenderPassList;
    private static final String TAG = "GearVRf";
    private GVRLight mLight;
    private boolean mLightMapEnabled;
    private boolean isLightEnabled;
    private HashMap<String, Integer> mShaderFeatures = new HashMap<String, Integer>();

    /**
     * Rendering hints.
     *
     * You might expect the rendering process to sort the scene graph, from back
     * to front, so it can then draw translucent objects over the objects behind
     * them. But that's not how GVRF works. Instead, it sorts the scene graph by
     * render order, then draws the sorted graph in traversal order. (Please
     * don't waste your time getting angry or trying to make sense of this;
     * please just take it as a bald statement of How GVRF Currently Works.)
     *
     * <p>
     * The point is, to get transparency to work as you expect, you do need to
     * explicitly call {@link GVRRenderData#setRenderingOrder(int)
     * setRenderingOrder():} objects are sorted from low render order to high
     * render order, so that a {@link #GEOMETRY} object will show through a
     * {@link #TRANSPARENT} object.
     */
    public abstract static class GVRRenderingOrder {
        public static final int STENCIL = -1000;
        /**
         * Rendered first, below any other objects at the same distance from the
         * camera
         */
        public static final int BACKGROUND = 1000;
        /**
         * The default render order, if you don't explicitly call
         * {@link GVRRenderData#setRenderingOrder(int)}
         */
        public static final int GEOMETRY = 2000;
        /** The rendering order for see-through objects */
        public static final int TRANSPARENT = 3000;
        /** The rendering order for sprites {@literal &c.} */
        public static final int OVERLAY = 4000;
    };

    /** Items for the rendering options bit mask. */
    public abstract static class GVRRenderMaskBit {
        /**
         * Render the mesh in the left {@link GVRCamera camera}.
         */
        public static final int Left = 0x1;
        /**
         * Render the mesh in the right {@link GVRCamera camera}.
         */
        public static final int Right = 0x2;
    }

    /**
     * Constructor.
     *
     * @param gvrContext
     *            Current {@link GVRContext}
     */
    public GVRRenderData(GVRContext gvrContext) {
        super(gvrContext, NativeRenderData.ctor());
        GVRRenderPass basePass = new GVRRenderPass(gvrContext);
        mRenderPassList = new ArrayList<GVRRenderPass>();
        addPass(basePass);
        isLightEnabled = true;
        mLightMapEnabled = false;
    }

    public GVRRenderData(GVRContext gvrContext, GVRMaterial material) {
        super(gvrContext, NativeRenderData.ctor());
        setOwnerObject(owner);
        GVRRenderPass basePass = new GVRRenderPass(gvrContext, material);
        isLightEnabled = true;
        mLightMapEnabled = false;
        mRenderPassList = new ArrayList<GVRRenderPass>();
        addPass(basePass);
    }


    static public long getComponentType() {
        return NativeRenderData.getComponentType();
    }

    /**
     * @return The {@link GVRMesh mesh} being rendered. If there is
     * a pending future mesh, it is resolved.
     */
    public GVRMesh getMesh() {
        return mMesh;
    }

    /**
     * Set the {@link GVRMesh mesh} to be rendered.
     *
     * @param mesh
     *            The mesh to be rendered.
     */
    public void setMesh(GVRMesh mesh) {
        GVRMesh oldMesh = mMesh;
        synchronized (this) {
            mMesh = mesh;
            for (GVRRenderPass pass : mRenderPassList)
            {
                pass.setMesh(mesh);
            }
        }
        NativeRenderData.setMesh(getNative(), mesh.getNative());
    }

    /**
     * Add a render {@link GVRRenderPass pass} to this RenderData.
     * @param pass
     */
    public void addPass(GVRRenderPass pass) {
        GVRMesh mesh = getMesh();
        mRenderPassList.add(pass);
        pass.setMesh(mesh);
        NativeRenderData.addPass(getNative(), pass.getNative());
    }

    /**
     * Remove a render {@link GVRRenderPass pass} to this RenderData.
     * @param passNum 0 based index of pass to remove
     */
    public void removePass(int passNum) {
        mRenderPassList.remove(passNum);
        NativeRenderData.removePass(getNative(), passNum);
    }

    /**
     * Get a Rendering {@link GVRRenderPass Pass} for this Mesh
     * @param passIndex The index of the RenderPass to get.
     */
    public GVRRenderPass getPass(int passIndex) {
        if (passIndex < mRenderPassList.size()) {
            return mRenderPassList.get(passIndex);
        } else {
            Log.e(TAG, "Trying to get invalid pass. Pass " + passIndex + " was not created.");
            return null;
        }
    }

    /**
     * Get the number of render passes
     * @return number of render passes
     */
    public int getPassCount() { return mRenderPassList.size(); }

    /**
     * @return The {@link GVRMaterial material} the {@link GVRMesh mesh} is
     *         being rendered with.
     */
    public GVRMaterial getMaterial() {
        return getMaterial(0);
    }

    /**
     * @param passIndex The {@link GVRRenderPass pass} index to retrieve material from.
     * @return The {@link GVRMaterial material} the {@link GVRMesh mesh} is
     *         being rendered with.
     */
    public GVRMaterial getMaterial(int passIndex)
    {
        if (passIndex < mRenderPassList.size()) {
            return mRenderPassList.get(passIndex).getMaterial();
        } else {
            Log.e(TAG, "Trying to get material from invalid pass. Pass " + passIndex + " was not created.");
            return null;
        }
    }

    /**
     * Set the {@link GVRMaterial material} the mesh will be rendered with.
     *
     * @param material
     *            The {@link GVRMaterial material} for rendering.
     */
    public void setMaterial(GVRMaterial material) {
        setMaterial(material, 0);
    }

    /**
     * Set the {@link GVRMaterial material} this pass will be rendered with.
     *
     * @param material
     *            The {@link GVRMaterial material} for rendering.
     * @param passIndex
     *            The rendering pass this material will be assigned to.
     *
     */
    public void setMaterial(GVRMaterial material, int passIndex)
    {
        GVRMaterial oldmtl = getMaterial(passIndex);
        if (passIndex < mRenderPassList.size())
        {
            mRenderPassList.get(passIndex).setMaterial(material);
        }
        else
        {
            Log.e(TAG, "Trying to set material from invalid pass. Pass " + passIndex + " was not created.");
        }
    }

    public void setShader(int shader, boolean useMultiview)
    {
        GVRRenderPass pass = mRenderPassList.get(0);
        pass.setShader(shader, useMultiview);
    }

    int getShader(boolean useMultiview)
    {
        GVRRenderPass pass = mRenderPassList.get(0);
        return pass.getShader(useMultiview);
    }

    /**
     * Set the shader template to use for rendering the mesh.
     *
     * A shader template generates the vertex and fragment shader
     * based on the mesh attributes, textures bound to the material
     * and light sources. The shader is generated by {@linkplain #bindShader(GVRScene)} bindShader }
     * which is called automatically after initialization.
     *
     * @param templateClass subclass of GVRShaderTemplate describing the
     *        shader to use for rendering the mesh
     * @see GVRShaderTemplate
     * @deprecated does nothing currently, GVRShaderId now contains the shader template
     */
    public void setShaderTemplate(Class<? extends GVRShaderTemplate> templateClass) {
    }

    /**
     * Selects a specific vertex and fragment shader to use for rendering.
     *
     * If a shader template has been specified, it is used to generate
     * a vertex and fragment shader based on mesh attributes, bound textures
     * and light sources. If the textures bound to the material are changed
     * or a new light source is added, this function must be called again
     * to select the appropriate shaders. This function may cause recompilation
     * of shaders which is quite slow.
     *
     * @param scene scene being rendered
     * @see GVRShaderTemplate GVRMaterialShader.getShaderType
     */
    public synchronized void bindShader(GVRScene scene, boolean isMultiview)
    {
        GVRRenderPass pass = mRenderPassList.get(0);
        GVRShaderId shader = pass.getMaterial().getShaderType();
        GVRShader template = shader.getTemplate(getGVRContext());
        if (template != null)
        {
            template.bindShader(getGVRContext(), this, scene, isMultiview);
        }
        for (int i = 1; i < mRenderPassList.size(); ++i)
        {
            pass = mRenderPassList.get(i);
            shader = pass.getMaterial().getShaderType();
            template = shader.getTemplate(getGVRContext());
            if (template != null)
            {
                template.bindShader(getGVRContext(), pass, scene, isMultiview);
            }
        }
    }
    /**
     * Selects a specific vertex and fragment shader to use for rendering.
     *
     * If a shader template has been specified, it is used to generate
     * a vertex and fragment shader based on mesh attributes, bound textures
     * and light sources. If the textures bound to the material are changed
     * or a new light source is added, this function must be called again
     * to select the appropriate shaders. This function may cause recompilation
     * of shaders which is quite slow.
     *
     * @param scene scene being rendered
     * @see GVRShaderTemplate GVRMaterialShader.getShaderType
     */
    public synchronized void bindShader(GVRScene scene)
    {
        GVRRenderPass pass = mRenderPassList.get(0);
        GVRShaderId shader = pass.getMaterial().getShaderType();
        GVRShader template = shader.getTemplate(getGVRContext());
        boolean isMultiview = getGVRContext().getActivity().getAppSettings().isMultiviewSet();
        if (template != null)
        {
            template.bindShader(getGVRContext(), this, scene, isMultiview);
        }
        for (int i = 1; i < mRenderPassList.size(); ++i)
        {
            pass = mRenderPassList.get(i);
            shader = pass.getMaterial().getShaderType();
            template = shader.getTemplate(getGVRContext());
            if (template != null)
            {
                template.bindShader(getGVRContext(), pass, scene, isMultiview);
            }
        }
    }

    private static Runnable sBindShaderFromNative = null;

    /**
     * Called from the GL thread during rendering when a
     * RenderData without a valid shader is encountered.
     */
    void bindShaderNative(GVRScene scene, boolean isMultiview)
    {
        bindShader(scene, isMultiview);
        /*
        if (sBindShaderFromNative == null)
        {
            sBindShaderFromNative = new Runnable()
            {
                public void run()
                {
                    bindShader(scene);
                }
            };
        }
        getGVRContext().runOnTheFrameworkThread(sBindShaderFromNative);
        */
    }

    /**
     * Enable lighting effect for the render_data. Note that it is different from
     * GVRLight.enable(). GVRLight.enable turns on a light, while this method
     * enables the lighting effect for the render_data.
     */
    public void enableLight()
    {
        if (!isLightEnabled)
        {
            NativeRenderData.enableLight(getNative());
            isLightEnabled = true;
            bindShader(getGVRContext().getMainScene());
        }
    }


    /**
     * Disable lighting effect for the render_data.
     * Note that it is different from GVRLight.disable().
     * GVRLight.disable turns off a light, while this method
     * disables the lighting effect for the render_data.
     */
    public void disableLight()
    {
        if (isLightEnabled)
        {
            NativeRenderData.disableLight(getNative());
            bindShader(null);
            isLightEnabled = false;
        }
    }

    /**
     * Enable lighting map effect for the render_data.
     */
    public void enableLightMap() {

        NativeRenderData.enableLightMap(getNative());
        mLightMapEnabled = true;
    }

    /**
     * Disable lighting map effect for the render_data.
     */
    public void disableLightMap() {

        NativeRenderData.disableLightMap(getNative());
        mLightMapEnabled = false;
    }

    /**
     * Get the enable/disable status for the lighting effect. Note that it is
     * different to enable/disable status of the light. The lighting effect is
     * applied if and only if {@code mLight} is enabled (i.e. on) AND the
     * lighting effect is enabled for the render_data.
     *
     * @return true if lighting effect is enabled, false if lighting effect is
     *         disabled.
     */
    public boolean isLightEnabled() {
        return isLightEnabled;
    }

    /**
     * Get the enable/disable status for the lighting map effect.
     *
     * @return true if lighting map effect is enabled, otherwise returns false.
     */
    public boolean isLightMapEnabled() {
        return mLightMapEnabled;
    }

    /**
     * Get the rendering options bit mask.
     *
     * @return The rendering options bit mask.
     * @see GVRRenderMaskBit
     */
    public int getRenderMask() {
        return NativeRenderData.getRenderMask(getNative());
    }

    /**
     * Set the rendering options bit mask.
     *
     * @param renderMask
     *            The rendering options bit mask.
     * @see GVRRenderMaskBit
     */
    public GVRRenderData setRenderMask(int renderMask) {
        NativeRenderData.setRenderMask(getNative(), renderMask);
        return this;
    }

    /**
     * @return The order in which this mesh will be rendered.
     * @see GVRRenderingOrder
     */
    public int getRenderingOrder() {
        return NativeRenderData.getRenderingOrder(getNative());
    }

    /**
     * Set the order in which this mesh will be rendered.
     *
     * @param renderingOrder
     *            See {@link GVRRenderingOrder}
     */
    public GVRRenderData setRenderingOrder(int renderingOrder) {
        NativeRenderData.setRenderingOrder(getNative(), renderingOrder);
        return this;
    }

    /**
     * @deprecated Use {@code getCullFace() } instead.
     * @see #getCullFace()
     * @return {@code true} if {@code GL_CULL_FACE} is enabled, {@code false} if
     *         not.
     */
    public boolean getCullTest() {
        return getCullFace(0) != GVRCullFaceEnum.None;
    }

    /**
     * @return current face to be culled See {@link GVRCullFaceEnum}.
     */
    public GVRCullFaceEnum getCullFace() {
        return getCullFace(0);
    }

    /**
     * @param passIndex
     *            The rendering pass index to query cull face state.
     * @return current face to be culled See {@link GVRCullFaceEnum}.
     */
    public GVRCullFaceEnum getCullFace(int passIndex) {
        if (passIndex < mRenderPassList.size()) {
            return mRenderPassList.get(passIndex).getCullFace();
        } else {
            Log.e(TAG, "Trying to get cull face from invalid pass. Pass " + passIndex + " was not created.");
            return GVRCullFaceEnum.Back;
        }
    }

    /**
     * @deprecated Use {@code setCullFace(GVRCullFaceEnum cullFace)} instead.
     * Set the {@code GL_CULL_FACE} option
     *
     * @param cullTest
     *            {@code true} if {@code GL_CULL_FACE} should be enabled,
     *            {@code false} if not.
     *
     * @see GVRRenderData#setCullFace
     */
    public GVRRenderData setCullTest(boolean cullTest) {
        if (cullTest) {
            setCullFace(GVRCullFaceEnum.Back);
        } else {
            setCullFace(GVRCullFaceEnum.None);
        }
        return this;
    }

    /**
     * Set the face to be culled
     *
     * @param cullFace
     *            {@code GVRCullFaceEnum.Back} Tells Graphics API to discard
     *            back faces, {@code GVRCullFaceEnum.Front} Tells Graphics API
     *            to discard front faces, {@code GVRCullFaceEnum.None} Tells
     *            Graphics API to not discard any face
     */
    public GVRRenderData setCullFace(GVRCullFaceEnum cullFace) {
        setCullFace(cullFace, 0); return this;
    }

    /**
     * Set the face to be culled
     *
     * @param cullFace
     *            {@code GVRCullFaceEnum.Back} Tells Graphics API to discard
     *            back faces, {@code GVRCullFaceEnum.Front} Tells Graphics API
     *            to discard front faces, {@code GVRCullFaceEnum.None} Tells
     *            Graphics API to not discard any face
     * @param passIndex
     *            The rendering pass to set cull face state
     */
    public GVRRenderData setCullFace(GVRCullFaceEnum cullFace, int passIndex) {
        if (passIndex < mRenderPassList.size()) {
            mRenderPassList.get(passIndex).setCullFace(cullFace);
        } else {
            Log.e(TAG, "Trying to set cull face to a invalid pass. Pass " + passIndex + " was not created.");
        }
        return this;
    }

    /**
     * @return {@code true} if {@code GL_POLYGON_OFFSET_FILL} is enabled,
     *         {@code false} if not.
     */
    public boolean getOffset() {
        return NativeRenderData.getOffset(getNative());
    }

    /**
     * Set the {@code GL_POLYGON_OFFSET_FILL} option
     *
     * @param offset
     *            {@code true} if {@code GL_POLYGON_OFFSET_FILL} should be
     *            enabled, {@code false} if not.
     */
    public GVRRenderData setOffset(boolean offset) {
        NativeRenderData.setOffset(getNative(), offset);
        return this;
    }

    /**
     * @return The {@code factor} value passed to {@code glPolygonOffset()} if
     *         {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * @see #setOffset(boolean)
     */
    public float getOffsetFactor() {
        return NativeRenderData.getOffsetFactor(getNative());
    }

    /**
     * Set the {@code factor} value passed to {@code glPolygonOffset()} if
     * {@code GL_POLYGON_OFFSET_FILL} is enabled.
     *
     * @param offsetFactor
     *            Per OpenGL docs: Specifies a scale factor that is used to
     *            create a variable depth offset for each polygon. The initial
     *            value is 0.
     * @see #setOffset(boolean)
     */
    public GVRRenderData setOffsetFactor(float offsetFactor) {
        NativeRenderData.setOffsetFactor(getNative(), offsetFactor);
        return this;
    }

    /**
     * @return The {@code units} value passed to {@code glPolygonOffset()} if
     *         {@code GL_POLYGON_OFFSET_FILL} is enabled.
     * @see #setOffset(boolean)
     */
    public float getOffsetUnits() {
        return NativeRenderData.getOffsetUnits(getNative());
    }

    /**
     * Set the {@code units} value passed to {@code glPolygonOffset()} if
     * {@code GL_POLYGON_OFFSET_FILL} is enabled.
     *
     * @param offsetUnits
     *            Per OpenGL docs: Is multiplied by an implementation-specific
     *            value to create a constant depth offset. The initial value is
     *            0.
     * @see #setOffset(boolean)
     */
    public GVRRenderData setOffsetUnits(float offsetUnits) {
        NativeRenderData.setOffsetUnits(getNative(), offsetUnits);
        return this;
    }

    /**
     * @return {@code true} if {@code GL_DEPTH_TEST} is enabled, {@code false}
     *         if not.
     */
    public boolean getDepthTest() {
        return NativeRenderData.getDepthTest(getNative());
    }

    /**
     * Set the {@code GL_DEPTH_TEST} option
     *
     * @param depthTest
     *            {@code true} if {@code GL_DEPTH_TEST} should be enabled,
     *            {@code false} if not.
     */
    public GVRRenderData setDepthTest(boolean depthTest) {
        NativeRenderData.setDepthTest(getNative(), depthTest);
        return this;
    }

    /** Set the glDepthMask option
     *
     *
     * @param depthMask
     *            {@code true} if glDepthMask should be enabled,
     *            {@code false} if not.
     */

    public GVRRenderData setDepthMask(boolean depthMask) {
        NativeRenderData.setDepthMask(getNative(), depthMask);
        return this;
    }

    /**
     * @return {@code true} if {@code GL_BLEND} is enabled, {@code false} if
     *         not.
     */
    public boolean getAlphaBlend() {
        return NativeRenderData.getAlphaBlend(getNative());
    }

    /**
     * Set the {@code GL_BLEND} option
     *
     * @param alphaBlend
     *            {@code true} if {@code GL_BLEND} should be enabled,
     *            {@code false} if not.
     */
    public GVRRenderData setAlphaBlend(boolean alphaBlend) {
        NativeRenderData.setAlphaBlend(getNative(), alphaBlend);
        return this;
    }

    /**
     * Sets the blend functions for alpha blending between the
     * screen (destination) and the texture (source) alpha.
     * The blend functions are symbolic constants that
     * specify how to scale the source and
     * destination before they are added together:
     * <p/>
     * destAlpha = destBlendFunc * destAlpha + sourceBlendFunc * sourceAlpha
     * <p/>
     * The functions are defined the same way as in OpenGL.
     * The source blend function defaults to GL_ONE,
     * the destination blend function defaults to GL_ONE_MINUS_SRC_ALPHA.
     *
     * @param sourceBlendFunc blend function for source
     * @param destBlendFunc   blend function for destination
     *  <p>
     *   The supported functions are: GL_ZERO, GL_ONE, GL_SRC_ALPHA, GL_DEST_ALPHA,
     *   GL_ONE_MINUS_SRC_ALPHA, GL_ONE_MINUS_DEST_ALPHA, GL_SRC_ALPHA_SATURATE.
     *   Color blending or blending with a constant alpha are not supported.
     *  </p>
     *
     * @see #getSourceAlphaBlendFunc() #getDestBlendFunc() #setAlphaBlend(boolean)
     */
    public void setAlphaBlendFunc(int sourceBlendFunc, int destBlendFunc)
    {
        NativeRenderData.setAlphaBlendFunc(getNative(), sourceBlendFunc, destBlendFunc);
    }

    /**
     * Gets the source blend function for alpha blending.
     * The blend functions are symbolic constants that
     * specify how to scale the source and
     * destination before they are added together:
     * <p/>
     * destAlpha = destBlendFunc * destAlpha + sourceBlendFunc * sourceAlpha
     * <p/>
     * The functions are defined the same way as in OpenGL.
     * The source blend function defaults to GL_ONE.
     * @return constant value for source blend function
     * @see #setAlphaBlendFunc(int, int) #getDestBlendFunc()
     */
    public int getSourceAlphaBlendFunc()
    {
        return NativeRenderData.getSourceAlphaBlendFunc(getNative());
    }

    /**
     * Gets the destination blend function for alpha blending.
     * The blend functions are symbolic constants that
     * specify how to scale the source and
     * destination before they are added together:
     * <p/>
     * destAlpha = destBlendFunc * destAlpha + sourceBlendFunc * sourceAlpha
     * <p/>
     * The functions are defined the same way as in OpenGL.
     * The destination blend function defaults to GL_ONE_MINUS_SRC_ALPHA.
     *
     * @return constant value for source blend function
     * @see #setAlphaBlendFunc(int, int) #getSourceAlphaBlendFunc()
     */
    public int getDestAlphaBlendFunc()
    {
        return NativeRenderData.getDestAlphaBlendFunc(getNative());
    }

    /**
     * @return {@code true} if {@code GL_ALPHA_TO_COVERAGE} is enabled, {@code false} if not
     */
    public boolean getAlphaToCoverage() {
        return NativeRenderData.getAlphaToCoverage(getNative());
    }

    /**
     * @param alphaToCoverage
     *            {@code true} if {@code GL_ALPHA_TO_COVERAGE} should be enabled,
     *            {@code false} if not.
     */
    public GVRRenderData setAlphaToCoverage(boolean alphaToCoverage) {
        NativeRenderData.setAlphaToCoverage(getNative(), alphaToCoverage);
        return this;
    }

    /**
     * @return value of sample coverage
     */
    public float getSampleCoverage(){
        return NativeRenderData.getSampleCoverage(getNative());
    }
    /**
     * @param sampleCoverage
     *                 Specifies the coverage of the modification mask.
     */
    public GVRRenderData setSampleCoverage(float sampleCoverage) {
        NativeRenderData.setSampleCoverage(getNative(),sampleCoverage);
        return this;
    }

    /**
     * @return whether the modification mask implied by value is inverted or not
     */
    public boolean getInvertCoverageMask(){
        return NativeRenderData.getInvertCoverageMask(getNative());
    }

    /**
     * @param invertCoverageMask
     *          Specifies whether the modification mask implied by value is inverted or not.
     */
    public GVRRenderData setInvertCoverageMask(boolean invertCoverageMask){
        NativeRenderData.setInvertCoverageMask(getNative(),invertCoverageMask);
        return this;
    }

    /**
     * @return The OpenGL draw mode (e.g. GL_TRIANGLES).
     */
    public int getDrawMode() {
        return NativeRenderData.getDrawMode(getNative());
    }

    /**
     * Set the draw mode for this mesh. Default is GL_TRIANGLES.
     *
     * @param drawMode
     */
    public GVRRenderData setDrawMode(int drawMode) {
        if (drawMode != GL_POINTS && drawMode != GL_LINES
                && drawMode != GL_LINE_STRIP && drawMode != GL_LINE_LOOP
                && drawMode != GL_TRIANGLES && drawMode != GL_TRIANGLE_STRIP
                && drawMode != GL_TRIANGLE_FAN) {
            throw new IllegalArgumentException(
                    "drawMode must be one of GL_POINTS, GL_LINES, GL_LINE_STRIP, GL_LINE_LOOP, GL_TRIANGLES, GL_TRIANGLE_FAN, GL_TRIANGLE_STRIP.");
        }
        NativeRenderData.setDrawMode(getNative(), drawMode);
        return this;
    }

    /**
     * Set the capturer for this texture. If capturer is null, the existing capturer
     * is removed. Whether the capturer takes effect depends on the shader associated
     * with {@code GVRMaterial}. In order to support the texture capturer, a native
     * shader should check {@code RenderData::get_texture_capturer}. See {@code
     * ExternalRendererShader} as an example.
     *
     * @param capturer The capturer.
     */
    public void setTextureCapturer(GVRTextureCapturer capturer) {
        if (capturer != null) {
            NativeRenderData.setTextureCapturer(getNative(), capturer.getNative());
        } else {
            NativeRenderData.setTextureCapturer(getNative(), 0);
        }
    }

    /**
     * Checks if a renderable object can cast shadows.
     * @return true if shadows are cast, false if not.
     * @see #setCastShadows(boolean)
     * @see GVRLightBase#getCastShadow()
     */
    public boolean getCastShadows() {
        return NativeRenderData.getCastShadows(getNative());
    }

    /**
     * Indicates whether a renderable object will cast shadows or not.
     * By default, all objects cast shadows. Transparent objects cast
     * non-transparent shadows. This function lets you disable shadow-casting.
     * @param castShadows true to cast shadows, false to not cast shadows
     */
    public GVRRenderData setCastShadows(boolean castShadows) {
        NativeRenderData.setCastShadows(getNative(), castShadows);
        return this;
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        if (mMesh != null) {
            sb.append(Log.getSpaces(indent));
            sb.append("Mesh: ");
            mMesh.prettyPrint(sb, indent);
        }
        sb.append(Log.getSpaces(indent));
        sb.append("Light: " + mLight);
        sb.append(System.lineSeparator());

        sb.append(Log.getSpaces(indent));
        sb.append("Light enabled: " + isLightEnabled);
        sb.append(System.lineSeparator());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }

    /**
     * See https://www.khronos.org/opengles/sdk/docs/man3/html/glStencilFunc.xhtml
     */
    public GVRRenderData setStencilFunc(int func, int ref, int mask) {
        NativeRenderData.setStencilFunc(getNative(), func, ref, mask);
        return this;
    }

    /**
     * See https://www.khronos.org/opengles/sdk/docs/man/xhtml/glStencilOp.xml
     */
    public GVRRenderData setStencilOp(int fail, int zfail, int zpass) {
        NativeRenderData.setStencilOp(getNative(), fail, zfail, zpass);
        return this;
    }

    /**
     * See https://www.khronos.org/opengles/sdk/docs/man/xhtml/glStencilMask.xml
     */
    public GVRRenderData setStencilMask(int mask) {
        NativeRenderData.setStencilMask(getNative(), mask);
        return this;
    }

    /**
     * @param flag enable or disable stencil testing; disabled by default
     */
    public GVRRenderData setStencilTest(boolean flag) {
        NativeRenderData.setStencilTest(getNative(), flag);
        return this;
    }
}

class NativeRenderData {
    static native long ctor();

    static native long getComponentType();

    static native void setMesh(long renderData, long mesh);

    static native void addPass(long renderData, long renderPass);

    static native void removePass(long renderData, long renderPass);

    static native void enableLight(long renderData);

    static native void disableLight(long renderData);

    static native void enableLightMap(long renderData);

    static native void disableLightMap(long renderData);

    static native int getRenderMask(long renderData);

    static native void setRenderMask(long renderData, int renderMask);

    static native int getRenderingOrder(long renderData);

    static native void setRenderingOrder(long renderData, int renderingOrder);

    static native boolean getOffset(long renderData);

    static native void setOffset(long renderData, boolean offset);

    static native float getOffsetFactor(long renderData);

    static native void setOffsetFactor(long renderData, float offsetFactor);

    static native float getOffsetUnits(long renderData);

    static native void setOffsetUnits(long renderData, float offsetUnits);

    static native boolean getDepthTest(long renderData);

    static native void setDepthTest(long renderData, boolean depthTest);

    static native void setDepthMask(long renderData, boolean depthMask);

    static native boolean getAlphaBlend(long renderData);

    static native void setAlphaBlend(long renderData, boolean alphaBlend);

    static native void setAlphaBlendFunc(long renderData, int sourceBlend, int destBlend);

    static native int getSourceAlphaBlendFunc(long renderData);

    static native int getDestAlphaBlendFunc(long renderData);

    static native boolean getAlphaToCoverage(long renderData);

    static native void setAlphaToCoverage(long renderData, boolean alphaToCoverage);

    static native float getSampleCoverage(long renderData);

    static native void setSampleCoverage(long renderData,float sampleCoverage);

    static native boolean getInvertCoverageMask(long renderData);

    static native void setInvertCoverageMask(long renderData,boolean invertCoverageMask);

    static native int getDrawMode(long renderData);

    static native void setDrawMode(long renderData, int draw_mode);

    static native void setTextureCapturer(long renderData, long texture_capturer);

    static native void setCastShadows(long renderData, boolean castShadows);

    static native boolean getCastShadows(long renderData);

    static native void setStencilFunc(long renderData, int func, int ref, int mask);

    static native void setStencilOp(long renderData, int fail, int zfail, int zpass);

    static native void setStencilMask(long renderData, int mask);

    static native void setStencilTest(long renderData, boolean flag);
}
