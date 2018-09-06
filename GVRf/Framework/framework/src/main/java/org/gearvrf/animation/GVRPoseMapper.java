package org.gearvrf.animation;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRPose;
import org.gearvrf.animation.GVRSkeleton;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GVRPoseMapper extends GVRAnimation
{
    private GVRSkeleton mSourceSkeleton;
    private GVRSkeleton mDestSkeleton;
    private int[]       mBoneMap;
    private GVRPose     mDestPose;

    /**
     * Constructs an animation retargeting engine.
     */
    public GVRPoseMapper(GVRSkeleton dstskel)
    {
        super(dstskel, 10);
        mDestSkeleton = dstskel;
        mDestPose = new GVRPose(dstskel);
    }

    /**
     * Constructs an animation retargeting engine.
     */
    public GVRPoseMapper(GVRSkeleton dstskel, GVRSkeleton srcskel)
    {
        super(dstskel, 10);
        mDestSkeleton = dstskel;
        mSourceSkeleton = srcskel;
        mDestPose = new GVRPose(dstskel);
    }

    /**
     * Set the source skeleton
     * @param source	source skeleton
     *
     * The source skeleton provides the input animation
     * which will be remapped to the target skeleton.
     *
     * @see GVRSkeleton
     */
    public void	setSourceSkeleton(GVRSkeleton source)
    {
        mSourceSkeleton = source;
    }

    public void	setDestSkeleton(GVRSkeleton dstskel)
    {
        mDestSkeleton = dstskel;
    }

    /**
     * Set the bone map between source and target.
     * @param bonemap	bone mapping data
     *
     * The bone map specifies the source bone index for each
     * target bone. The input should be an array of integers
     * with an entry for each target skeleton bone.
     * In the case where the target skeleton has a bone that is not in
     * the source skeleton, the index should be -1.
     *
     * @see GVRSkeleton
     */
    public void	setBoneMap(int[] bonemap)
    {
        GVRSkeleton	dstskel = mDestSkeleton;
        int			numbones;

        if (bonemap == null)
        {
            return;
        }
        if (dstskel == null)
        {
            return;
        }
        numbones = dstskel.getNumBones();
        if (numbones == 0)
        {
            return;
        }
        mBoneMap = bonemap;
    }


    /*!
     * @fn void PoseMapper::MakeBoneMap(IntArray& bonemap, const Skeleton* srcskel, const Skeleton* dstskel)
     * @param	srcskel	source Skeleton
     * @pararm	dstskel	destination Skeleton
     *
     * Constructs a bone mapping table which gives the index of the destination skeleton bone
     * corresponding to each bone in the source skeleton.
     * @returns 	array with bone mapping indices
     * @see #mapPose
     */
    public int[] makeBoneMap(GVRSkeleton srcskel, GVRSkeleton dstskel)
    {
        int numsrcbones = srcskel.getNumBones();
        int[] bonemap = new int[numsrcbones];
        GVRPose srcPose = srcskel.getBindPose();
        GVRPose dstPose = dstskel.getBindPose();

        for (int i = 0; i < numsrcbones; ++i)
        {
            int boneindex = -1;
            String bonename = srcskel.getBoneName(i);

            if (bonename != null)
            {
                boneindex = dstskel.getBoneIndex(bonename);
            }
            bonemap[i] = boneindex;
            if (boneindex >= 0)
            {
                Log.w("BONE", "%s\n%d: %s\n%d: %s",
                        bonename, i, srcPose.getBone(i).toString(),
                        boneindex, dstPose.getBone(boneindex).toString());
            }
            else
            {
                Log.w("GVRPoseMapper", "makeBoneMap: cannot find bone " + bonename);
            }
        }
        return bonemap;
    }

    /*
     * Updates the color and depth map textures from the Kinect cameras.
     * If a Skeleton is our target or a child, we update the joint angles
     * for the user associated with it.
     */
    public void animate(GVRHybridObject target, float time)
    {
        if ((mSourceSkeleton == null) || !mSourceSkeleton.isEnabled())
        {
            return;
        }
        mapLocalToTarget();
        mDestSkeleton.updateSkinPose();
    }


    /**
     * Maps the pose of the source skeleton onto the destination skeleton in local space.
     * <p>
     * The local bone rotations of matching bones are copied.
     * If the PoseMapper has a bone map, it is used to determine which bones
     * of the source skeleton correspond to which bones in the destination skeleton.
     * This function requires both the source and target skeletons to be set.
     *
     * @returns true if successful, false on error
     */
    public boolean mapLocalToTarget()
    {
        GVRSkeleton	srcskel = mSourceSkeleton;
        GVRSkeleton	dstskel = mDestSkeleton;
        Vector3f v = new Vector3f();

        if ((dstskel == null) || (srcskel == null))
        {
            return false;
        }
        if (mBoneMap == null)
        {
            mBoneMap = makeBoneMap(srcskel, dstskel);
        }
        GVRPose     srcpose = srcskel.getPose();
        Quaternionf q = new Quaternionf();
        int		    numsrcbones = srcskel.getNumBones();
        mDestPose.clearRotations();
        srcskel.getPosition(v);
        dstskel.setPosition(v);
        for (int i = 0; i < numsrcbones; ++i)
        {
            int	boneindex = mBoneMap[i];

            if (boneindex >= 0)
            {
                srcpose.getLocalRotation(i, q);
                mDestPose.setLocalRotation(boneindex, q.x, q.y, q.z, q.w);
            }
        }
        dstskel.applyPose(mDestPose, GVRSkeleton.ROTATION_ONLY);
        return true;
    }

    public boolean mapBindPose()
    {
        GVRSkeleton	srcskel = mSourceSkeleton;
        GVRSkeleton	dstskel = mDestSkeleton;
        Vector3f v = new Vector3f();
        Matrix4f mtx = new Matrix4f();

        if ((dstskel == null) || (srcskel == null))
        {
            return false;
        }
        if (mBoneMap == null)
        {
            mBoneMap = makeBoneMap(srcskel, dstskel);
        }
        GVRPose     srcpose = srcskel.getPose();
        GVRPose     dstbindpose = dstskel.getBindPose();
        int		    numsrcbones = srcskel.getNumBones();

        srcskel.getPosition(v);
        dstskel.setPosition(v);
        for (int i = 0; i < numsrcbones; ++i)
        {
            int	boneindex = mBoneMap[i];

            if (boneindex >= 0)
            {
                dstbindpose.getLocalMatrix(boneindex, mtx);
                mtx.invert();
                mtx.mul(srcpose.getBone(i).LocalMatrix);
                mDestPose.setLocalMatrix(boneindex, mtx);
            }
        }
        dstskel.applyPose(mDestPose, GVRSkeleton.BIND_POSE_RELATIVE);
        return true;
    }

    /**
     * Maps the pose of the source skeleton onto the destination skeleton in world space.
     * <p>
     * The world bone rotations of matching bones are copied.
     * If the PoseMapper has a bone map, it is used to determine which bones
     * of the source skeleton correspond to which bones in the destination skeleton.
     */
    public boolean mapWorldToTarget()
    {
        GVRSkeleton	srcskel = mSourceSkeleton;
        GVRSkeleton	dstskel = mDestSkeleton;

        if ((dstskel == null) || (srcskel == null))
        {
            return false;
        }
        if (mBoneMap == null)
        {
            mBoneMap = makeBoneMap(srcskel, dstskel);
        }
        GVRPose srcpose = srcskel.getPose();
        GVRPose	dstpose = dstskel.getPose();
        Vector3f    v = new Vector3f();
        int			numsrcbones = srcpose.getNumBones();
        Matrix4f	mtx = new Matrix4f();

        srcpose.sync();
        srcpose.getWorldPosition(0, v);
        dstpose.setPosition(v.x, v.y, v.z);
        for (int i = 0; i < numsrcbones; ++i)
        {
            int	boneindex = mBoneMap[i];

            if (boneindex >= 0)
            {
                srcpose.getWorldMatrix(i, mtx);
                dstpose.setWorldMatrix(boneindex, mtx);
            }
        }
        dstpose.sync();
        return true;
    }


    /**
     * Maps the pose of the destination skeleton onto the source skeleton in world space.
     * <p>
     * The world bone rotations of matching bones are copied.
     * If the PoseMapper has a bone map, it is used to determine which bones
     * of the source skeleton correspond to which bones in the destination skeleton.
     */
    public boolean mapWorldToSource()
    {
        GVRSkeleton	srcskel = mSourceSkeleton;
        GVRSkeleton	dstskel = mDestSkeleton;

        if ((dstskel == null) || (srcskel == null))
        {
            return false;
        }
        if (mBoneMap == null)
        {
            mBoneMap = makeBoneMap(srcskel, dstskel);
        }

        GVRPose     srcpose = srcskel.getPose();
        GVRPose	    dstpose = dstskel.getPose();
        int			numsrcbones = srcpose.getNumBones();
        Matrix4f	mtx = new Matrix4f();
        Vector3f    v = new Vector3f();

        dstpose.sync();
        dstpose.getWorldPosition(0, v);
        srcpose.setPosition(v.x, v.y, v.z);
        for (int i = 0; i < numsrcbones; ++i)
        {
            int	boneindex = mBoneMap[i];

            if (boneindex >= 0)
            {
                dstpose.getWorldMatrix(boneindex, mtx);
                srcpose.setWorldMatrix(i, mtx);
            }
        }
        srcpose.sync();
        return true;
    }

}