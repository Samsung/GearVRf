package org.gearvrf.animation;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventReceiver;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.IAssetEvents;
import org.gearvrf.IEventReceiver;
import org.gearvrf.IEvents;
import org.gearvrf.animation.keyframe.GVRSkeletonAnimation;
import org.gearvrf.animation.keyframe.TRSImporter;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Group of animations that can be collectively manipulated.
 *
 * Typically the animations belong to a particular model and
 * represent a sequence of poses for the model over time.
 * This class allows you to start, stop and set animation modes
 * for all the animations in the group at once.
 * An asset which has animations will have this component
 * attached to collect the animations for the asset.
 *
 * @see org.gearvrf.GVRAssetLoader
 * @see org.gearvrf.GVRExternalScene
 * @see GVRAvatar
 * @see GVRAnimationEngine
 */
public class GVRAvatar extends GVRBehavior implements IEventReceiver
{
    private static final String TAG = Log.tag(GVRAvatar.class);
    static private long TYPE_AVATAR = newComponentType(GVRAvatar.class);
    protected List<GVRAnimator> mAnimations;
    protected GVRSkeleton mSkeleton;
    protected final GVRSceneObject mModelRoot;
    protected boolean mIsRunning;
    protected GVREventReceiver mReceiver;
    protected List<GVRAnimator> mAnimQueue = new ArrayList<GVRAnimator>();

    /**
     * Make an instance of the GVRAnimator component.
     * Auto-start is not enabled - a call to start() is
     * required to run the animations.
     *
     * @param ctx GVRContext for this avatar
     */
    public GVRAvatar(GVRContext ctx, String name)
    {
        super(ctx);
        mReceiver = new GVREventReceiver(this);
        mType = getComponentType();
        mModelRoot = new GVRSceneObject(ctx);
        mModelRoot.setName(name);
        mAnimations = new ArrayList<GVRAnimator>();
    }

    static public long getComponentType() { return TYPE_AVATAR; }

    /**
     * Get the event receiver for this avatar.
     * <p>
     * The avatar will generate events when assets are loaded,
     * animations are started or finished. Clients can observe
     * these events by attaching IAvatarEvent listeners to
     * this event receiver.
     */
    public GVREventReceiver getEventReceiver() { return mReceiver; }

    /**
     * Get the name of this avatar (supplied at construction time).
     * @returns string with avatar name
     */
    public String getName() { return mModelRoot.getName(); }

    /**
     * Get the skeleton for the avatar.
     * <p>
     * The skeleton is part of the avatar model. When the asset loader loads
     * the avatar model, the skeleton should be part of the asset.
     * @return skeleton associated with the avatar
     */
    public  GVRSkeleton getSkeleton() { return mSkeleton; }

    /**
     * Get the root of the scene object hierarchy for the avatar.
     * <p>
     * The avatar model is constructed by the asset loader when the avatar
     * model is loaded. It contains the scene hierarchy with the skeleton
     * bones and the meshes for the avatar.
     * @return root of the avatar model hierarchy
     */
    public GVRSceneObject getModel() { return mModelRoot; }

    /**
     * Determine if this avatar is currently animating.
     */
    public boolean isRunning() { return mIsRunning; }

    /**
     * Query the number of animations owned by this avatar.
     * @return number of animations added to this avatar
     */
    public int getAnimationCount() { return mAnimations.size(); }


    protected GVROnFinish mOnFinish = new GVROnFinish()
    {
        public void finished(GVRAnimation animation)
        {
            if (mAnimQueue.size() > 0)
            {
                GVRAnimator animator = mAnimQueue.get(0);
                if (animator.findAnimation(animation) >= 0)
                {
                    animator.reset();
                    mAnimQueue.remove(0);
                    mIsRunning = false;
                    if (mAnimQueue.size() > 0)
                    {
                        animator = mAnimQueue.get(0);
                        animator.start(mOnFinish);
                        mModelRoot.getGVRContext().getEventManager().sendEvent(GVRAvatar.this, IAvatarEvents.class,
                                                                               "onAnimationFinished", animator, animation);
                        mModelRoot.getGVRContext().getEventManager().sendEvent(GVRAvatar.this, IAvatarEvents.class,
                                                                               "onAnimationStarted", animator);
                    }
                    else
                    {
                        mModelRoot.getGVRContext().getEventManager().sendEvent(GVRAvatar.this, IAvatarEvents.class,
                                                                               "onAnimationFinished", animator, animation);
                    }
                }
            }
        }
    };

