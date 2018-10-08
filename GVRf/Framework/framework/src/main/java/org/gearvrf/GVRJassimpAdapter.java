package org.gearvrf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.lang.Math.max;

import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRPose;
import org.gearvrf.animation.GVRSkeleton;
import org.gearvrf.animation.GVRSkin;
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRNodeAnimation;
import org.gearvrf.animation.keyframe.GVRSkeletonAnimation;
import org.gearvrf.jassimp.AiAnimBehavior;
import org.gearvrf.jassimp.AiAnimMesh;
import org.gearvrf.jassimp.AiAnimation;
import org.gearvrf.jassimp.AiBone;
import org.gearvrf.jassimp.AiBoneWeight;
import org.gearvrf.jassimp.AiCamera;
import org.gearvrf.jassimp.AiColor;
import org.gearvrf.jassimp.AiLight;
import org.gearvrf.jassimp.AiLightType;
import org.gearvrf.jassimp.AiMaterial;
import org.gearvrf.jassimp.AiMesh;
import org.gearvrf.jassimp.AiNode;
import org.gearvrf.jassimp.AiNodeAnim;
import org.gearvrf.jassimp.AiPostProcessSteps;
import org.gearvrf.jassimp.AiScene;
import org.gearvrf.jassimp.AiTexture;
import org.gearvrf.jassimp.AiTextureMapMode;
import org.gearvrf.jassimp.AiTextureType;
import org.gearvrf.jassimp.GVRNewWrapperProvider;
import org.gearvrf.jassimp.Jassimp;
import org.gearvrf.jassimp.JassimpConfig;
import org.gearvrf.shaders.GVRPBRShader;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static java.lang.Integer.parseInt;

class  GVRJassimpAdapter
{
    private static final String TAG = GVRJassimpAdapter.class.getSimpleName();
    public static GVRNewWrapperProvider sWrapperProvider = new GVRNewWrapperProvider();
    private AiScene mScene;
    private GVRContext mContext;
    private String mFileName;
    private GVRSkeleton mSkeleton;

    private static final int MAX_TEX_COORDS = JassimpConfig.MAX_NUMBER_TEXCOORDS;
    private static final int MAX_VERTEX_COLORS = JassimpConfig.MAX_NUMBER_COLORSETS;

    /*
     * Maps the name of the GVRSceneObject / AiNode to the GVRBone
     * attached to the GVRSceneObject
     */
    private Hashtable<String, AiBone> mBoneMap = new Hashtable<>();

    /*
     * Maps GVRSceneObject created for each Assimp node to the Assimp
     * mesh ID (the index of the mesh in AiScene).
     */
    private HashMap<GVRSceneObject, Integer> mNodeMap = new HashMap<>();

    /**
     * Maps the Assimp mesh ID to the corresponding GVRMesh
     */
    private GVRMesh[] mMeshes;

    /**
     * Maps the Assimp material ID (index of the material in AiScene)
     *  to the corresponding GVRMaterial
     */
    private GVRMaterial[] mMaterials;


    public GVRJassimpAdapter(GVRAssetLoader loader, String filename)
    {
        mFileName = filename;
    }

    public GVRMesh createMesh(GVRContext ctx, AiMesh aiMesh, EnumSet<GVRImportSettings> settings)
    {
        String vertexDescriptor = "float3 a_position";
        float[] verticesArray = null;
        float[] tangentsArray = null;
        float[] bitangentsArray = null;
        float[] normalsArray = null;
        boolean doTexturing = !settings.contains(GVRImportSettings.NO_TEXTURING);
        boolean doLighting = !settings.contains(GVRImportSettings.NO_LIGHTING);

        // Vertices
        FloatBuffer verticesBuffer = aiMesh.getPositionBuffer();
        if (verticesBuffer != null)
        {
            verticesArray = new float[verticesBuffer.capacity()];
            verticesBuffer.get(verticesArray, 0, verticesBuffer.capacity());
        }
        // TexCoords
        if (doTexturing)
        {
            for (int texIndex = 0; texIndex < MAX_TEX_COORDS; texIndex++)
            {
                FloatBuffer fbuf = aiMesh.getTexCoordBuffer(texIndex);
                if (fbuf != null)
                {
                    vertexDescriptor += " float2 a_texcoord";
                    if (texIndex > 0)
                    {
                        vertexDescriptor += texIndex;
                    }
                }
            }
        }
        // Normals
        if (doLighting)
        {

            FloatBuffer normalsBuffer = aiMesh.getNormalBuffer();
            if (normalsBuffer != null)
            {
                vertexDescriptor += " float3 a_normal";

                normalsArray = new float[normalsBuffer.capacity()];
                normalsBuffer.get(normalsArray, 0, normalsBuffer.capacity());
            }

        }

        for (int c = 0; c < MAX_VERTEX_COLORS; c++)
        {
            FloatBuffer fbuf = aiMesh.getColorBuffer(c);
            if (fbuf != null)
            {
                String name = "a_color";

                if (c > 0)
                {
                    name += c;
                }
                vertexDescriptor += " float4 " + name;
            }
        }

        if (aiMesh.hasBones())
        {
            vertexDescriptor += " float4 a_bone_weights int4 a_bone_indices";
        }
        if (doLighting && aiMesh.hasTangentsAndBitangents())
        {

            vertexDescriptor += " float3 a_tangent float3 a_bitangent";

            FloatBuffer tangentBuffer = aiMesh.getTangentBuffer();

            tangentsArray = new float[tangentBuffer.capacity()];
            tangentBuffer.get(tangentsArray, 0, tangentBuffer.capacity());
            bitangentsArray = new float[tangentsArray.length];

            Vector3f tangent = new Vector3f();
            Vector3f normal = new Vector3f();
            Vector3f bitangent = new Vector3f();

            for(int i = 0; i < tangentsArray.length; i += 3)
            {
                tangent.set(tangentsArray[i], tangentsArray[i + 1], tangentsArray[i + 2]);
                normal.set(normalsArray[i], normalsArray[i + 1], normalsArray[i + 2]);
                normal.cross(tangent, bitangent);
                bitangentsArray[i] = bitangent.x; bitangentsArray[i+1] = bitangent.y; bitangentsArray[i + 2] = bitangent.z;
            }
        }

        GVRMesh mesh = new GVRMesh(ctx, vertexDescriptor);

        // Vertex Colors
        for (int c = 0; c < MAX_VERTEX_COLORS; c++)
        {
            FloatBuffer fbuf = aiMesh.getColorBuffer(c);
            if (fbuf != null)
            {
                FloatBuffer coords = FloatBuffer.allocate(aiMesh.getNumVertices() * 4);
                FloatBuffer source = aiMesh.getColorBuffer(c);
                String name = "a_color";

                if (c > 0)
                {
                    name += c;
                }
                coords.put(source);
                mesh.setFloatVec(name, coords);
            }
        }

        IntBuffer indices = aiMesh.getIndexBuffer();
        int len = indices.capacity();
        GVRIndexBuffer indexBuffer = new GVRIndexBuffer(ctx, 4, len);

        indexBuffer.setIntVec(indices);
        mesh.setIndexBuffer(indexBuffer);

        if (verticesArray != null)
        {
            mesh.setVertices(verticesArray);
        }
        if (normalsArray != null)
        {
            mesh.setNormals(normalsArray);
        }
        if (tangentsArray != null)
        {
            mesh.setFloatArray("a_tangent", tangentsArray);
        }
        if (bitangentsArray != null)
        {
            mesh.setFloatArray("a_bitangent", bitangentsArray);
        }
        // TexCords
        if (doTexturing)
        {
            for (int texIndex = 0; texIndex < MAX_TEX_COORDS; texIndex++)
            {
                FloatBuffer fbuf = aiMesh.getTexCoordBuffer(texIndex);
                if (fbuf != null)
                {
                    FloatBuffer coords = FloatBuffer.allocate(aiMesh.getNumVertices() * 2);
                    if (aiMesh.getNumUVComponents(texIndex) == 2)
                    {
                        FloatBuffer coordsSource = aiMesh.getTexCoordBuffer(texIndex);
                        coords.put(coordsSource);
                    }
                    else
                    {
                        for (int i = 0; i < aiMesh.getNumVertices(); ++i)
                        {
                            float u = aiMesh.getTexCoordU(i, texIndex);
                            float v = aiMesh.getTexCoordV(i, texIndex);
                            coords.put(u);
                            coords.put(v);
                        }
                    }
                    mesh.setTexCoords(coords.array(), texIndex);
                }
            }
        }
        return mesh;
    }

