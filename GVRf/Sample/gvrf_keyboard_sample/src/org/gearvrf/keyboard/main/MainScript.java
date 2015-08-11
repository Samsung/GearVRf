
package org.gearvrf.keyboard.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.keyboard.R;
import org.gearvrf.keyboard.keyboard.model.KeyboardEventListener;
import org.gearvrf.keyboard.keyboard.numeric.Keyboard;
import org.gearvrf.keyboard.keyboard.numeric.Keyboard.KeyboardType;
import org.gearvrf.keyboard.mic.Mic;
import org.gearvrf.keyboard.mic.RecognitionMictListener;
import org.gearvrf.keyboard.mic.model.ExceptionFeedback;
import org.gearvrf.keyboard.model.AudioClip;
import org.gearvrf.keyboard.model.CharItem;
import org.gearvrf.keyboard.model.CharList;
import org.gearvrf.keyboard.model.Dashboard;
import org.gearvrf.keyboard.model.KeyboardCharItem;
import org.gearvrf.keyboard.model.SphereFlag;
import org.gearvrf.keyboard.model.SphereStaticList;
import org.gearvrf.keyboard.shader.GVRShaderAnimation;
import org.gearvrf.keyboard.shader.SphereShader;
import org.gearvrf.keyboard.shader.TransparentButtonShaderThreeStates;
import org.gearvrf.keyboard.speech.SoundWave;
import org.gearvrf.keyboard.spinner.Spinner;
import org.gearvrf.keyboard.spinner.SpinnerItemFactory;
import org.gearvrf.keyboard.textField.TextField;
import org.gearvrf.keyboard.util.Constants;
import org.gearvrf.keyboard.util.InteractiveText;
import org.gearvrf.keyboard.util.RenderingOrder;
import org.gearvrf.keyboard.util.StringUtil;
import org.gearvrf.keyboard.util.Util;
import org.gearvrf.keyboard.util.VRSamplesTouchPadGesturesDetector.SwipeDirection;

import android.text.TextUtils;
import android.view.MotionEvent;

public class MainScript extends GVRScript implements KeyboardEventListener {

    private GVRContext mGVRContext;
    private boolean isFirstTime = true;
    private SphereStaticList flagListCostructor;
    private SphereFlag lastSelectedSphereFlag;
    private TextField answer;
    private GVRSceneObject question;
    private Keyboard keyboard;
    private Mic mMic;
    private MainActivity mMainActivity;
    private ExceptionFeedback exceptionFeedback;
    private SoundWave soundWave1;
    private static final int QUESTION_HEIGHT = 90;
    private static final int QUESTION_WIDTH = 4000;
    private final int QUESTION_LINE_LENGTH = 45;
    private RecognitionMictListener mRecognitionMictListener;
    private boolean mDisableSnapSound = false;
    private Dashboard dashboard;
    private Spinner spinner;

    public void setActivity(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {

        mGVRContext = gvrContext;
        
        SpinnerItemFactory.getInstance(gvrContext).init();

        exceptionFeedback = new ExceptionFeedback(gvrContext);
        gvrContext.getMainScene().getMainCameraRig().getOwnerObject()
                .addChildObject(exceptionFeedback);

        keyboard = new Keyboard(gvrContext);
        keyboard.setOnKeyboardEventListener(this);

        AudioClip.getInstance(mGVRContext.getActivity());

        GVRSceneObject floor = new GVRSceneObject(mGVRContext,
                mGVRContext.createQuad(120.0f, 120.0f), mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext, R.drawable.floor)));

        floor.getTransform().setRotationByAxis(-90, 1, 0, 0);
        floor.getTransform().setPositionY(-10.0f);
        gvrContext.getMainScene().addSceneObject(floor);
        floor.getRenderData().setRenderingOrder(0);

        createSkybox();

        addCursorPosition();

        createSpinnerInvisible();

        createAnswer();

        createQuestion();

        createDashboard();

        createMic();

        createSoundWaves();

        configureKeyboardParent();

        flagListCostructor = new SphereStaticList(gvrContext);

