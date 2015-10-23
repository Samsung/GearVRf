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

package org.gearvrf.immersivepedia.gallery;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

public class GalleryArrow extends GVRSceneObject {

    private static final int Invisible = 0;
    private static final int Visible = 0x01;

    private static final float MIN_OPACITY = 0.01f;
    private static final float MAX_OPACITY = 0.9f;

    private int currentState = GalleryArrow.Invisible;
    private float opacity = 0.5f;
    private float speed = -0.02f;

    public GalleryArrow(GVRContext gvrContext, float width, float height, GVRTexture texture) {
        super(gvrContext, width, height, texture);
        this.currentState = GalleryArrow.Invisible;
    }

    protected void show() {
        this.currentState = GalleryArrow.Visible;
    }

    protected void hide() {
        this.currentState = GalleryArrow.Invisible;
    }

    protected void process() {

        if ((this.opacity <= MIN_OPACITY) && (this.currentState == GalleryArrow.Invisible)) {
            this.getRenderData().getMaterial().setOpacity(0f);
            return;
        }

        if (this.currentState == GalleryArrow.Invisible) {
            if (this.speed > 0f) {
                this.speed = this.speed * -1f;
            }
        }

        if (this.currentState == GalleryArrow.Visible) {
            if (this.opacity <= MIN_OPACITY) {
                speed = Math.abs(speed);
            }
            if (this.opacity >= MAX_OPACITY) {
                speed = Math.abs(speed) * -1f;
            }
        }

        this.opacity = this.opacity + speed;
        this.getRenderData().getMaterial().setOpacity(this.opacity);

    }

}