    public void setMeshMorphComponent(GVRMesh mesh, GVRSceneObject sceneObject, AiMesh aiMesh)
    {
        int nAnimationMeshes = aiMesh.getAnimationMeshes().size();
        if (nAnimationMeshes == 0)
            return;

        try
        {
            GVRMeshMorph morph = new GVRMeshMorph(mContext, nAnimationMeshes);
            sceneObject.attachComponent(morph);
            int blendShapeNum = 0;

            for (AiAnimMesh animMesh : aiMesh.getAnimationMeshes())
            {
                GVRVertexBuffer animBuff = new GVRVertexBuffer(mesh.getVertexBuffer(),
                                                               "float3 a_position float3 a_normal float3 a_tangent float3 a_bitangent");

                float[] vertexArray = null;
                float[] normalArray = null;
                float[] tangentArray = null;
                float[] bitangentArray = null;

                //copy target positions to anim vertex buffer
                FloatBuffer animPositionBuffer = animMesh.getPositionBuffer();
                if (animPositionBuffer != null)
                {
                    vertexArray = new float[animPositionBuffer.capacity()];
                    animPositionBuffer.get(vertexArray, 0, animPositionBuffer.capacity());
                    animBuff.setFloatArray("a_position", vertexArray);
                }

                //copy target normals to anim normal buffer
                FloatBuffer animNormalBuffer = animMesh.getNormalBuffer();
                if (animNormalBuffer != null)
                {
                    normalArray = new float[animNormalBuffer.capacity()];
                    animNormalBuffer.get(normalArray, 0, animNormalBuffer.capacity());
                    animBuff.setFloatArray("a_normal", normalArray);
                }

                //copy target tangents to anim tangent buffer
                FloatBuffer animTangentBuffer = animMesh.getTangentBuffer();
                if (animTangentBuffer != null)
                {
                    tangentArray = new float[animTangentBuffer.capacity()];
                    animTangentBuffer.get(tangentArray, 0, animTangentBuffer.capacity());
                    animBuff.setFloatArray("a_tangent", tangentArray);

                    //calculate bitangents
                    bitangentArray = new float[tangentArray.length];
                    for (int i = 0; i < tangentArray.length; i += 3)
                    {
                        Vector3f tangent =
                            new Vector3f(tangentArray[i], tangentArray[i + 1], tangentArray[i + 2]);
                        Vector3f normal =
                            new Vector3f(normalArray[i], normalArray[i + 1], normalArray[i + 2]);
                        Vector3f bitangent = new Vector3f();
                        normal.cross(tangent, bitangent);
                        bitangentArray[i] = bitangent.x;
                        bitangentArray[i + 1] = bitangent.y;
                        bitangentArray[i + 2] = bitangent.z;
                    }
                    animBuff.setFloatArray("a_bitangent", bitangentArray);
                }
                morph.setBlendShape(blendShapeNum, animBuff);
                blendShapeNum++;
            }
            morph.update();
        }
        catch (IllegalArgumentException ex)
        {
            sceneObject.detachComponent(GVRMeshMorph.getComponentType());
        }
    }

    public GVRSkin processBones(GVRMesh mesh, List<AiBone> aiBones)
    {
        final int MAX_WEIGHTS = 4;
        GVRVertexBuffer vbuf = mesh.getVertexBuffer();
        int nverts = vbuf.getVertexCount();
        int n = nverts * MAX_WEIGHTS;
        float[] weights = new float[n];
        int[] indices = new int[n];
        int[] boneMap = new int[aiBones.size()];
        int boneIndex = -1;
        GVRSkin skin = new GVRSkin(mSkeleton);

        // Process bones
        Arrays.fill(weights, 0, n - 1,  0.0f);
        Arrays.fill(indices, 0, n - 1, 0);

        /*
         * Accumulate vertex weights and indices for all the bones
         * in this mesh. All vertices have four indices and four weights.
         * If a vertex has less than four infuences, the weight is 0.
         */
        for (AiBone aiBone : aiBones)
        {
            String boneName = aiBone.getName();
            int boneId = mSkeleton.getBoneIndex(boneName);

            if (boneId < 0)
            {
                Log.e("BONE", "Bone %s not found in skeleton", boneName);
                continue;
            }
            Log.e("BONE", "%d %s -> %d", boneId, boneName, boneIndex + 1);
            boneMap[++boneIndex] = boneId;
            List<AiBoneWeight> boneWeights = aiBone.getBoneWeights();
            for (AiBoneWeight weight : boneWeights)
            {
                int vertexId = weight.getVertexId() * MAX_WEIGHTS;
                int i;
                for (i = 0; i < MAX_WEIGHTS; ++i)
                {
                    int j = vertexId + i;
                    if (weights[j] == 0.0f)
                    {
                        indices[j] = boneIndex;
                        weights[j] = weight.getWeight();
                        break;
                    }
                }
                if (i >= MAX_WEIGHTS)
                {
                    Log.w(TAG, "Vertex %d (total %d) has too many bones", vertexId, nverts);
                }
            }
        }
        skin.setBoneMap(boneMap);
        /*
         * Normalize the weights for each vertex.
         * Sum the weights and divide by the sum.
         */
        for (int v = 0; v < nverts; ++v)
        {
            float t = 0.0f;
            String is = v + " ";
            String ws = "";
            for (int i = 0; i < MAX_WEIGHTS; ++i)
            {
                int j = (v * MAX_WEIGHTS) + i;
                t += weights[j];
                //is += " " + indices[j];
                //ws += " " + weights[j];
            }
            //Log.v("BONES", is + ws);
            if (t > 0.000001f)
            {
                for (int i = 0; i < MAX_WEIGHTS; ++i)
                {
                    weights[(v * MAX_WEIGHTS) + i] /= t;
                }
            }
        }
        vbuf.setFloatArray("a_bone_weights", weights);
        vbuf.setIntArray("a_bone_indices", indices);
        return skin;
    }


