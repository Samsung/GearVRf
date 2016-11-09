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
 * The 3D Cursor library makes use of a {@link CursorTheme} object to describe the look and
 * behavior of a {@link Cursor} object.
 *
 * The library makes use of a "settings.xml" file to define a theme. The
 * library includes its own "settings.xml"(see the assets folder) with a set of pre defined
 * themes for all applications to use. The application can define its own theme by creating
 * a new "settings.xml" file and placing it in its assets folder. This file overwrites the settings
 * defined by the library. The "settings.xml" included in the library can be used as a reference
 * while creating a new customized "settings.xml" file.
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
 * As can be seen above {@link CursorTheme} is a collection of {@link CursorAsset}s. Properties of
 * the theme can be set using the xml.
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