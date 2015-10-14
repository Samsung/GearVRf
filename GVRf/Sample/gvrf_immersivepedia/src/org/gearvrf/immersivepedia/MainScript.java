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

package org.gearvrf.immersivepedia;

import android.media.MediaPlayer;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScript;
import org.gearvrf.immersivepedia.focus.FocusableController;
import org.gearvrf.immersivepedia.input.TouchPadInput;
import org.gearvrf.immersivepedia.scene.DinosaurScene;
import org.gearvrf.immersivepedia.scene.MenuScene;
import org.gearvrf.immersivepedia.util.AudioClip;

public class MainScript extends GVRScript {

    private static GVRContext mGvrContext;

    private MenuScene menuScene;
    public static DinosaurScene dinosaurScene;
    private static MediaPlayer mediaPlayer;

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {

        mGvrContext = gvrContext;
        GazeController.setupGazeCursor(gvrContext);

        AudioClip.getInstance(gvrContext.getActivity());

        mediaPlayer = MediaPlayer.create(gvrContext.getContext(), R.raw.sfx_ambient_1_1);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.start();

        dinosaurScene = new DinosaurScene(gvrContext);
        menuScene = new MenuScene(gvrContext);
    }

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }

    @Override
    public void onStep() {

        TouchPadInput.process();
        FocusableController.process(mGvrContext);

        if (mGvrContext.getMainScene().equals(dinosaurScene)) {
            dinosaurScene.onStep();
        }
    }

    public void onSingleTapConfirmed() {
        FocusableController.clickProcess(mGvrContext);
    }

    public void onSwipe() {
        FocusableController.swipeProcess(mGvrContext);
    }

    public static void clickOut() {
        if (mGvrContext.getMainScene().equals(MainScript.dinosaurScene)) {
            MainScript.dinosaurScene.closeObjectsInScene();
        }
    }

}