    /**
     * Load the
     * @param avatarResource
     */
    public void loadModel(GVRAndroidResource avatarResource)
    {
        EnumSet<GVRImportSettings> settings = GVRImportSettings.getRecommendedSettingsWith(EnumSet.of(GVRImportSettings.OPTIMIZE_GRAPH, GVRImportSettings.NO_ANIMATION));
        GVRContext ctx = mModelRoot.getGVRContext();
        GVRResourceVolume volume = new GVRResourceVolume(ctx, avatarResource);
        GVRSceneObject previousAvatar = (mModelRoot.getChildrenCount() > 0) ? mModelRoot.getChildByIndex(0) : null;

        if (previousAvatar != null)
        {
            mModelRoot.removeChildObject(previousAvatar);
        }
        ctx.getAssetLoader().loadModel(volume, mModelRoot, settings, false, mLoadModelHandler);
    }

    public void loadAnimation(GVRAndroidResource animResource)
    {
        String filePath = animResource.getResourcePath();
        GVRContext ctx = mModelRoot.getGVRContext();
        GVRResourceVolume volume = new GVRResourceVolume(ctx, animResource);

        if (filePath.endsWith(".txt"))
        {
            try
            {
                TRSImporter importer = new TRSImporter(ctx);
                GVRSkeletonAnimation skelAnim = importer.importAnimation(animResource, mSkeleton);
                GVRAnimator animator = new GVRAnimator(ctx);
                animator.setName(filePath);
                animator.addAnimation(skelAnim);
                addAnimation(animator);
                ctx.getEventManager().sendEvent(this,
                                                IAvatarEvents.class,
                                                "onAnimationLoaded",
                                                animator,
                                                filePath,
                                                null);
            }
            catch (IOException ex)
            {
                ctx.getEventManager().sendEvent(this,
                                                IAvatarEvents.class,
                                                "onAnimationLoaded",
                                                null,
                                                filePath,
                                                ex.getMessage());
            }
        }
        else
        {
            EnumSet<GVRImportSettings> settings = GVRImportSettings.getRecommendedSettingsWith(EnumSet.of(GVRImportSettings.OPTIMIZE_GRAPH, GVRImportSettings.NO_TEXTURING));

            GVRSceneObject animRoot = new GVRSceneObject(ctx);
            ctx.getAssetLoader().loadModel(volume, animRoot, settings, false, mLoadAnimHandler);
        }
    }


    /**
     * Adds an animation to this avatar.
     *
     * @param anim animation to add
     * @see GVRAvatar#removeAnimation(GVRAnimator)
     * @see GVRAvatar#clear()
     */
    public void addAnimation(GVRAnimator anim)
    {
        mAnimations.add(anim);
    }

    /**
     * Gets an animation from this avatar.
     *
     * @param index index of animation to get
     * @see GVRAvatar#addAnimation(GVRAnimator)
     */
    public GVRAnimator getAnimation(int index)
    {
        return mAnimations.get(index);
    }

    /**
     * Removes an animation from this avatar.
     *
     * @param anim animation to remove
     * @see GVRAvatar#addAnimation(GVRAnimator)
     * @see GVRAvatar#clear()
     */
    public void removeAnimation(GVRAnimator anim)
    {
        mAnimations.remove(anim);
    }

    /**
     * Removes all the animations from this avatar.
     * <p>
     * The state of the animations are not changed when removed. For example,
     * if the animations are already running they are not be stopped.
     *
     * @see GVRAvatar#removeAnimation(GVRAnimator)
     * @see GVRAvatar#addAnimation(GVRAnimator)
     */
    public void clear()
    {
        mAnimations.clear();
    }

    /**
     * Starts the named animation.
     * @see GVRAvatar#stop(String)
     * @see GVRAnimationEngine#start(GVRAnimation)
     */
    public void start(String name)
    {
        for (GVRAnimator anim : mAnimations)
        {
            if (name.equals(anim.getName()))
            {
                mAnimQueue.add(anim);
                if (mAnimQueue.size() > 1)
                {
                    mIsRunning = true;
                    anim.start(mOnFinish);
                }
            }
        }
    }

    /**
     * Starts the animation with the given index.
     * @param animIndex 0-based index of {@link GVRAnimator} to start;
     * @see GVRAvatar#stop()
     * @see #start(String)
     */
    public GVRAnimator start(int animIndex)
    {
        if ((animIndex < 0) || (animIndex >= mAnimations.size()))
        {
            throw new IndexOutOfBoundsException("Animation index out of bounds");
        }
        GVRAnimator anim = mAnimations.get(animIndex);
        mAnimQueue.add(anim);
        if (mAnimQueue.size() == 1)
        {
            mIsRunning = true;
            anim.start(mOnFinish);
        }
        return anim;
    }


