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

import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.debug.GVRConsole;
import org.gearvrf.script.IScriptable;
import org.gearvrf.utility.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains a the hierarchy of visible objects, a camera and processes events.
 * 
 * The scene receives events defined in {@link ISceneEvents} and {@link IPickEvents}.
 * To add a listener to these events, use the
 * following code:
 * <pre>
 *     ISceneEvents mySceneEventListener = new ISceneEvents() {
 *         ...
 *     };
 *     getEventReceiver().addListener(mySceneEventListener);
 * </pre>
 * 
 * The scene graph is a hierarchy with all the objects to display.
 * It has a single root and one main camera rig.
 * Each visible object, or node, in the scene graph is a {@link GVRSceneObject}
 * with a {@link GVRTransform} component that places and orients it in space
 * with respect to it's parent node. A node can have multiple children,
 * and a child inherits the concatenation of all of it's ancestors transformations.
 * @see GVRSceneObject
 * @see GVRTransform
 * @see IEventReceiver
 * @see ISceneEvents
 */
public class GVRScene extends GVRHybridObject implements PrettyPrint, IScriptable, IEventReceiver {
    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(GVRScene.class);
    public static final int MAX_LIGHTS = 16;
    private GVRCameraRig mMainCameraRig;
    private StringBuilder mStatMessage = new StringBuilder();
    private Set<GVRLightBase> mLightList = new HashSet<GVRLightBase>();
    private GVREventReceiver mEventReceiver = new GVREventReceiver(this);
    private GVRMaterial mShadowMaterial = null;
    private boolean mShadowMapDirty = true;
    private GVRSceneObject mSceneRoot;
    
    /**
     * Constructs a scene with a camera rig holding left & right cameras in it.
     * 
     * @param gvrContext
     *            {@link GVRContext} the app is using.
     */
    public GVRScene(GVRContext gvrContext) {
        super(gvrContext, NativeScene.ctor());
        mSceneRoot = new GVRSceneObject(gvrContext);
        NativeScene.addSceneObject(getNative(), mSceneRoot.getNative());
        GVRCamera leftCamera = new GVRPerspectiveCamera(gvrContext);
        leftCamera.setRenderMask(GVRRenderMaskBit.Left);

        GVRCamera rightCamera = new GVRPerspectiveCamera(gvrContext);
        rightCamera.setRenderMask(GVRRenderMaskBit.Right);

        GVRPerspectiveCamera centerCamera = new GVRPerspectiveCamera(gvrContext);
        centerCamera.setRenderMask(GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);

        GVRCameraRig cameraRig = new GVRCameraRig(gvrContext);
        cameraRig.attachLeftCamera(leftCamera);
        cameraRig.attachRightCamera(rightCamera);
        cameraRig.attachCenterCamera(centerCamera);

        addSceneObject(cameraRig.getOwnerObject());

        setMainCameraRig(cameraRig);
        setFrustumCulling(true);

        getEventReceiver().addListener(mSceneEventListener);
    }

    private GVRScene(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
        mSceneRoot = new GVRSceneObject(gvrContext);
        NativeScene.addSceneObject(getNative(), mSceneRoot.getNative());
        setFrustumCulling(true);
    }

    /**
     * Add a {@linkplain GVRSceneObject scene object} as
     * a child of the scene root.
     * 
     * @param sceneObject
     *            The {@linkplain GVRSceneObject scene object} to add.
     */
    public void addSceneObject(GVRSceneObject sceneObject) {
        mSceneRoot.addChildObject(sceneObject);
        bindShaders(sceneObject);
    }

    /**
     * Remove a {@linkplain GVRSceneObject scene object} from
     * the scene root.
     * 
     * @param sceneObject
     *            The {@linkplain GVRSceneObject scene object} to remove.
     */
    public void removeSceneObject(GVRSceneObject sceneObject) {
        mSceneRoot.removeChildObject(sceneObject);
    }

