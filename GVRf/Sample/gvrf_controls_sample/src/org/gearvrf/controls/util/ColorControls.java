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
package org.gearvrf.controls.util;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

public class ColorControls {
    
    private Resources resource;
    
    public ColorControls(Context context){
        this.resource = context.getResources();
    }
    
    public Color parseColor(int resourceId){
        
        int colorInteger = resource.getColor(resourceId);
        
        Color color = new Color(colorInteger);
        
        return color;
    }
    
    public  List<Color> parseColorArray(int resourceId){
        
        List<Color> colorList = new ArrayList<Color>();
        
        int[] colors = resource.getIntArray(resourceId);
        
        for (int i = 0; i < colors.length; i++) { 
            
            int color = colors[i];

            Color cor = new Color(color);
            
            colorList.add(cor);
        }
        
        return colorList;
    }
    
    public class Color{

        private  int hex;
        private  float alpha, red, green, blue;
        
        public Color(int color){
            
            hex = color;

            alpha = android.graphics.Color.alpha(color) / 255.0f;
            red = android.graphics.Color.red(color) / 255.0f;
            green = android.graphics.Color.green(color) / 255.0f;
            blue = android.graphics.Color.blue(color) / 255.0f;
        }

        public float getAlpha() {
            return alpha;
        }
        
        public int getHex() {
            return hex;
        }

        public float getRed() {
            return red;
        }

        public float getGreen() {
            return green;
        }

        public float getBlue() {
            return blue;
        }
    }
}