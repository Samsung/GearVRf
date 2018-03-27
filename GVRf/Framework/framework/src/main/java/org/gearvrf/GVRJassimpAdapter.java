package org.gearvrf;

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
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;
import org.gearvrf.jassimp.AiAnimBehavior;
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
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import static java.lang.Integer.parseInt;

class GVRJassimpAdapter {
    private static final String TAG = GVRJassimpAdapter.class.getSimpleName();
    public static GVRNewWrapperProvider sWrapperProvider = new GVRNewWrapperProvider();
    private GVRAssetLoader  mLoader;
    private List<INodeFactory> mNodeFactories;
    private AiScene mScene;
    private GVRContext mContext;
    private String mFileName;
    private static final int MAX_TEX_COORDS = JassimpConfig.MAX_NUMBER_TEXCOORDS;
    private static final int MAX_VERTEX_COLORS = JassimpConfig.MAX_NUMBER_COLORSETS;


    public interface INodeFactory {
        GVRSceneObject createSceneObject(GVRContext ctx, AiNode node);
    }

    public GVRJassimpAdapter(GVRAssetLoader loader, String filename) {
        mLoader = loader;
        mFileName = filename;
        mNodeFactories = new ArrayList<INodeFactory>();
    }

    public void addNodeFactory(INodeFactory factory) {
        // Insert new factory in front of the list to support overriding
        mNodeFactories.add(0, factory);
    }

