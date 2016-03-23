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

/***************************************************************************
 * Printing Java stacktrace when native crash happens. Using tag gvrf
 ***************************************************************************/

package org.gearvrf.utility;

class GVRJniException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String TAG = "gvrf";

    private GVRJniException(String message) {
        super(message);
    }

    public static void printCallStack(String message) {
        StackTraceElement[] elements = new GVRJniException(message)
                .getStackTrace();
        Log.d(TAG, message);
        for (int i = 1; i < elements.length; i++) {
            Log.d(TAG, elements[i].toString());
        }
    }
}
