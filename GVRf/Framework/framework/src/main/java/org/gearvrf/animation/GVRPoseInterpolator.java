/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.animation;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.keyframe.GVRFloatAnimation;
import org.gearvrf.animation.keyframe.GVRQuatAnimation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import static org.gearvrf.animation.GVRPose.Bone;
import org.gearvrf.utility.Log;

public class GVRPoseInterpolator extends GVRAnimation
{
    private GVRPose initialPose;
    private GVRPose finalPose;
    private GVRSkeleton pSkeleton;
    private Bone[] mBones;

    private Vector3f poseOnePos;
    private Vector3f poseTwoPos;
    private Vector3f poseOneScl;
    private Vector3f poseTwoScl;
    private Quaternionf poseOneRot;
    private Quaternionf poseTwoRot;

    private Vector3f tempVec;
    private Quaternionf tempQuat;

    private GVRQuatAnimation mRotInterpolator;
    private GVRFloatAnimation mPosInterpolator;
    private GVRFloatAnimation mSclInterpolator;

    private float pDuration;
    private float[] rotData;
    private float[] posData;
    private float[] sclData;
    private float[] poseData;
    private float[] posIData;
    private float[] sclIData;
    private float[] rotIData;

    private int poseDataSize;
    private int initialPosIndex;
    private int finalPosIndex;
    private int initialRotIndex;
    private int finalRotIndex;
    private int initialSclIndex;
    private int finalSclIndex;
    private float startTime;
    private float endTime;
    private int startTimeIndex;
    private int endTimeIndex;
    private int offset;
    private Matrix4f mat;

    public GVRPoseInterpolator(GVRSceneObject target, float duration, GVRPose poseOne, GVRPose poseTwo, GVRSkeleton skeleton)
    {
        super(target, duration);

        initialPose = poseOne;
        finalPose = poseTwo;
        pSkeleton = skeleton;

        poseOnePos =  new Vector3f(0,0,0);
        poseTwoPos =  new Vector3f(0,0,0);
        poseOneScl =  new Vector3f(0,0,0);
        poseTwoScl =  new Vector3f(0,0,0);
        poseOneRot =  new Quaternionf(0,0,0,1);
        poseTwoRot =  new Quaternionf(0,0,0,1);

        rotData = new float[10];
        posData = new float[8];
        sclData = new float[8];

        posIData = new float[3];
        sclIData = new float[3];
        rotIData = new float[4];

        tempVec = new Vector3f(0,0,0);
        tempQuat = new Quaternionf(0,0,0,1);
        initialPosIndex=0; //initial position index
        finalPosIndex=10; //final position index
        initialRotIndex=3; //initial rotation index
        finalRotIndex=13;  //final rotation index
        initialSclIndex=7;
        finalSclIndex=17;
        startTime = 0;
        endTime =  duration;
        startTimeIndex = 0;
        endTimeIndex =  4;
        offset = 0;
        poseDataSize = 20;
        mBones = new Bone[pSkeleton.getNumBones()];
        poseData = new float[poseDataSize*pSkeleton.getNumBones()];
        pDuration = duration;

        for (int i = 0; i < pSkeleton.getNumBones(); i++)
        {
            poseInterpolate(i);
        }
        mat = new Matrix4f();
    }

    public void poseInterpolate(int index)
    {
        setPosePositions(index);
        setPoseRotations(index);
        setPoseScale(index);
    }


    private void setPosePositions(int index)
    {
        initialPose.getLocalPosition(index,poseOnePos);
        finalPose.getLocalPosition(index,poseTwoPos);

        offset = index*poseDataSize;

        setPosePositions(offset+initialPosIndex,poseOnePos);
        setPosePositions(offset+finalPosIndex,poseTwoPos);
    }

    public void setPosePositions(int posOffset, Vector3f posePos)
    {
        poseData[posOffset]=posePos.x();
        poseData[posOffset+1]=posePos.y();
        poseData[posOffset+2]=posePos.z();
    }

