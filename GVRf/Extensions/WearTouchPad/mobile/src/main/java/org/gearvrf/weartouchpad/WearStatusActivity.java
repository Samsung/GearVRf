/*
 * Copyright 2016 Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.gearvrf.weartouchpad;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.gearvrf.wearconstants.TouchEvent;
import org.gearvrf.weartouchpad.WearInputService.LocalBinder;

public class WearStatusActivity extends AppCompatActivity {

    private static final String TAG = WearStatusActivity.class.getSimpleName();
    private LocalBroadcastManager broadcastManager;
    private ListView listView;
    private ArrayAdapter<String> listViewAdapter;
    private boolean serviceBound;
    private TextView tvConnectionStatus;
    private TextView tvPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.lvMotionEvents);
        listViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(listViewAdapter);
        tvConnectionStatus = (TextView) findViewById(R.id.tvConnectionStatus);
        tvPlaceholder = (TextView) findViewById(R.id.tvPlaceholder);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = new Intent(this, WearInputService.class);
        i.setAction(WearInputService.GET_LOCAL_BINDER_ACTION);
        bindService(i, serviceConnection, BIND_AUTO_CREATE);
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(motionEventReceiver, new IntentFilter(WearInputService
                .MOTION_INTENT_FILTER));
        broadcastManager.registerReceiver(connectionEventReceiver, new IntentFilter
                (WearInputService.CONNECTION_INTENT_FILTER));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (serviceBound) {
            unbindService(serviceConnection);
        }
        if (broadcastManager != null) {
            broadcastManager.unregisterReceiver(motionEventReceiver);
            broadcastManager.unregisterReceiver(connectionEventReceiver);
        }
    }

    private String getPrintableEvent(MotionEvent event) {
        StringBuilder builder = new StringBuilder();
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            builder.append(MotionEvent.actionToString(event.getAction()));
        } else {
            builder.append(event.getAction());
        }
        builder.append(", X=").append(String.format("%.2f", event.getX())).append(", Y=").append
                (String.format("%.2f", event.getY()));
        return builder.toString();
    }

    private BroadcastReceiver motionEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TouchEvent touchEvent = new TouchEvent(intent.getByteArrayExtra(WearInputService
                    .TOUCH_EVENT_EXTRA));
            if (touchEvent != null) {
                tvPlaceholder.setVisibility(View.INVISIBLE);
                listViewAdapter.insert(touchEvent.toString(), 0);
            }
        }
    };

    private BroadcastReceiver connectionEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int connectionStatus = intent.getIntExtra(WearInputService.CONNECTION_STATUS_EXTRA, -1);
            if (connectionStatus == WearMessageConstants.MSG_CONNECTION_SUCCESSFUL ||
                    connectionStatus == WearInputService.CONNECT_FROM_WEAR_APP) {
                tvConnectionStatus.setText(getResources().getString(R.string
                        .connection_successful));
            } else if (connectionStatus == WearMessageConstants.MSG_CONNECTION_UNSUCCESFUL) {
                tvConnectionStatus.setText(getResources().getString(R.string
                        .connection_unsuccessful));
            }
        }
    };

    public void clearList(View view) {
        listViewAdapter.clear();
        tvPlaceholder.setVisibility(View.VISIBLE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceBound = true;
            WearInputService wearInputService = ((LocalBinder) service).getService();
            wearInputService.connectToWearApp();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                showAboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.about_message)
                .setTitle(R.string.about_title)
                .setIcon(android.R.drawable.ic_menu_info_details);
        AlertDialog dialog = builder.create();
        dialog.show();
        ((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod
                .getInstance());
    }
}
