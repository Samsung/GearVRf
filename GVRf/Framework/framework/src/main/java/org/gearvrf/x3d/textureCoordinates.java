package org.gearvrf.x3d;

public class textureCoordinates {
	public short[] coords = new short[3];

	public textureCoordinates () {
	}
	
	public textureCoordinates (short x, short y, short z) {
		this.coords[0] = x;
		this.coords[1] = y;
		this.coords[2] = z;
	}
	public textureCoordinates (short[] tc) {
		for (int i = 0; i < 3; i++) {
			this.coords[i] = tc[i];
		}
	}

}
