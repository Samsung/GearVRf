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

package org.gearvrf.controls.menu.color;

import org.gearvrf.GVRContext;
import org.gearvrf.controls.R;
import org.gearvrf.controls.menu.MenuControlSceneObject;
import org.gearvrf.controls.util.ColorControls;
import org.gearvrf.controls.util.ColorControls.Color;

import java.util.ArrayList;
import java.util.List;

public class ParseColorItem {

    private List<MenuControlSceneObject> listItens = new ArrayList<MenuControlSceneObject>();

    public ParseColorItem(GVRContext gvrContext) {

        ColorControls color = new ColorControls(gvrContext.getContext());
        
        List<Color> colorList = color.parseColorArray(R.array.worm_colors);
        
        for(Color c : colorList){
            ColorsButton button = new ColorsButton(gvrContext, c);
            listItens.add(button);
        }
    }

    public ArrayList<MenuControlSceneObject> getList() {
        return (ArrayList<MenuControlSceneObject>) listItens;
    }
}