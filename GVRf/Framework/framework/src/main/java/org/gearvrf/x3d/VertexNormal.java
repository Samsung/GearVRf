package org.gearvrf.x3d;

public class VertexNormal {
	public float[] vector = { 0, 0, 1 };

	
	public VertexNormal (float x, float y, float z) {
		this.vector[0] = x;
		this.vector[1] = y;
		this.vector[2] = z;

	}
	public VertexNormal (float[] vn) {
		for (int i = 0; i < 3; i++) {
			this.vector[i] = vn[i];			
		}
	}
}



