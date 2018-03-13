# Copyright 2015 Samsung Electronics Co., LTD
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from abc import ABC, abstractmethod
import bpy
import socket
import math
import os.path
import filecmp
import shutil
import json


class BaseExporter(ABC):
    @abstractmethod
    def export(self):
        pass

    @abstractmethod
    def export_to_file(self):
        pass

    def normalize_quaternion(self, rotation):
        magnitude = math.sqrt(
            rotation[0] * rotation[0] + rotation[1] * rotation[1]
            + rotation[2] * rotation[2] + rotation[3] * rotation[3])

        w = rotation[0] / magnitude
        x = rotation[1] / magnitude
        y = rotation[2] / magnitude
        z = rotation[3] / magnitude
        return [w, x, y, z]

    def rotate_90_degree_x(self, rot):
        rotation = self.normalize_quaternion(rot)
        w = rotation[0] * math.sqrt(0.5) - rotation[1] * -math.sqrt(0.5)
        x = rotation[1] * math.sqrt(0.5) + rotation[0] * -math.sqrt(0.5)
        y = rotation[2] * math.sqrt(0.5) - rotation[3] * -math.sqrt(0.5)
        z = rotation[3] * math.sqrt(0.5) + rotation[2] * -math.sqrt(0.5)
        return [w, x, y, z]


class MeshExporter(BaseExporter):
    def __init__(self, client, server_url, obj, globalscale=0.01):
        self._client = client
        self._server_url = server_url
        self._obj = obj
        self._globalscale = globalscale
        self.export()

    def export(self):
        name = bpy.path.clean_name(self._obj.name) + ".fbx"
        url = self._server_url + name

        self.export_to_file(name)
        self._client.load_mesh(url, name)

    def export_to_file(self, name):
        # Export fbx file
        bpy.ops.export_scene.fbx(
            filepath=name, use_selection=True, object_types={'MESH'}, path_mode='STRIP', global_scale=self._globalscale)
        self.copy_textures()

    def copy_textures(self):
        imgs = set()
        for ms in self._obj.material_slots:
            for ts in ms.material.texture_slots:
                if ts:
                    if hasattr(ts.texture, 'image'):
                        img_name = ts.texture.image.filepath[2:]
                        imgs.add(img_name)

        filepath = bpy.path.abspath('//')
        for img_name in imgs:
            bn = os.path.basename(img_name)
            abspath = os.path.abspath(os.path.join(filepath, img_name))
            try:
                if not os.path.exists(bn) or not filecmp.cmp(abspath, bn):
                    shutil.copy(abspath, bn)
            except FileNotFoundError:
                print('Cannot find the texture file "%s"' % abspath)

class LightExporter(BaseExporter):
    def __init__(self, client, obj):
        self._client = client
        self._obj = obj
        self._light = bpy.data.lamps[self._obj.name]
        self.export()

    def export(self):
        prev_rotation_mode = self._obj.rotation_mode
        # Set rotation mode to quaternion to get the correct rotation values
        self._obj.rotation_mode = 'QUATERNION'
        # Rotate -90 degrees on X axis to ajust coordinate system to Y up and -Z foward
        self._new_rotation = self.rotate_90_degree_x(self._obj.rotation_quaternion)
        self._obj.rotation_mode = prev_rotation_mode

        if self._light.type == 'SPOT':
            outer_angle = math.degrees(self._light.spot_size)
            inner_angle = self.calc_inner_cone(self._light.distance, outer_angle, self._light.spot_blend)

            # GVRf sets spot light angle size on half of the cone
            outer_angle = outer_angle / 2.0
            inner_angle = inner_angle / 2.0

            self.export_to_file(outer_angle, inner_angle)
            self._client.load_light(
                self._obj.location, self._new_rotation, self._light.type, self._light.color, self._light.use_diffuse,
                self._light.use_specular, outer_cone=outer_angle, inner_cone=inner_angle)
        else:
            self.export_to_file()
            self._client.load_light(
                self._obj.location, self._new_rotation, self._light.type, self._light.color, self._light.use_diffuse,
                self._light.use_specular)

    def calc_inner_cone(self, distance, size, blend):
        outer_r = math.tan(math.radians(size / 2.0)) * distance
        outer_area = math.pi * math.pow(outer_r, 2)
        inner_area = outer_area * (1.0 - blend)
        inner_r = math.sqrt(inner_area / math.pi)
        inner_angle = math.degrees(math.atan(inner_r / distance))
        return inner_angle * 2

    def export_to_file(self, outer_angle=None, inner_angle=None):
        f = open(self._obj.name + '.json', 'w')
        json_string = self.create_json(
            self._obj.name, self._obj.location, self._new_rotation, self._light.type, self._light.color,
            self._light.use_diffuse, self._light.use_specular, outer=outer_angle, inner=inner_angle)
        f.write(json_string)
        f.close()

    def create_json(self, name, location, rotation, light_type, color, diffuse, specular, outer=None, inner=None):
        return json.dumps({
            'type': 'Light',
            'name': name,
            'location': [
                location[0],
                location[2],
                location[1] * -1
            ],
            'rotation': [
                rotation[0],
                rotation[1],
                rotation[2],
                rotation[3]
            ],
            'lightType': light_type,
            'color': [
                color[0],
                color[1],
                color[2]
            ],
            'use_diffuse': diffuse,
            'use_specular': specular,
            'outer_cone_angle': outer,
            'inner_cone_angle': inner
        }, indent=4)

class CameraExporter(BaseExporter):
    def __init__(self, client, obj):
        self._client = client
        self._obj = obj
        self.export()

    def export(self):
        prev_rotation_mode = self._obj.rotation_mode
        # Set rotation mode to quaternion to get the correct rotation values
        self._obj.rotation_mode = 'QUATERNION'
        # Rotate -90 degrees on X axis to ajust coordinate system to Y up and -Z foward
        self._new_rotation = self.rotate_90_degree_x(self._obj.rotation_quaternion)
        self._obj.rotation_mode = prev_rotation_mode

        self.export_to_file()
        self._client.load_camera(self._obj.location, self._new_rotation)

    def export_to_file(self):
        f = open(self._obj.name + '.json', 'w')
        json_string = self.create_json(self._obj.name, self._obj.location, self._new_rotation)
        f.write(json_string)
        f.close()

    def create_json(self, name, location, rotation):
        return json.dumps({
            'type': 'Camera',
            'name': name,
            'location': [
                location[0],
                location[2],
                location[1] * -1
            ],
            'rotation': [
                rotation[0],
                rotation[1],
                rotation[2],
                rotation[3]
            ],
            'near_clipping': 0,
            'far_clipping': 0
        }, indent=4)


def export(client, selected_objects, globalscale):
    ipv4 = socket.gethostbyname_ex(socket.getfqdn(''))[2][0]
    server_url = 'http://' + ipv4 + ':8000/'

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
            LightExporter(client, obj)

        elif obj.type == 'CAMERA':
            CameraExporter(client, obj)

        obj.select = was_selected

    client.disconnect()

    scene.objects.active = obj_active

    for obj in selection:
        obj.select = True


def clear_scene(client):
    client.clear_scene()
