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

package org.gearvrf.immersivepedia.util;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;
import org.gearvrf.immersivepedia.R;
import org.gearvrf.immersivepedia.focus.FocusableSceneObject;

public class PlayPauseButton extends FocusableSceneObject {

    public static final String PAUSE_HOVER = "inactive_pause";
    public static final String PAUSE_NORMAL = "normal_pause";

    public static final String PLAY_HOVER = "inactive_play";
    public static final String PLAY_NORMAL = "normal_play";

    private GVRTexture pauseHover;
    private GVRTexture pauseNormal;

    private GVRTexture playHover;
    private GVRTexture playNormal;
    private GVRContext gvrContext;

    public PlayPauseButton(GVRContext gvrContext, float f, float g, GVRTexture t) {
        super(gvrContext, f, g, t);
        this.gvrContext = gvrContext;
        loadTexture();
        setTextures();
    }

    private void loadTexture() {
        pauseHover = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.pause_hover));
        playHover = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.play_hover));
        pauseNormal = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.pause));
        playNormal = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.play));
    }

    private void setTextures() {
        getRenderData().getMaterial().setTexture(PAUSE_NORMAL, pauseNormal);
        getRenderData().getMaterial().setTexture(PAUSE_HOVER, pauseHover);
        getRenderData().getMaterial().setTexture(PLAY_HOVER, playHover);
        getRenderData().getMaterial().setTexture(PLAY_NORMAL, playNormal);
    }

}
