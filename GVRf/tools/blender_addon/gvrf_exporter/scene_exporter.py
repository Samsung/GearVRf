#!/usr/bin/python3

import bpy, socket
import os.path, filecmp, shutil

def __copy_textures(all_objects=True):
    if all_objects:
        col = bpy.data.objects
    else:
        col = bpy.context.selected_objects

    imgs = set()
    for o in col:
        for ms in o.material_slots:
            for ts in ms.material.texture_slots:
                if ts:
                    if hasattr(ts.texture, 'image'):
                        img_name = ts.texture.image.filepath[2:]
                        imgs.add(img_name)

    filepath = bpy.path.abspath('//')
    for img_name in imgs:
        bn = os.path.basename(img_name)
        abspath = os.path.abspath(os.path.join(filepath, img_name))
        if not os.path.exists(bn) or not filecmp.cmp(abspath, bn):
            shutil.copy(abspath, bn)


def export(gvrf_app, selected_objects):
    ipv4 = socket.gethostbyname_ex(socket.getfqdn(''))[2][0]
    server_url = 'http://' + ipv4 + ':8000/'

    __copy_textures()

    scene = bpy.context.scene
    obj_active = scene.objects.active

    selection = bpy.context.selected_objects

    gvrf_app.exec_command('importPackage(org.gearvrf);')
    gvrf_app.exec_command('var mainScene = gvrf.getMainScene();')

    for obj in scene.objects:
        if obj.type != 'MESH':
            continue

        if selected_objects and not obj.select:
            continue

        was_selected = obj.select
        obj.select = True

        # some exporters only use the active object
        scene.objects.active = obj

        name = bpy.path.clean_name(obj.name) + ".fbx"
        
        # Exporting object as FBX file
        #fn = os.path.join(assetsDir, name)
        fn = name
        bpy.ops.export_scene.fbx(filepath = fn, use_selection = True, object_types = { 'MESH' }, path_mode = 'STRIP')

        url = server_url + name
        gvrf_app.exec_command('var url = new java.net.URL("' + url + '");')
        gvrf_app.exec_command('var object = gvrf.getAssetLoader().loadModel(url, mainScene);')

        obj.select = was_selected

    gvrf_app.disconnect()

    scene.objects.active = obj_active

    for obj in selection:
        obj.select = True

def clear_scene(gvrf_app):
    gvrf_app.exec_command('importPackage(org.gearvrf);')
    gvrf_app.exec_command('var mainScene = gvrf.getMainScene();')
    gvrf_app.exec_command('mainScene.removeAllSceneObjects();')
