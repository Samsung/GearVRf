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


class BaseClient(ABC):
    @abstractmethod
    def connect(self, address=None, port=0):
        pass

    @abstractmethod
    def disconnect(self):
        pass

    @abstractmethod
    def load_mesh(self, url, name, is_animated):
        pass

    @abstractmethod
    def load_light(
            self, position, rotation, light_type, color, use_diffuse, use_specular, outer_cone=None, inner_cone=None):
        pass

    @abstractmethod
    def load_camera(self, position, rotation):
        pass

    @abstractmethod
    def clear_scene(self):
        pass
