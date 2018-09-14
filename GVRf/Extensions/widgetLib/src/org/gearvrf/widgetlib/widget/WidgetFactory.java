package org.gearvrf.widgetlib.widget;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.widget.NodeEntry.NameDemangler;
import org.gearvrf.widgetlib.widget.layout.basic.AbsoluteLayout;

public class WidgetFactory {
    /**
     * Create a {@link Widget} to wrap the specified {@link GVRSceneObject}. By
     * default, {@code sceneObject} is wrapped in an {@link GroupWidget}. If
     * another {@code Widget} class is specified in {@code sceneObject}'s
     * metadata (as "{@code class_WidgetClassName}"), it will be wrapped in an
     * instance of the specified class instead.
     *
     * @see NameDemangler#demangleString(String)
     *
     * @param sceneObject
     *            The {@code GVRSceneObject} to wrap.
     * @return A new {@code Widget} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     */
    @SuppressWarnings("unchecked")
    static Widget createWidget(final GVRSceneObject sceneObject)
            throws InstantiationException {
        Class<? extends Widget> widgetClass = GroupWidget.class;
        NodeEntry attributes = new NodeEntry(sceneObject);

        String className = attributes.getClassName();
        if (className != null) {
            try {
                widgetClass = (Class<? extends Widget>) Class
                        .forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, e, "createWidget()");
                throw new InstantiationException(e.getLocalizedMessage());
            }
        }
        Log.d(TAG, "createWidget(): widgetClass: %s",
              widgetClass.getSimpleName());