        GVRSceneObject cameraObject = gvrContext.getMainScene()
                .getMainCameraRig().getOwnerObject();
        for (GVRSceneObject spherePack : flagListCostructor.listFlag) {
            rotateObject(spherePack, cameraObject);

            double distance = Util.distance(spherePack, gvrContext
                    .getMainScene().getMainCameraRig().getOwnerObject());
            float scaleFactor = Util.getHitAreaScaleFactor((float) distance);
            spherePack.getTransform().setScale(scaleFactor, scaleFactor,
                    scaleFactor);
            spherePack
                    .getChildByIndex(0)
                    .getTransform()
                    .setScale(1 / scaleFactor, 1 / scaleFactor, 1 / scaleFactor);

            gvrContext.getMainScene().addSceneObject(spherePack);
        }

    }

    public void createSpinnerInvisible() {
        spinner = new Spinner(mGVRContext, 0, Keyboard.NUMERIC_KEYBOARD);
        spinner.getTransform().setPositionZ(0.2f);
        spinner.getTransform().setPositionY(0.05f);
        mGVRContext.getMainScene().addSceneObject(spinner);
        spinner.off();
    }

    private void createMic() {

        mMic = new Mic(mGVRContext, mMainActivity);
        mMic.setListnerResult(getRecognitionMictListener());
        mMic.updatePosition(answer);
        answer.addChildObject(mMic);
    }

    private RecognitionMictListener getRecognitionMictListener() {
        if (mRecognitionMictListener != null) {
            return mRecognitionMictListener;
        }

        mRecognitionMictListener = (new RecognitionMictListener() {

            @Override
            public void onError(String text, int error) {
                exceptionFeedback.show();
            }

            @Override
            public void onReadyForSpeech() {
                soundWave1.enableAnimation();
            }

            @Override
            public void onResults(ArrayList<String> resultList) {

                String result = resultList.get(0);
                if (Keyboard.NUMERIC_KEYBOARD == Keyboard.mode) {
                    result = getOnlyNumbers(result);
                }

                if (answer != null) {

                    if (!TextUtils.isEmpty(result)) {
                        answer.removeAllTextFieldItem();

                        for (int i = 0; i < result.length(); i++) {

                            Character charater = result.charAt(i);
                            int mode = CharList.getInstance(mGVRContext).getMode(charater);
                            int position = CharList.getInstance(mGVRContext).indexOf(String.valueOf(charater), mode);
                            answer.append(i, new CharItem(mode, position, String.valueOf(charater)));
                        }

                    } else {
                        exceptionFeedback.show();
                    }
                }
            }
        });

        return mRecognitionMictListener;
    }

    private void configureKeyboardParent() {
        keyboard.addChildObject(answer);
        keyboard.addChildObject(question);
        answer.addChildObject(soundWave1);
    }

    private void createSoundWaves() {
        soundWave1 = new SoundWave(mGVRContext, 13, 0, 10);
        soundWave1.getTransform().setScale(0.5f, 0.5f, 0.5f);
        soundWave1.getTransform().setPositionY(1.0f);
        soundWave1.getTransform().setPositionX(0.32f);
        soundWave1.getTransform().setPositionZ(0.01f);

        mMic.setRecognitionRmsChangeListener(soundWave1);
    }

    private void createSkybox() {

        mGVRContext.getMainScene().getMainCameraRig().getOwnerObject()
                .getTransform().setPosition(-0f, Util.applyRatioAt(1.70), 0f);

        GVRMesh spaceMesh = mGVRContext.loadMesh(new GVRAndroidResource(
                mGVRContext, R.drawable.skybox_esphere));
        GVRTexture spaceTexture = mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext, R.drawable.skybox));

        GVRSceneObject mSpaceSceneObject = new GVRSceneObject(mGVRContext, spaceMesh, spaceTexture);
        mGVRContext.getMainScene().addSceneObject(mSpaceSceneObject);
        mSpaceSceneObject.getRenderData().setRenderingOrder(0);
    }

    private void addCursorPosition() {

        GVRSceneObject headTracker = new GVRSceneObject(mGVRContext,
                mGVRContext.createQuad(0.5f, 0.5f), mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext, R.drawable.head_tracker)));

        headTracker.getTransform().setPositionZ(-9.0f);
        headTracker.getRenderData().setRenderingOrder(
                GVRRenderData.GVRRenderingOrder.OVERLAY);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mGVRContext.getMainScene().getMainCameraRig().getRightCamera().getOwnerObject().addChildObject(headTracker);
    }

    private void rotateObject(GVRSceneObject spherePack,
            GVRSceneObject cameraObject) {
        spherePack.getTransform().rotateByAxis(
                Util.getZRotationAngle(spherePack, cameraObject), 0, 0, 1);
        spherePack.getTransform().rotateByAxis(
                Util.getYRotationAngle(spherePack, cameraObject), 0, 1, 0);
        spherePack.getChildByIndex(0).getTransform().rotateByAxis(-Util.getZRotationAngle(spherePack, cameraObject), 0, 0, 1);
    }

    @Override
    public void onStep() {

        mMic.onUpdate();

        if (dashboard != null && lastSelectedSphereFlag != null) {
            dashboard.onUpdate();
            if (dashboard.onFocus) {
                heightSync();
            }
        }

        if (isFirstTime) {
            createAndAttachAllEyePointee();
            isFirstTime = false;

        }

        if (!keyboard.isEnabled()) {
            
            interactWithVisibleObjects();
        } else {

            keyboard.update();
            if (spinner != null)

                answer.spinnerUpdate();
        }
    }

    private void interactWithVisibleObjects() {
        GVREyePointeeHolder[] eyePointeeHolders = GVRPicker
                .pickScene(mGVRContext.getMainScene());

        if (lastSelectedSphereFlag != null
                && lastSelectedSphereFlag.answerState == SphereStaticList.ANSWERING) {
            lastSelectedSphereFlag.moveToCursor();
        }

        if (eyePointeeHolders.length == 0) {
            for (GVRSceneObject object : mGVRContext.getMainScene()
                    .getWholeSceneObjects()) {
                if (object instanceof SphereFlag && object.equals(lastSelectedSphereFlag)) {
                    if (((SphereFlag) object).answerState == SphereStaticList.MOVEABLE) {
                        restoreObjectToItsDefaultPosition(object);
                        this.mDisableSnapSound = false;
                        lastSelectedSphereFlag = null;
                    }
                }
            }
        } else {
            for (GVREyePointeeHolder eph : eyePointeeHolders) {
                for (GVRSceneObject object : mGVRContext.getMainScene()
                        .getWholeSceneObjects()) {

                    if (dashboard.hashCode() == object.hashCode()) {
                        continue;
                    }

                    if (eph.getOwnerObject().equals(object)
                            && (lastSelectedSphereFlag == null || lastSelectedSphereFlag.answerState != SphereStaticList.ANSWERING)) {
                        // TODO: Fix (SphereFlag) object.getChildByIndex(0) this
                        // object can be any object .... not only a Sphere.
                        if (object.getChildrenCount() > 0) {
                            lastSelectedSphereFlag = (SphereFlag) object.getChildByIndex(0);
                            if (lastSelectedSphereFlag.answerState == SphereStaticList.MOVEABLE) {
                                moveObject(object);

                                if (this.mDisableSnapSound == false) {
                                    this.mDisableSnapSound = true;
                                    AudioClip.getInstance(mGVRContext.getContext()).playSound(AudioClip.getSnapSoundID(), 1.0f, 1.0f);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void restoreObjectToItsDefaultPosition(GVRSceneObject object) {
        ((SphereFlag) object).stopFloatingSphere();
        ((SphereFlag) object).unspotSphere();
        ((SphereFlag) object).unsnapSphere(1.2f);
    }

    private void moveObject(GVRSceneObject object) {
        ((SphereFlag) object.getChildByIndex(0))
                .stopFloatingSphere();
        ((SphereFlag) (object.getChildByIndex(0))).snapSphere();
        ((SphereFlag) (object.getChildByIndex(0))).spotSphere();
    }

    public void onBackPressed() {

    }

    public void onSingleTap(MotionEvent e) {

        if (mMic != null) {

            mMic.onSingleTap();
        }

        if (keyboard.isEnabled()) {
            keyboard.tapKeyboard();

        } else if (lastSelectedSphereFlag != null
                && lastSelectedSphereFlag.answerState == SphereStaticList.MOVEABLE) {

            AudioClip.getInstance(mGVRContext.getContext()).playSound(AudioClip.getSelectionSoundID(), 1.0f, 1.0f);

            lastSelectedSphereFlag.stopFloatingSphere();
            lastSelectedSphereFlag.answerState = SphereStaticList.ANSWERING;

            animateSpheresBlurState(1, lastSelectedSphereFlag);
            lastSelectedSphereFlag.tapSphere();

            splitQuestion();

            answer.setNumberOfCharecters(lastSelectedSphereFlag.getAnswer().length());
            mMic.updatePosition(answer);
            float positionX = -(mMic.getTransform().getPositionX()) / 2;
            answer.getTransform().setPosition(positionX, 0.87f, Constants.CAMERA_DISTANCE);

            float[] keyboardPosition = Util.calculatePointBetweenTwoObjects(mGVRContext
                    .getMainScene().getMainCameraRig().getOwnerObject(),
                    lastSelectedSphereFlag.getInitialPositionVector(), Constants.SPHERE_SELECTION_DISTANCE);

            showKeyboard();

            soundWave1.update(answer.getSize(), answer.getInitialPosition());

            keyboard.getTransform().setPosition(
                    keyboardPosition[0] - 0.03f,
                    keyboardPosition[1] - 3.0f,
                    keyboardPosition[2]);
            keyboard.getTransform().rotateByAxis(
                    Util.getYRotationAngle(keyboard, mGVRContext.getMainScene()
                            .getMainCameraRig().getOwnerObject()), 0, 1, 0);
            if (dashboard != null) {
                dashboard.show();
                dashboard.reset();
                dashboard.getTransform().setPosition(
                        keyboardPosition[0] + Dashboard.getXPositionOffset(keyboardPosition[0]),
                        keyboardPosition[1] + Dashboard.Y_POSITION_OFFSET,
                        keyboardPosition[2] + Dashboard.getZPositionOffset(keyboardPosition[2]));

                dashboard.getTransform().rotateByAxis(
                        Util.getYRotationAngle(dashboard,
                                mGVRContext.getMainScene().getMainCameraRig()
                                        .getOwnerObject()), 0, 1, 0);
            }

        }
    }

    private void splitQuestion() {
        removeQuestionChildren();

        String questionString = lastSelectedSphereFlag.getQuestion();
        Vector<StringBuffer> lines = StringUtil.splitStringInLines(questionString, QUESTION_LINE_LENGTH);

        addQuestionLines(lines);
    }

    private void removeQuestionChildren() {
        List<GVRSceneObject> children = question.getChildren();
        final int size = children.size();
        for (int i = 0; i < size; i++) {
            question.removeChildObject(children.get(0));
        }
    }

    private void addQuestionLines(Vector<StringBuffer> lines) {
        for (int i = 0; i < lines.size(); i++) {
            InteractiveText line = new InteractiveText(mGVRContext, QUESTION_WIDTH, QUESTION_HEIGHT);
            line.currentText.maxLength = 9999;
            line.currentText.textSize = 80;
            line.setText(mGVRContext, lines.get(i).toString());
            line.getTransform().setPosition(0, 3.33f + 0.4f * (lines.size() - 1 - i), Constants.CAMERA_DISTANCE);
            line.getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.OPACITY, 0);
            line.getRenderData().setRenderingOrder(RenderingOrder.KEYBOARD);
            line.getRenderData().getMaterial().setFloat(TransparentButtonShaderThreeStates.OPACITY, 1);

            question.addChildObject(line);
        }
    }

    private void showKeyboard() {

        this.mGVRContext.runOnGlThread(new Runnable() {

            @Override
            public void run() {

                if (getOnlyNumbers(lastSelectedSphereFlag.getAnswer()).equals("")) {
                    
                    keyboard.showKeyboard(KeyboardType.ALPHA);

                } else {

                    keyboard.showKeyboard(KeyboardType.NUMERIC);
                }
            }
        });
    }

    public void animateSpheresBlurState(int on) {

        for (GVRSceneObject sphereFlag : flagListCostructor.listFlag) {
            if (lastSelectedSphereFlag != null
                    && !lastSelectedSphereFlag.equals(sphereFlag.getChildByIndex(0))) {
                new GVRShaderAnimation(sphereFlag.getChildByIndex(0),
                        SphereShader.BLUR_INTENSITY, 1, on).start(mGVRContext
                        .getAnimationEngine());
                ((SphereFlag) sphereFlag.getChildByIndex(0))
                        .restoreSpherePosition(2f);
            }
        }
    }

    public void animateSpheresBlurState(int on, GVRSceneObject lastIntem2) {

        for (GVRSceneObject sphereFlag : flagListCostructor.listFlag) {

            if (lastIntem2 != null
                    && !lastIntem2.equals(sphereFlag.getChildByIndex(0))) {
                ((SphereFlag) sphereFlag.getChildByIndex(0)).unselectSphere();
            }
        }
    }

    private void createAndAttachAllEyePointee() {
        for (GVRSceneObject object : mGVRContext.getMainScene()
                .getWholeSceneObjects()) {
            if (object instanceof SphereFlag) {

                ((SphereFlag) object).animateFloating();

                attachDefaultEyePointee(object.getParent());
            }
        }
    }

    private void attachDefaultEyePointee(GVRSceneObject sceneObject) {
        sceneObject.attachEyePointeeHolder();
    }

    public void spinnerListenerAnimation(SwipeDirection swipeDirection, float velocityY) {

        if (spinner != null && spinner.isActive()) {
            spinner.getSpinnerRoulette().animate(swipeDirection, velocityY);
        }
    }

    public void createAnswer() {

        answer = new TextField(mGVRContext, this);
        answer.setSpinner(spinner);
        answer.addChildObject(spinner);

    }

    public void createQuestion() {
        question = new GVRSceneObject(mGVRContext);
    }

    @Override
    public void onKeyDelete() {
        answer.removeCharacter(TextField.LAST_CHARACTER);
        AudioClip.getInstance(mGVRContext.getContext()).playSound(AudioClip.getKeyEnterSoundID(), 1.0f, 1.0f);
    }

    @Override
    public void onKeyConfirm() {

        dashboard.hide();

        lastSelectedSphereFlag.giveAnswer(answer.getCurrentText());
        answer.cleanText();
        mGVRContext.getPeriodicEngine().runAfter(new Runnable() {
            @Override
            public void run() {
                animateSpheresBlurState(0);
            }
        }, 3f);
    }

    @Override
    public void onKeyPressedWhitItem(KeyboardCharItem keyboarCharItem) {

        int position = CharList.getInstance(mGVRContext).indexOf(keyboarCharItem.getCharacter());
        CharItem charItem = new CharItem(Keyboard.mode, position, keyboarCharItem.getCharacter());

        answer.append(charItem);
    }

    private String getOnlyNumbers(String string) {

        String numberOnly = string.replaceAll("[^0-9]", "");

        return numberOnly;
    }

    private void createDashboard() {
        dashboard = new Dashboard(mGVRContext, R.raw.empty);
        dashboard.getTransform().setPosition(0, 0, 0);
    }

    public void heightSync() {

        if (lastSelectedSphereFlag != null && (lastSelectedSphereFlag.canMoveTogetherDashboard())
                && (dashboard.isHeightSyncLocked() == false)) {

            if (dashboard.isAboveAnchorPoint()) {
                moveDashDown();

            } else {

                moveDashUp();
            }
        }
    }

    private void moveDashDown() {

        float moveFactor = dashboard.getDeltaY() * Dashboard.HEIGHT_SYNC_SPEED_FACTOR;

        if (moveFactor < 0) {
            moveDash();
        }
    }

    private void moveDashUp() {

        float moveFactor = dashboard.getDeltaY() * Dashboard.HEIGHT_SYNC_SPEED_FACTOR;

        if (moveFactor > 0) {
            moveDash();
        }
    }

    private void moveDash() {

        float moveFactor = dashboard.getDeltaY() * Dashboard.HEIGHT_SYNC_SPEED_FACTOR;

        keyboard.getTransform().setPositionY(
                keyboard.getTransform().getPositionY() + moveFactor);
        dashboard.getTransform().setPositionY(
                dashboard.getTransform().getPositionY() + moveFactor);

        lastSelectedSphereFlag.getParent()
                .getTransform()
                .setPositionY(
                        lastSelectedSphereFlag.getParent().getTransform().getPositionY()
                                + moveFactor);
    }

}
