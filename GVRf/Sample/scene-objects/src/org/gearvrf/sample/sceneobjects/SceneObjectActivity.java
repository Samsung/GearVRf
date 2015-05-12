package org.gearvrf.sample.sceneobjects;

import android.app.Activity;
import android.os.Bundle;
import org.gearvrf.GVRActivity;

public class SceneObjectActivity extends GVRActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setScript(new SampleViewManager(), "gvr_note4.xml");
    }
}
