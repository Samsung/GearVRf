package org.gearvrf.x3d;

public class coordinates {
	public short[] coords = new short[3];

	public coordinates () {
	}
	
	public coordinates (short x, short y, short z) {
		this.coords[0] = x;
		this.coords[1] = y;
		this.coords[2] = z;
	}
	public coordinates (short[] coord) {
		for (int i = 0; i < 3; i++) {
			this.coords[i] = coord[i];
		}
	}
}
