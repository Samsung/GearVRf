package org.gearvrf.jassimp2;

import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gearvrf.GVRBone;
import org.gearvrf.GVRBoneWeight;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GVRJassimpAdapter {
    private static final String TAG = GVRJassimpAdapter.class.getSimpleName();
    public static GVRNewWrapperProvider sWrapperProvider = new GVRNewWrapperProvider();
    private static GVRJassimpAdapter sInstance;
    private List<INodeFactory> mNodeFactories;

    public interface INodeFactory {
        GVRSceneObject createSceneObject(GVRContext ctx, AiNode node);
    }

    private GVRJassimpAdapter() {
        mNodeFactories = new ArrayList<INodeFactory>();
    }

    public synchronized static GVRJassimpAdapter get() {
        if (sInstance == null) {
            sInstance = new GVRJassimpAdapter();
        }
        return sInstance;
    }

    public void addNodeFactory(INodeFactory factory) {
        // Insert new factory in front of the list to support overriding
        mNodeFactories.add(0, factory);
    }

    public void removeNodeFactory(INodeFactory factory) {
        mNodeFactories.remove(factory);
    }

    public GVRMesh createMesh(GVRContext ctx, AiMesh aiMesh) {
        GVRMesh mesh = new GVRMesh(ctx);

        // Vertices
        FloatBuffer verticesBuffer = aiMesh.getPositionBuffer();
        if (verticesBuffer != null) {
            float[] verticesArray = new float[verticesBuffer.capacity()];
            verticesBuffer.get(verticesArray, 0, verticesBuffer.capacity());
            mesh.setVertices(verticesArray);
        }

        // Tangents
        FloatBuffer tangetsBuffer = aiMesh.getTangentBuffer();
        if(tangetsBuffer != null) {
            float[] tangentsArray = new float[tangetsBuffer.capacity()];
            tangetsBuffer.get(tangentsArray, 0, tangetsBuffer.capacity());
            mesh.setVec3Vector("a_tangent", tangentsArray);
        }
        
        // Bitangents
        FloatBuffer bitangentsBuffer = aiMesh.getBitangentBuffer();
        if(bitangentsBuffer != null) {
            float[] bitangentsArray = new float[bitangentsBuffer.capacity()];
            bitangentsBuffer.get(bitangentsArray, 0, bitangentsBuffer.capacity());
            mesh.setVec3Vector("a_bitangent", bitangentsArray);
        }
        
        // Normals
        FloatBuffer normalsBuffer = aiMesh.getNormalBuffer();
        if (normalsBuffer != null) {
            float[] normalsArray = new float[normalsBuffer.capacity()];
            normalsBuffer.get(normalsArray, 0, normalsBuffer.capacity());
            mesh.setNormals(normalsArray);
        }

        // TexCoords
        final int coordIdx = 0;
        FloatBuffer fbuf = aiMesh.getTexCoordBuffer(coordIdx);
        if (fbuf != null) {
            FloatBuffer coords = FloatBuffer.allocate(aiMesh.getNumVertices() * 2);
            if (aiMesh.getNumUVComponents(coordIdx) == 2) {
                FloatBuffer coordsSource = aiMesh.getTexCoordBuffer(coordIdx);
                coords.put(coordsSource);
            } else {
                for (int i = 0; i < aiMesh.getNumVertices(); ++i) {
                    float u = aiMesh.getTexCoordU(i, coordIdx);
                    float v = aiMesh.getTexCoordV(i, coordIdx);
                    coords.put(u);
                    coords.put(v);
                }
            }
            mesh.setTexCoords(coords.array());
        }

        // Triangles
        IntBuffer indexBuffer = aiMesh.getIndexBuffer();
        if (indexBuffer != null) {
            CharBuffer triangles = CharBuffer.allocate(indexBuffer.capacity());
            for (int i = 0; i < indexBuffer.capacity(); ++i) {
                triangles.put((char)indexBuffer.get());
            }
            mesh.setIndices(triangles.array());
        }

        // Bones
        if (aiMesh.hasBones()) {
            List<GVRBone> bones = new ArrayList<GVRBone>();
            for (AiBone bone : aiMesh.getBones()) {
                bones.add(createBone(ctx, bone));
            }
            mesh.setBones(bones);
        }

        return mesh;
    }

    private GVRBone createBone(GVRContext ctx, AiBone aiBone) {
        GVRBone bone = new GVRBone(ctx);

        bone.setName(aiBone.getName());
        bone.setOffsetMatrix(aiBone.getOffsetMatrix(sWrapperProvider));

        List<GVRBoneWeight> weights = new ArrayList<GVRBoneWeight>();
        for (AiBoneWeight aiBoneWeight : aiBone.getBoneWeights()) {
            weights.add(createBoneWeight(ctx, aiBoneWeight));
        }
        bone.setBoneWeights(weights);

        return bone;
    }

    private GVRBoneWeight createBoneWeight(GVRContext ctx, AiBoneWeight aiBoneWeight) {
        GVRBoneWeight boneWeight = new GVRBoneWeight(ctx);

        boneWeight.setVertexId(aiBoneWeight.getVertexId());
        boneWeight.setWeight(aiBoneWeight.getWeight());

        return boneWeight;
    }

    public GVRSceneObject createSceneObject(GVRContext ctx, AiNode node) {
        GVRSceneObject sceneObject = null;

        for (INodeFactory factory : mNodeFactories) {
            sceneObject = factory.createSceneObject(ctx, node);
            if (sceneObject != null)
                return sceneObject;
        }

        // Default
        sceneObject = new GVRSceneObject(ctx);
        sceneObject.setName(node.getName());

        return sceneObject;
    }

    public GVRKeyFrameAnimation createAnimation(AiAnimation aiAnim, GVRSceneObject target) {
        GVRKeyFrameAnimation anim = new GVRKeyFrameAnimation(aiAnim.getName(), target,
                (float)aiAnim.getDuration(), (float)aiAnim.getTicksPerSecond());

        // Convert node anims
        for (AiNodeAnim aiNodeAnim : aiAnim.getChannels()) {
            GVRAnimationChannel channel = createAnimChannel(aiNodeAnim);
            anim.addChannel(channel);
        }

        anim.prepare();

        return anim;
    }

    private GVRAnimationChannel createAnimChannel(AiNodeAnim aiNodeAnim) {
        GVRAnimationChannel node = new GVRAnimationChannel(aiNodeAnim.getNodeName(), aiNodeAnim.getNumPosKeys(),
                aiNodeAnim.getNumRotKeys(),  aiNodeAnim.getNumScaleKeys(),
                convertAnimationBehavior(aiNodeAnim.getPreState()),
                convertAnimationBehavior(aiNodeAnim.getPostState()));

        // Pos keys
        int i;
        for (i = 0; i < aiNodeAnim.getNumPosKeys(); ++i) {
            float[] pos = aiNodeAnim.getPosKeyVector(i, sWrapperProvider);
            node.setPosKeyVector(i, (float)aiNodeAnim.getPosKeyTime(i), new Vector3f(FloatBuffer.wrap(pos)));
        }

        // Rot keys
        for (i = 0; i < aiNodeAnim.getNumRotKeys(); ++i) {
            Quaternionf rot = aiNodeAnim.getRotKeyQuaternion(i, sWrapperProvider);
            node.setRotKeyQuaternion(i, (float)aiNodeAnim.getRotKeyTime(i), rot);
        }

        // Scale keys
        for (i = 0; i < aiNodeAnim.getNumScaleKeys(); ++i) {
            float[] scale = aiNodeAnim.getScaleKeyVector(i, sWrapperProvider);
            node.setScaleKeyVector(i, (float)aiNodeAnim.getScaleKeyTime(i), new Vector3f(FloatBuffer.wrap(scale)));
        }

        return node;
    }

    private GVRAnimationBehavior convertAnimationBehavior(AiAnimBehavior behavior) {
        switch (behavior) {
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
            default:
                // Unsupported setting
                Log.e(TAG, "Unsupported setting %s", setting);
                return null;
        }
    }
}
