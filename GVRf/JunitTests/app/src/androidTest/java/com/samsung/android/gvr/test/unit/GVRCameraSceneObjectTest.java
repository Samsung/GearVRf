package com.samsung.android.gvr.test.unit;

import android.hardware.Camera;

import org.gearvrf.GVRMesh;

import com.samsung.android.gvr.app.R;
import com.samsung.android.gvr.app.base.utils.UtilResource;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import org.gearvrf.scene_objects.GVRCameraSceneObject;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * Created by m.gorll on 2/27/2015.
 */
public class GVRCameraSceneObjectTest extends ActivityInstrumentationGVRf {




    public void testCreateCameraSceneObject() {
        Camera c = Camera.open();
        try {
            GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext, R.mipmap.cylinder));
            GVRCameraSceneObject object = new GVRCameraSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh, c);
            TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(object);
            assertNotNull(object);

            object.pause();
            object.resume();
        } finally {
            c.release();
        }
    }

    public void testCreateCameraSceneObject2() {
        Camera c = Camera.open();
        try {
            GVRCameraSceneObject object = new GVRCameraSceneObject(TestDefaultGVRViewManager.mGVRContext, 10f, 10f, c);
            TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(object);
            assertNotNull(object);

            object.pause();
            object.resume();
        } finally {
            c.release();
        }
    }

    //método onDrawFrame(float drawTime) deve ser chamado pelo framework, embora seja publico, por isso não foi testado.

}
