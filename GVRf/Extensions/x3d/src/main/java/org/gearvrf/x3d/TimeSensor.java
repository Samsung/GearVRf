
/* Copyright 2016 Samsung Electronics Co., LTD
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

/**
 * @author m1.williams
 * Equivalent to X3D's TimeSensor node.
 * Link to gvrKeyFrameAnimation allows the TimeSensor's loop and enabled
 * properties to start and stop animations
 */

package org.gearvrf.x3d;

import org.gearvrf.GVRContext;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;

import java.util.ArrayList;

public class TimeSensor {
	String name = null;
	private float cycleInterval = 1;
	private boolean enabled = true;
	private boolean loop = false;
	float pauseTime = 0;
	float resumeTime = 0;
	float startTime = 0;
	float stopTime = 0;
    private ArrayList<GVRKeyFrameAnimation> gvrKeyFrameAnimations = new ArrayList<GVRKeyFrameAnimation>();

	public TimeSensor() {
		this.name = null;
		this.cycleInterval = 1;
		this.enabled = true;
		this.loop = false;
		this.pauseTime = 0;
		this.resumeTime = 0;
		this.startTime = 0;
		this.stopTime = 0;
	}
	
	public TimeSensor(String name, float cycleInterval, boolean enabled,
			boolean loop, float pauseTime, float resumeTime,
			float startTime, float stopTime) {
		this.name = name;
		if ( cycleInterval > 0) this.cycleInterval = cycleInterval;
        else this.cycleInterval = 1;
		this.enabled = enabled;
		this.loop = loop;
		this.pauseTime = pauseTime;
		this.resumeTime = resumeTime;
		this.startTime = startTime;
		this.stopTime = stopTime;
	}

    /**
     * setEnabled will set the TimeSensor enable variable.
     * if true, then it will started the animation
     * if fase, then it changes repeat mode to ONCE so the animation will conclude.
     * There is no simple stop() animation
     * @param enable
     * @param gvrContext
     */
    public void setEnabled(boolean enable, GVRContext gvrContext) {
        if (this.enabled != enabled ) {
            // a change in the animation stopping / starting
            for (GVRKeyFrameAnimation gvrKeyFrameAnimation : gvrKeyFrameAnimations) {
                if (enable) gvrKeyFrameAnimation.start(gvrContext.getAnimationEngine());
                else {
                    gvrKeyFrameAnimation.setRepeatMode(GVRRepeatMode.ONCE);
                }
            }
            this.enabled = enable;
        }
    }

    /**
     * SetLoop will either set the GVRKeyFrameAnimation's Repeat Mode to REPEATED if loop is true.
     * or it will set the GVRKeyFrameAnimation's Repeat Mode to ONCE if loop is false
     * if loop is set to TRUE, when it was previously FALSE, then start the Animation.
     * @param doLoop
     * @param gvrContext
     */
    public void setLoop(boolean doLoop, GVRContext gvrContext) {
        if (this.loop != doLoop ) {
            // a change in the loop
            for (GVRKeyFrameAnimation gvrKeyFrameAnimation : gvrKeyFrameAnimations) {
                if (doLoop) gvrKeyFrameAnimation.setRepeatMode(GVRRepeatMode.REPEATED);
                else gvrKeyFrameAnimation.setRepeatMode(GVRRepeatMode.ONCE);
            }
            // be sure to start the animations if loop is true
            if ( doLoop ) {
                for (GVRKeyFrameAnimation gvrKeyFrameAnimation : gvrKeyFrameAnimations) {
                    gvrKeyFrameAnimation.start(gvrContext.getAnimationEngine() );
                }
            }
            this.loop = doLoop;
        }
    }

    /**
     * Get the state of the loop property
     * @return
     */
    public boolean getLoop() {
        return this.loop;
    }

    /**
     * Set the TimeSensor's cycleInterval property
     * Currently, this does not change the duration of the animation.
     * @param newCycleInterval
     */
    public void setCycleInterval(float newCycleInterval) {
        if ( (this.cycleInterval != newCycleInterval) && (newCycleInterval > 0) ) {
            for (GVRKeyFrameAnimation gvrKeyFrameAnimation : gvrKeyFrameAnimations) {
                //TODO Cannot easily change the GVRAnimation's GVRChannel once set.
            }
            this.cycleInterval = newCycleInterval;
        }
    }

    /**
     * Return the TimeSensor's cycleInterval value
     * @return
     */
    public float getCycleInterval() {
        return this.cycleInterval;
    }

    /**
     * A TimeSensor can control several X3D animations and thus we have an array list
     * of all the GVRAnimations involved with this TimeSensor so we can set the loop or
     * enabled properties for this single TimeSensor
     * @param gvrKeyFrameAnimation
     */
    public void addGVRKeyFrameAnimation(GVRKeyFrameAnimation gvrKeyFrameAnimation) {
        this.gvrKeyFrameAnimations.add(gvrKeyFrameAnimation);
    }

}



