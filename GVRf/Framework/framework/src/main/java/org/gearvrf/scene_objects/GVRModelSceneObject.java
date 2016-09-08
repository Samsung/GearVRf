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

package org.gearvrf.scene_objects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.PrettyPrint;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.utility.Log;

/**
 * {@linkplain GVRSceneObject Scene object} that holds a loaded model.
 * You can specify the path to an asset and it will automatically
 * load the asset for you.
 */

public class GVRModelSceneObject extends GVRSceneObject {
    protected List<GVRAnimation> mAnimations;
    protected String mFileName = null;

    /**
     * Holds a loaded model.
     *
     * @param gvrContext
     *            current {@link GVRContext}
     */
    public GVRModelSceneObject(GVRContext gvrContext) {
        super(gvrContext);
        mAnimations = new ArrayList<GVRAnimation>();
    }

    /**
     * Loads the specified model as a child of this scene object.
     *
     * @param gvrContext
     *            current {@link GVRContext}
     * @param pathName
     *            path to asset to load (from assets, sdcard or URL)
     * @see GVRAssetLoader.loadModel
     */
    public GVRModelSceneObject(GVRContext gvrContext, String pathName) {
        super(gvrContext);
        mFileName = pathName;
        mAnimations = new ArrayList<GVRAnimation>();
    }

    /**
     * Gets the filename of the asset associated with the model.
     * @return model file name or null if none specified
     */
    public String getFileName() { return mFileName; }

    /**
     * Loads the asset specified by the file name provided.
     * The function returns immediately even though parts of
     * the asset may still be loading in the background.
     * It does not add the model to the current scene.
     * If the model is already in the scene it will be
     * disabled until all of the textures are available.
     * If no file name was given on the constructor, this
     * function does nothing. If the asset has already
     * been loaded, this function does nothing.
     * @throws IOException if the asset does not exist
     * @see GVRModelSceneObject
     * @see GVRModelSceneObject.getFileName
     */
    public void load() throws IOException
    {
        if ((getChildrenCount() == 0) && (mFileName != null))
        {
            getGVRContext().getAssetLoader().loadModel(this, null);
        }
    }

    /**
     * Gets the list of animations loaded with the model.
     * 
     * @return list of animations.
     */
    public List<GVRAnimation> getAnimations() {
        return mAnimations;
    }

    @Override
    public void prettyPrint(StringBuffer sb, int indent) {
        super.prettyPrint(sb, indent);

        // dump animations
        for (GVRAnimation anim : mAnimations) {
            if (anim instanceof PrettyPrint) {
                ((PrettyPrint) anim).prettyPrint(sb, indent + 2);
            } else {
                sb.append(Log.getSpaces(indent + 2));
                sb.append(anim);
                sb.append(System.lineSeparator());
            }
        }
    }
}
