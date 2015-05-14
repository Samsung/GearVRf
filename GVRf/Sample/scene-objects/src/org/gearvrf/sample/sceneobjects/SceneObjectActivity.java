package org.gearvrf.sample.sceneobjects;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;

public class SceneObjectActivity extends GVRActivity
{
    private static final String TAG = "SceneObjectActivity";
    private SampleViewManager mViewManager;
    private float lastY = 0;
    private float lastX = 0;
    private float lastYAngle = 0;
    private float lastXAngle = 0;
    private float yangle = 0;
    private float xangle = 0;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mViewManager = new SampleViewManager();
        setScript(mViewManager, "gvr_note4.xml");
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastY = event.getY();
            lastX = event.getX();
        }
        
        float xdifference = lastX - event.getX();
        // android.util.Log.d(TAG, "difference = " + difference);
        if (Math.abs(xdifference) > 10) {
            xangle = lastXAngle + xdifference / 10;
            android.util.Log.d(TAG, "xangle = " + xangle);
            mViewManager.setXAngle(xangle);
            lastX = event.getX();
            lastXAngle = xangle;
        }
        
        float ydifference = lastY - event.getY();
        // android.util.Log.d(TAG, "difference = " + difference);
        if (Math.abs(ydifference) > 10) {
            yangle = lastYAngle + ydifference / 10;
            android.util.Log.d(TAG, "yangle = " + yangle);
            mViewManager.setYAngle(yangle);
            lastY = event.getY();
            lastYAngle = yangle;
        }       
        return true;
    }
}
