
/**
  *  https://developers.google.com/vr/unity/reference/class/gvr-arm-model-offsets
  *  The GvrArmModel is a standard interface to interact with a scene with the controller.
  * It is responsible for:
  * Determining the orientation and location of the controller.
  * Predict the location of the shoulder, elbow, wrist, and pointer.
  *
  */

package org.gearvrf;
import org.joml.Vector3f;

public interface GVRArmModel{
    void setAddedElbowHeight(float elbowHeight);
    void setAddedElbowDepth(float elbowDepth);
    void setPointerTiltAngle(float tiltAngle);
    void setHandedness(GearCursorController.Handedness newHandedness);
    void setUseAccelerometer(boolean newUseAccelerometer);
    void setFadeDistanceFromFace(float distanceFromFace);
    void setTooltipMinDistanceFromFace(float distanceFromFace);
    Vector3f getPointerPositionOffset();
    float getAddedElbowHeight();
    float getAddedElbowDepth();
    float getPointerTiltAngle();
    boolean getUseAccelerometer();
    float getFadeDistanceFromFace();
    float getTooltipMinDistanceFromFace();
    float getControllerAlphaValue();
    float getTooltipAlphaValue();
    GearCursorController.GazeBehavior getGazeBehavior();
    void setGazeBehavior(GearCursorController.GazeBehavior gazeBehavior);
}