    private class BoneCollector implements GVRSceneObject.SceneVisitor
    {
        /**
         * List of node names in the order they are encountered
         * traversing the parents of the nodes before their children.
         * Only named nodes below the root which are animated and have
         * associated bones are in this list.
         */
        final List<String> mBoneNames = new ArrayList<>();
        private GVRSceneObject mRoot = null;

        public BoneCollector()
        {
        }

        public List<String> getBoneNames() { return mBoneNames; }

        @Override
        public boolean visit(GVRSceneObject obj)
        {
            String nodeName = obj.getName();

            if (!"".equals(nodeName))
            {
                AiBone aiBone = mBoneMap.get(nodeName);

                if (mBoneNames.contains(nodeName))    // duplicate bone ID
                {
                    Log.e("BONE", "Multiple bones with the same name: " + nodeName);
                    return true;
                }
                if (aiBone == null)
                {
                    GVRSceneObject parent = obj.getParent();

                    if (obj.getChildrenCount() == 0)
                    {
                        return true;
                    }
                    if (parent == null)
                    {
                        return true;
                    }
                    String parName = parent.getName();

                    if ("".equals(parName))
                    {
                        return true;
                    }
                    int parBoneId = mBoneNames.indexOf(parName);

                    if (parBoneId < 0)
                    {
                        return true;
                    }
                }
                int boneId = mBoneNames.size();
                mBoneNames.add(nodeName);
                if (mRoot == null)
                {
                    mRoot = obj;
                    Log.d("BONE", "Root bone %s id = %d", nodeName, boneId);
                }
                else if (mRoot.getParent() == obj.getParent())
                {
                    mRoot = mRoot.getParent();
                    mBoneNames.add(0, mRoot.getName());
                    Log.d("BONE", "Root bone %s id = %d", nodeName, mBoneNames.indexOf(mRoot.getName()));
                }
                Log.d("BONE", "Adding bone %s id = %d", nodeName, boneId);
            }
            return true;
        }
    };

    public void createAnimation(AiAnimation aiAnim, GVRSceneObject target, GVRAnimator animator)
    {
        Map<String, GVRAnimationChannel> animMap = new HashMap<>();
        float duration =  (float) (aiAnim.getDuration() / aiAnim.getTicksPerSecond());

        for (AiNodeAnim aiNodeAnim : aiAnim.getChannels())
        {
            String nodeName = aiNodeAnim.getNodeName();
            GVRAnimationChannel channel = createAnimChannel(aiNodeAnim, (float) aiAnim.getTicksPerSecond());

            animMap.put(nodeName, channel);
        }
        if (mSkeleton != null)
        {
            GVRSkeletonAnimation anim = new GVRSkeletonAnimation(aiAnim.getName(), target, duration);

            anim.setSkeleton(mSkeleton, null);
            attachBoneAnimations(anim, animMap);
            animator.addAnimation(anim);
        }
        /*
         * Any animation channels that are not part of the skeleton
         * are added separately as node animations (which just modify
         * a single node's matrix)
         */
        for (AiNodeAnim aiNodeAnim : aiAnim.getChannels())
        {
            String nodeName = aiNodeAnim.getNodeName();
            GVRAnimationChannel channel = animMap.get(nodeName);

            if (channel == null)
            {
                continue;
            }
            GVRSceneObject obj = target.getSceneObjectByName(nodeName);
            if (obj != null)
            {
                GVRNodeAnimation nodeAnim = new GVRNodeAnimation(nodeName, obj, duration, channel);
                animator.addAnimation(nodeAnim);
                Log.d("BONE", "Adding node animation for %s", nodeName);
            }
        }
    }

    /*
     * if there was a skinned mesh the bone map acts as a lookup
     * table that maps bone names to their corresponding AiBone objects.
     * The BoneCollector constructs the skeleton from the bone names in
     * the map and the scene nodes, attempting to connect bones
     * where there are gaps to produce a complete skeleton.
     */
    private void makeSkeleton(GVRSceneObject root)
    {
        if (!mBoneMap.isEmpty())
        {
            BoneCollector nodeProcessor = new BoneCollector();

            root.forAllDescendants(nodeProcessor);
            mSkeleton = new GVRSkeleton(root, nodeProcessor.getBoneNames());
            GVRPose bindPose = new GVRPose(mSkeleton);
            Matrix4f bindPoseMtx = new Matrix4f();
            GVRSceneObject skelRoot = mSkeleton.getOwnerObject().getParent();
            Matrix4f rootMtx = skelRoot.getTransform().getModelMatrix4f();

            rootMtx.invert();
            for (int boneId = 0; boneId < mSkeleton.getNumBones(); ++boneId)
            {
                String boneName = mSkeleton.getBoneName(boneId);
                AiBone aiBone = mBoneMap.get(boneName);
                GVRSceneObject bone = mSkeleton.getBone(boneId);

                if (aiBone != null)
                {
                    float[] matrixdata = aiBone.getOffsetMatrix(sWrapperProvider);

                    bindPoseMtx.set(matrixdata);
                    bindPoseMtx.invert();
                    bindPose.setWorldMatrix(boneId, bindPoseMtx);
                }
                else
                {
                    GVRTransform t = bone.getTransform();
                    Matrix4f mtx = t.getModelMatrix4f();

                    mtx.invert();
                    rootMtx.mul(mtx, mtx);
                    bindPose.setWorldMatrix(boneId, mtx);
                    Log.e("BONE", "no bind pose matrix for bone %s", boneName);
                }
            }
            mSkeleton.setBindPose(bindPose);
        }
    }

