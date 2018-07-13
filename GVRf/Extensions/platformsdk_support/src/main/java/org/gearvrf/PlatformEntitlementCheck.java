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

import org.gearvrf.utility.Log;

/**
 * Perform an entitlement check. To use do this in your GVRScript/GVRMain's onInit
 * <pre>
 *      public void onInit(final GVRContext gvrContext) {
 *          PlatformEntitlementCheck.start(gvrContext, "<your_app_id>", new PlatformEntitlementCheck.ResultListener() {
 *              @Override
 *              public void onSuccess() {
 *                  //entitlement check succeeded
 *              }
 *              @Override
 *              public void onFailure() {
 *                  //entitlement check failed
 *              }
 *          });
 *          ...
 *      }
 * </pre>
 */
public final class PlatformEntitlementCheck {

    public interface ResultListener {
        void onSuccess();
        void onFailure();
    }

    /**
     * Starts asynchronous check. Result will be delivered to the listener on the main thread.
     *
     * @param context
     * @param appId application id from the oculus dashboard
     * @param listener listener to invoke when the result is available
     * @throws IllegalStateException in case the platform sdk cannot be initialized
     * @throws IllegalArgumentException if listener is null
     */
    public static void start(final GVRContext context, final String appId, final ResultListener listener) {
        if (null == listener) {
            throw new IllegalArgumentException("listener cannot be null");
        }

        final Activity activity = context.getActivity();
        final long result = create(activity, appId);
        if (0 != result) {
            throw new IllegalStateException("Could not initialize the platform sdk; error code: " + result);
        }

        context.registerDrawFrameListener(new GVRDrawFrameListener() {
            @Override
            public void onDrawFrame(float frameTime) {
                final int result = processEntitlementCheckResponse();
                if (0 != result) {
                    context.unregisterDrawFrameListener(this);

                    final Runnable runnable;
                    if (-1 == result) {
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                listener.onFailure();
                            }
                        };
                    } else {
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess();
                            }
                        };
                    }
                    activity.runOnUiThread(runnable);
                }
            }
        });
    }

    private static native int create(Activity activity, String appId);
    private static native int processEntitlementCheckResponse();

    static {
        System.loadLibrary("gvrf-platformsdk");
    }
    private static final String TAG = "PlatformEntitlementCheck";
}
