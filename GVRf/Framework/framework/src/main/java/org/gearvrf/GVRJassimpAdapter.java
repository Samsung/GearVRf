package org.gearvrf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;
import org.gearvrf.jassimp2.AiAnimBehavior;
import org.gearvrf.jassimp2.AiAnimation;
import org.gearvrf.jassimp2.AiBone;
import org.gearvrf.jassimp2.AiBoneWeight;
import org.gearvrf.jassimp2.AiCamera;
import org.gearvrf.jassimp2.AiColor;
import org.gearvrf.jassimp2.AiLight;
import org.gearvrf.jassimp2.AiLightType;
import org.gearvrf.jassimp2.AiMaterial;
import org.gearvrf.jassimp2.AiMesh;
import org.gearvrf.jassimp2.AiNode;
import org.gearvrf.jassimp2.AiNodeAnim;
import org.gearvrf.jassimp2.AiPostProcessSteps;
import org.gearvrf.jassimp2.AiScene;
import org.gearvrf.jassimp2.AiTexture;
import org.gearvrf.jassimp2.AiTextureMapMode;
import org.gearvrf.jassimp2.AiTextureType;
import org.gearvrf.jassimp2.GVRNewWrapperProvider;
import org.gearvrf.jassimp2.Jassimp;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static java.lang.Integer.parseInt;

class GVRJassimpAdapter {
    private static final String TAG = GVRJassimpAdapter.class.getSimpleName();
    public static GVRNewWrapperProvider sWrapperProvider = new GVRNewWrapperProvider();
    private GVRAssetLoader  mLoader;
    private List<INodeFactory> mNodeFactories;
    private AiScene mScene;
    private GVRContext mContext;
    private String mFileName;
    private static final int MAX_TEX_COORDS = 8;

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

