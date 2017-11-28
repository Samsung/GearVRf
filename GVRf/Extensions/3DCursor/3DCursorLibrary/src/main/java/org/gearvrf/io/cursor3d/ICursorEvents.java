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

package org.gearvrf.io.cursor3d;

import org.gearvrf.GVRPicker;
import org.gearvrf.IEvents;


public interface ICursorEvents extends IEvents
{
    public void onCursorScale(Cursor cursor);
    public void onTouchStart(Cursor cursor, GVRPicker.GVRPickedObject hit);
    public void onTouchEnd(Cursor cursor, GVRPicker.GVRPickedObject hit);
    public void onExit(Cursor cursor, GVRPicker.GVRPickedObject hit);
    public void onEnter(Cursor cursor, GVRPicker.GVRPickedObject hit);
    public void onDrag(Cursor cursor, GVRPicker.GVRPickedObject hit);
}