    private void attachBoneAnimations(GVRSkeletonAnimation skelAnim, Map<String, GVRAnimationChannel> animMap)
    {
        GVRPose bindPose = mSkeleton.getBindPose();
        Matrix4f bindPoseMtx = new Matrix4f();
        Vector3f vec = new Vector3f();
        final float EPSILON = 0.00001f;
        String boneName = mSkeleton.getBoneName(0);
        GVRAnimationChannel channel = animMap.get(boneName);
        AiBone aiBone;

        bindPose.getWorldMatrix(0, bindPoseMtx);
        bindPoseMtx.getScale(vec);
        if (channel != null)
        {
            skelAnim.addChannel(boneName, channel);
            animMap.remove(boneName);
            /*
             * This is required because of a bug in the FBX importer
             * which does not scale the animations to match the bind pose
             */
            bindPoseMtx.getScale(vec);
            float delta = vec.lengthSquared();
            delta = 3.0f - delta;
            if (Math.abs(delta) > EPSILON)
            {
                fixKeys(channel, vec);
            }
        }
        for (int boneId = 1; boneId < mSkeleton.getNumBones(); ++boneId)
        {
            boneName = mSkeleton.getBoneName(boneId);
            aiBone = mBoneMap.get(boneName);
            channel = animMap.get(boneName);

            if (channel != null)
            {
                if (aiBone != null)
                {
                    skelAnim.addChannel(boneName, channel);
                    animMap.remove(boneName);
                }
                else
                {
                    Log.e("BONE", "no bind pose matrix for bone %s", boneName);
                }
            }
        }
    }

    /*
     * Some FBX files are exported as centimeters. Assimp does not correctly compute the scale keys.
     * They should include the scaling from the bind pose since the animations are NOT relative
     * to the bind pose.
     */
    private void fixKeys(GVRAnimationChannel channel, Vector3f scaleFactor)
    {
        float[] temp = new float[3];
        for (int i = 0; i < channel.getNumPosKeys(); ++i)
        {
            float time = (float) channel.getPosKeyTime(i);
            channel.getPosKeyVector(i, temp);
            temp[0] *= scaleFactor.x;
            temp[1] *= scaleFactor.y;
            temp[2] *= scaleFactor.z;
            channel.setPosKeyVector(i, time, temp);
        }
        for (int i = 0; i < channel.getNumScaleKeys(); ++i)
        {
            float time = (float) channel.getScaleKeyTime(i);
            channel.getScaleKeyVector(i, temp);
            temp[0] *= scaleFactor.x;
            temp[1] *= scaleFactor.y;
            temp[2] *= scaleFactor.z;
            channel.setScaleKeyVector(i, time, temp);
        }
    }

    private GVRAnimationChannel createAnimChannel(AiNodeAnim aiNodeAnim, float ticksPerSec)
    {
        GVRAnimationChannel channel = new GVRAnimationChannel(aiNodeAnim.getNodeName(), aiNodeAnim.getNumPosKeys(),
                aiNodeAnim.getNumRotKeys(),  aiNodeAnim.getNumScaleKeys(),
                convertAnimationBehavior(aiNodeAnim.getPreState()),
                convertAnimationBehavior(aiNodeAnim.getPostState()));
        // Pos keys
        int i;
        float t;

        if (aiNodeAnim.getNumPosKeys() > 0)
        {
            float[] curpos = aiNodeAnim.getPosKeyVector(0, sWrapperProvider);
            int nextIndex = 1;

            t = (float) aiNodeAnim.getPosKeyTime(0) / ticksPerSec;
            channel.setPosKeyVector(0, t, curpos);
            for (i = 1; i < aiNodeAnim.getNumPosKeys(); ++i)
            {
                float[] pos = aiNodeAnim.getPosKeyVector(i, sWrapperProvider);
                if (!isEqual(pos, curpos))
                {
                    t = (float) aiNodeAnim.getPosKeyTime(i) / ticksPerSec;
                    channel.setPosKeyVector(nextIndex++, t, pos);
                    curpos = pos;
                }
            }
            channel.resizePosKeys(nextIndex);
        }

        if (aiNodeAnim.getNumRotKeys() > 0)
        {
            Quaternionf currot = aiNodeAnim.getRotKeyQuaternion(0, sWrapperProvider);
            int nextIndex = 1;

            t = (float) aiNodeAnim.getRotKeyTime(0) / ticksPerSec;
            channel.setRotKeyQuaternion(0, t, currot);
            for (i = 1; i < aiNodeAnim.getNumRotKeys(); ++i)
            {
                Quaternionf rot = aiNodeAnim.getRotKeyQuaternion(i, sWrapperProvider);
                if (!isEqual(rot, currot))
                {
                    t = (float) aiNodeAnim.getRotKeyTime(i) / ticksPerSec;
                    channel.setRotKeyQuaternion(nextIndex++, t, rot);
                    currot = rot;
                }
            }
            channel.resizeRotKeys(nextIndex);
        }

        if (aiNodeAnim.getNumScaleKeys() > 0)
        {
            int nextIndex = 1;
            float[] curscale = aiNodeAnim.getScaleKeyVector(0, sWrapperProvider);

            t = (float) aiNodeAnim.getScaleKeyTime(0) / ticksPerSec;
            channel.setScaleKeyVector(0, t, curscale);
            for (i = 1; i < aiNodeAnim.getNumScaleKeys(); ++i)
            {
                float[] scale = aiNodeAnim.getScaleKeyVector(i, sWrapperProvider);

                if (!isEqual(scale, curscale))
                {
                    t = (float) aiNodeAnim.getScaleKeyTime(i) / ticksPerSec;
                    channel.setScaleKeyVector(nextIndex++, t, scale);
                }
            }
            channel.resizeScaleKeys(nextIndex);
        }
        return channel;
    }

    private boolean isEqual(float[] arr1, float[] arr2)
    {
        final float EPSILON = 0.0001f;

        for (int i = 0; i < arr1.length; ++i)
        {
            if (Math.abs(arr2[i] - arr1[i]) > EPSILON)
            {
                return false;
            }
        }
        return true;
    }

    private boolean isEqual(Quaternionf q1, Quaternionf q2)
    {
        final float EPSILON = 0.00001f;

        if (Math.abs(q1.x - q2.x) > EPSILON) return false;
        if (Math.abs(q1.y - q2.y) > EPSILON) return false;
        if (Math.abs(q1.z - q2.z) > EPSILON) return false;
        if (Math.abs(q1.w - q2.w) > EPSILON) return false;
        return true;
    }

