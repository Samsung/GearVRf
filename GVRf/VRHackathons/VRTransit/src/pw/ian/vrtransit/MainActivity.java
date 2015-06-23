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


package pw.ian.vrtransit;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.firebase.client.Firebase;

public class MainActivity extends GVRActivity {
    private long lastDownTime = 0;
    private MUNIVisualizerScript s;
	
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Firebase.setAndroidContext(this);
        s = new MUNIVisualizerScript(this);
        setScript(s, "gvr_note4.xml");
        Log.i("VRTransit", "Init application");
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("VRTransit", "Touch received");
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // check if it was a quick tap
            if (event.getEventTime() - lastDownTime < 200) {
            	s.handleTap();
            } else {
            	s.handleLongPress();
            }
        }

        return true;
    }
}