    /**
     * Stops all of the animations associated with this animator.
     * @see GVRAvatar#start(String)
     * @see GVRAnimationEngine#stop(GVRAnimation)
     */
    public void stop(String name)
    {
        for (GVRAnimator anim : mAnimations)
        {
            if (name.equals(anim.getName()))
            {
                mIsRunning = false;
                anim.stop();
            }
        }
    }

    /**
     * Stops the currently running animation, if any.
     * @see GVRAvatar#start(String)
     * @see GVRAnimationEngine#stop(GVRAnimation)
     */
    public void stop()
    {
        if (mIsRunning && (mAnimQueue.size() > 0))
        {
            mIsRunning = false;
            GVRAnimator animator = mAnimQueue.get(0);
            mAnimQueue.clear();
            animator.stop();
        }
    }

    public void centerModel(GVRSceneObject model)
    {
        GVRSceneObject.BoundingVolume bv = model.getBoundingVolume();
        float x = 0;
        float y = 0;
        float z = 0;
        float sf = 1 / bv.radius;
        model.getTransform().setScale(sf, sf, sf);
        bv = model.getBoundingVolume();
        model.getTransform().setPosition(x - bv.center.x, y - bv.center.y, z - bv.center.z - 1.5f * bv.radius);
    }

    protected IAssetEvents mLoadModelHandler = new IAssetEvents()
    {
        public void onAssetLoaded(GVRContext context, GVRSceneObject avatar, String filePath, String errors)
        {
            List<GVRComponent> components = avatar.getAllComponents(GVRSkeleton.getComponentType());

            if ((errors != null) && !errors.isEmpty())
            {
                Log.e(TAG, "Asset load errors: " + errors);
            }
            if (components.size() > 0)
            {
                mSkeleton = (GVRSkeleton) components.get(0);
                mSkeleton.poseFromBones();
                mSkeleton.updateSkinPose();
            }
            else
            {
                Log.e(TAG, "Avatar skeleton not found in asset file " + filePath);
            }
            context.getEventManager().sendEvent(GVRAvatar.this, IAvatarEvents.class, "onAvatarLoaded", avatar, filePath, errors);
        }

        public void onModelLoaded(GVRContext context, GVRSceneObject model, String filePath) { }
        public void onTextureLoaded(GVRContext context, GVRTexture texture, String filePath) { }
        public void onModelError(GVRContext context, String error, String filePath) { }
        public void onTextureError(GVRContext context, String error, String filePath) { }
    };

    public interface IAvatarEvents extends IEvents
    {
        public void onAvatarLoaded(GVRSceneObject avatarRoot, String filePath, String errors);
        public void onAnimationLoaded(GVRAnimator animator, String filePath, String errors);
        public void onAnimationStarted(GVRAnimator animator);
        public void onAnimationFinished(GVRAnimator animator, GVRAnimation animation);
    }

    protected IAssetEvents mLoadAnimHandler = new IAssetEvents()
    {
        public void onAssetLoaded(GVRContext context, GVRSceneObject animRoot, String filePath, String errors)
        {
            GVRAnimator animator = (GVRAnimator) animRoot.getComponent(GVRAnimator.getComponentType());
            if (animator == null)
            {
                if (errors == null)
                {
                    errors = "No animations found in " + filePath;
                }
                context.getEventManager().sendEvent(GVRAvatar.this, IAvatarEvents.class, "onAnimationLoaded", null, filePath, errors);
                return;
            }
            GVRSkeletonAnimation skelAnim = (GVRSkeletonAnimation) animator.getAnimation(0);
            if (skelAnim.getSkeleton() != mSkeleton)
            {
                GVRPoseMapper poseMapper = new GVRPoseMapper(mSkeleton, skelAnim.getSkeleton());

                animator.addAnimation(poseMapper);
            }
            addAnimation(animator);
            context.getEventManager().sendEvent(GVRAvatar.this, IAvatarEvents.class, "onAnimationLoaded", animator, filePath, errors);
        }

        public void onModelLoaded(GVRContext context, GVRSceneObject model, String filePath)
        {
            centerModel(model);
        }

        public void onTextureLoaded(GVRContext context, GVRTexture texture, String filePath) { }
        public void onModelError(GVRContext context, String error, String filePath) { }
        public void onTextureError(GVRContext context, String error, String filePath) { }
    };
}
