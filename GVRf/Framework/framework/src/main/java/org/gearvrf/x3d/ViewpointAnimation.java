package org.gearvrf.x3d;

import java.util.Date;

import org.gearvrf.GVRSceneObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;


/**
 * 
 * @author m1.williams ViewpointAnimation controls animation from one
 *         <Viewpoint> position to another. X3D permits animation between
 *         viewpoint positions
 */
public class ViewpointAnimation
{
  private long animationTime;
  private long beginAnimation;
  private long endAnimation;
  private boolean animate = false;
  private float[] beginPos =
  {
      0, 0, 0
  };
  private float[] endPos =
  {
      0, 0, 0
  };
  private Quaternionf beginQuaternion = new Quaternionf();
  private Quaternionf endQuaternion = new Quaternionf();

  private float[] totalTranslation =
  {
      0, 0, 0
  };

  public float[] currentPos =
  {
      0, 0, 0
  };
  public Quaternionf currentQuaternion = new Quaternionf();

  public ViewpointAnimation()
  {
  }

  /**
   * 
   * @param animationTime
   *          - animation in seconds
   * @param beginPos
   *          - beginning viewpoint x, y, z position
   * @param endPos
   *          - ending viewpoint x, y, z position
   * @param beginQuaternion
   *          - begining rotation as a quaternion
   * @param endQuaternion
   *          - ending rotation as a quaternion
   */
  public void setupAnimation(float animationTime, float[] beginPos,
      float[] endPos, Quaternionf beginQuaternion, Quaternionf endQuaternion)
  {
    this.animationTime = (long) (animationTime * 1000);
    // check for shortest animation rotation
    float quaternionDotProduct = endQuaternion.dot(beginQuaternion);
    if (quaternionDotProduct < 0)
    {

      // negate the endQuaternion to get a shorter rotation
      endQuaternion.w = -endQuaternion.w;
      endQuaternion.x = -endQuaternion.x;
      endQuaternion.y = -endQuaternion.y;
      endQuaternion.z = -endQuaternion.z;
    }
    this.beginQuaternion.set(beginQuaternion);
    this.endQuaternion.set(endQuaternion);

    for (int i = 0; i < 3; i++)
    {

      this.beginPos[i] = beginPos[i];
      this.endPos[i] = endPos[i];
      this.totalTranslation[i] = this.endPos[i] - beginPos[i];
    }

  }

  public boolean getActiveAnimation()
  {
    return this.animate;
  }

  /**
   * once animation is setup, start the animation record the beginning and
   * ending time for the animation
   */
  public void startAnimation()
  {
    Date time = new Date();
    this.beginAnimation = time.getTime();
    this.endAnimation = beginAnimation + (long) (animationTime * 1000);
    this.animate = true;
  }

  /**
   * called per frame of animation to update the camera position
   */
  public void updateAnimation()
  {
    Date time = new Date();
    long currentTime = time.getTime() - this.beginAnimation;
    if (currentTime > animationTime)
    {
      this.currentQuaternion.set(endQuaternion);
      for (int i = 0; i < 3; i++)
      {

        this.currentPos[i] = this.endPos[i];
      }
      this.animate = false;
    }

    else
    {
      float t = (float) currentTime / animationTime;
      this.currentQuaternion = this.beginQuaternion.slerp(this.endQuaternion,
                                                          t);
      for (int i = 0; i < 3; i++)
      {
        this.currentPos[i] = this.beginPos[i] + totalTranslation[i] * t;
      }

    }
  }

}

