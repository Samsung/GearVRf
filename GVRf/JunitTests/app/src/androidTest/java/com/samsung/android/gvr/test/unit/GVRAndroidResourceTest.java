package com.samsung.android.gvr.test.unit;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.samsung.android.gvr.app.R;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;
import org.gearvrf.GVRAndroidResource;
import org.junit.Test;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

/**
 * Created by j.elidelson on 5/15/2015.
 */
public class GVRAndroidResourceTest extends ActivityInstrumentationGVRf {

    public void testConstructorNullResource() {
        try {
            GVRAndroidResource gvrAndroidResource;
            gvrAndroidResource = new GVRAndroidResource(TestDefaultGVRViewManager.mGVRContext, null);
            assertNotNull("Resource was null: ", gvrAndroidResource);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void testConstructorNullContext() {
        try {
            Context ctx = null;
            GVRAndroidResource gvrAndroidResource;
            gvrAndroidResource = new GVRAndroidResource(ctx,R.raw.sample_20140509_r);
            assertNotNull("Resource was null: ", gvrAndroidResource);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    public void testConstructorStringPath() {
        String resourcePath = Environment.getExternalStorageDirectory()+"/home.png";
        try {
            GVRAndroidResource gvrAndroidResource = new GVRAndroidResource(resourcePath);
            assertNotNull("Resource is null: " + resourcePath, gvrAndroidResource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void testConstructorFile() {
        String resourcePath = Environment.getExternalStorageDirectory()+"/home.png";
        try {
            File file = new File(resourcePath);
            GVRAndroidResource gvrAndroidResource = new GVRAndroidResource(file);
            assertNotNull("Resource is null: " + resourcePath, gvrAndroidResource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void testConstructor_GVRContext_Resource() {
        try {
            GVRAndroidResource gvrAndroidResource = new GVRAndroidResource(TestDefaultGVRViewManager.mGVRContext,R.raw.sample_20140509_r);
            assertNotNull("R.raw.cinema was not loaded: ", gvrAndroidResource);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }



}