    private GVRAnimationBehavior convertAnimationBehavior(AiAnimBehavior behavior)
    {
        switch (behavior)
        {
        case DEFAULT:
            return GVRAnimationBehavior.DEFAULT;
        case CONSTANT:
            return GVRAnimationBehavior.CONSTANT;
        case LINEAR:
            return GVRAnimationBehavior.LINEAR;
        case REPEAT:
            return GVRAnimationBehavior.REPEAT;
        default:
            // Unsupported setting
            Log.e(TAG, "Cannot convert animation behavior: %s", behavior);
            return GVRAnimationBehavior.DEFAULT;
        }
    }

    public Set<AiPostProcessSteps> toJassimpSettings(EnumSet<GVRImportSettings> settings) {
        Set<AiPostProcessSteps> output = new HashSet<AiPostProcessSteps>();

        for (GVRImportSettings setting : settings) {
            AiPostProcessSteps aiSetting = fromGVRSetting(setting);
            if (aiSetting != null) {
                output.add(aiSetting);
            }
        }

        return output;
    }

    public AiPostProcessSteps fromGVRSetting(GVRImportSettings setting) {
        switch (setting) {
            case CALCULATE_TANGENTS:
                return AiPostProcessSteps.CALC_TANGENT_SPACE;
            case JOIN_IDENTICAL_VERTICES:
                return AiPostProcessSteps.JOIN_IDENTICAL_VERTICES;
            case TRIANGULATE:
                return AiPostProcessSteps.TRIANGULATE;
            case CALCULATE_NORMALS:
                return AiPostProcessSteps.GEN_NORMALS;
            case CALCULATE_SMOOTH_NORMALS:
                return AiPostProcessSteps.GEN_SMOOTH_NORMALS;
            case LIMIT_BONE_WEIGHT:
                return AiPostProcessSteps.LIMIT_BONE_WEIGHTS;
            case IMPROVE_VERTEX_CACHE_LOCALITY:
                return AiPostProcessSteps.IMPROVE_CACHE_LOCALITY;
            case SORTBY_PRIMITIVE_TYPE:
                return AiPostProcessSteps.SORT_BY_PTYPE;
            case OPTIMIZE_MESHES:
                return AiPostProcessSteps.OPTIMIZE_MESHES;
            case OPTIMIZE_GRAPH:
                return AiPostProcessSteps.OPTIMIZE_GRAPH;
            case FLIP_UV:
                return AiPostProcessSteps.FLIP_UVS;
            case START_ANIMATIONS:
                return null;
            case NO_ANIMATION:
            case NO_LIGHTING:
            case NO_TEXTURING:
                return null;
            default:
                // Unsupported setting
                Log.e(TAG, "Unsupported setting %s", setting);
                return null;
        }
    }

    public void processScene(GVRAssetLoader.AssetRequest request, final GVRSceneObject model, AiScene scene)
    {
        Hashtable<String, GVRLight> lightList = new Hashtable<String, GVRLight>();
        EnumSet<GVRImportSettings> settings = request.getImportSettings();
        boolean doAnimation = !settings.contains(GVRImportSettings.NO_ANIMATION);
        GVRSceneObject modelParent = model.getParent();

        if (modelParent != null)
        {
            modelParent.removeChildObject(model);
        }
        mScene = scene;
        mContext = model.getGVRContext();
        if (scene == null)
        {
            return;
        }
        GVRSceneObject camera = makeCamera();
        if (camera != null)
        {
            model.addChildObject(camera);
        }
        if (!settings.contains(GVRImportSettings.NO_LIGHTING))
        {
            importLights(scene.getLights(), lightList);
        }
        mMeshes = new GVRMesh[scene.getNumMeshes()];
        mMaterials = new GVRMaterial[scene.getNumMaterials()];

        traverseGraph(model, scene.getSceneRoot(sWrapperProvider), lightList);
        makeSkeleton(model);
        if (doAnimation)
        {
            processAnimations(model, scene, settings.contains(GVRImportSettings.START_ANIMATIONS));
        }
        for (Map.Entry<GVRSceneObject, Integer> entry : mNodeMap.entrySet())
        {
            GVRSceneObject obj = entry.getKey();
            int meshId = entry.getValue();

            if (meshId >= 0)
            {
                processMesh(request, obj, meshId);
            }
        }
        if (modelParent != null)
        {
            modelParent.addChildObject(model);
        }
    }

    private GVRAnimator processAnimations(GVRSceneObject model, AiScene scene, boolean startAnimations)
    {
        List<AiAnimation> animations = scene.getAnimations();
        if (animations.size() > 0)
        {
            GVRAnimator animator = new GVRAnimator(mContext, startAnimations);
            model.attachComponent(animator);
            for (AiAnimation aiAnim : scene.getAnimations())
            {
                createAnimation(aiAnim, model, animator);
            }
            return animator;
        }
        return null;
    }

    private GVRSceneObject makeCamera()
    {
        List<AiCamera> cameras = mScene.getCameras();
        if (cameras.size() == 0)
        {
            return null;
        }
        GVRSceneObject mainCamera = new GVRSceneObject(mContext);
        GVRCameraRig cameraRig = GVRCameraRig.makeInstance(mContext);
        AiCamera aiCam = cameras.get(0);
        float[] up = (float[]) aiCam.getUp(Jassimp.BUILTIN);
        float[] fwd = (float[]) aiCam.getLookAt(Jassimp.BUILTIN);
        float[] pos = (float[]) aiCam.getPosition(Jassimp.BUILTIN);
        Matrix4f mtx = new Matrix4f();

        mtx.setLookAt(pos[0], pos[1], pos[2],
                pos[0] + fwd[0], pos[1] + fwd[1], pos[2] + fwd[2],
                up[0], up[1], up[2]);
        mainCamera.setName("MainCamera");
        mainCamera.getTransform().setModelMatrix(mtx);
        cameraRig.setNearClippingDistance(aiCam.getClipPlaneNear());
        cameraRig.setFarClippingDistance(aiCam.getClipPlaneFar());
        mainCamera.attachComponent(cameraRig);
        return mainCamera;
    }

