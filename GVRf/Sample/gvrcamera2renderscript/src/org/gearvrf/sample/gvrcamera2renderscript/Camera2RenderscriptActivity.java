package org.gearvrf.sample.gvrcamera2renderscript;

import org.gearvrf.GVRActivity;
import android.os.Bundle;

public class Camera2RenderscriptActivity extends GVRActivity 
{
	private Camera2RenderscriptManager mManger;
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mManger = new Camera2RenderscriptManager(this);
        setScript(mManger, "gvr_note4.xml");
    }
	
    @Override
    protected void onPause() {
        super.onPause();
        mManger.onPause();
        finish();
    }
}
