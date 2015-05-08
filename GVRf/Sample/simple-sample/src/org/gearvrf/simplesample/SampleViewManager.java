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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.scene_objects.GVRCameraSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;
import org.gearvrf.scene_objects.GVRWebViewSceneObject;

import android.graphics.Color;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SampleViewManager extends GVRScript {

    private GVRContext mGVRContext;
    private GVRScene mMainScene;
    private GVRSceneObject mMainObject;

    @Override
    public void onInit(GVRContext gvrContext) {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;

        mMainScene = gvrContext.getNextMainScene();

        // set background color
        GVRCameraRig mainCameraRig = mMainScene.getMainCameraRig();
        mainCameraRig.getLeftCamera()
                .setBackgroundColor(Color.WHITE);
        mainCameraRig.getRightCamera()
                .setBackgroundColor(Color.WHITE);
        
        buildSceneObjects();
        buildAnimation();
        
        buildEyePointeeHolder();

        // add the scene object to the scene graph
        mMainScene.addSceneObject(mMainObject);
        
    }
    
    private void buildSceneObjects(){

        mMainObject = buildImageObject();
//        mMainObject = buildVideoObject();
//        mMainObject = buildWebViewObject();
//        mMainObject = buildCameraObject();
//        mMainObject = buildModelObject();

        // set the scene object position
        mMainObject.getTransform().setPosition(0.0f, 0.0f, -5.0f);
    }

    
    
    @Override
    public void onStep() {
    	checkEyePointee();
    }

    
    
    // 1. Create image object
	private GVRSceneObject buildImageObject(){
		
		// Create a image scene object (this constructor creates a rectangular scene object)
		GVRSceneObject imageObject = new GVRSceneObject(mGVRContext);
		
		
		// Set Mesh from factory.
        GVRRenderData renderData = new GVRRenderData(mGVRContext);
        renderData.setMesh(mGVRContext.createQuad(4.0f, 2.0f));
		
		 // Load and set texture
        GVRTexture texture = mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext, R.drawable.gearvr_logo));

        // Creat Material and set Texture.
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.Unlit.ID);
        material.setMainTexture(texture);
        renderData.setMaterial(material);

        imageObject.attachRenderData(renderData);
        
        

        // Or, you can make this way. very easily.
        // imageObject = new GVRSceneObject(mGVRContext, 4.0f, 2.0f, texture);
        
        return imageObject;
	}
	
	
	
	// 2. Create video object
	private GVRSceneObject buildVideoObject(){

		// Create your own MediaPlayer.
		// This MediaPlayer follow Android MediaPlayer.
        MediaPlayer mediaPlayer = MediaPlayer.create(mGVRContext.getContext(), R.drawable.tron);
        mediaPlayer.start();
        
        // From inside of GVRVideoSceneObject, refresh MediaPlayer automatically.
		GVRVideoSceneObject videoObject = new GVRVideoSceneObject(mGVRContext, 4.0f,2.0f, mediaPlayer, GVRVideoType.MONO);
		// If you want to use specific MediaPlayer, you can make your own object like GVRVideoObject.
		
		return videoObject;
	}
	
	
	
	// 3. Create webview object
	private GVRSceneObject buildWebViewObject(){

		// Create your own WebView.
		// This WebView follow Android WebView.
		WebView webView = new WebView(mGVRContext.getContext());
		webView.setInitialScale(100);
		webView.measure(2000, 1000);
		webView.layout(0, 0, 2000, 1000);
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl("http://www.google.com");
		
		WebSettings set = webView.getSettings();
		set.setJavaScriptEnabled(true);
		
		
		// From inside of GVRWebViewSceneObject, refresh per 30 fps. ( 0.5 sec ). Also you can change from there.
		GVRWebViewSceneObject webviewObject = new GVRWebViewSceneObject(mGVRContext, 4.0f, 2.0f, webView);
		
		return webviewObject;
	}
	
	
	
	// 4. Create camera object
	private GVRSceneObject buildCameraObject(){
		
		Camera camera = Camera.open();
		
		GVRCameraSceneObject cameraObject = new GVRCameraSceneObject(mGVRContext, 4.0f,  2.0f, camera);
		
		return cameraObject;
	}
	
	
	
	// 5. Create model object
	private GVRSceneObject buildModelObject(){

		// Load mesh from GVRAndroidResouce.
		// Mesh load use Assimp library. 
		// Please see "http://assimp.sourceforge.net"
        GVRMesh sphereMesh = mGVRContext.loadMesh(new GVRAndroidResource(mGVRContext, R.raw.sphere));

        GVRSceneObject modelObject = new GVRSceneObject(mGVRContext, sphereMesh,
        		mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext, R.drawable.earthmap1k)));
        
        return modelObject;
	}
	
	
	
	// 6. Create animation
	private void buildAnimation(){
		
		// Create rotation animation.
		GVRAnimation rotAnimation = new GVRRotationByAxisAnimation(	//
											mMainObject, 50f, 360.0f, //
											0.0f, 1.0f, 0.0f);
		// If you set repeat count to -1, it will be infinite.
		rotAnimation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        
		
        rotAnimation.start(mGVRContext.getAnimationEngine());
	}
	
	
	
	// 7. Build eye point holder for picking.
	private void buildEyePointeeHolder(){
		
		// Create eye point holder.
		// This holder will hold object's eye point.
		// And eye point can determine mesh or box.
		GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(
                mGVRContext);
        GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(mGVRContext,
                mMainObject.getRenderData().getMesh());
        
        // If want to use box type, like this.
        GVRMeshEyePointee eyeRectPointee = new GVRMeshEyePointee(mGVRContext,
        		mMainObject.getRenderData().getMesh().getBoundingBox());
        
        eyePointeeHolder.addPointee(eyePointee);
        mMainObject.attachEyePointeeHolder(eyePointeeHolder);
	}
	
	
	
	// 8. picking object.
	private void checkEyePointee(){
		
		for (GVREyePointeeHolder eph : GVRPicker.pickScene(mGVRContext.getMainScene())) {
            if (eph.getOwnerObject().equals(mMainObject)) {
            	//eye checked Object
                break;
            }
        }
	}
}



