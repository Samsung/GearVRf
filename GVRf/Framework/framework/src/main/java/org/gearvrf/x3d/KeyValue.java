package org.gearvrf.x3d;

public class KeyValue {
	public float[] keyValues = null;

	
	public KeyValue (float x, float y, float z) {
		this.keyValues = new float[3];
		this.keyValues[0] = x;
		this.keyValues[1] = y;
		this.keyValues[2] = z;
	}
	
	public KeyValue (float w, float x, float y, float z) {
		this.keyValues = new float[4];
		this.keyValues[0] = w;
		this.keyValues[1] = x;
		this.keyValues[2] = y;
		this.keyValues[3] = z;
	}
	
	public KeyValue (float[] values) {
		this.keyValues = new float[values.length];
		for (int i = 0; i < values.length; i++) {
			this.keyValues[i] = values[i];
		}
	}
}



