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

package org.gearvrf.plugins.widget;

public class GVRWidgetSceneObjectMeshInfo {

    public float topLeftx;
    public float topLefty;
    public float bottomrightx;
    public float bottomrighty;

    public int[] mTopLeftOfView;// x y coordinates of the top left of the view
    public int[] mBottomRightOfView;// x y coordinates of bottom right of the
                                    // view

    public float z = 0.0f;// Z is constant for easy mesh creation, app can
                          // transform widget after creation to play with it.

}