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

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.PrettyPrint;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.utility.Log;

/**
 * {@linkplain GVRSceneObject Scene object} that holds a loaded model.
 */

public class GVRModelSceneObject extends GVRSceneObject {
    protected List<GVRAnimation> mAnimations;

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