        // TexCords
        for(int texIndex=0; texIndex< MAX_TEX_COORDS; texIndex++) {
            FloatBuffer fbuf = aiMesh.getTexCoordBuffer(texIndex);
            if (fbuf != null) {
                FloatBuffer coords = FloatBuffer.allocate(aiMesh.getNumVertices() * 2);
                if (aiMesh.getNumUVComponents(texIndex) == 2) {
                    FloatBuffer coordsSource = aiMesh.getTexCoordBuffer(texIndex);
                    coords.put(coordsSource);
                } else {
                    for (int i = 0; i < aiMesh.getNumVertices(); ++i) {
                        float u = aiMesh.getTexCoordU(i, texIndex);
                        float v = aiMesh.getTexCoordV(i, texIndex);
                        coords.put(u);
                        coords.put(v);
                    }
                }
                mesh.setTexCoords(coords.array(), texIndex);
            }
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

    public void processScene(GVRAssetLoader.AssetRequest request, GVRSceneObject model, AiScene scene, GVRResourceVolume volume) throws IOException
    {
        List<AiLight> aiLights = scene.getLights();
        Hashtable<String, GVRLightBase> lightList = new Hashtable<String, GVRLightBase>();
        GVRSceneObject camera;

        mScene = scene;
        mContext = model.getGVRContext();
        camera = makeCamera();
        if (camera != null)
        {
            model.addChildObject(camera);
        }
        importLights(aiLights, lightList);
        if (scene != null)
        {
            recurseAssimpNodes(request, model, scene.getSceneRoot(sWrapperProvider), lightList);
            List<AiAnimation> animations = scene.getAnimations();
            if (animations.size() > 0)
            {
                GVRAnimator animator = new GVRAnimator(mContext);
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
        GVRLightBase> lightlist) throws IOException {
        final GVRSceneObject sceneObject;
        final GVRContext context = mContext;

        if (node.getNumMeshes() == 0) {
            sceneObject = createSceneObject(mContext, node);
            parentSceneObject.addChildObject(sceneObject);
        } else if (node.getNumMeshes() == 1) {
            // add the scene object to the scene graph
            sceneObject = createSubSceneObject(request, parentSceneObject, node, 0);
        } else {
            sceneObject = createSceneObject(mContext, node);
            parentSceneObject.addChildObject(sceneObject);
            for (int i = 0; i < node.getNumMeshes(); i++) {
                GVRSceneObject childSceneObject = createSubSceneObject(request, sceneObject, node, i);
            }
        }

        if (node.getTransform(sWrapperProvider) != null) {
            float[] matrix = node.getTransform(sWrapperProvider);
            sceneObject.getTransform().setModelMatrix(matrix);
        }
        attachLights(lightlist, sceneObject);
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

    private void attachLights(Hashtable<String, GVRLightBase> lightlist, GVRSceneObject sceneObject){
        String name = sceneObject.getName();
        GVRLightBase light =  lightlist.get(name);
        if (light != null) {
            Quaternionf q = new Quaternionf();
            q.rotationX((float) Math.PI / 2.0f);
            light.setDefaultOrientation(q);
            sceneObject.attachLight(light);
        }

    }

    /**
     * Helper method to create a new {@link GVRSceneObject} with the mesh at the
     * index {@link index} of the node mesh array with a color or texture
     * material.
     *
     * @param assetRelativeFilename
     *            A filename, relative to the {@code assets} directory. The file
     *            can be in a sub-directory of the {@code assets} directory:
     *            {@code "foo/bar.png"} will open the file
     *            {@code assets/foo/bar.png}
     *
     * @param node
     *            A reference to the AiNode for which we want to recurse all its
     *            children and meshes.
     *
     * @param index
     *            The index of the mesh in the array of meshes for that node.
     *
     * @param wrapperProvider
     *            AiWrapperProvider for unwrapping Jassimp properties.
     *
     * @return The new {@link GVRSceneObject} with the mesh at the index
     *         {@link index} for the node {@link node}
     *
     * @throws IOException
     *             File does not exist or cannot be read
     */
    private GVRSceneObject createSubSceneObject(
            GVRAssetLoader.AssetRequest assetRequest,
            GVRSceneObject parent,
            AiNode node,
            int index)
            throws IOException {
        AiMesh aiMesh = mScene.getMeshes().get(node.getMeshes()[index]);
        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                createMesh(mContext, aiMesh));
        AiMaterial material = mScene.getMaterials().get(aiMesh.getMaterialIndex());
        final GVRMaterial meshMaterial = new GVRMaterial(mContext, GVRMaterial.GVRShaderType.BeingGenerated.ID);

        /* Diffuse color & Opacity */
        AiColor diffuseColor = material.getDiffuseColor(sWrapperProvider);        /* Opacity */
        float opacity = diffuseColor.getAlpha();
        if (material.getOpacity() > 0) {
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


        /* Specular Exponent */
        float specularExponent = material.getShininess();
        meshMaterial.setSpecularExponent(specularExponent);

        /* Diffuse Texture */
        loadTextures(assetRequest, material, meshMaterial);


        GVRSceneObject sceneObject = createSceneObject(mContext, node);
        GVRRenderData sceneObjectRenderData = new GVRRenderData(mContext);
        sceneObjectRenderData.setMesh(futureMesh);

        sceneObjectRenderData.setMaterial(meshMaterial);
        sceneObjectRenderData.setShaderTemplate(GVRPhongShader.class);
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
        wrapModeMap.put(AiTextureMapMode.WRAP,GVRTextureParameters.TextureWrapType.GL_REPEAT );
        wrapModeMap.put(AiTextureMapMode.CLAMP,GVRTextureParameters.TextureWrapType.GL_CLAMP_TO_EDGE );
        wrapModeMap.put(AiTextureMapMode.MIRROR, GVRTextureParameters.TextureWrapType.GL_MIRRORED_REPEAT );

    }

    private void loadTextures(GVRAssetLoader.AssetRequest assetRequest, AiMaterial material, final GVRMaterial meshMaterial) throws IOException {
        for (final AiTextureType texType : AiTextureType.values())
        {
            if (texType == AiTextureType.UNKNOWN) {
                continue;
            }
            for (int i = 0; i < material.getNumTextures(texType); ++i) {
                final String texFileName = material.getTextureFile(texType, i);

                if ("".equals(texFileName)) {
                    continue;
                }
                int uvIndex = material.getTextureUVIndex(texType, i);
                int blendop = material.getTextureOp(texType, i).ordinal();
                Log.e("RC", "tex index " + uvIndex);
                String typeName = textureMap.get(texType);
                String textureKey = typeName + "Texture";
                String texCoordKey = "a_texcoord";
                String shaderKey = typeName + "_coord";
                if (uvIndex > 0) {
                    texCoordKey += uvIndex;
                }
                if (i > 0) {
                    textureKey += i;
                    shaderKey += i;
                    meshMaterial.setFloat(textureKey + "_blendop", (float) blendop);
                }
                meshMaterial.setTexCoord(textureKey, texCoordKey, shaderKey);
                GVRTextureParameters texParams = new GVRTextureParameters(mContext);
                texParams.setWrapSType(wrapModeMap.get(material.getTextureMapModeU(texType,i)));
                texParams.setWrapTType(wrapModeMap.get(material.getTextureMapModeV(texType,i)));
                if (texFileName.startsWith("*"))
                {
                    try
                    {
                        GVRAndroidResource texresource = new GVRAndroidResource(mContext, mFileName + texFileName);
                        GVRTexture texture = mLoader.findTexture(texresource);
                        if (texture == null)
                        {
                            texture = loadEmbeddedTexture(texFileName, texParams);
                            mLoader.cacheTexture(texresource, texture);
                            mContext.getEventManager().sendEvent(mContext,
                                    IAssetEvents.class,
                                    "onTextureLoaded", new Object[] { mContext, texture, texFileName });
                        }
                        meshMaterial.setTexture(textureKey, texture);
                    }
                    catch (NumberFormatException | IndexOutOfBoundsException ex)
                    {
                        mContext.getEventManager().sendEvent(mContext,
                                IAssetEvents.class,
                                "onTextureError", new Object[] { mContext, ex.getMessage(), texFileName });
                    }
                }
                else
                {
                    GVRAssetLoader.TextureRequest texRequest = new GVRAssetLoader.MaterialTextureRequest(assetRequest.getContext(), texFileName, meshMaterial, textureKey, texParams);
                    assetRequest.loadTexture(texRequest);
                }
            }
        }
    }

    /**
     * Load an embedded texture from the JASSIMP AiScene.
     * An embedded texture is represented as an AiTexture object in Java.
     * The AiTexture contains the pixel data for the bitmap.
     *
     * @param fileName filename for the embedded texture reference.
     *                 The filename inside starts with '*' followed
     *                 by an integer texture index into AiScene embedded textures
     * @return GVRTexture made from embedded texture
     */
    private GVRTexture loadEmbeddedTexture(String fileName, GVRTextureParameters texParams) throws NumberFormatException, IndexOutOfBoundsException
    {
        int texIndex = parseInt(fileName.substring(1));
        AiTexture tex = mScene.getTextures().get(texIndex);
        Bitmap bmap = null;

        if (tex.getHeight() == 0)
        {
            ByteArrayInputStream input = new ByteArrayInputStream(tex.getByteData());
            bmap = BitmapFactory.decodeStream(input);
        }
        else
        {
            bmap = Bitmap.createBitmap(tex.getWidth(), tex.getHeight(), Bitmap.Config.ARGB_8888);
            bmap.setPixels(tex.getIntData(), 0, tex.getWidth(), 0, 0, tex.getWidth(), tex.getHeight());
        }
        GVRBitmapTexture bmapTex = new GVRBitmapTexture(mContext, bmap, texParams);
        return bmapTex;
    }

    private void importLights(List<AiLight> lights, Hashtable<String, GVRLightBase> lightlist){
        for(AiLight light: lights){
            AiLightType type = light.getType();
            if(type == AiLightType.DIRECTIONAL){
                GVRDirectLight gvrLight = new GVRDirectLight(mContext);
                setPhongLightProp(gvrLight,light);
                setLightProp(gvrLight, light);
                String name = light.getName();
                lightlist.put(name, gvrLight);
            }
            if(type == AiLightType.POINT){
                GVRPointLight gvrLight = new GVRPointLight(mContext);
                setPhongLightProp(gvrLight,light);
                setLightProp(gvrLight, light);
                String name = light.getName();
                lightlist.put(name, gvrLight);
            }
            if(type == AiLightType.SPOT){
                GVRSpotLight gvrLight = new GVRSpotLight(mContext);
                setPhongLightProp(gvrLight,light);
                setLightProp(gvrLight, light);
                gvrLight.setFloat("inner_cone_angle", (float)Math.cos(light.getAngleInnerCone()));
                gvrLight.setFloat("outer_cone_angle",(float)Math.cos(light.getAngleOuterCone()));
                String name = light.getName();
                lightlist.put(name, gvrLight);
            }
        }

    }

    private void setLightProp(GVRLightBase gvrLight, AiLight assimpLight){
        gvrLight.setFloat("attenuation_constant", assimpLight.getAttenuationConstant());
        gvrLight.setFloat("attenuation_linear", assimpLight.getAttenuationLinear());
        gvrLight.setFloat("attenuation_quadratic", assimpLight.getAttenuationQuadratic());
    }

    private void setPhongLightProp(GVRLightBase gvrLight, AiLight assimpLight){
        org.gearvrf.jassimp2.AiColor ambientCol= assimpLight.getColorAmbient(sWrapperProvider);
        org.gearvrf.jassimp2.AiColor diffuseCol= assimpLight.getColorDiffuse(sWrapperProvider);
        org.gearvrf.jassimp2.AiColor specular = assimpLight.getColorSpecular(sWrapperProvider);
        gvrLight.setVec4("ambient_intensity", ambientCol.getRed(), ambientCol.getGreen(), ambientCol.getBlue(),ambientCol.getAlpha());
        gvrLight.setVec4("diffuse_intensity", diffuseCol.getRed(), diffuseCol.getGreen(),diffuseCol.getBlue(),diffuseCol.getAlpha());
        gvrLight.setVec4("specular_intensity", specular.getRed(),specular.getGreen(),specular.getBlue(), specular.getAlpha());
    }
}