    private void setPoseRotations(int index)
    {
        initialPose.getLocalRotation(index,poseOneRot);
        finalPose.getLocalRotation(index,poseTwoRot);
        offset = index*poseDataSize;
        setPoseRotations(offset+initialRotIndex,poseOneRot);
        setPoseRotations(offset+finalRotIndex,poseTwoRot);
    }

    public void setPoseRotations(int rotOffset, Quaternionf poseRot)
    {
        poseData[rotOffset]=poseRot.x();
        poseData[rotOffset+1]=poseRot.y();
        poseData[rotOffset+2]=poseRot.z();
        poseData[rotOffset+3]=poseRot.w();
    }

    private void setPoseScale(int index)
    {
        initialPose.getLocalScale(index,poseOneScl);
        finalPose.getLocalScale(index,poseTwoScl);
        offset = index*poseDataSize;
        setPoseScale(offset+initialSclIndex,poseOneScl);
        setPoseScale(offset+finalSclIndex,poseTwoScl);
    }

    public void setPoseScale(int sclOffset, Vector3f poseScl)
    {
        poseData[sclOffset]=poseScl.x();
        poseData[sclOffset+1]=poseScl.y();
        poseData[sclOffset+2]=poseScl.z();
    }

   public void updatePos(int offset)
   {
       posData[startTimeIndex] = startTime;
       posData[endTimeIndex] = endTime;
       updatePos(1, offset+initialPosIndex);
       updatePos(5, offset+finalPosIndex);
   }

    public void updatePos(int pos, int posOffset)
    {
        posData[pos] = poseData[posOffset];
        posData[pos+1] = poseData[posOffset+1];
        posData[pos+2] = poseData[posOffset+2];
    }

    public void updateRot(int offset)
    {
        rotData[startTimeIndex] = startTime;
        rotData[endTimeIndex+1] = endTime;
        updateRot(1, offset+initialRotIndex);
        updateRot(6, offset+finalRotIndex);
    }

    public void updateRot(int rot, int rotOffset)
    {
        rotData[rot] = poseData[rotOffset];
        rotData[rot+1] = poseData[rotOffset+1];
        rotData[rot+2] = poseData[rotOffset+2];
        rotData[rot+3] = poseData[rotOffset+3];
    }

    public void updateScl(int offset)
    {
        sclData[startTimeIndex] = startTime;
        sclData[endTimeIndex] = endTime;
        updateScl(1, offset+initialSclIndex);
        updateScl(5, offset+finalSclIndex);
    }

    public void updateScl(int scl, int sclOffset)
    {
        sclData[scl] = poseData[sclOffset];
        sclData[scl+1] = poseData[sclOffset+1];
        sclData[scl+2] = poseData[sclOffset+2];
    }

    protected void animate(GVRHybridObject target, float ratio)
    {
        animate(pDuration * ratio);
    }

    public void animate(float timer)
    {

        initialPose = pSkeleton.getPose();

        for(int  i= 0;i < pSkeleton.getNumBones();i++)
        {
            offset = i*poseDataSize;
            updatePos(offset);
            updateRot(offset);
            updateScl(offset);
            mPosInterpolator = new GVRFloatAnimation(posData, 4);
            mRotInterpolator = new GVRQuatAnimation(rotData);
            mSclInterpolator = new GVRFloatAnimation(sclData, 4);
            mPosInterpolator.animate(timer,posIData);
            mRotInterpolator.animate(timer,rotIData);
            mSclInterpolator.animate(timer,sclIData);
            mat.translationRotateScale(posIData[0], posIData[1], posIData[2],rotIData[0], rotIData[1], rotIData[2], rotIData[3],sclIData[0], sclIData[1], sclIData[2]);
            initialPose.setLocalMatrix(i, mat);
            setPosePositions(i);
            setPoseRotations(i);
            setPoseScale(i);
        }
        pSkeleton.poseToBones();
        pSkeleton.updateBonePose();
        pSkeleton.updateSkinPose();

    }


}