    /**
     * Remove all scene objects.
     */
    public void removeAllSceneObjects() {
    	GVRCameraRig rig = getMainCameraRig();
        GVRSceneObject head = rig.getOwnerObject();
        rig.removeAllChildren();
        head.getParent().removeChildObject(head);
        NativeScene.removeAllSceneObjects(getNative());
        mLightList.clear();
        mSceneRoot = new GVRSceneObject(getGVRContext());
        mSceneRoot.addChildObject(head);
        NativeScene.addSceneObject(getNative(), mSceneRoot.getNative());
    }

    /**
     * Clears the scene and resets the scene to initial state.
     * Currently, it only removes all scene objects.
     */
    public void clear() {
        removeAllSceneObjects();
    }

    /**
     * Get the root of the scene hierarchy.
     * This node is a common ancestor to all the objects
     * in the scene.
     * @return top level scene object.
     * @see addSceneObject
     * @see removeSceneObject
     */
    public GVRSceneObject getRoot() {
        return mSceneRoot;
    }
    
    /**
     * The top-level scene objects (children of the root node).
     * 
     * @return A read-only list containing all direct children of the root node.
     *
     * @since 2.0.0
     * @see getRoot
     * @see addSceneObject
     * @see removeSceneObject
     */
    public List<GVRSceneObject> getSceneObjects() {
        return mSceneRoot.getChildren();
    }

    /**
     * @return The {@link GVRCameraRig camera rig} used for rendering the scene
     *         on the screen.
     */
    public GVRCameraRig getMainCameraRig() {
        return mMainCameraRig;
    }

    /**
     * Set the {@link GVRCameraRig camera rig} used for rendering the scene on
     * the screen.
     * 
     * @param cameraRig
     *            The {@link GVRCameraRig camera rig} to render with.
     */
    public void setMainCameraRig(GVRCameraRig cameraRig) {
        mMainCameraRig = cameraRig;
        NativeScene.setMainCameraRig(getNative(), cameraRig.getNative());

        final GVRContext gvrContext = getGVRContext();
        if (this == gvrContext.getMainScene()) {
            gvrContext.getActivity().setCameraRig(getMainCameraRig());
        }
    }

    /**
     * @return The flattened hierarchy of {@link GVRSceneObject objects} as an
     *         array.
     * This function is inefficient if your hierarchy is large and should
     * not be called per frame (in onStep).
     */
    public GVRSceneObject[] getWholeSceneObjects() {
        List<GVRSceneObject> list = new ArrayList<GVRSceneObject>();
        
        addChildren(list, mSceneRoot);
        return list.toArray(new GVRSceneObject[list.size()]);
    }

    private void addChildren(List<GVRSceneObject> list, GVRSceneObject sceneObject) {
        for (GVRSceneObject child : sceneObject.rawGetChildren()) {
            list.add(child);
            addChildren(list, child);
        }
    }

    /**
     * Performs case-sensitive search
     * 
     * @param name
     * @return null if nothing was found or name was null/empty
     */
    public GVRSceneObject[] getSceneObjectsByName(final String name) {
        if (null == name || name.isEmpty()) {
            return null;
        }
        return mSceneRoot.getSceneObjectsByName(name);
    }

    /**
     * Performs case-sensitive depth-first search
     * 
     * @param name
     * @return first match in the graph; null if nothing was found or name was null/empty;
     * in case there might be multiple matches consider using getSceneObjectsByName
     */
    public GVRSceneObject getSceneObjectByName(final String name) {
        if (null == name || name.isEmpty()) {
            return null;
        }
        return mSceneRoot.getSceneObjectByName(name);
    }

    /**
     * Enable / disable picking of visible objects.
     * Picking only visible objects is enabled by default.
     * Only objects that can be seen will be pickable by GVRPicker.
     * Disable this feature if you want to be able to pick
     * stuff the viewer cannot see.
     * @param flag true to enable pick only visible, false to enable pick all colliders
     * @see GVRPicker
     */
    public void setPickVisible(boolean flag) {
        NativeScene.setPickVisible(getNative(), flag);
    }
    
