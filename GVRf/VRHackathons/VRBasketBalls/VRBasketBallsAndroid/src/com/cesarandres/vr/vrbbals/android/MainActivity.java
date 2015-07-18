package com.cesarandres.vr.vrbbals.android;

import org.gearvrf.GVRActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

public class MainActivity extends GVRActivity {
	private long lastDownTime = 0;

	private BallSpinnerScript script;
	
	public enum COMMANDS {
		DISCONNECTED,
		CONNECTED,
		LEFT,
		UP,
		RESET,
		NONE
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		script = new BallSpinnerScript(this);
		setScript(script, "gvr_note4.xml");
		Log.i("VRBasketBall", "Init application");

		new LeapClientThread(this).start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i("VRBasketBall", "Touch received");
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			lastDownTime = event.getDownTime();
		}

		if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			// check if it was a quick tap
			if (event.getEventTime() - lastDownTime < 200) {
				script.setCommand(COMMANDS.LEFT);
			} else if (event.getEventTime() - lastDownTime > 2000) {
				script.handleLongPress();
			}
		}

		return true;
	}

	public void postEvent(final String command) {
		try{
			COMMANDS newCommand = COMMANDS.valueOf(command);
			script.setCommand(newCommand);
		} catch(Exception e){
			Log.e("Posting Event", e.getLocalizedMessage());
		}
	}
}