    public void removeNodeFactory(INodeFactory factory) {
        mNodeFactories.remove(factory);
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
        boolean doAnimation = !settings.contains(GVRImportSettings.NO_ANIMATION);

        // Vertices
        FloatBuffer verticesBuffer = aiMesh.getPositionBuffer();
        if (verticesBuffer != null) {
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
        for(int c = 0; c < MAX_VERTEX_COLORS; c++)
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

        if (doAnimation && aiMesh.hasBones())
        {
            vertexDescriptor += " float4 a_bone_weights int4 a_bone_indices";
        }
        GVRMesh mesh = new GVRMesh(ctx, vertexDescriptor);

        // Vertex Colors
        for(int c = 0; c < MAX_VERTEX_COLORS; c++)
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
        // Bones
        if (doAnimation && aiMesh.hasBones())
        {
            processBones(mesh, aiMesh.getBones());
        }
        return mesh;
    }

    public void processBones(GVRMesh mesh, List<AiBone> aiBones)
    {
        final int MAX_WEIGHTS = 4;
        GVRVertexBuffer vbuf = mesh.getVertexBuffer();
        int nverts = vbuf.getVertexCount();
        int n = nverts * MAX_WEIGHTS;
        float[] weights = new float[n];
        int[] indices = new int[n];
        GVRContext ctx = mesh.getGVRContext();

        // Process bones
        int boneId = -1;
        Arrays.fill(weights, 0, n - 1,  0.0f);
        Arrays.fill(indices, 0, n - 1, 0);
        ArrayList<GVRBone> bones = new ArrayList<GVRBone>();

        /*
         * Accumulate vertex weights and indices for all the bones
         * in this mesh. All vertices have four indices and four weights.
         * If a vertex has less than four infuences, the weight is 0.
         */
        for (AiBone aiBone : aiBones)
        {
            GVRBone b = createBone(ctx, aiBone);
            bones.add(b);
            boneId++;
            Log.e("BONE", aiBone.getName() + " " + boneId);

            List<AiBoneWeight> boneWeights = aiBone.getBoneWeights();
            for (AiBoneWeight weight : boneWeights)
            {
                int vertexId = weight.getVertexId() * MAX_WEIGHTS;
                int i, j = 0;
                for (i = 0; i < MAX_WEIGHTS; ++i)
                {
                    j = vertexId + i;
                    if (weights[j] == 0.0f)
                    {
                        indices[j] = boneId;
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
        mesh.setBones(bones);
    }

    private GVRBone createBone(GVRContext ctx, AiBone aiBone) {
        float[] mtx = aiBone.getOffsetMatrix(sWrapperProvider);
        GVRBone bone = new GVRBone(ctx);

        bone.setName(aiBone.getName());
        bone.setOffsetMatrix(mtx);
        return bone;
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
            node.setPosKeyVector(i, (float)aiNodeAnim.getPosKeyTime(i), pos[0], pos[1], pos[2]);
        }

        // Rot keys
        for (i = 0; i < aiNodeAnim.getNumRotKeys(); ++i) {
            Quaternionf rot = aiNodeAnim.getRotKeyQuaternion(i, sWrapperProvider);
            node.setRotKeyQuaternion(i, (float)aiNodeAnim.getRotKeyTime(i), rot);
        }

        // Scale keys
        for (i = 0; i < aiNodeAnim.getNumScaleKeys(); ++i) {
            float[] scale = aiNodeAnim.getScaleKeyVector(i, sWrapperProvider);
            node.setScaleKeyVector(i, (float)aiNodeAnim.getScaleKeyTime(i), scale[0], scale[1], scale[2]);
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

    public void processScene(GVRAssetLoader.AssetRequest request, GVRSceneObject model, AiScene scene, GVRResourceVolume volume, boolean startAnimations)
    {
        List<AiLight> aiLights = scene.getLights();
        Hashtable<String, GVRLight> lightList = new Hashtable<String, GVRLight>();
        GVRSceneObject camera;

        EnumSet<GVRImportSettings> settings = request.getImportSettings();
        mScene = scene;
        mContext = model.getGVRContext();
        camera = makeCamera();
        if (camera != null)
        {
            model.addChildObject(camera);
        }
        if (!settings.contains(GVRImportSettings.NO_LIGHTING))
        {
            importLights(aiLights, lightList);
        }
        if (scene == null)
        {
            return;
        }
        recurseAssimpNodes(request, model, scene.getSceneRoot(sWrapperProvider), lightList);
        if (!settings.contains(GVRImportSettings.NO_ANIMATION))
        {
            List<AiAnimation> animations = scene.getAnimations();
            if (animations.size() > 0)
            {
                GVRAnimator animator = new GVRAnimator(mContext, startAnimations);
                model.attachComponent(animator);
                for (AiAnimation aiAnim : scene.getAnimations())
                {
                    GVRAnimation animation = createAnimation(aiAnim, model);
                    GVRModelSceneObject modelRoot = null;
                    if (GVRModelSceneObject.class.isAssignableFrom(model.getClass()))
                    {
                        modelRoot = (GVRModelSceneObject) model;
                    }
                    if (animation != null)
                    {
                        animator.addAnimation(animation);
                        if (modelRoot != null)
                        {
                            modelRoot.getAnimations().add(animation);
                        }
                    }
                }
            }
        }
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

    private void recurseAssimpNodes(
        GVRAssetLoader.AssetRequest request,
        GVRSceneObject parentSceneObject,
        AiNode node,
        Hashtable<String,
                GVRLight> lightlist) {
        final GVRSceneObject sceneObject;
        final GVRContext context = mContext;

        if (node.getNumMeshes() == 0) {
            sceneObject = createSceneObject(mContext, node);
            parentSceneObject.addChildObject(sceneObject);
        } else if (node.getNumMeshes() == 1) {
            // add the scene object to the scene graph
            AiMesh aiMesh = mScene.getMeshes().get(node.getMeshes()[0]);
            sceneObject = createSubSceneObject(request, parentSceneObject, node, aiMesh);
        } else {
            sceneObject = createSceneObject(mContext, node);
            parentSceneObject.addChildObject(sceneObject);
            for (int i = 0; i < node.getNumMeshes(); i++) {
                AiMesh aiMesh = mScene.getMeshes().get(node.getMeshes()[i]);
                GVRSceneObject childSceneObject = createSubSceneObject(request, sceneObject, node, aiMesh);
            }
        }

        if (node.getTransform(sWrapperProvider) != null) {
            float[] matrix = node.getTransform(sWrapperProvider);
            sceneObject.getTransform().setModelMatrix(matrix);
        }
        if (!request.getImportSettings().contains(GVRImportSettings.NO_LIGHTING))
        {
            attachLights(lightlist, sceneObject);
        }
        for (AiNode child : node.getChildren()) {
            recurseAssimpNodes(request, sceneObject, child, lightlist);
        }

        context.runOnTheFrameworkThread(new Runnable() {
            public void run() {
                // Inform the loaded object after it has been attached to the scene graph
                context.getEventManager().sendEvent(
                        sceneObject,
                        ISceneObjectEvents.class,
                        "onLoaded");
            }
        });
     }

    private void attachLights(Hashtable<String, GVRLight> lightlist, GVRSceneObject sceneObject){
        String name = sceneObject.getName();
        GVRLight light =  lightlist.get(name);
        if (light != null) {
            Quaternionf q = new Quaternionf();
            q.rotationX((float) Math.PI / 2.0f);
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
     * @param node
     *            A reference to the AiNode for which we want to recurse all its
     *            children and meshes.
     *
     * @param aiMesh
     *            The assimp mesh
     **
     * @return The new {@link GVRSceneObject} with the input mesh for the node {@link node}
     *
     * @throws IOException
     *             File does not exist or cannot be read
     */
    private GVRSceneObject createSubSceneObject(
            GVRAssetLoader.AssetRequest assetRequest,
            GVRSceneObject parent,
            AiNode node,
            AiMesh aiMesh)
    {
        EnumSet<GVRImportSettings> settings = assetRequest.getImportSettings();
        GVRMesh mesh = createMesh(mContext, aiMesh, settings);
        AiMaterial material = mScene.getMaterials().get(aiMesh.getMaterialIndex());
        final GVRMaterial meshMaterial = createMaterial(material, assetRequest.getImportSettings());
        GVRSceneObject sceneObject = createSceneObject(mContext, node);
        GVRRenderData sceneObjectRenderData = new GVRRenderData(mContext);
        AiColor diffuseColor = material.getDiffuseColor(sWrapperProvider);        /* Opacity */
        float opacity = diffuseColor.getAlpha();

        sceneObjectRenderData.setMesh(mesh);
        if (!settings.contains(GVRImportSettings.NO_TEXTURING))
        {
            loadTextures(assetRequest, material, meshMaterial, aiMesh);
        }
        if (settings.contains(GVRImportSettings.NO_LIGHTING))
        {
            sceneObjectRenderData.disableLight();
            if (material.getOpacity() > 0)
            {
                opacity *= material.getOpacity();
            }
            meshMaterial.setVec3("u_color",
                                 diffuseColor.getRed(),
                                 diffuseColor.getGreen(),
                                 diffuseColor.getBlue());
            meshMaterial.setFloat("u_opacity", opacity);
        }
        else
        {
            /* Diffuse color & Opacity */
            if (material.getOpacity() > 0)
            {
                opacity *= material.getOpacity();
            }
            meshMaterial.setVec4("diffuse_color",diffuseColor.getRed(),
                    diffuseColor.getGreen(), diffuseColor.getBlue(), opacity);

            /* Specular color */
                AiColor specularColor = material.getSpecularColor(sWrapperProvider);
                meshMaterial.setSpecularColor(specularColor.getRed(),
                                              specularColor.getGreen(), specularColor.getBlue(),
                                              specularColor.getAlpha());


            /* Ambient color */
                AiColor ambientColor = material.getAmbientColor(sWrapperProvider);
                meshMaterial.setAmbientColor(ambientColor.getRed(),
                                             ambientColor.getGreen(), ambientColor.getBlue(),
                                             ambientColor.getAlpha());


            /* Emissive color */
                AiColor emissiveColor = material.getEmissiveColor(sWrapperProvider);
                meshMaterial.setVec4("emissive_color", emissiveColor.getRed(),
                                     emissiveColor.getGreen(), emissiveColor.getBlue(),
                                     emissiveColor.getAlpha());
        }

        /* Specular Exponent */
        float specularExponent = material.getShininess();
        meshMaterial.setSpecularExponent(specularExponent);

        sceneObjectRenderData.setMaterial(meshMaterial);
        sceneObject.attachRenderData(sceneObjectRenderData);

        parent.addChildObject(sceneObject);
        return sceneObject;
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
    }

    private static final Map<AiTextureMapMode, GVRTextureParameters.TextureWrapType> wrapModeMap;
    static
    {
        wrapModeMap = new HashMap<AiTextureMapMode, GVRTextureParameters.TextureWrapType>();
        wrapModeMap.put(AiTextureMapMode.WRAP, GVRTextureParameters.TextureWrapType.GL_REPEAT );
        wrapModeMap.put(AiTextureMapMode.CLAMP, GVRTextureParameters.TextureWrapType.GL_CLAMP_TO_EDGE );
        wrapModeMap.put(AiTextureMapMode.MIRROR, GVRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT );

    }

    private GVRMaterial createMaterial(AiMaterial material, EnumSet<GVRImportSettings> settings)
    {
        boolean layered = false;

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
        GVRShaderId shaderType = GVRMaterial.GVRShaderType.Phong.ID;
        if (settings.contains(GVRImportSettings.NO_LIGHTING))
        {
            shaderType = GVRMaterial.GVRShaderType.Texture.ID;
        }
        else if (layered)
        {
            shaderType = GVRMaterial.GVRShaderType.PhongLayered.ID;
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

        if (uvIndex > 0)
        {
            texCoordKey += uvIndex;
        }
        if (texIndex > 1)
        {
            assetRequest.onTextureError(mContext, "Layering only supported for two textures, ignoring " + mFileName, mFileName);
            return;
        }
        if (texIndex > 0)
        {
            textureKey += texIndex;
            shaderKey += texIndex;
            gvrmtl.setInt(textureKey + "_blendop", blendop);
        }
        GVRTextureParameters texParams = new GVRTextureParameters(mContext);
        texParams.setWrapSType(wrapModeMap.get(aimtl.getTextureMapModeU(texType, texIndex)));
        texParams.setWrapTType(wrapModeMap.get(aimtl.getTextureMapModeV(texType, texIndex)));
        GVRTexture gvrTex = new GVRTexture(mContext, texParams);
        GVRAssetLoader.TextureRequest texRequest;

        gvrTex.setTexCoord(texCoordKey, shaderKey);
        gvrmtl.setTexture(textureKey, gvrTex);
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
            if (texType == AiTextureType.UNKNOWN)
            {
                continue;
            }
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
        for(AiLight light: lights)
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
                setAttenuatipn(l, light);
            }
        }
    }

    private void setAttenuatipn(GVRLight gvrLight, AiLight assimpLight)
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
