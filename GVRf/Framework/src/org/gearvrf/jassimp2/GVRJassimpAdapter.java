package org.gearvrf.jassimp2;

import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.utility.Log;

public class GVRJassimpAdapter {
    private static final String TAG = GVRJassimpAdapter.class.getSimpleName();
    public static GVRNewWrapperProvider sWrapperProvider = new GVRNewWrapperProvider();
    private static GVRJassimpAdapter sAdapter;

    private GVRJassimpAdapter() {
    }

    public synchronized static GVRJassimpAdapter get() {
        if (sAdapter == null) {
            sAdapter = new GVRJassimpAdapter();
        }
        return sAdapter;
    }

    public static void set(GVRJassimpAdapter newAdapter) {
        sAdapter = newAdapter;
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

        // Normals
        FloatBuffer normalsBuffer = aiMesh.getNormalBuffer();
        if (normalsBuffer != null) {
            float[] normalsArray = new float[normalsBuffer.capacity()];
            normalsBuffer.get(normalsArray, 0, normalsBuffer.capacity());
            mesh.setNormals(normalsArray);
        }

        // TexCoords (UV only)
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
            mesh.setTriangles(triangles.array());
        }

        return mesh;
    }

    public GVRSceneObject createSceneObject(GVRContext ctx, AiNode node) {
        GVRSceneObject sceneObject = new GVRSceneObject(ctx);
        sceneObject.setName(node.getName());
        return sceneObject;
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
