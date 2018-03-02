from abc import ABC, abstractmethod
import bpy, socket, math
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
    def __init__(self, client, server_url, obj, globalscale = 0.01):
        self._client = client
        self._server_url = server_url
        self._obj = obj
        self._globalscale = globalscale
        self.export()

    def export(self):
        name = bpy.path.clean_name(self._obj.name) + ".fbx"

        # Export fbx file
        bpy.ops.export_scene.fbx(filepath = name, use_selection = True, object_types = { 'MESH' }, path_mode = 'STRIP', global_scale = self._globalscale)

        url = self._server_url + name

        self._client.load_mesh(url, name)

class LightExport(BaseExporter):
    def __init__(self, client, obj):
        self._client = client
        self._obj = obj
        self._light = bpy.data.lamps[self._obj.name]
        self.export()

    def export(self):
        if self._light.type == 'SPOT':
            outer_angle = math.degrees(self._light.spot_size)
            inner_angle = self.calc_inner_cone(self._light.distance, outer_angle, self._light.spot_blend)

            # GVRf sets spot light angle size on half of the cone
            outer_angle = outer_angle / 2.0
            inner_angle = inner_angle / 2.0

            self.export_to_file(outer_angle, inner_angle)
            self._client.load_light(
                self._obj.location,
                self._obj.rotation_quaternion,
                self._light.type,
                self._light.color,
                self._light.use_diffuse,
                self._light.use_specular,
                outer_cone = outer_angle,
                inner_cone = inner_angle
            )
        else:
            self.export_to_file()
            self._client.load_light(
                self._obj.location,
                self._obj.rotation_quaternion,
                self._light.type,
                self._light.color,
                self._light.use_diffuse,
                self._light.use_specular,
            )

    def calc_inner_cone(self, distance, size, blend):
        outer_r = math.tan(math.radians(size / 2.0)) * distance
        outer_area = math.pi * math.pow(outer_r, 2)
        inner_area = outer_area * (1.0 - blend)
        inner_r = math.sqrt(inner_area / math.pi)
        inner_angle = math.degrees(math.atan(inner_r / distance))
        return inner_angle * 2

    def export_to_file(self, outer_angle = None, inner_angle = None):
        f = open(self._obj.name + '.txt', 'w')
        f.write('name: %s\n' % self._obj.name)
        # Change coordinate system to Y-Up and -Z foward
        f.write('location: %s, %s, %s\n' % (self._obj.location[0], self._obj.location[2], self._obj.location[1] * -1))
        f.write('rotation: %s, %s, %s, %s\n' % (-self._obj.rotation_quaternion[1], self._obj.rotation_quaternion[2], -self._obj.rotation_quaternion[3], self._obj.rotation_quaternion[0]))
        f.write('type: %s\n' % self._light.type)
        f.write('color: %s, %s, %s\n' % (self._light.color[0], self._light.color[1], self._light.color[2]))
        f.write('use_diffuse: %s\n' % self._light.use_diffuse)
        f.write('use_specular: %s\n' % self._light.use_specular)
        if outer_angle and inner_angle:
            f.write('outer_cone_angle: %s\n' % outer_angle)
            f.write('inner_cone_angle: %s\n' % inner_angle)
        f.close()

def export(client, selected_objects, globalscale):
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
            MeshExporter(client, server_url, obj, globalscale)

        elif obj.type == 'LAMP':
            LightExport(client, obj)

        obj.select = was_selected

    client.disconnect()

    scene.objects.active = obj_active

    for obj in selection:
        obj.select = True

def clear_scene(client):
    client.clear_scene()
