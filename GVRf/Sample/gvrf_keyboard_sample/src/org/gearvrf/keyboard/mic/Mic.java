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

package org.gearvrf.keyboard.mic;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.keyboard.main.MainActivity;
import org.gearvrf.keyboard.mic.model.MicItem;
import org.gearvrf.keyboard.textField.TextField;
import org.gearvrf.keyboard.util.SceneObjectNames;

import java.util.ArrayList;

public class Mic extends GVRSceneObject implements RecognitionListener {

    private static final float MIC_SIZE_ADJUST = 0.5f;
    private MicGroupHitArea mMicHitGroupArea;
    private MicGroupRun mMicGroupRun;
    private MicGroupProgress mMicGroupProgress;
    private MicGroupIcons mMicGroupIcons;
    private MicGroupHover mMicGroupHover;
    private boolean inProgress = false;

    MainActivity mMainActivity;
    boolean mSeeMe = false;
    private RecognitionRmsChangeListener mListnerRmsChange;
    private RecognitionMictListener mListnerResult;

    public void setRecognitionRmsChangeListener(RecognitionRmsChangeListener listnerRmsChange) {
        mListnerRmsChange = listnerRmsChange;
    }

    public void updatePosition(TextField textField) {

        getTransform()
                .setPositionX((textField.getSize()) + MicItem.WIDTH / 2);

    }

    public RecognitionMictListener getListnerResult() {
        return mListnerResult;
    }

    public void setListnerResult(RecognitionMictListener listnerResult) {
        this.mListnerResult = listnerResult;
    }

    public Mic(GVRContext gvrContext, MainActivity mainActivity) {
        super(gvrContext);
        setName(SceneObjectNames.MIC);
        mMainActivity = mainActivity;

        mMicHitGroupArea = new MicGroupHitArea(gvrContext);
        mMicHitGroupArea.getTransform().setPositionZ(0.0f);

        mMicGroupIcons = new MicGroupIcons(gvrContext);
        mMicGroupIcons.getTransform().setPositionZ(-0.01f);

        mMicGroupHover = new MicGroupHover(gvrContext);
        mMicGroupHover.getTransform().setPositionZ(-0.02f);

        mMicGroupRun = new MicGroupRun(gvrContext);
        mMicGroupRun.getTransform().setPositionZ(-0.03f);

        mMicGroupProgress = new MicGroupProgress(gvrContext);
        mMicGroupProgress.getTransform().setPositionZ(-0.04f);

        addChildObject(mMicGroupIcons);
        addChildObject(mMicGroupHover);
        addChildObject(mMicHitGroupArea);
        addChildObject(mMicGroupRun);
        addChildObject(mMicGroupProgress);

        getTransform().setScale(MIC_SIZE_ADJUST, MIC_SIZE_ADJUST, MIC_SIZE_ADJUST);

    }

    public enum Action {
        HOVER, UN_HOVER, RUN, RUN_OUT_ANIMATION, WORK_PROGRESS, STOP, WORK_PROGRESS_OUT_ANIMATION
    }

    public void changeState(final Action state) {

        switch (state) {
            case HOVER:
                hover();
                break;
            case UN_HOVER:
                unHover();
                break;
            case RUN:
                run();
                break;
            case RUN_OUT_ANIMATION:
                runOut();
                break;
            case WORK_PROGRESS:
                progress();
                break;
            case WORK_PROGRESS_OUT_ANIMATION:
                progressOut();
                break;

            default:
                break;
        }
    }

    private void runOut() {

        if (mMicGroupRun != null) {
            mMicGroupRun.hide();
        }

        if (mMicGroupIcons != null) {
            mMicGroupIcons.hide();
        }
    }

    private void progressOut() {

        if (mMicGroupRun != null) {
            mMicGroupRun.hide();
        }

        if (mMicGroupProgress != null) {
            mMicGroupProgress.hide();
        }
    }

    private void run() {
        if (mMicGroupRun != null) {
            mMicGroupRun.show();
        }
        if (mMicGroupIcons != null) {
            mMicGroupIcons.show();
        }
    }

    private void progress() {
        if (mMicGroupProgress != null) {
            mMicGroupProgress.show();
        }
    }

    private void unHover() {
        if (mMicGroupHover != null) {
            mMicGroupHover.hide();
        }
    }

    private void hover() {
        if (mMicGroupHover != null) {
            mMicGroupHover.show();
        }
    }

    public void onUpdate() {

        if (mMicHitGroupArea != null) {
            verifyEyePointee();
        }
    }

    private void verifyEyePointee() {

        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker.pickScene(this.getGVRContext()
                .getMainScene());

        if (eyePointeeHolders.length == 0) {
            tryChangeSeeMe(false);
        } else {
            handleEyePointeeListNew(eyePointeeHolders);
        }
    }

    private void handleEyePointeeListNew(GVREyePointeeHolder[] eyePointeeHolders) {

        // https://github.com/Samsung/GearVRf/issues/102
        if (mMicHitGroupArea != null && mMicHitGroupArea.mHitArea != null) {

            for (GVREyePointeeHolder gVREyePointeeHolder : eyePointeeHolders) {

                boolean seeMeNow = false;

                if (mMicHitGroupArea.mHitArea.hashCode() == gVREyePointeeHolder.getOwnerObject().hashCode()) {
                    tryChangeSeeMe(true);
                    seeMeNow = true;
                }

                if (!seeMeNow) {
                    tryChangeSeeMe(false);
                }
            }
        }
    }

    private void tryChangeSeeMe(boolean newValue) {

        if (newValue != mSeeMe) {

            mSeeMe = newValue;
            seeMeChangedTo(mSeeMe);
        }
    }

    private void seeMeChangedTo(boolean seeMe) {
        updateHover();
    }

    public void onSingleTap() {

        if (mSeeMe && !inProgress) {
            mMainActivity.createRecognizer();
            mMainActivity.startRecognizer(this);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {

        changeState(Action.RUN_OUT_ANIMATION);
        changeState(Action.WORK_PROGRESS);

        if (mListnerRmsChange != null) {
            mListnerRmsChange.onRmsEnd();
        }
    }

    @Override
    public void onError(int error) {

        String text = "";

        over();

        if (mListnerRmsChange != null) {
            mListnerRmsChange.onRmsEnd();
        }

        if (mListnerResult != null) {
            mListnerResult.onError(text, error);
        }
    }

    private void updateHover() {

        if (mSeeMe && !inProgress) {
            changeState(Action.HOVER);
        } else {
            changeState(Action.UN_HOVER);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

        inProgress = true;

        changeState(Action.UN_HOVER);
        changeState(Action.RUN);

        if (mListnerResult != null) {
            mListnerResult.onReadyForSpeech();
        }
    }

    @Override
    public void onResults(Bundle results) {

        ArrayList<String> resultList = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (mListnerResult != null) {
            mListnerResult.onResults(resultList);
        }

        over();
    }

    private void over() {

        changeState(Action.RUN_OUT_ANIMATION);
        changeState(Action.WORK_PROGRESS_OUT_ANIMATION);

        inProgress = false;
        updateHover();
    }

    @Override
    public void onRmsChanged(float rmsdB) {

        if (mListnerRmsChange != null) {
            mListnerRmsChange.onRmsChanged(rmsdB);
        }
    }

    public boolean isInProgress() {
        return inProgress;
    }
}
