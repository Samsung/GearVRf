#!/usr/bin/python3

import bpy, socket

def export(gvrf_app):
    _ipv4 = socket.gethostbyname_ex(socket.getfqdn(""))[2][0]
    serverUrl = "http://" + _ipv4 + ":8000/"

    scene = bpy.context.scene
    obj_active = scene.objects.active

    selection = bpy.context.selected_objects

    bpy.ops.object.select_all(action='DESELECT')

    gvrf_app.exec_command('importPackage(org.gearvrf);')
    gvrf_app.exec_command('var mainScene = gvrf.getMainScene();')
    gvrf_app.exec_command('mainScene.removeAllSceneObjects();')

    for obj in scene.objects:
        if obj.type != 'MESH':
            continue

        was_selected = obj.select
        obj.select = True

        # some exporters only use the active object
        scene.objects.active = obj

        name = bpy.path.clean_name(obj.name) + ".fbx"
        
        # Exporting object as FBX file
        #fn = os.path.join(assetsDir, name)
        fn = name
        bpy.ops.export_scene.fbx(filepath=fn, use_selection=True, object_types ={'MESH'}, path_mode = 'STRIP')

        url = serverUrl + name
        gvrf_app.exec_command('var url = new java.net.URL("' + url + '");')
        gvrf_app.exec_command('var object = gvrf.getAssetLoader().loadModel(url, mainScene);')
        gvrf_app.exec_command('mainScene.addSceneObject(object);')

        obj.select = was_selected

    gvrf_app.disconnect()

    scene.objects.active = obj_active

    for obj in selection:
        obj.select = True