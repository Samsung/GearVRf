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

package org.gearvrf.io.cursor3d;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;

import java.io.IOException;

/**
 * The 3D Cursor library makes use of a {@link CursorTheme} object to describe the look and
 * behavior of a {@link Cursor} object.
 *
 * A {@link CursorTheme} is a collection of {@link CursorAsset}s corresponding to specific
 * {@link Cursor} behaviors.
 *
 * The library makes use of a "settings.xml" file to define the assets. The
 * library includes its own "settings.xml"(see the assets folder) with a set of pre defined
 * themes (with included assets) for all applications to use. The application can define its own
 * theme by creating a new "settings.xml" file and placing it in its assets folder. This file
 * overwrites the settings defined by the library. The "settings.xml" included in the library can
 * be used as a reference while creating a new customized "settings.xml" file.
 * <p/>
 * The following xml(taken from "settings.xml") describes the definition of a theme:
 * <p/>
 * <pre>
 * {@code
 *   <theme name="Crystal Sphere"
 *          cursorType="object"
 *          id="crystal_sphere">
 *       <asset action="default"
 *              animated="no"
 *              soundEnabled="no"
 *              src="poly_sphere_default"
 *              type="3D"/>
 *       <asset action="click"
 *              animated="no"
 *              soundEnabled="no"
 *              src="poly_sphere_click"
 *              type="3D"/>
 *       <asset action="intersect"
 *              animated="no"
 *              soundEnabled="no"
 *              src="poly_sphere_collision"
 *              type="3D"/>
 *   </theme>
 * }
 * </pre>
 *
 * As can be seen above {@link CursorAsset} can be defined using the xml file.
 */
abstract class CursorAsset {
    private static final String TAG = CursorAsset.class.getSimpleName();
    // Attributes
    private boolean soundEnabled;
    private String soundSrc;
    private final Action action;
    private String src;
    private boolean animated;
    private AssetType assetType;

    private AssetFileDescriptor soundFd;
    private CursorAudioManager audioManager;
    protected GVRContext context;
    protected final CursorType cursorType;
    private GlobalSettings globalSettings;

    enum Action {
        DEFAULT,
        CLICK,
        LOADING,
        HOVER,
        INTERSECT,
        GESTURE
    }

    /**
     * Create a new asset object.
     *
     * @param context the GVRf context
     * @param action  the action associated with the asset
     */
    CursorAsset(GVRContext context, CursorType cursorType, Action action) {
        this.action = action;
        this.context = context;
        this.cursorType = cursorType;
        audioManager = CursorAudioManager.getInstance(context.getContext());
        globalSettings = GlobalSettings.getInstance();
    }

    /**
     * Return the key
     *
     * @return The string representing the key for the {@link CursorAsset}
     */
    Action getAction() {
        return action;
    }

    /**
     * Returns whether this asset has an attached sound.
     *
     * @return true if the asset has an attached sound, false otherwise
     */
    boolean hasSound() {
        return soundSrc != null;
    }

    /**
     * Setting the enabled as <code>true</code> enables the sound if there is one.
     * <p/>
     * By default the sound is enabled.
     *
     * @param enabled true to enable sound on the asset, false otherwise.
     */
    void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    /**
     * Lets the caller know if the sound is enabled.
     *
     * @return returns <code>true</code> if sound is enabled, <code>false</code> otherwise
     */
    boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Use the set method to "set" this asset to the given {@link GVRSceneObject}.
     *
     * @param sceneObject the {@link GVRSceneObject} to set the asset.
     */
    void set(CursorSceneObject sceneObject) {
        if (hasSound() && soundEnabled && globalSettings.isSoundEnabled()) {
            audioManager.play(this);
        }
    }

    /**
     * Use the reset method to remove this asset from the given {@link GVRSceneObject}.
     *
     * @param sceneObject the {@link GVRSceneObject}  for the asset to be removed
     */
    void reset(CursorSceneObject sceneObject) {
        // Override to use
    }

    /**
     * In order to understand this method it is important to look at the way the GVRf framework,
     * manages {@link GVRAndroidResource}s like the mesh and the texture:
     * <p>
     * GVRf maintains a copy of the mesh and the texture files in its own resource cache, it
     * would be wasteful for this library to maintain a similar copy  of the asset resource
     * with the {@link CursorAsset} class as well. Instead every time a {@link CursorTheme}
     * is loaded all its accompanying assets are also loaded using this call.
     * </p>
     * In most cases the {@link CursorAsset} will use this method to issue a {@link
     * GVRContext#loadFutureMesh (GVRAndroidResource)} or {@link GVRContext#loadFutureTexture
     * (GVRAndroidResource)} call to check the GVRf resource cache to load an existing copy or to
     * asynchronously load a new copy on demand.
     */
    abstract void load(CursorSceneObject sceneObject);

    /**
     * Use this method to clear references to the resources that were loaded when {@link #load
     * (CursorSceneObject)} was last called.
     * <p>
     * This helps garbage collect the resources if not needed. GVRf maintains a cached copy of
     * the objects and for cases where the resource is needed immediately the GVRf framework
     * would do so with a resource cache provided it has not been garbage collected.
     * </p>
     */
    abstract void unload(CursorSceneObject sceneObject);

    void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    void setSrc(String src) {
        this.src = src;
    }

    void setAnimated(boolean animated) {
        this.animated = animated;
    }

    void setSoundSrc(String soundSrc) {
        AssetManager manager = context.getContext().getAssets();
        try {
            soundFd = manager.openFd(soundSrc);
            this.soundSrc = soundSrc;
        } catch (IOException e) {
            Log.d(TAG, "Cannot open sound file:" + soundSrc);
        }
    }

    String getSoundSrc() {
        return soundSrc;
    }

    AssetFileDescriptor getSoundFd() {
        return soundFd;
    }

    String getSrc() {
        return src;
    }

    AssetType getAssetType() {
        return assetType;
    }

    boolean getAnimated() {
        return animated;
    }
}