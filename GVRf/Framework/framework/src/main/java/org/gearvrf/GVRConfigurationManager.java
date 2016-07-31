/* Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf;

import android.app.Activity;

import org.gearvrf.utility.DockEventReceiver;

import java.lang.ref.WeakReference;

abstract class GVRConfigurationManager {

    protected WeakReference<GVRActivity> mActivity;
    private boolean isDockListenerRequired = true;

    protected GVRConfigurationManager(GVRActivity gvrActivity) {
        mActivity = new WeakReference<>(gvrActivity);
    }

    /**
     * For cases where the dock events are not required by the {@link GVRViewManager}, this
     * method returns a <code>false</code>
     *
     * @return <code>true</code> if the framework needs to register a listener to receive
     * dock events.
     */
    public boolean isDockListenerRequired(){
        return isDockListenerRequired;
    }

    /**
     * Set the flag to determine if the dock listener is required or not
     * @param value <code>true</code> is the dock listener is required, <code>false</code>
     *              otherwise.
     */
    void setDockListenerRequired(boolean value){
        isDockListenerRequired = value;
    }

    /**
     * @return true if GearVR is connected, false otherwise
     */
    public abstract boolean isHmtConnected();


    DockEventReceiver makeDockEventReceiver(final Activity gvrActivity, final Runnable runOnDock,
                                            final Runnable runOnUndock) {
        return new DockEventReceiver(gvrActivity, runOnDock, runOnUndock);
    }

}
