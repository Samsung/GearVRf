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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.io.cursor3d.Cursor;
import org.gearvrf.io.cursor3d.CursorManager;
import org.gearvrf.io.cursor3d.CursorType;
import org.gearvrf.io.cursor3d.CustomKeyEvent;
import org.gearvrf.io.cursor3d.IoDevice;
import org.gearvrf.io.cursor3d.R;
import org.gearvrf.utility.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SettingsView extends BaseView implements View.OnClickListener,
        OnCheckedChangeListener {
    private static final String TAG = SettingsView.class.getSimpleName();
    private ListView cursorList;
    private List<Cursor> cursors;
    private CursorManager cursorManager;
    private Cursor currentCursor;
    SettingsChangeListener changeListener;
    CursorAdapter cursorAdapter;
    private ToggleButton tbSoundEnabled;

    public interface SettingsChangeListener {
        void onBack(boolean cascading);

        /**
         * Called when the {@link IoDevice} of the settings cursor changes.
         *
         * @param device the new {@link IoDevice} for the settings cursor
         * @return the new controller id for the settings cursor
         */
        int onDeviceChanged(IoDevice device);
    }

    //Called on main thread
    public SettingsView(final GVRContext context, final GVRScene
            scene, CursorManager cursorManager, int settingsCursorId, final Cursor currentCursor,
                        SettingsChangeListener changeListener) {
        super(context, scene, settingsCursorId, R.layout.settings_layout);
        Log.d(TAG, "new SettingsView, hash=" + this.hashCode());
        final GVRActivity activity = context.getActivity();
        this.changeListener = changeListener;
        this.cursorManager = cursorManager;
        cursorList = (ListView) findViewById(R.id.lvCursors);
        cursors = cursorManager.getActiveCursors();
        cursors.addAll(cursorManager.getInactiveCursors());
        this.currentCursor = currentCursor;
        tbSoundEnabled = (ToggleButton) findViewById(R.id.tbSoundEnable);
        tbSoundEnabled.setChecked(cursorManager.isSoundEnabled());
        tbSoundEnabled.setOnCheckedChangeListener(this);
        // sort the cursors
        Collections.sort(cursors, new Comparator<Cursor>() {
            @Override
            public int compare(Cursor cursor1, Cursor cursor2) {
                return cursor1.getName().compareTo(cursor2.getName());
            }
        });

        TextView tvDoneButton = (TextView) findViewById(R.id.tvDoneButton);
        tvDoneButton.setOnClickListener(this);

        cursorAdapter = new CursorAdapter(activity, cursors);
        cursorList.setAdapter(cursorAdapter);
        render(0.0f, 0.0f, BaseView.QUAD_DEPTH);
    }

    SettingsChangeListener configChangeListener = new SettingsChangeListener() {
        @Override
        public void onBack(boolean cascading) {
            Log.d(TAG, "onBack: cascading=" + cascading);
            cursorAdapter.notifyDataSetChanged();
            if (!cascading) {
                enable();
            } else {
                changeListener.onBack(true);
            }
        }

        @Override
        public int onDeviceChanged(IoDevice device) {
            int settingsCursorId = changeListener.onDeviceChanged(device);
            setSettingsCursorId(settingsCursorId);
            return settingsCursorId;
        }
    };

    private void createConfigView(Cursor cursor) {
        if (cursor.isEnabled()) {
            disable();
            new CursorConfigView(context, cursorManager, cursor, currentCursor, scene,
                    settingsCursorId, configChangeListener);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvDoneButton) {
            Log.d(TAG, "Clicked done button");
            hide();
            changeListener.onBack(false);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.tbSoundEnable) {
            cursorManager.setSoundEnabled(isChecked);
        }
    }

    public class CursorAdapter extends ArrayAdapter<Cursor> {
        int redColor;
        int greenColor;
        LayoutInflater layoutInflater;

        public CursorAdapter(Context context, List<Cursor> cursors) {
            super(context, 0, cursors);
            greenColor = activity.getResources().getColor(R.color.green);
            redColor = activity.getResources().getColor(R.color.red);
            layoutInflater = LayoutInflater.from(getContext());
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Cursor cursor = getItem(position);
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.cursor_list_element_layout,
                        parent, false);
            }
            TextView tvCursorName = (TextView) convertView.findViewById(R.id.tvCursorName);
            final TextView tvIoDevice = (TextView) convertView.findViewById(R.id.tvIoDevice);
            ImageView ivCursorTheme = (ImageView) convertView.findViewById(R.id.ivCursorIcon);
            TextView tvCursorType = (TextView) convertView.findViewById(R.id.tvCursorType);
            final ToggleButton tbCursorEnable = (ToggleButton) convertView.findViewById(R.id
                    .tbCursorEnable);
            ImageView ivCursorEdit = (ImageView) convertView.findViewById(R.id.ivCursorEdit);
            ImageView ivCursorIndicator = (ImageView) convertView.findViewById(R.id
                    .ivCursorIndicator);
            if (cursor == currentCursor) {
                ivCursorIndicator.setVisibility(View.VISIBLE);
            } else {
                ivCursorIndicator.setVisibility(View.INVISIBLE);
            }

            tvCursorName.setText(cursor.getName());
            updateIoDevice(cursor, tvIoDevice);

            if (cursor.getCursorType() == CursorType.LASER) {
                tvCursorType.setText(R.string.cursor_type_laser);
            } else {
                tvCursorType.setText(R.string.cursor_type_object);
            }

            tbCursorEnable.setOnCheckedChangeListener(null);
            tbCursorEnable.setChecked(cursor.isEnabled());
            ivCursorTheme.setImageResource(ThemeMap.getThemePreview(cursor.getCursorTheme().getId
                    ()));

            tbCursorEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Log.d(TAG, "Setting cursor:" + cursor.getName() + " to enable");
                        cursor.setEnable(true);
                        notifyDataSetChanged();
                    } else {
                        IoDevice ioDevice = cursor.getIoDevice();
                        if (ioDevice != null && cursor == currentCursor) {
                            Log.d(TAG, "Cannot disable :" + cursor.getName());
                            tbCursorEnable.setChecked(true);
                        } else {
                            Log.d(TAG, "Setting cursor:" + cursor.getName() + " to disable");
                            cursor.setEnable(false);
                            notifyDataSetChanged();
                        }
                    }
                }
            });

            ivCursorEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createConfigView(cursor);
                }
            });
            return convertView;
        }

        private void updateIoDevice(final Cursor cursor, final TextView tvIoDevice) {
            IoDevice ioDevice = cursor.getIoDevice();
            if (cursor.isEnabled() && ioDevice != null) {
                tvIoDevice.setText(ioDevice.getName());
                tvIoDevice.setTextColor(greenColor);
            } else {
                tvIoDevice.setText(R.string.no_io_device);
                tvIoDevice.setTextColor(redColor);
            }
        }
    }

    @Override
    void onSwipeEvent(KeyEvent keyEvent) {
        //TODO: Combine logic since they are identical, keep for now for logging
        switch (keyEvent.getKeyCode()) {
            case CustomKeyEvent.KEYCODE_SWIPE_LEFT:
                Log.d(TAG, "Swipe left, SettingsView hashCode=" + hashCode());
                //Back event: Issue normal back
                hide();
                changeListener.onBack(false);
                break;
            case CustomKeyEvent.KEYCODE_SWIPE_RIGHT:
                Log.d(TAG, "Swipe right, SettingsView hashCode=" + hashCode());
                //OK event: Issue cascading back
                hide();
                changeListener.onBack(false);
                break;
            default:
                //No need to handle other event types
                break;
        }
    }
}