    private void traverseGraph(GVRSceneObject parent, AiNode node, Hashtable<String, GVRLight> lightlist)
    {
        GVRSceneObject sceneObject = new GVRSceneObject(mContext);
        final int[] nodeMeshes = node.getMeshes();
        String nodeName = node.getName();
        AiNode aiChild = null;

        mNodeMap.put(sceneObject, -1);
        sceneObject.setName(nodeName);
        attachLights(lightlist, sceneObject);
        parent.addChildObject(sceneObject);
        if (node.getTransform(sWrapperProvider) != null)
        {
            float[] matrix = node.getTransform(sWrapperProvider);
            sceneObject.getTransform().setModelMatrix(matrix);
        }

        if (node.getNumMeshes() == 1)
        {
            Integer meshId = nodeMeshes[0];
            if ("".equals(nodeName))
            {
                if ((mNodeMap.get(parent) == null) ||
                    ((aiChild = handleNoName(node, sceneObject)) == null))
                {
                    nodeName = "mesh";
                    sceneObject.setName(nodeName + "-" + meshId);
                }
                else
                {
                    node = aiChild;
                }
            }
            mNodeMap.put(sceneObject, meshId);
            findBones(mScene.getMeshes().get(meshId));
        }
        else if (node.getNumMeshes() > 1)
        {
            for (Integer i = 0; i < node.getNumMeshes(); i++)
            {
                int meshId = nodeMeshes[i];
                GVRSceneObject child = new GVRSceneObject(mContext);
                child.setName(nodeName + "-" + meshId);
                sceneObject.addChildObject(child);
                mNodeMap.put(child, meshId);
                findBones(mScene.getMeshes().get(meshId));
            }
        }
        else if ("".equals(nodeName) &&
                ((aiChild = handleNoName(node, sceneObject)) != null))
        {
            node = aiChild;
        }
        for (AiNode child : node.getChildren())
        {
            traverseGraph(sceneObject, child, lightlist);
        }
    }

    private AiNode handleNoName(AiNode ainode, GVRSceneObject gvrnode)
    {
        if (ainode.getNumChildren() > 1)
        {
            return null;
        }
        AiNode aichild = ainode.getChildren().get(0);
        String childName = aichild.getName();

        if ("".equals(childName))
        {
            return null;
        }
        if (aichild.getNumMeshes() > 0)
        {
            return null;
        }
        gvrnode.setName(childName);
        float[] matrix = aichild.getTransform(sWrapperProvider);
        Matrix4f childMtx = new Matrix4f();
        Matrix4f parMtx = gvrnode.getTransform().getLocalModelMatrix4f();
        childMtx.set(matrix);
        parMtx.mul(childMtx);
        gvrnode.getTransform().setModelMatrix(parMtx);
        return aichild;
    }

    private void findBones(AiMesh aiMesh)
    {
        for (AiBone aiBone : aiMesh.getBones())
        {
            String boneName = aiBone.getName();

            if (mBoneMap.get(boneName) == null)
            {
                mBoneMap.put(aiBone.getName(), aiBone);
                Log.e("BONE", "Adding bone %s", boneName);
            }
        }
    }

    private void attachLights(Hashtable<String, GVRLight> lightlist, GVRSceneObject sceneObject)
    {
        String name = sceneObject.getName();
        if ("".equals(name))
        {
            return;
        }
        GVRLight light =  lightlist.get(name);
        if (light != null)
        {
            Quaternionf q = new Quaternionf();
            q.rotationX((float) -Math.PI / 2.0f);
            q.normalize();
            light.setDefaultOrientation(q);
            sceneObject.attachLight(light);
        }
    }

    /**
     * Helper method to create a new {@link GVRSceneObject} with a given mesh
     *
     * @param assetRequest
     *            GVRAssetRequest containing the original request to load the model
     *
     * @param sceneObject
     *            The GVRSceneObject to process
     *
     * @param meshId
     *            The index of the assimp mesh in the AiScene mesh list
     */
    private void processMesh(
            GVRAssetLoader.AssetRequest assetRequest,
            GVRSceneObject sceneObject,
            int meshId)
    {
        EnumSet<GVRImportSettings> settings = assetRequest.getImportSettings();
        AiMesh aiMesh = mScene.getMeshes().get(meshId);
        GVRMesh mesh = mMeshes[meshId];
        GVRMaterial gvrMaterial = mMaterials[aiMesh.getMaterialIndex()];

        if (mesh == null)
        {
            mesh = createMesh(mContext, aiMesh, settings);
            mMeshes[meshId] = mesh;
            if (aiMesh.hasBones() && (mSkeleton != null))
            {
                GVRSkin skin = processBones(mesh, aiMesh.getBones());
                if (skin != null)
                {
                    sceneObject.attachComponent(skin);
                }
            }
        }
        else
        {
            Log.v("BONE", "instancing mesh %s", sceneObject.getName());
        }
        if (gvrMaterial == null)
        {
            AiMaterial material = mScene.getMaterials().get(aiMesh.getMaterialIndex());
            gvrMaterial = processMaterial(assetRequest, material, aiMesh);
            mMaterials[aiMesh.getMaterialIndex()] = gvrMaterial;
        }
        GVRRenderData renderData = new GVRRenderData(mContext, gvrMaterial);

        renderData.setMesh(mesh);
        if (settings.contains(GVRImportSettings.NO_LIGHTING))
        {
            renderData.disableLight();
        }
        sceneObject.attachRenderData(renderData);
        setMeshMorphComponent(mesh, sceneObject, aiMesh);
    }

    private static final Map<AiTextureType, String> textureMap;
    static
    {
        textureMap = new HashMap<AiTextureType, String>();
        textureMap.put(AiTextureType.DIFFUSE,"diffuse");
        textureMap.put(AiTextureType.SPECULAR,"specular");
        textureMap.put(AiTextureType.AMBIENT,"ambient");
        textureMap.put(AiTextureType.EMISSIVE,"emissive");
        textureMap.put(AiTextureType.HEIGHT,"height");
        textureMap.put(AiTextureType.NORMALS,"normal");
        textureMap.put(AiTextureType.SHININESS,"shininess");
        textureMap.put(AiTextureType.OPACITY,"opacity");
        textureMap.put(AiTextureType.DISPLACEMENT,"displacement");
        textureMap.put(AiTextureType.LIGHTMAP,"lightmap");
        textureMap.put(AiTextureType.REFLECTION,"reflection");
        textureMap.put(AiTextureType.UNKNOWN, "metallicRoughness");
    }

    private static final Map<AiTextureMapMode, GVRTextureParameters.TextureWrapType> wrapModeMap;
    static
    {
        wrapModeMap = new HashMap<AiTextureMapMode, GVRTextureParameters.TextureWrapType>();
        wrapModeMap.put(AiTextureMapMode.WRAP, GVRTextureParameters.TextureWrapType.GL_REPEAT );
        wrapModeMap.put(AiTextureMapMode.CLAMP, GVRTextureParameters.TextureWrapType.GL_CLAMP_TO_EDGE );
        wrapModeMap.put(AiTextureMapMode.MIRROR, GVRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT );
        wrapModeMap.put(AiTextureMapMode.GL_REPEAT, GVRTextureParameters.TextureWrapType.GL_REPEAT );
        wrapModeMap.put(AiTextureMapMode.GL_CLAMP, GVRTextureParameters.TextureWrapType.GL_CLAMP_TO_EDGE );
        wrapModeMap.put(AiTextureMapMode.GL_MIRRORED_REPEAT, GVRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT );
    }

