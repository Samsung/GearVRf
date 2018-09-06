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

public class BVHImporter
{
    private String mFileName;
    private final GVRContext mContext;
    private final ArrayList<String> mBoneNames = new ArrayList();
    private final ArrayList<Vector3f> mBonePositions = new ArrayList();
    private final ArrayList<Integer> mBoneParents = new ArrayList();

    public BVHImporter(GVRContext ctx)
    {
        mContext = ctx;
    }

    public GVRSkeletonAnimation importAnimation(GVRAndroidResource res) throws IOException
    {
        InputStream stream = res.getStream();

        if (stream == null)
        {
            throw new IOException("Cannot open " + res.getResourceFilename());
        }
        InputStreamReader inputreader = new InputStreamReader(stream);
        BufferedReader buffreader = new BufferedReader(inputreader);

        int numbones = readSkeleton(buffreader);
        GVRSkeleton skel = createSkeleton();
        return readMotion(buffreader, skel);
    }

    public GVRPose importPose(GVRAndroidResource res)  throws IOException
    {
        InputStream stream = res.getStream();

        if (stream == null)
        {
            throw new IOException("Cannot open " + res.getResourceFilename());
        }
        InputStreamReader inputreader = new InputStreamReader(stream);
        BufferedReader buffreader = new BufferedReader(inputreader);

        int numbones = readSkeleton(buffreader);
        GVRSkeleton skel = createSkeleton();
        return readPose(buffreader, skel);
    }

    public GVRSkeleton importSkeleton(GVRAndroidResource res) throws IOException
    {
        InputStream stream = res.getStream();

        if (stream == null)
        {
            throw new IOException("Cannot open " + res.getResourceFilename());
        }
        InputStreamReader inputreader = new InputStreamReader(stream);
        BufferedReader buffreader = new BufferedReader(inputreader);

        int numbones = readSkeleton(buffreader);
        return createSkeleton();
    }

    private int readSkeleton(BufferedReader buffreader) throws IOException
    {
        int         numbones = 0;
        String 		bonename = "";
        float		x, y, z;
        String      line;
        int         parentIndex = -1;
        int         boneIndex = 0;

        while ((line = buffreader.readLine().trim()) != null)
        {
            String[]    words = line.split(" ");
            String      opcode;

            if (line == "")
                continue;
            /*
             * Parsing skeleton definition with joint names and positions.
             */
            if (words.length < 1)               // has an argument?
                continue;
            opcode = words[0];
            if ((opcode == "ROOT") ||			// found root bone?
                    (opcode == "JOINT"))			// found any bone?
            {
                bonename = words[1];            // save the bone name
                mBoneParents.add(boneIndex, parentIndex);
                mBoneNames.add(boneIndex, bonename);
                ++numbones;
            }
            else if (opcode == "OFFSET")       // bone position
            {
                float xpos = Float.parseFloat(words[1]);
                float ypos = Float.parseFloat(words[2]);
                float zpos = Float.parseFloat(words[3]);

                if (bonename.length() > 0)		// save position for the bone
                {
                    mBonePositions.add(boneIndex, new Vector3f(xpos, ypos, zpos));
                    Log.d("BVH", "%s %f %f %f", bonename, xpos, ypos, zpos);
                }
                parentIndex = boneIndex;
                ++boneIndex;
                bonename = "";
                continue;
            }
            else if (opcode == "MOTION")
            {
                break;
            }
        }
        return numbones;
    }

    private GVRSkeleton createSkeleton()
    {
        int[] boneparents = new int[mBoneParents.size()];
        GVRSkeleton skel;

        for (int i = 0; i < mBoneParents.size(); ++i)
        {
            boneparents[i] = mBoneParents.get(i);
        }
        skel = new GVRSkeleton(mContext, boneparents);
        for (int i = 0; i < mBoneNames.size(); ++i)
        {
            skel.setBoneName(i, mBoneNames.get(i));
        }
        return skel;
    }

    public GVRPose readPose(BufferedReader buffreader, GVRSkeleton skel) throws IOException
    {
        float		x, y, z;
        String      line;
        String 		bonename = "";
        int         frameIndex = 0;
        Quaternionf q = new Quaternionf();
        Quaternionf b = new Quaternionf();
        GVRPose     pose = new GVRPose(skel);
        GVRPose     bindpose = skel.getBindPose();

        /*
         * Parse and accumulate all the motion keyframes.
         * Keyframes for the root bone position are in rootPosKeys;
         * Keyframes for each bone's rotations are in rootKeysPerBone;
         */
        while ((line = buffreader.readLine().trim()) != null)
        {
            String[]    words = line.split(" ");

            if (line == "")
                continue;
            if (words[0].startsWith("Frame"))
            {
                continue;
            }
            /*
             * Parsing motion for each frame.
             * Each line in the file contains the root joint position and rotations for all joints.
             */
            x = Float.parseFloat(words[0]);	// root bone position
            y = Float.parseFloat(words[1]);
            z = Float.parseFloat(words[2]);
            pose.setPosition(x, y, z);
            int boneIndex = 0;
            for (int i = 3; i < words.length; i += 3)
            {
                bonename = skel.getBoneName(boneIndex);
                if (bonename == null)
                {
                    throw new IOException("Cannot find bone " + bonename + " in skeleton");
                }
                z = Float.parseFloat(words[i]);	// Z, Y, X rotation angles
                y = Float.parseFloat(words[i + 1]);
                x = Float.parseFloat(words[i + 2]);
                q.rotationZ(z * (float) Math.PI / 180);
                q.rotateX(x * (float)Math.PI / 180);
                q.rotateY(y * (float)Math.PI / 180);
                q.normalize();
                bindpose.getLocalRotation(boneIndex, b);
                q.mul(b);
                int f = (boneIndex * 5) * frameIndex;
                pose.setLocalRotation(boneIndex, q.x, q.y, q.z, q.w);
            }
            break;
        }
        return pose;
    }

