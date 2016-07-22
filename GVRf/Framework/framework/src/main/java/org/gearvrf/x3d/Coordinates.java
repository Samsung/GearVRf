package org.gearvrf.x3d;

public class Coordinates
{
  private short[] coords = new short[3];

  public Coordinates()
  {
  }

  public Coordinates(short x, short y, short z)
  {
    this.coords[0] = x;
    this.coords[1] = y;
    this.coords[2] = z;
  }

  public Coordinates(short[] coord)
  {
    for (int i = 0; i < 3; i++)
    {
      this.coords[i] = coord[i];
    }
  }

  public short[] getCoordinates()
  {
    return coords;
  }

  public short getCoordinate(int i)
  {
    return coords[i];
  }

}