    private static final Map<Integer, GVRTextureParameters.TextureFilterType> filterMap;
    static
    {
        filterMap = new HashMap<Integer, GVRTextureParameters.TextureFilterType>();
        filterMap.put(GLES20.GL_LINEAR, GVRTextureParameters.TextureFilterType.GL_LINEAR);
        filterMap.put(GLES20.GL_NEAREST, GVRTextureParameters.TextureFilterType.GL_NEAREST);
        filterMap.put(GLES20.GL_NEAREST_MIPMAP_NEAREST, GVRTextureParameters.TextureFilterType.GL_NEAREST_MIPMAP_NEAREST);
        filterMap.put(GLES20.GL_NEAREST_MIPMAP_LINEAR, GVRTextureParameters.TextureFilterType.GL_NEAREST_MIPMAP_LINEAR);
        filterMap.put(GLES20.GL_LINEAR_MIPMAP_NEAREST, GVRTextureParameters.TextureFilterType.GL_LINEAR_MIPMAP_NEAREST);
        filterMap.put(GLES20.GL_LINEAR_MIPMAP_LINEAR, GVRTextureParameters.TextureFilterType.GL_LINEAR_MIPMAP_LINEAR);
    }

    private GVRMaterial processMaterial(
            GVRAssetLoader.AssetRequest assetRequest,
            AiMaterial aiMaterial,
            AiMesh aiMesh)
    {
        EnumSet<GVRImportSettings> settings = assetRequest.getImportSettings();
        GVRMaterial gvrMaterial = createMaterial(aiMaterial, settings);
        AiColor diffuseColor = aiMaterial.getDiffuseColor(sWrapperProvider);
        float opacity = diffuseColor.getAlpha();

        if (!settings.contains(GVRImportSettings.NO_TEXTURING))
        {
            loadTextures(assetRequest, aiMaterial, gvrMaterial, aiMesh);
        }
        if (settings.contains(GVRImportSettings.NO_LIGHTING))
        {
            if (aiMaterial.getOpacity() > 0)
            {
                opacity *= aiMaterial.getOpacity();
            }
            gvrMaterial.setVec3("u_color",
                    diffuseColor.getRed(),
                    diffuseColor.getGreen(),
                    diffuseColor.getBlue());
            gvrMaterial.setFloat("u_opacity", opacity);
        }
        else
        {
            /* Diffuse color & Opacity */
            if (aiMaterial.getOpacity() > 0)
            {
                opacity *= aiMaterial.getOpacity();
            }
            gvrMaterial.setVec4("diffuse_color",diffuseColor.getRed(),
                    diffuseColor.getGreen(), diffuseColor.getBlue(), opacity);

            /* Specular color */
            AiColor specularColor = aiMaterial.getSpecularColor(sWrapperProvider);
            gvrMaterial.setSpecularColor(specularColor.getRed(),
                    specularColor.getGreen(), specularColor.getBlue(),
                    specularColor.getAlpha());


            /* Ambient color */
            AiColor ambientColor = aiMaterial.getAmbientColor(sWrapperProvider);
            if (gvrMaterial.hasUniform("ambient_color"))
            {
                gvrMaterial.setAmbientColor(ambientColor.getRed(),
                        ambientColor.getGreen(), ambientColor.getBlue(),
                        ambientColor.getAlpha());
            }


            /* Emissive color */
            AiColor emissiveColor = aiMaterial.getEmissiveColor(sWrapperProvider);
            gvrMaterial.setVec4("emissive_color", emissiveColor.getRed(),
                    emissiveColor.getGreen(), emissiveColor.getBlue(),
                    emissiveColor.getAlpha());
        }

        /* Specular Exponent */
        float specularExponent = aiMaterial.getShininess();
        gvrMaterial.setSpecularExponent(specularExponent);
        return gvrMaterial;
    }

    private GVRMaterial createMaterial(AiMaterial material, EnumSet<GVRImportSettings> settings)
    {
        boolean layered = false;
        GVRShaderId shaderType;

        for (final AiTextureType texType : AiTextureType.values())
        {
            if (texType != AiTextureType.UNKNOWN)
            {
                if (material.getNumTextures(texType) > 1)
                {
                    layered = true;
                }
            }
        }
        if (!settings.contains(GVRImportSettings.NO_LIGHTING))
        {
            try
            {
                boolean glosspresent = material.getSpecularGlossinessUsage();
                shaderType = new GVRShaderId(GVRPBRShader.class);
                GVRMaterial m = new GVRMaterial(mContext, shaderType);

                //use specular glossiness workflow, if present
                if (glosspresent)
                {
                    AiColor diffuseFactor = material.getDiffuseColor(sWrapperProvider);
                    AiColor specularFactor = material.getSpecularColor(sWrapperProvider);
                    //gltf2importer.cpp in the assimp lib defines shininess as glossiness_factor * 1000.0f
                    float glossinessFactor = material.getShininess() / 1000.0f;

                    m.setDiffuseColor(diffuseFactor.getRed(), diffuseFactor.getGreen(), diffuseFactor.getBlue(), diffuseFactor.getAlpha());
                    m.setSpecularColor(specularFactor.getRed(), specularFactor.getGreen(), specularFactor.getBlue(), specularFactor.getAlpha());
                    m.setFloat("glossinessFactor", glossinessFactor);
                }
                else
                {
                    float metallic = material.getMetallic();
                    float roughness = material.getRoughness();
                    AiColor baseColorFactor = material.getDiffuseColor(sWrapperProvider);

                    m.setFloat("roughness", roughness);
                    m.setFloat("metallic", metallic);
                    m.setDiffuseColor(baseColorFactor.getRed(), baseColorFactor.getGreen(), baseColorFactor.getBlue(), baseColorFactor.getAlpha());
                }

                Bitmap bitmap = BitmapFactory.decodeResource(
                        mContext.getContext().getResources(), R.drawable.brdflookup);
                GVRTexture brdfLUTtex = new GVRTexture(mContext);
                brdfLUTtex.setImage(new GVRBitmapImage(mContext, bitmap));
                m.setTexture("brdfLUTTexture", brdfLUTtex);
                return m;
            }
            catch (IllegalArgumentException e)
            {
                shaderType = GVRMaterial.GVRShaderType.Phong.ID;
            }
            if (layered)
            {
                shaderType = GVRMaterial.GVRShaderType.PhongLayered.ID;
            }
        }
        else
        {
            shaderType = GVRMaterial.GVRShaderType.Texture.ID;
        }
        return new GVRMaterial(mContext, shaderType);
    }

