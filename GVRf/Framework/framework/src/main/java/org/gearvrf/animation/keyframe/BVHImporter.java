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
package org.gearvrf.animation.keyframe;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRPose;
import org.gearvrf.animation.GVRSkeleton;
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRSkeletonAnimation;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class BVHImporter
{
    private String mFileName;
    private final GVRContext mContext;
    private final ArrayList<String> mBoneNames = new ArrayList();
    private final ArrayList<Vector3f> mBonePositions = new ArrayList();
    private final ArrayList<Integer> mBoneParents = new ArrayList();
    private final ArrayList<Integer> mBoneChannels = new ArrayList();
    private BufferedReader mReader;
    private boolean channelOrder=false;
    private int xPosOffset;
    private int yPosOffset;
    private int zPosOffset;
    private int xRotOffset;
    private int yRotOffset;
    private int zRotOffset;

    public BVHImporter(GVRContext ctx)
    {
        mContext = ctx;
    }

    public GVRSkeletonAnimation importAnimation(GVRAndroidResource res, GVRSkeleton skel) throws IOException
    {
        InputStream stream = res.getStream();

        mFileName = res.getResourceFilename();
        if (stream == null)
        {
            throw new IOException("Cannot open " + mFileName);
        }
        InputStreamReader inputreader = new InputStreamReader(stream);
        mReader = new BufferedReader(inputreader);
        readSkeleton();
        return readMotion(skel);
    }

    public GVRPose importPose(GVRAndroidResource res)  throws IOException
    {
        InputStream stream = res.getStream();

        if (stream == null)
        {
            throw new IOException("Cannot open " + res.getResourceFilename());
        }
        InputStreamReader inputreader = new InputStreamReader(stream);
        mReader = new BufferedReader(inputreader);
        readSkeleton();
        GVRSkeleton skel = createSkeleton();
        return readPose(skel);
    }

    public GVRSkeleton importSkeleton(GVRAndroidResource res) throws IOException
    {
        InputStream stream = res.getStream();

        if (stream == null)
        {
            throw new IOException("Cannot open " + res.getResourceFilename());
        }
        InputStreamReader inputreader = new InputStreamReader(stream);
        mReader = new BufferedReader(inputreader);
        readSkeleton();
        return createSkeleton();
    }

    private int readSkeleton() throws IOException
    {
        String line;

        while ((line = mReader.readLine()) != null)
        {
            line.trim();
            String[] words;

            if (line == "")
                continue;
            words = line.split("[ \t]");
            if (words[0].equals("ROOT"))
            {
                parseJoint(words[1], -1);
                return mBoneParents.size();
            }
        }
        return 0;
    }

    private void parseJoint(String bonename, final int parentIndex) throws IOException
    {
        String      line;
        final int   boneIndex = mBoneParents.size();

        mBoneParents.add(boneIndex, parentIndex);
        mBoneNames.add(boneIndex, bonename);
        mBoneChannels.add(boneIndex, 0);
        while ((line = mReader.readLine().trim()) != null)
        {
            String[]    words = line.split("[ \t]");
            String      opcode;

            if (line == "")
                continue;
            if (words.length < 1)           // has an argument?
                continue;
            opcode = words[0];
            if (opcode.equals("End"))       // end site
            {
                bonename = "end_" + mBoneNames.get(boneIndex);
                parseJoint(bonename, boneIndex);
            }
            else if ((opcode.equals("ROOT")) ||   // found root bone?
                    (opcode.equals("JOINT")))      // found any bone?
            {
                parseJoint(words[1], boneIndex);
            }
            else if (opcode.equals("OFFSET"))       // bone position
            {
                float xpos = Float.parseFloat(words[1]);
                float ypos = Float.parseFloat(words[2]);
                float zpos = Float.parseFloat(words[3]);

                mBonePositions.add(boneIndex, new Vector3f(xpos, ypos, zpos));
            }
            else if (opcode.equals("CHANNELS"))
            {
                if(!channelOrder)
                {
                       for(int j=2; j<5; j++)
                       {
                           if(words[j].equals("Xposition"))  //positions order
                           {
                               xPosOffset = j-2;
                           }
                           else if (words[j].equals("Yposition"))
                           {
                               yPosOffset = j-2;
                           }
                           else if (words[j].equals("Zposition"))
                           {
                               zPosOffset = j-2;
                           }

                       }

                        for(int j=5; j<words.length; j++)
                        {
                            if(words[j].equals("Xrotation"))  //rotations order
                            {
                                xRotOffset = j-5;
                            }
                            else if (words[j].equals("Yrotation"))
                            {
                                yRotOffset = j-5;
                            }
                            else if (words[j].equals("Zrotation"))
                            {
                                zRotOffset = j-5;
                            }

                        }

                    channelOrder = true;
                }
               mBoneChannels.add(boneIndex, Integer.parseInt(words[1]));
            }
            else if (opcode.equals("MOTION") || opcode.equals("}"))
            {
                break;
            }
        }
    }

    public GVRSkeleton createSkeleton()
    {
        int[] boneparents = new int[mBoneParents.size()];
        GVRSkeleton skel;

        for (int i = 0; i < mBoneParents.size(); ++i)
        {
            boneparents[i] = mBoneParents.get(i);
        }
        skel = new GVRSkeleton(mContext, boneparents);
        GVRPose bindpose = new GVRPose(skel);

        for (int i = 0; i < mBoneNames.size(); ++i)
        {
            Vector3f p = mBonePositions.get(i);
            bindpose.setLocalPosition(i, p.x, p.y, p.z);
            skel.setBoneName(i, mBoneNames.get(i));
        }
        skel.setBindPose(bindpose);
        return skel;
    }

    private GVRPose readPose(GVRSkeleton skel) throws IOException
    {
        float       x, y, z;
        String      line;
        String      bvhbonename = "";
        Quaternionf q = new Quaternionf();
        GVRPose     pose = new GVRPose(skel);

        /*
         * Parse and accumulate all the motion keyframes.
         * Keyframes for the root bone position are in rootPosKeys;
         * Keyframes for each bone's rotations are in rootKeysPerBone;
         */
        while ((line = mReader.readLine().trim()) != null)
        {
            String[]    words;

            line = line.trim();
            words = line.split("[\t ]");
            if (line == "")
            {
                continue;
            }
            if (words[0].startsWith("Frame"))
            {
                continue;
            }
            int boneIndex = 0;
            int bvhboneIndex = 0;
            int i = 0;
            while (i + 5 < words.length)
            {
                String boneNameSkel = skel.getBoneName(boneIndex);
                bvhbonename = mBoneNames.get(bvhboneIndex);
                if (bvhbonename.equals(boneNameSkel))
                {
                    if (bvhbonename == null)
                    {
                        throw new IOException("Cannot find bone " + bvhbonename + " in skeleton");
                    }
                    x = Float.parseFloat(words[i+xPosOffset]);    // positions
                    y = Float.parseFloat(words[i+yPosOffset]);
                    z = Float.parseFloat(words[i+zPosOffset]);
                    pose.setLocalPosition(boneIndex, x, y, z);

                    x = Float.parseFloat(words[i+xRotOffset]);
                    y = Float.parseFloat(words[i+yRotOffset]);
                    z = Float.parseFloat(words[i+zRotOffset]);

                    //rotation order

                    for(int order = 0; order < 3; order++)
                    {
                        if(xRotOffset == order)
                        {
                            if(order == 0)
                            {
                                q.rotationX(x * (float) Math.PI / 180);
                            }
                            else
                            {
                                q.rotateX(x * (float) Math.PI / 180);
                            }

                        }
                        else if(yRotOffset == order)
                        {
                            if(order == 0)
                            {
                                q.rotationY(y * (float) Math.PI / 180);
                            }
                            else
                            {
                                q.rotateY(y * (float) Math.PI / 180);
                            }

                        }
                        else if(zRotOffset == order)
                        {
                            if(order == 0)
                            {
                                q.rotationZ(z * (float) Math.PI / 180);
                            }
                            else
                            {
                                q.rotateZ(z * (float) Math.PI / 180);
                            }

                        }

                    }
                    q.normalize();

                    pose.setLocalRotation(boneIndex, q.x, q.y, q.z, q.w);
                    boneIndex++;
                    bvhboneIndex++;

                }
                else
                {
                    boneIndex++;
                }
            }
        }
        return pose;
    }

    public GVRSkeletonAnimation readMotion(GVRSkeleton skel) throws IOException
    {
        int         numbones = skel.getNumBones();
        float       x, y, z;
        String      line;
        String      bonename = "";
        float       secondsPerFrame = 0;
        float       curTime = 0;
        float[]     rotKeys;
        float[]     posKeys;
        ArrayList<float[]> rotKeysPerBone = new ArrayList<>(numbones);
        ArrayList<float[]> posKeysPerBone = new ArrayList<>(numbones);
        Quaternionf q = new Quaternionf();
        Quaternionf b = new Quaternionf();
        GVRPose bindpose = skel.getBindPose();
        int frameIndex = 0;
        int numFrames = 0;

        /*
         * Parse and accumulate all the motion keyframes.
         * Keyframes for the root bone position are in rootPosKeys;
         * Keyframes for each bone's rotations are in rootKeysPerBone;
         */
        while ((line = mReader.readLine()) != null)
        {
            String[] words;

            line = line.trim();
            if (line == "")
            {
                continue;
            }
            words = line.split("[\t ]");
            if (words[0].equals("MOTION"))
            {
                continue;
            }
            if (words[0].startsWith("Frames"))
            {
                for (int i = 0; i < numbones; i++)
                {
                    numFrames = Integer.parseInt(words[1]);
                    float[] r = new float[5 * numFrames];
                    float[] p = new float[4 * numFrames];
                    rotKeysPerBone.add(r);
                    posKeysPerBone.add(p);
                }
                continue;
            }
            if (words[0].equals("Frame") && words[1].startsWith("Time"))
            {
                secondsPerFrame = Float.parseFloat(words[2]);
                continue;
            }
            /*
             * Parsing motion for each frame.
             * Each line in the file contains the root joint position and rotations for all joints.
             */
            int boneIndex = 0;
            int i = 0;
            int f;
            while (i + 3 <= words.length)
            {
                bonename = mBoneNames.get(boneIndex);
                if (bonename == null)
                {
                    throw new IOException("Cannot find bone " + bonename + " in skeleton");
                }
                if (mBoneChannels.get(boneIndex) == 0)
                {
                    ++boneIndex;
                    continue;
                }
                if (mBoneChannels.get(boneIndex) > 3)
                {

                    f = frameIndex * 4;
                    posKeys = posKeysPerBone.get(boneIndex);
                    x = Float.parseFloat(words[i + xPosOffset]);     // X, Y, Z position
                    y = Float.parseFloat(words[i + yPosOffset]);
                    z = Float.parseFloat(words[i + zPosOffset]);

                    posKeys[f] = curTime;
                    posKeys[f + 1] = x;                 // bone position
                    posKeys[f + 2] = y;
                    posKeys[f + 3] = z;
                    i += 3;
                }

                rotKeys = rotKeysPerBone.get(boneIndex);

                x = Float.parseFloat(words[i + xRotOffset]);
                y = Float.parseFloat(words[i + yRotOffset]);
                z = Float.parseFloat(words[i + zRotOffset]);
                i += 3;

                //rotation order

                for(int order = 0; order < 3; order++)
                {
                    if(xRotOffset == order)
                    {
                        if(order == 0)
                        {
                            q.rotationX(x * (float) Math.PI / 180);
                        }
                        else
                        {
                            q.rotateX(x * (float) Math.PI / 180);
                        }

                    }
                    else if(yRotOffset == order)
                    {
                        if(order == 0)
                        {
                            q.rotationY(y * (float) Math.PI / 180);
                        }
                        else
                        {
                            q.rotateY(y * (float) Math.PI / 180);
                        }

                    }
                    else if(zRotOffset == order)
                    {
                        if(order == 0)
                        {
                            q.rotationZ(z * (float) Math.PI / 180);
                        }
                        else
                        {
                            q.rotateZ(z * (float) Math.PI / 180);
                        }

                    }

                }

                q.normalize();
                bindpose.getLocalRotation(boneIndex, b);
                q.mul(b);
                f = 5 * frameIndex;
                rotKeys[f++] = curTime;
                rotKeys[f++] = q.x;
                rotKeys[f++] = q.y;
                rotKeys[f++] = q.z;
                rotKeys[f] = q.w;
                boneIndex++;
                //Log.d("BVH", "%s %f %f %f %f", bonename, q.x, q.y, q.z, q.w);
            }
            curTime += secondsPerFrame;
            if (++frameIndex >= numFrames)
            {
                break;
            }
        }
        /*
         * Create a skeleton animation with separate channels for each bone
         */
        GVRAnimationChannel channel;
        GVRSkeletonAnimation skelanim = new GVRSkeletonAnimation(mFileName, skel, curTime);
        Vector3f pos = new Vector3f();
        for (int boneIndex = 0; boneIndex < mBoneNames.size(); ++boneIndex)
        {
            if (mBoneChannels.get(boneIndex) == 0)
            {
                continue;
            }
            bonename = mBoneNames.get(boneIndex);
            rotKeys = rotKeysPerBone.get(boneIndex);
            posKeys = posKeysPerBone.get(boneIndex);
            if (mBoneChannels.get(boneIndex) == 3)
            {
                skel.getBindPose().getLocalPosition(boneIndex, pos);
                posKeys = new float[] { 0, pos.x, pos.y, pos.z };
            }
            channel = new GVRAnimationChannel(bonename, posKeys, rotKeys, null,
                    GVRAnimationBehavior.DEFAULT, GVRAnimationBehavior.DEFAULT);
            skelanim.addChannel(bonename, channel);
        }
        return skelanim;
    }
}

