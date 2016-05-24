/*
 * Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.io.cursor3d;

/**
 * Register this listener to receive callbacks whenever a {@link Cursor} is activated or
 * deactivated from the application.
 * <p/>
 * The methods associated with the listener will be triggered only when a Cursor becomes active
 * or inactive during runtime.
 * <p/>
 * To know the Cursor objects already available to the application at app launch use the
 * {@link CursorManager#getActiveCursors()} call to get a list of all the cursors currently
 * active
 */
public interface CursorActivationListener {
    /**
     * Called when a {@link Cursor} has been activated. i.e when a {@link Cursor} is enabled and
     * has a compatible {@link IoDevice} attached.
     * <p>
     * Note that this call will be made based on the type of Cursor, for eg. will be
     * called twice for dual cursors.
     * </p>
     *
     * @param cursor an object representing a {@link Cursor}.
     */
    void onActivated(Cursor cursor);

    /**
     * Notification are received whenever a {@link Cursor} is deactivated. i.e. when there is no
     * compatible {@link IoDevice} available or cursor has been disabled.
     *
     * @param cursor an object representing a {@link Cursor} that has been removed.
     */
    void onDeactivated(Cursor cursor);
}