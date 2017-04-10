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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorManager;
import org.gearvrf.io.cursor3d.CursorTheme;
import org.gearvrf.io.cursor3d.CursorType;
import org.gearvrf.io.cursor3d.CustomKeyEvent;
import org.gearvrf.io.cursor3d.IoDevice;
import org.gearvrf.io.cursor3d.R;
import org.gearvrf.io.cursor3d.settings.SettingsView.SettingsChangeListener;
import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

class CursorConfigView extends BaseView implements View.OnClickListener {
    private static final String TAG = CursorConfigView.class.getSimpleName();
    private Cursor cursor;
    private final Cursor currentCursor;
    private List<CursorTheme> themes;
    private Drawable themeNormal;
    private Drawable themeSelected;
    private Drawable ioDeviceNormal;
    private Drawable ioDeviceSelected;
    private Drawable greenCircle;
    private Drawable greyCircle;

    private LayoutInflater layoutInflater;
    private CursorManager cursorManager;
    int selectedThemeIndex = 0;
    List<View> themeViews;
    int selectedIoDeviceIndex = 0;
    private List<IoDevice> ioDevicesDisplayed;
    private HashSet<IoDevice> availableIoDevices;
    List<View> ioDeviceViews;
    private SettingsChangeListener changeListener;

    //Called on main thread
    CursorConfigView(final GVRContext context, CursorManager cursorManager, Cursor cursor, Cursor
            currentCursor, final GVRScene scene, int
                             settingsCursorId, SettingsChangeListener changeListener) {
        super(context, scene, settingsCursorId, R.layout.cursor_configuration_layout);
        final GVRActivity activity = context.getActivity();
        loadDrawables(activity);
        layoutInflater = (LayoutInflater) activity.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE);
        this.cursorManager = cursorManager;
        this.cursor = cursor;
        this.currentCursor = currentCursor;
        this.changeListener = changeListener;
        loadThemes();

        TextView tvCursorName = (TextView) findViewById(R.id.tvCursorName);
        tvCursorName.setText(cursor.getName());
        TextView tvCursorType = (TextView) findViewById(R.id.tvCursorType);
        if (cursor.getCursorType() == CursorType.LASER) {
            tvCursorType.setText(R.string.cursor_type_laser);
        } else {
            tvCursorType.setText(R.string.cursor_type_object);
        }
        TextView tvBackButton = (TextView) findViewById(R.id.tvBackButton);
        tvBackButton.setOnClickListener(this);

        TextView done = (TextView) findViewById(R.id.done);
        done.setOnClickListener(this);

        LinearLayout llThemes = (LinearLayout) findViewById(R.id.llThemes);
        themeViews = new ArrayList<View>();
        for (CursorTheme theme : themes) {
            addTheme(theme, llThemes, theme == cursor.getCursorTheme());
        }

        LinearLayout llIoDevices = (LinearLayout) findViewById(R.id.llIoDevices);
        ioDevicesDisplayed = cursor.getAvailableIoDevices();
        availableIoDevices = new HashSet<IoDevice>(ioDevicesDisplayed);
        List<IoDevice> usedIoDevices = cursorManager.getUsedIoDevices();
        HashSet<IoDevice> compatibleIoDevices = new HashSet<IoDevice>(cursor
                .getCompatibleIoDevices());
        for (IoDevice usedIoDevice : usedIoDevices) {
            if (compatibleIoDevices.contains(usedIoDevice) && !availableIoDevices.contains
                    (usedIoDevice))
                ioDevicesDisplayed.add(usedIoDevice);
        }
        ioDeviceViews = new ArrayList<View>();
        for (IoDevice ioDevice : ioDevicesDisplayed) {
            addIoDevice(ioDevice, llIoDevices, ioDevice == cursor.getIoDevice());
        }

