package org.gearvrf.x3d;

import java.util.Date;

import org.gearvrf.GVRSceneObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class ViewpointAnimation {
   private long animationTime;
   private long beginAnimation;
   private long endAnimation;
   private boolean animate = false;
   private float[] beginPos = {0, 0, 0};
   private float[] endPos = {0, 0, 0};
   private Quaternionf beginQuaternion = new Quaternionf();
   private Quaternionf endQuaternion = new Quaternionf();

   private float[] totalTranslation = {0, 0, 0};

   public float[] currentPos = {0, 0, 0};
   public Quaternionf currentQuaternion = new Quaternionf();

  public ViewpointAnimation() { }

  public void setupAnimation( float animationTime, float[] beginPos, float[] endPos, Quaternionf beginQuaternion, Quaternionf endQuaternion ) {
    this.animationTime = (long)(animationTime * 1000);
    // check for shortest animation rotation
    float quaternionDotProduct = endQuaternion.dot(beginQuaternion);
    if ( quaternionDotProduct < 0) {
      // negate the endQuaternion to get a shorter rotation
      endQuaternion.w = -endQuaternion.w;
      endQuaternion.x = -endQuaternion.x;
      endQuaternion.y = -endQuaternion.y;
      endQuaternion.z = -endQuaternion.z;
    }
    this.beginQuaternion.set(beginQuaternion);
    this.endQuaternion.set(endQuaternion);
    //this.totalRotation = this.endQuaternion. - this.beginQuaternion[i];
    for (int i = 0; i < 3; i++ ) {
      this.beginPos[i] = beginPos[i];
      this.endPos[i] = endPos[i];
      this.totalTranslation[i] = this.endPos[i] - beginPos[i];
    }
    
    /* to be implemented later
    // Get the beginning and ending look at points
    //  for performing the animation from one look at point
    //  to another look at point
    Matrix4f bgnMatrix = new Matrix4f();
    bgnMatrix = beginQuaternion.get(bgnMatrix);
    bgnMatrix.setTranslation(beginPos[0], beginPos[1], beginPos[2]);
    Vector4f bgnLookAt = new Vector4f(0, 0, 10, 1);
    bgnLookAt = bgnMatrix.transform(bgnLookAt);
    
    Matrix4f endMatrix = new Matrix4f();
    endMatrix = endQuaternion.get(endMatrix);
    endMatrix.setTranslation(endPos[0], endPos[1], endPos[2]);
    Vector4f endLookAt = new Vector4f(0, 0, 10, 1);
    endLookAt = endMatrix.transform(endLookAt);
    */
  }

  public boolean getActiveAnimation() {
     return this.animate;
  }

  public void startAnimation() {
      //System.out.println("BGN: Q0 w:" + beginQuaternion.w + ", y:" + beginQuaternion.y +
    //		  	",  Q1 w:" +endQuaternion.w + ", y:" + endQuaternion.y +"; P: BGN x: " +
    //		  	beginPos[0]  +", END x: " + endPos[0]);
	 Date time = new Date();
     this.beginAnimation = time.getTime();
     this.endAnimation = beginAnimation + (long)(animationTime * 1000);
     this.animate = true;
  }
  
  /*
   // used to verify that the Quaternion's SLERP function was producing correct results
  private Quaternionf mySLERP(float t, Quaternionf bgnQuat, Quaternionf endQuat) {
	  Quaternionf quat = new Quaternionf();
	  double theta = Math.acos(bgnQuat.dot(endQuat));
	  float bgnQuatSinAngle = (float)Math.sin(theta * (1 - t));
	  float endQuatSinAngle = (float)Math.sin(theta * t );
	  float DivideSinAngle = (float)Math.sin(theta);
	  quat.w = (bgnQuat.w*bgnQuatSinAngle + endQuat.w*endQuatSinAngle)/DivideSinAngle;
	  quat.x = (bgnQuat.x*bgnQuatSinAngle + endQuat.x*endQuatSinAngle)/DivideSinAngle;
	  quat.y = (bgnQuat.y*bgnQuatSinAngle + endQuat.y*endQuatSinAngle)/DivideSinAngle;
	  quat.z = (bgnQuat.z*bgnQuatSinAngle + endQuat.z*endQuatSinAngle)/DivideSinAngle;
	  return quat;
  }
  */

  public void updateAnimation() {
	Date time = new Date();
    long currentTime = time.getTime() - this.beginAnimation;
    if ( currentTime > animationTime ) {
      //  System.out.println("end: Q w:" + currentQuaternion.w +", y:" +currentQuaternion.y + "; P: x: " + currentPos[0] );
      this.currentQuaternion.set(endQuaternion);
      for (int i = 0; i < 3; i++) {
        this.currentPos[i] = this.endPos[i];
      }
      this.animate = false;
    }
    else {
      float t = (float) currentTime / animationTime;
      this.currentQuaternion = this.beginQuaternion.slerp(this.endQuaternion, t);
      //this.currentQuaternion = mySLERP(t, this.beginQuaternion, this.endQuaternion); // testing my own SLERP function
      for (int i = 0; i < 3; i++) {
        this.currentPos[i] = this.beginPos[i] + totalTranslation[i] * t;
      }
      //System.out.println("t: " + t + "Q w:" + currentQuaternion.w +", y:" +currentQuaternion.y + "; P: x: " + currentPos[0] );
    }
  }

}