    private void loadTexture(GVRAssetLoader.AssetRequest assetRequest,
                             final AiMaterial aimtl, final GVRMaterial gvrmtl,
                             final AiTextureType texType, int texIndex,
                             int uvIndex)
    {
        int blendop = aimtl.getTextureOp(texType, texIndex).ordinal();
        String typeName = textureMap.get(texType);
        String textureKey = typeName + "Texture";
        String texCoordKey = "a_texcoord";
        String shaderKey = typeName + "_coord";
        final String texFileName = aimtl.getTextureFile(texType, texIndex);
        final boolean usingPBR = (gvrmtl.getShaderType() == mContext.getShaderManager().getShaderType(GVRPBRShader.class));

        if (uvIndex > 0)
        {
            texCoordKey += uvIndex;
        }
        if (texIndex > 1)
        {
            assetRequest.onModelError(mContext, "Layering only supported for two textures, ignoring " + texFileName, mFileName);
            return;
        }
        if (texIndex > 0)
        {
            if (usingPBR)
            {
                return;
            }
            textureKey += texIndex;
            shaderKey += texIndex;
            gvrmtl.setInt(textureKey + "_blendop", blendop);
        }
        GVRTextureParameters texParams = new GVRTextureParameters(mContext);
        texParams.setWrapSType(wrapModeMap.get(aimtl.getTextureMapModeU(texType, texIndex)));
        texParams.setWrapTType(wrapModeMap.get(aimtl.getTextureMapModeV(texType, texIndex)));
        texParams.setMinFilterType(filterMap.get(aimtl.getTextureMinFilter(texType, texIndex)));
        texParams.setMagFilterType(filterMap.get(aimtl.getTextureMagFilter(texType, texIndex)));

        GVRTexture gvrTex = new GVRTexture(mContext, texParams);
        GVRAssetLoader.TextureRequest texRequest;

        gvrTex.setTexCoord(texCoordKey, shaderKey);
        gvrmtl.setTexture(textureKey, gvrTex);
        if (!usingPBR && typeName.equals("lightmap"))
        {
            gvrmtl.setVec2("u_lightmap_scale", 1, 1);
            gvrmtl.setVec2("u_lightmap_offset", 0, 0);
        }

        if (texFileName.startsWith("*"))
        {
            AiTexture tex = null;
            try
            {
                int embeddedIndex = parseInt(texFileName.substring(1));
                tex = mScene.getTextures().get(embeddedIndex);
                texRequest = new GVRAssetLoader.TextureRequest(assetRequest, gvrTex, mFileName + texFileName);
                assetRequest.loadEmbeddedTexture(texRequest, tex);
            }
            catch (NumberFormatException | IndexOutOfBoundsException ex)
            {
                assetRequest.onModelError(mContext, ex.getMessage(), mFileName);
            }
            catch (IOException ex2)
            {
                assetRequest.onTextureError(mContext, ex2.getMessage(), mFileName);
            }
        }
        else
        {
            texRequest = new GVRAssetLoader.TextureRequest(assetRequest, gvrTex, texFileName);
            assetRequest.loadTexture(texRequest);
        }
    }

    private void loadTextures(GVRAssetLoader.AssetRequest assetRequest, AiMaterial aimtl, final GVRMaterial gvrmtl, final AiMesh aimesh)
    {
        for (final AiTextureType texType : AiTextureType.values())
        {
            for (int i = 0; i < aimtl.getNumTextures(texType); ++i)
            {
                final String texFileName = aimtl.getTextureFile(texType, i);

                if (!"".equals(texFileName))
                {
                    int uvIndex = aimtl.getTextureUVIndex(texType, i);
                    if (!aimesh.hasTexCoords(uvIndex))
                    {
                        uvIndex = 0;
                    }
                    loadTexture(assetRequest, aimtl, gvrmtl, texType, i, uvIndex);
                }
            }
        }
    }

    private void importLights(List<AiLight> lights, Hashtable<String, GVRLight> lightlist)
    {
        for (AiLight light: lights)
        {
            GVRLight l;
            AiLightType type = light.getType();
            String name = light.getName();

            if (type == AiLightType.DIRECTIONAL)
            {
                l = new GVRDirectLight(mContext);
            }
            else if (type == AiLightType.POINT)
            {
                l = new GVRPointLight(mContext);
            }
            else if (type == AiLightType.SPOT)
            {
                float outerAngleRadians = light.getAngleOuterCone();
                float innerAngleRadians = light.getAngleInnerCone();
                GVRSpotLight gvrLight = new GVRSpotLight(mContext);

                if (innerAngleRadians == 0.0f)
                {
                    innerAngleRadians = outerAngleRadians / 1.5f;
                }
                gvrLight.setInnerConeAngle((float) Math.toDegrees(innerAngleRadians));
                gvrLight.setOuterConeAngle((float) Math.toDegrees(outerAngleRadians));
                l = gvrLight;
            }
            else
            {
                continue;
            }
            lightlist.put(name, l);
            org.gearvrf.jassimp.AiColor ambientCol = light.getColorAmbient(sWrapperProvider);
            org.gearvrf.jassimp.AiColor diffuseCol = light.getColorDiffuse(sWrapperProvider);
            org.gearvrf.jassimp.AiColor specular = light.getColorSpecular(sWrapperProvider);
            float[] c = new float[3];
            getColor(ambientCol, c);
            l.setVec4("ambient_intensity", c[0], c[1], c[2], 1.0f);
            getColor(diffuseCol, c);
            l.setVec4("diffuse_intensity", c[0], c[1], c[2], 1.0f);
            getColor(specular, c);
            l.setVec4("specular_intensity", c[0], c[1], c[2], 1.0f);
            if ((l instanceof GVRPointLight) || (l instanceof GVRSpotLight))
            {
                setAttenuation(l, light);
            }
        }
    }

    private void setAttenuation(GVRLight gvrLight, AiLight assimpLight)
    {
        float aconstant = assimpLight.getAttenuationConstant();
        float alinear = assimpLight.getAttenuationLinear();
        float aquad = assimpLight.getAttenuationQuadratic();

        if (Double.isInfinite(alinear))
        {
            alinear = 1.0f;
        }
        if (Double.isInfinite(aquad))
        {
            aquad = 1.0f;
        }
        if ((aconstant + aquad + alinear) == 0.0f)
        {
            aconstant = 1.0f;
        }
        gvrLight.setFloat("attenuation_constant", aconstant);
        gvrLight.setFloat("attenuation_linear", alinear);
        gvrLight.setFloat("attenuation_quadratic", aquad);
    }

    private void getColor(AiColor c, float[] color)
    {
        color[0] = c.getRed();
        color[1] = c.getGreen();
        color[2] = c.getBlue();
        float scale = max(max(color[0], color[1]), color[2]);
        if (scale > 1)
        {
            color[0] /= scale;
            color[1] /= scale;
            color[2] /= scale;
        }
    }
}
