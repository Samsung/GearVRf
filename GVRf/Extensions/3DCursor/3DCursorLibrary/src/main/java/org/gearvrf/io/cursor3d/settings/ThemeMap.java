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

import org.gearvrf.io.cursor3d.R;

import java.util.HashMap;

class ThemeMap {
    private static ThemeMap instance;
    private HashMap<String, Integer> map;

    private ThemeMap() {
        map = new HashMap<String, Integer>();
        map.put("red_dot", R.drawable.theme_red_dot);
        map.put("white_dot", R.drawable.theme_white_dot);
        map.put("blue_arrow", R.drawable.theme_blue_arrow);
        map.put("plaid_sphere", R.drawable.theme_plaid_sphere);
        map.put("crystal_sphere", R.drawable.theme_crystal_sphere);
        map.put("right_hand", R.drawable.theme_handright);
        map.put("left_hand", R.drawable.theme_handleft);
    }

    private static ThemeMap getInstance() {
        if (instance == null) {
            instance = new ThemeMap();
        }
        return instance;
    }

    public static int getThemePreview(String themeId) {
        ThemeMap themeMap = getInstance();
        if (themeMap.map.containsKey(themeId)) {
            return themeMap.map.get(themeId);
        } else {
            return android.R.drawable.sym_def_app_icon;
        }
    }
}
