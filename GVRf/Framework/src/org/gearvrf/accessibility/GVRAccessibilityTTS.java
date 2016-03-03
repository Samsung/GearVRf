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

import java.util.ArrayList;

import org.gearvrf.GVRContext;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

/**
 * This class initializes Speech Recognizer, capture user voice, convert it to text and treats it as a command by comparing to a list of predefined text commands. 8
 *
 */
final class GVRAccessibilityTTS implements RecognitionListener {
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private boolean mActive;
    private GVRContext mContext;
    private GVRAccessibilitySpeechListener mSpeechListener;

    public GVRAccessibilityTTS(GVRContext context) {

        mContext = context;
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext.getActivity());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                mContext.getActivity().getPackageName());
        mSpeechRecognizer.setRecognitionListener(this);
    }

    @Override
    public void onBeginningOfSpeech() {
        if (mSpeechListener != null)
            mSpeechListener.onBeginningOfSpeech();
    }

    @Override
    public void onBufferReceived(byte[] arg0) {

    }

    @Override
    public void onEndOfSpeech() {
        if (mSpeechListener != null)
            mSpeechListener.onEndOfSpeech();
    }

    @Override
    public void onError(int arg0) {
        if (mSpeechListener != null)
            mSpeechListener.onError(arg0);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
    }

    @Override
    public void onResults(Bundle results) {

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        AccessibilityFeature.getInstance(mContext).findMatch(matches);
        if (mSpeechListener != null)
            mSpeechListener.onFinish();

        for (String string : matches) {

            Log.e("test", string);
        }
    }

    @Override
    public void onRmsChanged(float arg0) {

    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean mActive) {
        this.mActive = mActive;
    }

    public void setSpeechRecognizer(SpeechRecognizer mSpeechRecognizer) {
        this.mSpeechRecognizer = mSpeechRecognizer;
    }

    public GVRAccessibilitySpeechListener getSpeechListener() {
        return mSpeechListener;
    }

    public void setSpeechListener(GVRAccessibilitySpeechListener mSpeechListener) {
        this.mSpeechListener = mSpeechListener;
    }

    public Intent getSpeechRecognizerIntent() {
        return mSpeechRecognizerIntent;
    }

    public SpeechRecognizer getSpeechRecognizer() {
        return mSpeechRecognizer;
    }

}
