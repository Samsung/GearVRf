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
import org.gearvrf.scene_objects.GVRViewSceneObject;
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
    EditObjectView(final GVRScene scene, EditViewChangeListener
            editViewChangeListener) {
        super(scene, R.layout.edit_object_layout);

        this.editViewChangeListener = editViewChangeListener;
    }

    @Override
    // UI Thread
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        ((Button) view.findViewById(R.id.bDone)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.bScaleUp)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.bScaleDown)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.bRemoveFromScene)).setOnClickListener(this);
        tvSceneObjectName = (TextView) view.findViewById(R.id.tvSceneObjectName);

        sbYaw = (SeekBar) view.findViewById(R.id.sbYaw);
        sbYaw.setOnSeekBarChangeListener(this);

        sbPitch = (SeekBar) view.findViewById(R.id.sbPitch);
        sbPitch.setOnSeekBarChangeListener(this);

        sbRoll = (SeekBar) view.findViewById(R.id.sbRoll);
        sbRoll.setOnSeekBarChangeListener(this);
    }

    public void setSceneObject(GVRSceneObject attachedSceneObject) {
        this.sceneObject = attachedSceneObject;
    }

    public void render() {
        mViewSceneObject.getGVRContext().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvSceneObjectName.setText(SCENE_OBJECT_NAME + sceneObject.getName());
            }
        });

        mViewSceneObject.getTransform().setScale( 7.0f, 7.0f, 1.0f);
        mViewSceneObject.getTransform().setPosition(0, -4, -10);
        mViewSceneObject.getTransform().setRotation(0.950f, -0.313f, 0.0f, 0.0f);

        /* FIXME:
        Matrix4f cameraMatrix = this.scene.getMainCameraRig()
                .getHeadTransform().getModelMatrix4f();
        Matrix4f objectMatrix = mViewSceneObject.getTransform().getModelMatrix4f();
        Matrix4f finalMatrix = cameraMatrix.mul(objectMatrix);
        mViewSceneObject.getTransform().setModelMatrix(finalMatrix); */
        show();
    }

    void show() {
        mViewSceneObject.getGVRContext().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sbYaw.setProgress(0);
                sbPitch.setProgress(0);
                sbRoll.setProgress(0);
                sbYaw.setOnSeekBarChangeListener(EditObjectView.this);
                sbPitch.setOnSeekBarChangeListener(EditObjectView.this);
                sbRoll.setOnSeekBarChangeListener(EditObjectView.this);
            }
        });

        super.show();
    }

    void hide() {
        mViewSceneObject.getGVRContext().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sbYaw.setOnSeekBarChangeListener(null);
                sbPitch.setOnSeekBarChangeListener(null);
                sbRoll.setOnSeekBarChangeListener(null);
            }
        });
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
