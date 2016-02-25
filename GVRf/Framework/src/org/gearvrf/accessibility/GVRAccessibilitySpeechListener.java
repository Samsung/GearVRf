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
package org.gearvrf.accessibility;

/**
 * 
 * Used for receiving notifications from the SpeechRecognizer when the recognition related events occur.
 *
 */
public interface GVRAccessibilitySpeechListener {

    /**
     * The user has started to speak.
     */
    public void onBeginningOfSpeech();

    /**
     * Finish speak recognition.
     */
    public void onFinish();

    /**
     * Called after the user stops speaking.
     */
    public void onEndOfSpeech();

    /**
     * A network or recognition error occurred.
     * 
     * @param code
     *            error int: code is defined in SpeechRecognizer.
     */
    public void onError(int code);

    /**
     * The sound level in the audio stream has changed. There is no guarantee that this method will be called.
     * 
     * @param rmsdB
     *            float: the new RMS dB value.
     */
    public void onRmsChanged(float rmsdB);

}
