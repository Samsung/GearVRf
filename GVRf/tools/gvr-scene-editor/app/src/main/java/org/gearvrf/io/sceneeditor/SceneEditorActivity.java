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

package org.gearvrf.io.sceneeditor;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import org.gearvrf.GVRActivity;
import org.gearvrf.utility.Log;

public class SceneEditorActivity extends GVRActivity {
    private static final String TAG = SceneEditorActivity.class.getSimpleName();
    private final static int PERMISSION_REQUEST_CODE = 13;
    private final static String[] PERMISSIONS = {Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private SceneEditorMain cursorMain;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        cursorMain = new SceneEditorMain();
        setMain(cursorMain, "gvr.xml");

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);
            Log.d(TAG, "Permissions requested!");
        } else {
            Log.d(TAG, "Permissions guaranteed!");
        }
    }

    private static boolean hasPermissions(Activity context, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        cursorMain.saveState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cursorMain.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission " + PERMISSIONS[i] + " denied!");
                    finish();
                    return;
                }
            }
            Log.d(TAG, "Permissions accepted!");
        }
    }
}
