package org.gearvrf.x3d;

public class TimeSensor {
	String name = null;
	float cycleInterval = 1;
	boolean enabled = true;
	boolean loop = false;
	float pauseTime = 0;
	float resumeTime = 0;
	float startTime = 0;
	float stopTime = 0;
	
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
		this.cycleInterval = cycleInterval;
		this.enabled = enabled;
		this.loop = loop;
		this.pauseTime = pauseTime;
		this.resumeTime = resumeTime;
		this.startTime = startTime;
		this.stopTime = stopTime;
	}

}



