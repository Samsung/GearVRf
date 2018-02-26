from abc import ABC, abstractmethod
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


class BaseExporter(ABC):
    @abstractmethod
    def export(self):
        pass

class MeshExporter(BaseExporter):
    def __init__(self, client, server_url, obj):
        self._client = client
        self._server_url = server_url
        self._obj = obj
        self.export()
    
    def export(self):
        name = bpy.path.clean_name(self._obj.name) + ".fbx"

        # Export fbx file
        bpy.ops.export_scene.fbx(filepath = name, use_selection = True, object_types = { 'MESH' }, path_mode = 'STRIP')

        url = self._server_url + name

        self._client.load_mesh(url)

class LightExport(BaseExporter):
    def __init__(self, client, light):
        self._client = client)
        self._light = light
        self.export()

    def export(self):
        pass

def export(client, selected_objects):
    ipv4 = socket.gethostbyname_ex(socket.getfqdn(''))[2][0]
    server_url = 'http://' + ipv4 + ':8000/'

    __copy_textures()

    scene = bpy.context.scene
    obj_active = scene.objects.active

    selection = bpy.context.selected_objects

    for obj in scene.objects:
        if selected_objects and not obj.select:
            continue

        was_selected = obj.select
        obj.select = True

        # some exporters only use the active object
        scene.objects.active = obj

        if obj.type == 'MESH':
            MeshExporter(client, server_url, obj)

        elif obj.type == 'LAMP':
            LightExport(client, obj)

        obj.select = was_selected

    client.disconnect()

    scene.objects.active = obj_active

    for obj in selection:
        obj.select = True

def clear_scene(client):
    client.clear_scene()
