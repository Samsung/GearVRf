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

import org.gearvrf.GVRContext;

/**
 * Class responsible to manager speech recognize for all accessibility features such as {@code GVRAccessibilityZoom}, {@code GVRAccessibilityTalkBack}, {@code GVRAccessibilityInvertedColors}
 *
 */
public class GVRAccessibilitySpeech {

    private GVRAccessibilityTTS mTts;

    public GVRAccessibilitySpeech(GVRContext gvrContext) {

        mTts = new GVRAccessibilityTTS(gvrContext);
    }

    /**
     * Start speech recognizer.
     * 
     * @param speechListener
     */
    public void start(GVRAccessibilitySpeechListener speechListener) {
        mTts.setSpeechListener(speechListener);
        mTts.getSpeechRecognizer().startListening(mTts.getSpeechRecognizerIntent());
    }

    /**
     * Active speech for accessibility features.
     */
    public void active() {
        mTts.setActive(true);
    }

    /**
     * Inactive speech for accessibility features.
     */
    public void inactive() {
        mTts.setActive(false);
    }

    /**
     * Destroys the SpeechRecognizer object.
     */
    public void destroy() {
        mTts.getSpeechRecognizer().destroy();
    }

}
