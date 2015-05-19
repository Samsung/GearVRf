package com.samsung.android.gvr.test.unit;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import com.samsung.android.gvr.app.base.misc.ColorShader;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by santhyago on 2/24/15.
 */
public class GVRBitmapTextureTest extends ActivityInstrumentationGVRf {
    GVRBitmapTexture bitmapTexture;


    /**
     * Tests constructor with a null bitmap
     */
    //FIXME GitHub Issues (https://github.com/Samsung/Gear-VR-Hybrid/issues/12)
    public void ignoreConstructorNullBitmap() {
        try {
            new GVRBitmapTexture(TestDefaultGVRViewManager.mGVRContext, (Bitmap) null);
            fail("Constructor allows null bitmap object.");
        } catch (NullPointerException e) {
            fail(e.getMessage());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests constructor with a null GVRContext
     */
    public void testConstructorNullContext() {
        AssetManager assets = getInstrumentation().getTargetContext().getAssets();
        InputStream is = null;
        try {
            is = assets.open("coke.jpg");
        } catch (FileNotFoundException e) {
            fail("File not found.");
        } catch (IOException e) {
            fail(e.getMessage());
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        try {
            bitmapTexture = new GVRBitmapTexture(null, bitmap);
            fail("Constructor allows null GVRContext object.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Tests constructor with a null GVRContext
     */
    public void testConstructorBigJPG() {
        AssetManager assets = getInstrumentation().getTargetContext().getAssets();
        InputStream is = null;
        try {
            is = assets.open("big_texture.jpg");
        } catch (FileNotFoundException e) {
            fail("File not found.");
        } catch (IOException e) {
            fail(e.getMessage());
        }
        Log.d("testConstructorBigJPG","try decodeStream");
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        try {
            Log.d("testConstructorBigJPG","try new GVRBitmapTexture");
            bitmapTexture = new GVRBitmapTexture(TestDefaultGVRViewManager.mGVRContext, bitmap);

        } catch (NullPointerException e) {
            fail("Constructor not allows object.");
        }
    }

    /**
     * Tests method loadTexture with a 1.8 mb jpg file image
     */
    public void testDecodeBigJPG() {

        try {
            Random random = new Random(System.currentTimeMillis());

            GVRSceneObject object = getColorBoard(1.0f, 1.0f);
            object.getTransform().setPosition(
                    random.nextInt(4) - 2.0f,
                    random.nextInt(6) - 3.0f,
                    random.nextInt(10) - 5.0f);
            object.setName("test");

            //To attachDefaultEyePointee(object);

            GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(TestDefaultGVRViewManager.mGVRContext);
            GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(TestDefaultGVRViewManager.mGVRContext, object.getRenderData().getMesh());
            eyePointeeHolder.addPointee(eyePointee);
            object.attachEyePointeeHolder(eyePointeeHolder);

            TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(object);

            GVRMaterialShaderId STANDARD_SHADER = GVRMaterial.GVRShaderType.Unlit.ID;

            String file = null;

            file= "big_texture18.jpg";
            GVRTexture gvrTexture = TestDefaultGVRViewManager.mGVRContext.loadTexture(file);
            GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, STANDARD_SHADER);
            material.setMainTexture(gvrTexture);
            object.getRenderData().setMaterial(material);


        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * Tests constructor with a null GVRContext
     */
    public void testDecodeSmallJPG() {

        try {
            Random random = new Random(System.currentTimeMillis());

            GVRSceneObject object = getColorBoard(1.0f, 1.0f);
            object.getTransform().setPosition(
                    random.nextInt(4) - 2.0f,
                    random.nextInt(6) - 3.0f,
                    random.nextInt(10) - 5.0f);
            object.setName("test");
            //attachDefaultEyePointee(object);

            GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(TestDefaultGVRViewManager.mGVRContext);
            GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(TestDefaultGVRViewManager.mGVRContext, object.getRenderData().getMesh());
            eyePointeeHolder.addPointee(eyePointee);
            object.attachEyePointeeHolder(eyePointeeHolder);

            TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(object);

            GVRMaterialShaderId STANDARD_SHADER = GVRMaterial.GVRShaderType.Unlit.ID;

            String file = null;
                 file=    "coke.jpg";
           // file= "big_texture18.jpg";
            GVRTexture gvrTexture = TestDefaultGVRViewManager.mGVRContext.loadTexture(file);
            GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, STANDARD_SHADER);
            material.setMainTexture(gvrTexture);
            object.getRenderData().setMaterial(material);


        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
    protected GVRSceneObject getColorBoard(float width, float height)
    {

          final float UNPICKED_COLOR_R = 0.7f;
          final float UNPICKED_COLOR_G = 0.7f;
         final float UNPICKED_COLOR_B = 0.7f;
         final float UNPICKED_COLOR_A = 1.0f;
        ColorShader mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        material.setVec4(ColorShader.COLOR_KEY, UNPICKED_COLOR_R, UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRSceneObject board = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, width, height);
        board.getRenderData().setMaterial(material);

        return board;
    }



    //FIXME GitHub Issues (https://github.com/Samsung/Gear-VR-Hybrid/issues/13)
    public void ignoreConstructorEmptyString() {
        try {
            bitmapTexture = new GVRBitmapTexture(TestDefaultGVRViewManager.mGVRContext, "");
            fail("Constructor allows empty asset file name.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