    public void inValidateShadowMap(){
        NativeScene.invalidateShadowMap(getNative());
    }
    /**
     * Sets the frustum culling for the {@link GVRScene}.
     */
    public void setFrustumCulling(boolean flag) {
        NativeScene.setFrustumCulling(getNative(), flag);
    }

    /**
     * Sets the occlusion query for the {@link GVRScene}.
     */
    public void setOcclusionQuery(boolean flag) {
        NativeScene.setOcclusionQuery(getNative(), flag);
    }

    private GVRConsole mStatsConsole = null;
    private boolean mStatsEnabled = false;
    private boolean pendingStats = false;

    /**
     * Returns whether displaying of stats is enabled for this scene.
     * 
     * @return whether displaying of stats is enabled for this scene.
     */
    public boolean getStatsEnabled() {
        return mStatsEnabled;
    }

    /**
     * Set whether to enable display of stats for this scene.
     * 
     * @param enabled
     *            Flag to indicate whether to enable display of stats.
     */
    public void setStatsEnabled(boolean enabled) {
        pendingStats = enabled;
    }

    void updateStatsEnabled() {
        if (mStatsEnabled == pendingStats) {
            return;
        }

        mStatsEnabled = pendingStats;
        if (mStatsEnabled && mStatsConsole == null) {
            mStatsConsole = new GVRConsole(getGVRContext(),
                    GVRConsole.EyeMode.BOTH_EYES);
            mStatsConsole.setCanvasWidthHeight(512, 512);
            mStatsConsole.setXOffset(125.0f);
            mStatsConsole.setYOffset(125.0f);
        }

        if (mStatsEnabled && mStatsConsole != null) {
            mStatsConsole.setEyeMode(GVRConsole.EyeMode.BOTH_EYES);
        } else if (!mStatsEnabled && mStatsConsole != null) {
            mStatsConsole.setEyeMode(GVRConsole.EyeMode.NEITHER_EYE);
        }
    }

    void resetStats() {
        updateStatsEnabled();
        if (mStatsEnabled) {
            mStatsConsole.clear();
            NativeScene.resetStats(getNative());
        }
    }

    void updateStats() {
        if (mStatsEnabled) {
            int numberDrawCalls = NativeScene.getNumberDrawCalls(getNative());
            int numberTriangles = NativeScene.getNumberTriangles(getNative());

            mStatsConsole.writeLine("Draw Calls: %d", numberDrawCalls);
            mStatsConsole.writeLine("Triangles: %d", numberTriangles);

            if (mStatMessage.length() > 0) {
                String lines[] = mStatMessage.toString().split(System.lineSeparator());
                for (String line : lines)
                    mStatsConsole.writeLine("%s", line);
            }
        }
    }

    /**
     * Add an additional string to stats message for this scene.
     * 
     * @param message
     *            String to add to stats message.
     */
    public void addStatMessage(String message) {
        if (mStatMessage.length() > 0) {
            mStatMessage.delete(0, mStatMessage.length());
        }
        mStatMessage.append(message);
    }

    /**
     * Remove the stats message from this scene.
     * 
     */
    public void killStatMessage() {
        mStatMessage.delete(0, mStatMessage.length());
    }

    /**
     * Exports the scene to the given file path at some
     * of the following supported formats:
     *
     *     Collada ( .dae )
     *     Wavefront Object ( .obj )
     *     Stereolithography ( .stl )
     *     Stanford Polygon Library ( .ply )
     *
     * The current supported formats are the same supported
     * by Assimp library. It will export according to file's
     * extension.
     *
     * @param filepath Absolute file path to export the scene.
     */
    public void export(String filepath) {
        NativeScene.exportToFile(getNative(), filepath);
    }

