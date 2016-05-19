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

package org.gearvrf.io.cursor3d.settings;

import android.view.View;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.io.cursor3d.R;
import org.gearvrf.utility.Log;

class IoChangeDialogView extends BaseView implements View.OnClickListener {
    private static final String TAG = IoChangeDialogView.class.getSimpleName();
    private static final float DIALOG_HEIGHT = 3f;
    private static final float DIALOG_WIDTH = 4.5f;
    private static final float DIALOG_DEPTH_OFFSET = 0.5f;

    public interface DialogResultListener {
        void onConfirm();

        void onCancel();
    }

    DialogResultListener dialogResultListener;

    IoChangeDialogView(final GVRContext context, final GVRScene scene, int
            settingsCursorId, DialogResultListener dialogResultListener) {
        super(context, scene, settingsCursorId, R.layout.iochange_dialog_layout, DIALOG_HEIGHT,
                DIALOG_WIDTH);
        this.dialogResultListener = dialogResultListener;
        TextView tvConfirm = (TextView) findViewById(R.id.tvConfirm);
        TextView tvCancel = (TextView) findViewById(R.id.tvCancel);
        tvConfirm.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        render(0.0f, 0.0f, BaseView.QUAD_DEPTH + DIALOG_DEPTH_OFFSET);
        this.dialogResultListener = dialogResultListener;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvConfirm) {
            Log.d(TAG, "Confirmed dialog");
            hide();
            dialogResultListener.onConfirm();
        } else if (id == R.id.tvCancel) {
            Log.d(TAG, "Cancelled dialog");
            hide();
            dialogResultListener.onCancel();
        }
    }
}
