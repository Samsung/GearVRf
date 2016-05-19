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

import org.gearvrf.io.cursor3d.CursorAsset.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows the user to customize the various aspects of the 3D Cursor.
 * <p/>
 * A {@link CursorTheme} is a collection of {@link CursorAsset}s.
 */
/* TODO Made public for use in the sample. Will be made private after the settings
 * is implemented.
 */
public class CursorTheme {

    private String id;
    private String name;
    private String description;
    private Map<Action, CursorAsset> assets;
    private CursorType type;

    CursorTheme() {
        assets = new HashMap<Action, CursorAsset>();
    }

    /**
     * Add a new asset, replaces the old one if one already exists.
     *
     * @param asset
     */
    void addAsset(CursorAsset asset) {
        assets.put(asset.getAction(), asset);
    }

    /**
     * Get an assets using the key
     *
     * @param action associated with the asset
     * @return the corresponding {@link CursorAsset}
     */
    CursorAsset getAsset(Action action) {
        return assets.get(action);
    }

    /**
     * Set the name for the {@link CursorTheme}
     *
     * @param name the name String.
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the theme, or null if empty
     *
     * @return a String containing the name if there is one.
     */
    public String getName() {
        return name;
    }

    /**
     * Set a description for the theme
     *
     * @param description A String containing the description
     */
    void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns a description of the theme, or null if empty
     *
     * @return a String containing the description of the theme if there is one.
     */
    String getDescription() {
        return description;
    }

    /**
     * Return a list of all the assets contained in the theme.
     *
     * @return a list of all assets representing this theme.
     */
    List<CursorAsset> getCursorAssets() {
        return new ArrayList<CursorAsset>(assets.values());
    }

    /**
     * The id uniquely identifies this {@link CursorTheme}.
     *
     * @return the String that uniquely identifies this {@link CursorTheme}.
     */
    public String getId() {
        return id;
    }

    /**
     * Ready this {@link CursorTheme} by loading all the available {@link CursorAsset}s
     */
    void load(CursorSceneObject sceneObject) {
        for (CursorAsset asset : assets.values()) {
            asset.load(sceneObject);
        }
    }

    /**
     * Notify the {@link CursorAsset}s that the {@link CursorTheme} has been unloaded.
     */
    void unload(CursorSceneObject sceneObject) {
        for (CursorAsset asset : assets.values()) {
            asset.unload(sceneObject);
        }
    }

    void setId(String id) {
        this.id = id;
    }

    Collection<CursorAsset> getAssets() {
        return assets.values();
    }

    CursorType getType() {
        return type;
    }

    void setType(CursorType type) {
        this.type = type;
    }

    public CursorType getCursorType() {
        return type;
    }
}