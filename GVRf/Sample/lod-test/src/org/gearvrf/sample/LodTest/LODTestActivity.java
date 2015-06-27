package org.gearvrf.sample.LodTest;

import org.gearvrf.GVRActivity;
import org.gearvrf.sample.LodTest.LODTestScript;

import android.app.Activity;
import android.os.Bundle;

public class LODTestActivity extends GVRActivity
{
    LODTestScript lodScript;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        lodScript = new LODTestScript();
        setScript(lodScript, "gvr.xml");
    }
}
