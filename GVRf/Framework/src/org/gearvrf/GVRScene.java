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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.debug.GVRConsole;
import org.gearvrf.utility.Log;

/** The scene graph */
public class GVRScene extends GVRHybridObject implements PrettyPrint {
    @SuppressWarnings("unused")
    private static final String TAG = Log.tag(GVRScene.class);

    private final List<GVRSceneObject> mSceneObjects = new ArrayList<GVRSceneObject>();
    private GVRCameraRig mMainCameraRig;
    private StringBuilder mStatMessage = new StringBuilder();

    /**
     * Constructs a scene with a camera rig holding left & right cameras in it.
     * 
     * @param gvrContext
     *            {@link GVRContext} the app is using.
     */
    public GVRScene(GVRContext gvrContext) {
        super(gvrContext, NativeScene.ctor());

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
    }

    private GVRScene(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
        setFrustumCulling(true);
    }

    /**
     * Add an {@linkplain GVRSceneObject scene object}
     * 
     * @param sceneObject
     *            The {@linkplain GVRSceneObject scene object} to add.
     */
    public void addSceneObject(GVRSceneObject sceneObject) {
        mSceneObjects.add(sceneObject);
        NativeScene.addSceneObject(getNative(), sceneObject.getNative());
    }

    /**
     * Remove a {@linkplain GVRSceneObject scene object}
     * 
     * @param sceneObject
     *            The {@linkplain GVRSceneObject scene object} to remove.
     */
    public void removeSceneObject(GVRSceneObject sceneObject) {
        mSceneObjects.remove(sceneObject);
        NativeScene.removeSceneObject(getNative(), sceneObject.getNative());
    }

    /**
     * Remove all scene objects.
     */
    public void removeAllSceneObjects() {
        mSceneObjects.clear();
        NativeScene.removeAllSceneObjects(getNative());
    }

    /**
     * Clears the scene and resets it to initial state. Currently, it only
     * removes all scene objects.
     */
    public void clear() {
        removeAllSceneObjects();
    }

    /**
     * The top-level scene objects.
     * 
     * @return A read-only list containing all the 'root' scene objects (those
     *         that were added directly to the scene).
     * 
     * @since 2.0.0
     */
    public List<GVRSceneObject> getSceneObjects() {
        return Collections.unmodifiableList(mSceneObjects);
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
     */
    public GVRSceneObject[] getWholeSceneObjects() {
        List<GVRSceneObject> list = new ArrayList<GVRSceneObject>(mSceneObjects);
        for (GVRSceneObject child : mSceneObjects) {
            addChildren(list, child);
        }
        return list.toArray(new GVRSceneObject[list.size()]);
    }

    private void addChildren(List<GVRSceneObject> list,
            GVRSceneObject sceneObject) {
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

        final List<GVRSceneObject> matches = new ArrayList<GVRSceneObject>();
        GVRScene.getSceneObjectsByName(matches, mSceneObjects, name);

        return 0 != matches.size() ? matches.toArray(new GVRSceneObject[matches.size()]) : null;
    }

    static void getSceneObjectsByName(final List<GVRSceneObject> matches,
            final List<GVRSceneObject> children, final String name) {
        synchronized (children) {
            for (final GVRSceneObject child : children) {
                if (name.equals(child.getName())) {
                    matches.add(child);
                }
                getSceneObjectsByName(matches, child.rawGetChildren(), name);
            }
        }
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

        return GVRScene.getSceneObjectByName(mSceneObjects, name);
    }

    static GVRSceneObject getSceneObjectByName(final List<GVRSceneObject> children, final String name) {
        synchronized (children) {
            for (final GVRSceneObject child : children) {
                final GVRSceneObject scene = getSceneObjectByName(child.rawGetChildren(), name);
                if (null != scene) {
                    return scene;
                }
                if (name.equals(child.getName())) {
                    return child;
                }
            }
        }
        return null;
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

    private GVRDirectionalLight mDirectionalLight;

    public void setDirectionalLight(GVRDirectionalLight light) {
        mDirectionalLight = light;

        if (light != null) {
            NativeScene.attachDirectionalLight(getNative(), light.getNative());
        } else {
            NativeScene.attachDirectionalLight(getNative(), 0);
        }
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
        for (GVRSceneObject child : mSceneObjects) {
            child.prettyPrint(sb, indent + 2);
        }
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
}

class NativeScene {

    static native long ctor();

    static native void addSceneObject(long scene, long sceneObject);

    static native void removeSceneObject(long scene, long sceneObject);

    static native void removeAllSceneObjects(long scene);

    public static native void setFrustumCulling(long scene, boolean flag);

    public static native void setOcclusionQuery(long scene, boolean flag);

    static native void setMainCameraRig(long scene, long cameraRig);

    public static native void resetStats(long scene);

    public static native int getNumberDrawCalls(long scene);

    public static native int getNumberTriangles(long scene);

    public static native void exportToFile(long scene, String file_path);

    static native void attachDirectionalLight(long scene, long light);
}
