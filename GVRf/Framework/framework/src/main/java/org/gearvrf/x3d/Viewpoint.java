package org.gearvrf.x3d;

import org.gearvrf.GVRSceneObject;

public class Viewpoint {
   private float[] centerOfRotation = {0, 0, 0};
   private String description = "";
   private float fieldOfView = (float) Math.PI / 4;
   private boolean jump = true;
   private String name = "";
   private float[] orientation = {0, 0, 1, 0};
   private float[] position = {0, 0, 10};
   private boolean retainUserOffsets = false;
   private boolean isBound = false;
   GVRSceneObject parent = null;

  public Viewpoint() { }

  public Viewpoint(float[] centerOfRotation, String description, float fieldOfView, 
		  boolean jump, String name, float[] orientation, float[] position, 
		  boolean retainUserOffsets, GVRSceneObject parent) {
     this.description = description;
     this.fieldOfView = fieldOfView;
     this.jump = jump;
     this.name = name;
     this.retainUserOffsets = retainUserOffsets;
     for (int i = 0; i < 3; i++) {
        this.centerOfRotation[i] = centerOfRotation[i];
        this.orientation[i] = orientation[i];
        this.position[i] = position[i];
     }
     this.orientation[3] = orientation[3];
     this.parent = parent;
  }

  public float[] getCenterOfRotation() {
     return this.centerOfRotation;
  }

  public String getDescription() {
     return this.description;
  }

  public float getFieldOfView() {
     return this.fieldOfView;
  }

  public boolean getJump() {
     return this.jump;
  }

  public String getName() {
     return this.name;
  }

  public float[] getOrientation() {
     return this.orientation;
  }

  public float[] getPosition() {
     return this.position;
  }

  public boolean getRetainUserOffsets() {
     return this.retainUserOffsets;
  }

  public GVRSceneObject getParent() {
	  return this.parent;
  }

  public boolean getIsBound() {
	  return this.isBound;
  }

  public void setIsBound(boolean isBound) {
	  this.isBound = isBound;
  }
}



