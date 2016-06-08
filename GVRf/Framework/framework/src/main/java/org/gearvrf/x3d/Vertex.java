package org.gearvrf.x3d;

public class Vertex {
	public float[] point = { 0, 0, 0 };

	
	public Vertex (float x, float y, float z) {
		this.point[0] = x;
		this.point[1] = y;
		this.point[2] = z;

	}
	
	public Vertex (float[] values) {
		for (int i = 0; i < 3; i++) {
			this.point[i] = values[i];
		}
	}
}



