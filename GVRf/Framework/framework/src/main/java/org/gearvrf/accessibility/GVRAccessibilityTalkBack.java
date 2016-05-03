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

import java.util.Locale;

import org.gearvrf.utility.Log;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

/**
 * {@link GVRAccessibilityTalkBack} responsible for converting the value from a string variable to audio.
 */
public class GVRAccessibilityTalkBack {

    private TextToSpeech mTextToSpeech;
    private Locale mLocale;
    private Context mContext;
    private String mText;
    private boolean mActive;

    private static final String TAG = Log.tag(GVRAccessibilityTalkBack.class);

    /**
     * @param locale
     *            target language
     * @param context
     *            Android context
     * @param text
     *            to be converted
     */
    public GVRAccessibilityTalkBack(Locale locale, Context context, String text) {
        mText = text;
        mLocale = locale;
        mContext = context;

        init();
    }

    /**
     * @param Android
     *            context.
     */
    public GVRAccessibilityTalkBack(Context context) {
        mContext = context;
        init();
    }

    /**
     * Initialize {@code TextToSpeech} and treats unsupported languages exceptions by setting a default language.
     */
    private void init() {

        mTextToSpeech = new TextToSpeech(mContext, new OnInitListener() {

            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    checkCurrentLocale();
                    int result = mTextToSpeech.setLanguage(mLocale);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        mLocale = getDefaultLocale();
                        init();
                        Log.i(TAG, "set default locale (English US) for gvrAccessibility");
                    }
                }
            }
        });

    }

    public Locale getLocale() {
        return mLocale;
    }

    public void setLocale(Locale mLocale) {
        this.mLocale = mLocale;
        init();
    }

    public void speak() {

        if (isActive())
            mTextToSpeech.speak(mText, TextToSpeech.QUEUE_FLUSH, null);

    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
        init();
    }

    /**
     * Interrupts the current utterance (whether played or rendered to file), discards other utterances in the queue and Releases the res ources used by the TextToSpeech engine. It is good practice, for instance, to call this method in the
     * onDestroy() method of an Activity so the TextToSpeech engine can be cleanly stopped.
     */
    public void shutdown() {
        mTextToSpeech.stop();
        mTextToSpeech.shutdown();
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        this.mActive = active;
    }

    /**
     * By default locale is English US.
     * 
     * @return
     */
    public Locale getDefaultLocale() {
        return Locale.US;
    }

    private void checkCurrentLocale() {
        if (mLocale == null)
            mLocale = mContext.getResources().getConfiguration().locale;
    }

}