        return createWidget(sceneObject, attributes, widgetClass);
    }

    /**
     * Create a {@link Widget} of the specified {@code widgetClass} to wrap
     * {@link GVRSceneObject sceneObject}.
     *
     * @param sceneObject
     *            The {@code GVRSceneObject} to wrap
     * @param widgetClass
     *            The {@linkplain Class} of the {@code Widget} to wrap
     *            {@code sceneObject} with.
     * @return A new {@code Widget} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     */
    public static Widget createWidget(final GVRSceneObject sceneObject,
            Class<? extends Widget> widgetClass) throws InstantiationException {
        NodeEntry attributes = new NodeEntry(sceneObject);
        return createWidget(sceneObject, attributes, widgetClass);
    }

    /**
     * Create an {@link AbsoluteLayout} {@link Widget} to wrap a
     * {@link GVRSceneObject} that is a child of the specified {@code root}
     * {@code GVRSceneObject}.
     *
     * @param root
     *            The root {@code GVRSceneObject} containing the desired child.
     * @param childName
     *            Name of the child of {@code root} to wrap.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     */
    public static Widget createWidget(final GVRSceneObject root,
            final String childName) throws InstantiationException {
        GroupWidget widget = (GroupWidget)createWidget(root, childName, GroupWidget.class);
        widget.applyLayout(new AbsoluteLayout());
        return widget;
    }

    /**
     * Create an {@link Widget} of the specified {@code widgetClass} to wrap a
     * {@link GVRSceneObject} that is a child of the specified {@code root}
     * {@code GVRSceneObject}.
     *
     * @param root
     *            The root {@code GVRSceneObject} containing the desired child.
     * @param childName
     *            Name of the child of {@code root} to wrap.
     * @param widgetClass
     *            The {@linkplain Class} of the {@code Widget} to wrap the child
     *            {@code GVRSceneObject} with.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     */
    static public Widget createWidget(GVRSceneObject root,
            final String childName, final Class<? extends Widget> widgetClass)
            throws InstantiationException {
        Widget result = null;
        if (root != null) {
            root = findByName(childName, root);
            if (root != null) {
                try {
                    result = createWidget(root, widgetClass);
                    Log.d(TAG, "createWidget(): created %s '%s'",
                          widgetClass.getSimpleName(), childName);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    Log.e(TAG,
                          "createWidget(): couldn't instantiate '%s' as %s!",
                          childName, widgetClass.getSimpleName());
                    throw e;
                }
            } else {
                Log.e(TAG, "createWidget(): can't find '%s'!", childName);
            }
        } else {
            Log.e(TAG, "createWidget(): root is null!");
        }
        return result;
    }

    /**
     * Create an {@link AbsoluteLayout} to wrap the root {@link GVRSceneObject}
     * of the scene graph loaded from a file.
     *
     * @param gvrContext
     *            The {@link GVRContext} to load the model into.
     * @param modelFile
     *            The asset file to load the model from.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     * @throws IOException
     *             If the model file can't be read.
     */
    @SuppressWarnings("unused")
    public static Widget createWidgetFromModel(final GVRContext gvrContext,
            final String modelFile) throws InstantiationException, IOException {
        GroupWidget widget = (GroupWidget) createWidgetFromModel(gvrContext, modelFile,
                                     GroupWidget.class);
        widget.applyLayout(new AbsoluteLayout());
        return widget;
    }

    /**
     * Create a {@link Widget} of the specified {@code widgetClass} to wrap the
     * root {@link GVRSceneObject} of the scene graph loaded from a file.
     *
     * @param gvrContext
     *            The {@link GVRContext} to load the model into.
     * @param modelFile
     *            The asset file to load the model from.
     * @param widgetClass
     *            The {@linkplain Class} of the {@code Widget} to wrap the root
     *            {@code GVRSceneObject} with.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code Widget} can't be instantiated for any reason.
     * @throws IOException
     *             If the model file can't be read.
     */
    @SuppressWarnings("WeakerAccess")
    public static Widget createWidgetFromModel(final GVRContext gvrContext,
            final String modelFile, Class<? extends Widget> widgetClass)
            throws InstantiationException, IOException {
        GVRSceneObject rootNode = loadModel(gvrContext, modelFile);
        return createWidget(rootNode, widgetClass);
    }

    /**
     * Create an {@link AbsoluteLayout} {@link Widget} to wrap a
     * {@link GVRSceneObject} that is a child of the {@code root}
     * {@code GVRSceneObject} of the scene graph loaded from a file.
     *
     * @param gvrContext
     *            The {@link GVRContext} to load the model into.
     * @param modelFile
     *            The asset file to load the model from.
     * @param nodeName
     *            Name of the child of {@code root} to wrap.
     * @return A new {@code AbsoluteLayout} instance.
     * @throws InstantiationException
     *             If the {@code AbsoluteLayout} can't be instantiated for any
     *             reason.
     * @throws IOException
     *             If the model file can't be read.
     */
    @SuppressWarnings("unused")
    public static Widget createWidgetFromModel(final GVRContext gvrContext,
            final String modelFile, final String nodeName)
            throws InstantiationException, IOException {
        GroupWidget widget = (GroupWidget)createWidgetFromModel(gvrContext, modelFile, nodeName,
                                     GroupWidget.class);
        widget.applyLayout(new AbsoluteLayout());
        return widget;
    }

    /**
     * Create a {@link Widget} of the specified {@code widgetClass} to wrap a
     * {@link GVRSceneObject} that is a child of the {@code root}
     * {@code GVRSceneObject} of the scene graph loaded from a file.
     *
     * @param gvrContext
     *            The {@link GVRContext} to load the model into.
     * @param modelFile
     *            The asset file to load the model from.
     * @param nodeName
     *            Name of the child of {@code root} to wrap.
     * @param widgetClass
     *            The {@linkplain Class} of the {@code Widget} to wrap the child
     *            {@code GVRSceneObject} with.
     * @return A new {@code Widget} instance.
     * @throws InstantiationException
     *             If the {@code AbsoluteLayout} can't be instantiated for any
     *             reason.
     * @throws IOException
     *             If the model file can't be read.
     */
    public static Widget createWidgetFromModel(final GVRContext gvrContext,
            final String modelFile, final String nodeName,
            Class<? extends Widget> widgetClass) throws InstantiationException,
            IOException {
        GVRSceneObject rootNode = loadModel(gvrContext, modelFile,
                                                    nodeName);
        return createWidget(rootNode, widgetClass);
    }

    /* Model loading */

    /**
     * Load model from file
     *
     * @param gvrContext Valid {@link GVRContext} instance
     * @param modelFile Path to the model's file, relative to the {@code assets} directory
     * @return root object The root {@link GVRSceneObject} of the model
     * @throws IOException If reading the model file fails
     */
    public static GVRSceneObject loadModel(final GVRContext gvrContext,
                                           final String modelFile) throws IOException {
        return loadModel(gvrContext, modelFile, new HashMap<String, Integer>());
    }

    /**
     * Load model from file starting from the specific node name
     * @param gvrContext Valid {@link GVRContext} instance
     * @param modelFile Path to the model's file, relative to the {@code assets} directory
     * @param nodeName name of the starting node
     * @return root object
     * @throws IOException If reading the model file fails
     */
    public static GVRSceneObject loadModel(final GVRContext gvrContext,
                                           final String modelFile, final String nodeName)
            throws IOException {
        return loadModel(gvrContext, modelFile, nodeName, new HashMap<String, Integer>());
    }

    private static GVRSceneObject loadModel(final GVRContext gvrContext,
                                           final String modelFile,
                                            final HashMap<String, Integer> duplicates)
            throws IOException {
        return loadModel(gvrContext, modelFile, null, duplicates);
    }

    private static GVRSceneObject loadModel(
            final GVRContext gvrContext, final String modelFile,
            String nodeName, final HashMap<String, Integer> duplicates) throws IOException {
        GVRSceneObject assimpScene = gvrContext.getAssetLoader().loadModel(modelFile,
                org.gearvrf.GVRImportSettings.getRecommendedSettings(), true, null);
//        printOutScene(assimpScene, 0);
        GVRSceneObject root = getRootNode(assimpScene);

        // JAssimp can create multiple objects for the same node.
        // It can happen, for instance, for multiple meshes nodes.
        // FBx can  also generate multiple objects with $AssimpFbx$ substring
        // in the name with no render data but with some transformation applied.
        // It depends on the model exporting options we cannot manage on our side.
        avoidNameDuplication(root, duplicates, 0);
        if (nodeName != null && !nodeName.isEmpty()) {
            return findByName(nodeName, root);
        }
        return root;
    }

    @SuppressWarnings("unused")
    private static void printOutScene(final GVRSceneObject scene, int level) {
        Log.d(TAG, "model:: %d) name = %s [%s], renderData = %s transfrom = %s",
                level, scene.getName(), scene, scene.getRenderData(), scene.getTransform());
        if (scene.children() != null) {
            for (GVRSceneObject child: scene.children()) {
                printOutScene(child, level + 1);
            }
        }
    }

    private static GVRSceneObject getRootNode(GVRSceneObject node) {
        GVRSceneObject root = null;
        if (ROOT_NODE_NAME.equals(node.getName())) {
            root = node;
        } else if (node.getChildrenCount() > 0) {
            for (GVRSceneObject child: node.getChildren()) {
                root = getRootNode(child);
                if (root != null) {
                    break;
                }
            }
        }
        return root;
    }

    private static GVRSceneObject findByName(final String name,
                                            final GVRSceneObject root) {
        Log.d(TAG, "findByName(): searching for '%s' on node '%s'", name,
                root.getName());
        return findByName(name, root, 0);
    }

    private static void avoidNameDuplication(final GVRSceneObject root,
                                            final Map<String, Integer> map, int level) {
        Log.d(TAG, "avoidNameDuplication(): %d '%s'", level, root.getName());
        NodeEntry entry = new NodeEntry(root);
        Log.d(TAG, "avoidNameDuplication(): entry: %s", entry);
        if (entry != null) {
            String entryName = entry.getName();
            int num = 1;
            boolean duplicated = map.containsKey(entryName);
            if (duplicated) {
                String name = root.getName();
                num = ((map.get(entryName)));
                root.setName(name.replace(entryName, entryName + num));
                Log.w(TAG, "Duplicated scene object: %s renamed to %s", name,
                        root.getName());
                num++;
            }
            map.put(entryName, num);
        }

        if (root.children() != null) {
            for (GVRSceneObject child : root.children()) {
                avoidNameDuplication(child, map, level + 1);
            }
        }
    }

    private static Widget createWidget(final GVRSceneObject sceneObject,
                                       NodeEntry attributes, Class<? extends Widget> widgetClass)
            throws InstantiationException {
        try {
            Constructor<? extends Widget> ctor = widgetClass
                    .getConstructor(GVRContext.class, GVRSceneObject.class,
                            NodeEntry.class);
            return ctor.newInstance(sceneObject
                    .getGVRContext(), sceneObject, attributes);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
            Log.e(TAG, e, "createWidget()");
            throw new InstantiationException(e.getLocalizedMessage());
        }
    }

    private static GVRSceneObject findByName(final String name,
                                             final GVRSceneObject root, int level) {
        GVRSceneObject obj = null;
        try {
            Log.d(TAG, "findByName():    %s (%d)", root.getName(), level);
            NodeEntry entry = new NodeEntry(root);
            if (name != null && name.equals(entry.getName())) {
                obj = root;
            } else if (root.getChildrenCount() > 0) {
                for (GVRSceneObject child: root.getChildren()) {
                    obj = findByName(name, child, level + 1);
                    if (obj != null) {
                        Log.d(TAG, "found object [%s] %s", name, obj.getName());
                        break;
                    }
                }
            }
        } catch (IllegalFormatException e) {
            e.printStackTrace();
            Log.e(TAG, e, "findByName()");
        }

        return obj;
    }

    private static final String ROOT_NODE_NAME = "RootNode";
    private static String TAG = WidgetFactory.class.getSimpleName();
}