        render(0.0f, 0.0f, BaseView.QUAD_DEPTH);
    }

    private void loadDrawables(Context context) {
        Resources resources = context.getResources();
        themeNormal = resources.getDrawable(R.drawable.hlist_background);
        themeSelected = resources.getDrawable(R.drawable.hlist_background_selected);
        ioDeviceNormal = resources.getDrawable(R.drawable.hlist_background);
        ioDeviceSelected = resources.getDrawable(R.drawable.hlist_background_selected);
        greenCircle = resources.getDrawable(R.drawable.green_circle);
        greyCircle = resources.getDrawable(R.drawable.grey_circle);
    }

    private void loadThemes() {
        List<CursorTheme> themes = new ArrayList<CursorTheme>();
        for (CursorTheme theme : cursorManager.getCursorThemes()) {
            if (theme.getCursorType() == cursor.getCursorType()) {
                themes.add(theme);
            }
        }
        this.themes = themes;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tvBackButton) {
            navigateBack(false);
        } else if (id == R.id.done) {
            Log.d(TAG, "Done clicked, close menu");
            navigateBack(true);
        }
    }

    public void addTheme(CursorTheme cursorTheme, ViewGroup parent, boolean isSelected) {
        View themeElementView = layoutInflater.inflate(R.layout.theme_element_layout, parent,
                false);

        TextView tvThemeName = (TextView) themeElementView.findViewById(R.id.tvThemeName);
        ImageView ivThemePreview = (ImageView) themeElementView.findViewById(R.id.ivThemePreview);
        LinearLayout llThemeElement = (LinearLayout) themeElementView.findViewById(R.id
                .llThemeElement);

        tvThemeName.setText(cursorTheme.getName());
        ivThemePreview.setImageResource(ThemeMap.getThemePreview(cursorTheme.getId()));
        if (isSelected) {
            llThemeElement.setBackground(themeSelected);
        } else {
            llThemeElement.setBackground(themeNormal);
        }

        parent.addView(themeElementView);
        themeViews.add(themeElementView);
        if (isSelected) {
            selectedThemeIndex = themeViews.size() - 1;
        }
        themeElementView.setOnClickListener(themeOnClickListener);
    }

    View.OnClickListener themeOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int newSelected = themeViews.indexOf(v);
            Log.d(TAG, "Clicked on position:" + newSelected);
            if (newSelected != selectedThemeIndex) {
                View newThemeView = themeViews.get(newSelected);
                View themeView = themeViews.get(selectedThemeIndex);
                LinearLayout llNewThemeElement = (LinearLayout) newThemeView.findViewById(R.id
                        .llThemeElement);
                LinearLayout llThemeElement = (LinearLayout) themeView.findViewById(R.id
                        .llThemeElement);
                selectedThemeIndex = newSelected;
                llNewThemeElement.setBackground(themeSelected);
                llThemeElement.setBackground(themeNormal);
                cursor.setCursorTheme(themes.get(selectedThemeIndex));
            }
        }
    };

    private void addIoDevice(IoDevice ioDevice, ViewGroup parent, boolean isActive) {

        View convertView = layoutInflater.inflate(R.layout.iodevice_element_layout, parent, false);

        TextView tvIoDeviceName = (TextView) convertView.findViewById(R.id.tvIoDeviceName);
        View vConnectedStatus = convertView.findViewById(R.id.vConnectedStatus);
        RelativeLayout rlIoDeviceElement = (RelativeLayout) convertView.findViewById(R.id
                .rlIoDeviceElement);

        String ioDeviceName = ioDevice.getName();
        if (ioDeviceName == null) {
            ioDeviceName = ioDevice.getDeviceId();
        }
        tvIoDeviceName.setText(ioDeviceName);
        if (isActive) {
            rlIoDeviceElement.setBackground(ioDeviceSelected);
        } else {
            rlIoDeviceElement.setBackground(ioDeviceNormal);
        }

        if (availableIoDevices.contains(ioDevice)) {
            vConnectedStatus.setBackground(greenCircle);
        } else {
            vConnectedStatus.setBackground(greyCircle);
        }

        parent.addView(convertView);
        ioDeviceViews.add(convertView);
        if (isActive) {
            selectedIoDeviceIndex = ioDeviceViews.size() - 1;
        }
        convertView.setOnClickListener(ioDeviceOnClickListener);
    }

    private View.OnClickListener ioDeviceOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int newIoDevicePosition = ioDeviceViews.indexOf(v);
            Log.d(TAG, "Clicked on position:" + newIoDevicePosition);

            IoDevice newIoDevice = ioDevicesDisplayed.get(newIoDevicePosition);
            if (availableIoDevices.contains(newIoDevice) && newIoDevicePosition !=
                    selectedIoDeviceIndex) {
                if (cursor != currentCursor) {
                    try {
                        cursor.attachIoDevice(newIoDevice);
                    } catch (IOException e) {
                        Log.e(TAG, "Device " + newIoDevice.getName() + "cannot be attached");
                    }
                    markIoDeviceSelected(newIoDevicePosition);
                } else {
                    createIoChangeDialog(newIoDevice, newIoDevicePosition);
                }
            }
        }
    };

    private void markIoDeviceSelected(int newIoDevicePosition) {
        View newIoDeviceView = ioDeviceViews.get(newIoDevicePosition);
        View ioDeviceView = ioDeviceViews.get(selectedIoDeviceIndex);
        RelativeLayout rlNewIoDeviceElement = (RelativeLayout) newIoDeviceView.findViewById(R.id
                .rlIoDeviceElement);
        RelativeLayout rlIoDeviceElement = (RelativeLayout) ioDeviceView.findViewById(R.id
                .rlIoDeviceElement);
        selectedIoDeviceIndex = newIoDevicePosition;
        rlNewIoDeviceElement.setBackground(ioDeviceSelected);
        rlIoDeviceElement.setBackground(ioDeviceNormal);
    }

    private void createIoChangeDialog(final IoDevice ioDevice, final int newIoDevicePosition) {
        setSensorEnabled(false);
        new IoChangeDialogView(context, scene, settingsCursorId, new IoChangeDialogView
                .DialogResultListener() {
            @Override
            public void onConfirm() {
                setSettingsCursorId(changeListener.onDeviceChanged(ioDevice));
                markIoDeviceSelected(newIoDevicePosition);
                setSensorEnabled(true);
                navigateBack(true);
            }

            @Override
            public void onCancel() {
                setSensorEnabled(true);
            }
        });
    }

    private void navigateBack(boolean cascading) {
        hide();
        changeListener.onBack(cascading);
    }

    @Override
    void onSwipeEvent(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case CustomKeyEvent.KEYCODE_SWIPE_LEFT:
                Log.d(TAG, "Swipe left");
                //Back event: Issue normal back
                navigateBack(false);
                break;
            case CustomKeyEvent.KEYCODE_SWIPE_RIGHT:
                Log.d(TAG, "Swipe right");
                //OK event: Issue cascading back
                navigateBack(true);
                break;
            default:
                //No need to handle other event types
                break;
        }
    }
}
