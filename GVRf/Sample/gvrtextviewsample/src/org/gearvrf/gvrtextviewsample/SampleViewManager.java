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

package org.gearvrf.simplesample;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRScript;
import org.gearvrf.debug.GVRConsole;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import android.graphics.Color;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SampleViewManager extends GVRScript {

    private GVRContext mGVRContext;
    GVRTextViewSceneObject sceneObject;
    LinearLayout mTextView;
    GVRConsole console;
    SampleActivity mActivity;
    
    
    SampleViewManager(SampleActivity activity){
    	mActivity = activity;
    }
    
    @Override
    public void onInit(GVRContext gvrContext) {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;

        GVRScene scene = gvrContext.getNextMainScene();
        
        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
//        mainCameraRig.getLeftCamera()
//                .setBackgroundColor(Color.WHITE);
//        mainCameraRig.getRightCamera()
//                .setBackgroundColor(Color.WHITE);

        // create a scene object (this constructor creates a rectangular scene
        // object that uses the standard 'unlit' shader)
//        mTextView = mActivity.getTextView();
//        TextView currentTextView = (TextView)mTextView.getChildAt(0);
//        textSize = currentTextView.getTextSize();
 //       sceneObject = new GVRTextViewSceneObject(gvrContext, 2.0f, 2.0f, mTextView);
        sceneObject = new GVRTextViewSceneObject(gvrContext, mActivity);
        textSize = sceneObject.getTextSize();
        
        // set the scene object position
        sceneObject.getTransform().setPosition(0.0f, 0.0f, -2.0f);

        // add the scene object to the scene graph
        gvrContext.getNextMainScene().getMainCameraRig().getOwnerObject().addChildObject(sceneObject);
//        scene.addSceneObject(sceneObject);

    }
    int counter = 0;
    public void onTap(){
    	Log.d("Tap", "Tapped");

    }
    boolean init = false;
    String[] strs = new String[]{"shit","holyshit","colorchange","nochange", "I am handsome"};
    int[] colors = new int[]{Color.BLUE, Color.BLACK, Color.GREEN, Color.WHITE, Color.MAGENTA};
    float textSize = 0.0f;
    @Override
    public void onStep() {
    	/*if(!init){
    		console = new GVRConsole(mGVRContext, EyeMode.BOTH_EYES);
    		init = true;
    	}else{
        	counter++;
        	console.writeLine("Oh my god %d", counter);
    	}*/
    	
    	counter++;
    	if(counter % 50 == 0){
    		int curState = (counter / 50) % 5;
    		Log.d("change", "change to state " + curState);
/*    		TextView currentTextView = (TextView)mTextView.getChildAt(0);
    		currentTextView.setText(strs[curState]);
    		currentTextView.setTextColor(colors[curState]);
    		currentTextView.setTextSize(textSize * curState * 0.5f);*/
    		//sceneObject.setTextColor(colors[curState]);
    		//mTextView.setTextSize(textSize * curState * 0.5f);
    		//sceneObject.setTextSize(textSize * curState * 0.5f);
    		sceneObject.setText(strs[curState]);
    		sceneObject.setTextColor(colors[curState]);
    		sceneObject.setTextSize(textSize * curState * 0.5f);
    		Log.d("change", "change to state end" + curState);
    	}
    }

}
