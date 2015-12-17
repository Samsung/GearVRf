package org.gearvrf.accessibility;

/*
 * Copyright 2015 Samsung Electronics Co., LTD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

import java.util.ArrayList;

import org.gearvrf.GVRContext;
import org.gearvrf.R;

import android.content.Context;
import android.media.AudioManager;

final class AccessibilityFeature {

    private String volumeUp;
    private String volumeDown;
    private String zoomIn;
    private String zoomOut;
    private String invertedColors;
    private GVRAccessibilityManager managerFeatures;
    private AudioManager mAudioManager;
    private GVRContext mGvrContext;
    private static AccessibilityFeature instance;

    private AccessibilityFeature(GVRContext gvrContext) {
        mGvrContext = gvrContext;
        mAudioManager = (AudioManager) gvrContext.getActivity().getSystemService(Context.AUDIO_SERVICE);
        managerFeatures = new GVRAccessibilityManager(gvrContext);
    }

    public static synchronized AccessibilityFeature getInstance(GVRContext gvrContext) {
        if (instance == null) {
            instance = new AccessibilityFeature(gvrContext);
        }

        return instance;
    }

    private void loadCadidateString() {
        volumeUp = mGvrContext.getActivity().getResources().getString(R.string.volume_up);
        volumeDown = mGvrContext.getActivity().getResources().getString(R.string.volume_down);
        zoomIn = mGvrContext.getActivity().getResources().getString(R.string.zoom_in);
        zoomOut = mGvrContext.getActivity().getResources().getString(R.string.zoom_out);
        invertedColors = mGvrContext.getActivity().getResources().getString(R.string.inverted_colors);
    }

    public void findMatch(ArrayList<String> speechResult) {

        loadCadidateString();

        for (String matchCandidate : speechResult) {
            if (volumeUp.equals(matchCandidate)) {
                startVolumeUp();
                break;
            } else if (volumeDown.equals(matchCandidate)) {
                startVolumeDown();
                break;
            } else if (zoomIn.equals(matchCandidate)) {
                startZoomIn();
                break;
            } else if (zoomOut.equals(matchCandidate)) {
                startZoomOut();
                break;
            } else if (invertedColors.equals(matchCandidate)) {
                startInvertedColors();
                break;
            }
        }
    }

    private void startVolumeUp() {

    }

    private void startVolumeDown() {

        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }

    private void startZoomIn() {
        managerFeatures.getZoom().zoomIn(mGvrContext.getMainScene());
    }

    private void startZoomOut() {
        managerFeatures.getZoom().zoomOut(mGvrContext.getMainScene());
    }

    private void startInvertedColors() {
        if (managerFeatures.getInvertedColors().isInverted()) {
            managerFeatures.getInvertedColors().turnOff(mGvrContext.getMainScene());
        } else {
            managerFeatures.getInvertedColors().turnOn(mGvrContext.getMainScene());
        }
    }

}
