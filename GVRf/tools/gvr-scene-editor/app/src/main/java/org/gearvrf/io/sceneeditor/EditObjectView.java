/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.io.sceneeditor;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.joml.Matrix4f;

class EditObjectView extends BaseView implements OnClickListener, OnSeekBarChangeListener {
    private static final String TAG = EditObjectView.class.getSimpleName();
    private static final float SCALEUP_FACTOR = 1.1f;
    private static final float SCALEDOWN_FACTOR = 0.9f;
    private static final String SCENE_OBJECT_NAME = "SceneObject Name: " ;
    private EditViewChangeListener editViewChangeListener;
    private GVRSceneObject sceneObject;
    private SeekBar sbYaw,sbPitch, sbRoll;
    private int prevYaw, prevPitch, prevRoll;
    private TextView tvSceneObjectName;

    enum ScaleDirection {
        SCALE_UP, SCALE_DOWN
    }

    public interface EditViewChangeListener extends WindowChangeListener {
        void onScaleChange();
        void removeFromScene();
    }

    //Called on main thread
    EditObjectView(final GVRContext context, final GVRScene scene, EditViewChangeListener
            editViewChangeListener) {
        super(context, scene, R.layout.edit_object_layout, 5, 7, false);
        ((Button) findViewById(R.id.bDone)).setOnClickListener(this);
        ((Button) findViewById(R.id.bScaleUp)).setOnClickListener(this);
        ((Button) findViewById(R.id.bScaleDown)).setOnClickListener(this);
        ((Button) findViewById(R.id.bRemoveFromScene)).setOnClickListener(this);
        tvSceneObjectName = (TextView) findViewById(R.id.tvSceneObjectName);

        sbYaw = (SeekBar) findViewById(R.id.sbYaw);
        sbYaw.setOnSeekBarChangeListener(this);

        sbPitch = (SeekBar) findViewById(R.id.sbPitch);
        sbPitch.setOnSeekBarChangeListener(this);

        sbRoll = (SeekBar) findViewById(R.id.sbRoll);
        sbRoll.setOnSeekBarChangeListener(this);

        this.editViewChangeListener = editViewChangeListener;
    }

    public void setSceneObject(GVRSceneObject attachedSceneObject) {
        this.sceneObject = attachedSceneObject;
    }

    public void render() {
        tvSceneObjectName.setText(SCENE_OBJECT_NAME + sceneObject.getName());
        if(viewSceneObject == null) {
            initializeViewSceneObject();
        }
        viewSceneObject.getTransform().reset();
        viewSceneObject.getTransform().setPosition(0, 0, -10);
        viewSceneObject.getTransform().rotateByAxisWithPivot(-35, 1, 0, 0, 0, 0, 0);
        Matrix4f cameraMatrix = gvrContext.getMainScene().getMainCameraRig()
                .getHeadTransform().getModelMatrix4f();
        Matrix4f objectMatrix = viewSceneObject.getTransform().getModelMatrix4f();
        Matrix4f finalMatrix = cameraMatrix.mul(objectMatrix);
        viewSceneObject.getTransform().setModelMatrix(finalMatrix);
        show();
    }

    void show() {
        sbYaw.setProgress(0);
        sbPitch.setProgress(0);
        sbRoll.setProgress(0);
        sbYaw.setOnSeekBarChangeListener(this);
        sbPitch.setOnSeekBarChangeListener(this);
        sbRoll.setOnSeekBarChangeListener(this);
        super.show();
    }

    void hide() {
        sbYaw.setOnSeekBarChangeListener(null);
        sbPitch.setOnSeekBarChangeListener(null);
        sbRoll.setOnSeekBarChangeListener(null);
        super.hide();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bDone:
                hide();
                editViewChangeListener.onClose();
                break;
            case R.id.bScaleUp:
                scaleObject(ScaleDirection.SCALE_UP);
                editViewChangeListener.onScaleChange();
                break;
            case R.id.bScaleDown:
                scaleObject(ScaleDirection.SCALE_DOWN);
                editViewChangeListener.onScaleChange();
                break;
            case R.id.bRemoveFromScene:
                hide();
                editViewChangeListener.removeFromScene();
                break;
        }
    }

    private void scaleObject(ScaleDirection direction) {
        GVRTransform transform = sceneObject.getTransform();
        float[] scale = new float[]{transform.getScaleX(), transform.getScaleY(),
                transform.getScaleZ()};

        if (direction == ScaleDirection.SCALE_DOWN) {
            transform.setScale(scale[0] * SCALEDOWN_FACTOR,
                    scale[1] * SCALEDOWN_FACTOR, scale[2] * SCALEDOWN_FACTOR);
        } else {
            transform.setScale(scale[0] * SCALEUP_FACTOR, scale[1] *
                    SCALEUP_FACTOR, scale[2] * SCALEUP_FACTOR);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        GVRTransform transform = sceneObject.getTransform();
        float angle;
        switch (seekBar.getId()) {
            case R.id.sbYaw:
                angle = (progress - prevYaw)*3.6f;
                transform.rotateByAxis(angle,0,1,0);
                prevYaw = progress;
                break;
            case R.id.sbPitch:
                angle = (progress - prevPitch)*3.6f;
                transform.rotateByAxis(angle,1,0,0);
                prevPitch = progress;
                break;
            case R.id.sbRoll:
                angle = (progress - prevRoll)*3.6f;
                transform.rotateByAxis(angle,0,0,1);
                prevRoll = progress;
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

}
