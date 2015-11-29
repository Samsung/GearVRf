package org.gearvrf.samples.Minimal360Photo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.gearvrf.GVRActivity;

public class Minimal360PhotoActivity extends GVRActivity
{
    private final String photosphereMimeType = "application/vnd.google.panorama360+jpg";
    private final String TAG = "Minimal360PhotoActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Minimal360PhotoScript script = new Minimal360PhotoScript();
        setScript(script, "gvr.xml");

        Intent receivedIntent = this.getIntent();
        if(receivedIntent.getAction().equals(Intent.ACTION_SEND)) {
            String mimeType = receivedIntent.getType();
            if(mimeType.compareTo(photosphereMimeType) == 0) {
                Uri receivedUri = (Uri)receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                if(receivedUri != null) {
                    script.setPhotosphere(this, receivedUri);    
                }
            }
        }
    }
}