    public GVRSkeletonAnimation readMotion(BufferedReader buffreader, GVRSkeleton skel) throws IOException
    {
        int         numbones = skel.getNumBones();
        float		x, y, z;
        String      line;
        String 		bonename = "";
        int         numFrames = 0;
        float       secondsPerFrame = 0;
        float       curTime = 0;
        float[]     rotKeys;
        int         frameIndex = 0;
        ArrayList<float[]> rotKeysPerBone = new ArrayList<>(numbones);
        float[]     rootPosKeys = null;
        Quaternionf q = new Quaternionf();
        Quaternionf b = new Quaternionf();
        GVRPose     bindpose = skel.getBindPose();

        /*
         * Parse and accumulate all the motion keyframes.
         * Keyframes for the root bone position are in rootPosKeys;
         * Keyframes for each bone's rotations are in rootKeysPerBone;
         */
        while ((line = buffreader.readLine().trim()) != null)
        {
            String[]    words = line.split(" ");

            if (line == "")
                continue;
            if (words[0].startsWith("Frames"))
            {
                numFrames = Integer.parseInt(words[1]);
                rootPosKeys = new float[numFrames];
                continue;
            }
            if (words[1].startsWith("Time"))
            {
                secondsPerFrame = Float.parseFloat(words[2]);
                continue;
            }
            /*
             * Parsing motion for each frame.
             * Each line in the file contains the root joint position and rotations for all joints.
             */
            rootPosKeys[frameIndex] = curTime;
            rootPosKeys[frameIndex + 1] = Float.parseFloat(words[0]);	// root bone position
            rootPosKeys[frameIndex + 2] = Float.parseFloat(words[1]);
            rootPosKeys[frameIndex + 3] = Float.parseFloat(words[2]);
            int boneIndex = 0;
            for (int i = 3; i < words.length; i += 3)
            {
                bonename = skel.getBoneName(boneIndex);
                if (bonename == null)
                {
                    throw new IOException("Cannot find bone " + bonename + " in skeleton");
                }
                rotKeys = rotKeysPerBone.get(boneIndex);
                if (rotKeys == null)
                {
                    rotKeys = new float[5 * numbones];
                    rotKeysPerBone.set(boneIndex, rotKeys);
                }
                z = Float.parseFloat(words[i]);	// Z, Y, X rotation angles
                y = Float.parseFloat(words[i + 1]);
                x = Float.parseFloat(words[i + 2]);
                if (true)
                {
                    q.rotationZ(z * (float) Math.PI / 180);
                    q.rotateX(x * (float)Math.PI / 180);
                    q.rotateY(y * (float)Math.PI / 180);
                }
                else
                {
                    q.rotationY(y * (float) Math.PI / 180);
                    q.rotateX(x * (float) Math.PI / 180);
                    q.rotateZ(z * (float) Math.PI / 180);
                }
                q.normalize();
                bindpose.getLocalRotation(boneIndex, b);
                q.mul(b);
                int f = (boneIndex * 5) * frameIndex;
                rotKeys[f++] = curTime;
                rotKeys[f++] = q.x;
                rotKeys[f++] = q.y;
                rotKeys[f++] = q.z;
                rotKeys[f] = q.w;
                frameIndex++;
                curTime += secondsPerFrame;
                Log.d("BVH", "%s %f %f %f %f", bonename, q.x, q.y, q.z, q.w);
            }
        }
        /*
         * Create a skeleton animation with separate channels for each bone
         */
        GVRSkeletonAnimation skelanim = new GVRSkeletonAnimation(mFileName, skel.getOwnerObject(), curTime);
        rotKeys = rotKeysPerBone.get(0);
        GVRAnimationChannel channel = new GVRAnimationChannel(skel.getBoneName(0), rootPosKeys, rotKeys, null,
                GVRAnimationBehavior.DEFAULT, GVRAnimationBehavior.DEFAULT);
        skelanim.addChannel(skel.getBoneName(0), channel);
        for (int boneIndex = 1; boneIndex < numbones; ++boneIndex)
        {
            bonename = skel.getBoneName(boneIndex);
            rotKeys = rotKeysPerBone.get(boneIndex);
            channel = new GVRAnimationChannel(bonename, null, rotKeys, null,
                    GVRAnimationBehavior.DEFAULT, GVRAnimationBehavior.DEFAULT);

            skelanim.addChannel(bonename, channel);
        }
        return skelanim;
    }
}