    /**
     * Bind the correct vertex and fragment shaders on all renderable objects.
     * 
     * Setting the shader template for a GVRRenderData selects what kind
     * of shader to use but does not actually construct a vertex and fragment shader.
     * This function does that for all the renderable objects that need it.
     *
     * All shaders should be bound after scene initialization is complete.
     * If new assets are loaded that add lights to the scene after initialization,
     * bindShaders may need to be called again to regenerate the correct shaders
     * for the new lighting conditions.
     * {@link GVRRenderData.bindShader GVRShaderTemplate }
     */
    public void bindShaders() {
        ArrayList<GVRLightBase> lights = mSceneRoot.getAllComponents(GVRLightBase.getComponentType());
        for (GVRLightBase light : lights) {
            addLight(light);
        }
        ArrayList<GVRRenderData> renderers = mSceneRoot.getAllComponents(GVRRenderData.getComponentType());
        for (GVRRenderData rdata : renderers) {
            rdata.bindShader(this);
        }
    }

    /**
     * Bind the correct vertex and fragment shaders on the given hierarchy.
     * This function sets the shader template for all the GVRRenderData components
     * in the input hierarchy (but does not construct vertex and fragment shaders.)
     *
     * If new assets are loaded that add lights to the scene after initialization,
     * bindShaders may need to be called again to regenerate the correct shaders
     * for the new lighting conditions. This function is called whenever a scene
     * object is added at the root of the scene.
     * @see GVRRenderData.bindShader
     * @see GVRShaderTemplate
     * @see GVRScene.addSceneObject
     */
    public void bindShaders(GVRSceneObject root) {
        ArrayList<GVRLightBase> lights = root.getAllComponents(GVRLightBase.getComponentType());
        int added = 0;
        for (GVRLightBase light : lights) {
            if (addLight(light))
            {
                ++added;
            }
        }
        if (added > 0)
        {
            bindShaders();
        }
        else
        {
            ArrayList<GVRRenderData> renderDataList = root.getAllComponents(GVRRenderData
                    .getComponentType());
            for (GVRRenderData renderData : renderDataList) {
                renderData.bindShader(this);
            }           
        }
    }
        
    /**
     * Add a light to the scene's light list.
     * @param light light to add
     * @see GVRScene.getLightList
     */
    private boolean addLight(GVRLightBase light) {
        Integer lightIndex = mLightList.size();
            
        if (lightIndex >= MAX_LIGHTS)
        {
            Log.e(TAG, "Exceeded maximum number of lights");
        	return false;
        }
        String name = "light" + lightIndex.toString();
        if (NativeScene.addLight(getNative(), light.getNative()))
        {
        	mLightList.add(light);
        	NativeLight.setLightID(light.getNative(), name);
        	return true;
        }
        return false;
    }
    
    /**
     * Get the list of lights used by this scene.
     * 
     * This list is maintained by GearVRF by gathering the
     * lights attached to the scene objects in the scene.
     * 
     * @return array of lights
     */
    public GVRLightBase[] getLightList() {
        GVRLightBase[] list = new GVRLightBase[mLightList.size()];
        mLightList.toArray(list);
        return list;
    }
    
    /**
     * Prints the {@link GVRScene} object with indentation.
     *
     * @param sb
     *         The {@code StringBuffer} object to receive the output.
     *
     * @param indent
     *         Size of indentation in number of spaces.
     */
    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        sb.append(Log.getSpaces(indent));
        sb.append(getClass().getSimpleName());
        sb.append(System.lineSeparator());

        sb.append(Log.getSpaces(indent + 2));
        if (mMainCameraRig == null) {
            sb.append("MainCameraRig: null");
            sb.append(System.lineSeparator());
        } else {
            sb.append("MainCameraRig:");
            sb.append(System.lineSeparator());
            mMainCameraRig.prettyPrint(sb, indent + 4);
        }

