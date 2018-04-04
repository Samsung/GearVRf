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

from telnetlib import Telnet
from .base_client import BaseClient
from . import gvrf_commands


class GvrfClient(BaseClient):
    _addr = None
    _port = 0

    _conn = None
    _debug = False

    def __init__(self, address=None, port=0, do_connect=True, do_debug=False):
        self._debug = do_debug
        if do_connect:
            self.connect(address, port)

    def connect(self, address=None, port=0):
        if self._addr != address:
            self._addr = address
        if self._port != port:
            self._port = port
        if self._addr is not None:
            self._conn = Telnet(self._addr, self._port)
            r = self._conn.read_until(b'gvrf>')
            self._debug and print(r)
            self._conn.write(b'js\n')
            r = self._conn.read_until(b'js>')
            self._debug and print(r)

        self.prepare_scene()

    def disconnect(self):
        self._conn.close()

    def reconnect(self):
        self.connect(self._addr, self._port)

    def exec_command(self, command, wait_for=b'js>'):
        self.send_command(command)
        self.wait_completion(wait_for)

    def send_command(self, command):
        if type(command) != bytes:
            command = bytes(command, encoding='utf8')

        if command[-1] != 10:  # CR
            command = command + b'\n'

        self._conn.write(command)

    def wait_completion(self, wait_for=b'js>'):
        if type(wait_for) != bytes:
            wait_for = bytes(wait_for, encoding='utf8')

        r = self._conn.read_until(wait_for)
        self._debug and print(r)

    def prepare_scene(self):
        self.exec_command(gvrf_commands.import_package())
        self.exec_command(gvrf_commands.import_package_animation())
        self.exec_command(gvrf_commands.get_scene())
        self.exec_command(gvrf_commands.get_camera())

    def clear_scene(self):
        self.exec_command(gvrf_commands.clear_scene())

    def load_mesh(self, url, name, is_animated):
        var_url = 'url'
        var_obj = 'obj'
        self.exec_command(gvrf_commands.create_url(var_url, url))
        self.exec_command(gvrf_commands.remove_scene_obj(name))
        self.exec_command(gvrf_commands.load_model_from_url(var_obj, var_url))
        if is_animated:
            self.exec_command(gvrf_commands.get_animator(var_obj))
            self.exec_command(gvrf_commands.animator_start())
            self.exec_command(gvrf_commands.animator_set_repeat_mode())
            # Set repeat count -1 to repeat infinitely
            self.exec_command(gvrf_commands.animator_set_repeat_count(-1))

    def load_light(
            self, name, position, rotation, light_type, color, use_diffuse, use_specular, outer_cone=None,
            inner_cone=None):

        self.exec_command(gvrf_commands.remove_scene_obj(name))
        self.create_light_node(name, position, rotation)

        if (light_type == 'POINT'):
            self.create_point_light(color, use_diffuse, use_specular)

        elif (light_type == 'SUN'):
            self.create_direct_light(color, use_diffuse, use_specular)

        elif (light_type == 'SPOT'):
            self.create_spot_light(color, use_diffuse, use_specular, outer_cone, inner_cone)

        else:
            print(light_type + ' light type is not supported in GVRf.')

    def load_camera(self, position, rotation, near_clipping, far_clipping):
        var_obj = gvrf_commands._var_camera

        self.exec_command(gvrf_commands.set_position(var_obj, position[0], position[2], position[1] * -1))
        self.exec_command(gvrf_commands.set_rotation(var_obj, rotation[0], rotation[1], rotation[2], rotation[3]))
        self.exec_command(gvrf_commands.set_near_clipping(near_clipping))
        self.exec_command(gvrf_commands.set_far_clipping(far_clipping))

    def create_light_node(self, name, position, rotation):
        var_obj = 'lightNode'

        self.exec_command(gvrf_commands.create_scene_obj(var_obj))
        self.exec_command(gvrf_commands.set_obj_name(var_obj, name))

        self.exec_command(gvrf_commands.set_position(var_obj, position[0], position[2], position[1] * -1))
        self.exec_command(gvrf_commands.set_rotation(var_obj, rotation[0], rotation[1], rotation[2], rotation[3]))
        self.exec_command(gvrf_commands.add_on_scene(var_obj))

    def create_point_light(self, color, use_diffuse, use_specular):
        var_obj = 'lightNode'
        var_light = 'pointLight'

        self.exec_command(gvrf_commands.create_point_light(var_light))

        if (use_diffuse):
            self.exec_command(gvrf_commands.set_diffuse(var_light, color[0], color[1], color[2], 1.0))
        if (use_specular):
            self.exec_command(gvrf_commands.set_specular(var_light, color[0], color[1], color[2], 1.0))

        self.exec_command(gvrf_commands.attach_light(var_obj, var_light))

    def create_direct_light(self, color, use_diffuse, use_specular):
        var_obj = 'lightNode'
        var_light = 'directLight'

        self.exec_command(gvrf_commands.create_direct_light(var_light))

        if (use_diffuse):
            self.exec_command(gvrf_commands.set_diffuse(var_light, color[0], color[1], color[2], 1.0))
        if (use_specular):
            self.exec_command(gvrf_commands.set_specular(var_light, color[0], color[1], color[2], 1.0))

        self.exec_command(gvrf_commands.attach_light(var_obj, var_light))

    def create_spot_light(self, color, use_diffuse, use_specular, outer_cone, inner_cone):
        var_obj = 'lightNode'
        var_light = 'spotLight'

        self.exec_command(gvrf_commands.create_spot_light(var_light))

        if (use_diffuse):
            self.exec_command(gvrf_commands.set_diffuse(var_light, color[0], color[1], color[2], 1.0))
        if (use_specular):
            self.exec_command(gvrf_commands.set_specular(var_light, color[0], color[1], color[2], 1.0))

        self.exec_command(gvrf_commands.set_inner_cone(var_light, inner_cone))
        self.exec_command(gvrf_commands.set_outer_cone(var_light, outer_cone))

        self.exec_command(gvrf_commands.attach_light(var_obj, var_light))
