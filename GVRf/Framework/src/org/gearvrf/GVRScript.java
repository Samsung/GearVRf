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

/**
 * Extend this class to create a GVRF application.
 * <p>
 * All methods are called from the GL thread so it is safe to make GL calls
 * either directly or indirectly (through GVRF methods). The GL thread runs at
 * {@linkplain Thread#MAX_PRIORITY top priority:} Android systems have many
 * processes running at any time, and all {@linkplain Thread#NORM_PRIORITY
 * default priority} threads compete with each other.
 */
public abstract class GVRScript {
    /**
     * Called when the GL surface is created, when your app is loaded.
     * 
     * This is where you should build your initial scene graph. Any expensive
     * calls you make here are 'hidden' (in the sense that they won't cause the
     * app to skip any frames) but they <em>will</em> still affect app startup
     * time: use lazy-create patterns where you can, and/or load large bitmaps
     * in a background (non-GL) thread.
     */
    public abstract void onInit(GVRContext gvrContext);

    /**
     * Called every frame.
     * 
     * This is where you start animations, and where you add or change
     * {@linkplain GVRSceneObject scene objects.} Keep this method as short as
     * possible, to avoid dropping any frames.
     * 
     * <p>
     * This is the 3rd user-definable step in drawing a frame:
     * <ul>
     * <li>Process the {@link GVRContext#runOnGlThread(Runnable)} queue
     * <li>Run all
     * {@linkplain GVRContext#registerDrawFrameListener(GVRDrawFrameListener)
     * registered frame listeners}
     * <li><b>Call your {@code onStep()} handler</b>.
     * </ul>
     * 
     * After these steps, {@link GVRViewManager} does stereo rendering and
     * applies the lens distortion.
     */
    public abstract void onStep();
}