        // Show all scene objects
        mSceneRoot.prettyPrint(sb, indent + 2);
    }

    /**
     * Apply the light map texture to the scene.
     *
     * @param texture Texture atlas with the baked light map of the scene.
     */
    public void applyLightMapTexture(GVRTexture texture) {
        applyTextureAtlas("lightmap", texture, GVRMaterial.GVRShaderType.LightMap.ID);
    }

    /**
     * Apply the texture atlas to the scene.
     *
     * @param key Name of the texture. Common texture names are "main", "lightmap", etc.
     * @param texture The texture atlas
     * @param shaderId The shader to render the texture atlas.
     */
    public void applyTextureAtlas(String key, GVRTexture texture, GVRMaterialShaderId shaderId) {
        if (!texture.isAtlasedTexture()) {
            Log.w(TAG, "Invalid texture atlas to the scene!");
            return;
        }

        List<GVRAtlasInformation> atlasInfoList = texture.getAtlasInformation();

        for (GVRAtlasInformation atlasInfo: atlasInfoList) {
            GVRSceneObject sceneObject = getSceneObjectByName(atlasInfo.getName());

            if (sceneObject == null || sceneObject.getRenderData() == null) {
                Log.w(TAG, "Null render data or scene object " + atlasInfo.getName()
                        + " not found to apply texture atlas.");
                continue;
            }

            if (shaderId == GVRMaterial.GVRShaderType.LightMap.ID
                    && !sceneObject.getRenderData().isLightMapEnabled()) {
                // TODO: Add support to enable and disable light map at run time.
                continue;
                    }

            sceneObject.getRenderData().getMaterial().setShaderType(shaderId);
            sceneObject.getRenderData().getMaterial().setTexture(key + "_texture", texture);
            sceneObject.getRenderData().getMaterial().setTextureAtlasInfo(key, atlasInfo);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        prettyPrint(sb, 0);
        return sb.toString();
    }
    
    @Override
    public GVREventReceiver getEventReceiver() {
        return mEventReceiver;
    }

    // Default scene event handler
    private ISceneEvents mSceneEventListener = new ISceneEvents() {
        @Override
        public void onInit(GVRContext gvrContext, GVRScene scene) {
            recursivelySendOnInit(mSceneRoot);
        }
        private void recursivelySendOnInit(GVRSceneObject sceneObject) {
            getGVRContext().getEventManager().sendEvent(
                    sceneObject, ISceneObjectEvents.class, "onInit", getGVRContext(), sceneObject);

            for (GVRSceneObject child : sceneObject.rawGetChildren()) {
                recursivelySendOnInit(child);
            }
        }

        @Override
        public void onAfterInit() {
            bindShaders();
            recursivelySendSimpleEvent(mSceneRoot, "onAfterInit");
        }

        @Override
        public void onStep() {
            // Send "onStep" to all scene objects and their children
            recursivelySendSimpleEvent(mSceneRoot, "onStep");
         }

        private void recursivelySendSimpleEvent(GVRSceneObject sceneObject, String eventName) {
            getGVRContext().getEventManager().sendEvent(
                    sceneObject, ISceneObjectEvents.class, eventName);

            for (GVRSceneObject child : sceneObject.getChildren()) {
                recursivelySendSimpleEvent(child, eventName);
            }
        }
    };
}

class NativeScene {

    static native long ctor();

    static native void addSceneObject(long scene, long sceneObject);
   
    public static native void invalidateShadowMap(long scene);

    static native void removeSceneObject(long scene, long sceneObject);

    static native void removeAllSceneObjects(long scene);

    public static native void setFrustumCulling(long scene, boolean flag);

    public static native void setOcclusionQuery(long scene, boolean flag);

    static native void setMainCameraRig(long scene, long cameraRig);

    public static native void resetStats(long scene);

    public static native int getNumberDrawCalls(long scene);

    public static native int getNumberTriangles(long scene);

    public static native void exportToFile(long scene, String file_path);

    static native boolean addLight(long scene, long light);
    
    static native void setMainScene(long scene);
    
    static native void setPickVisible(long scene, boolean flag);
}
