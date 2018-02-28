from abc import ABC, abstractmethod

class BaseClient(ABC):
    @abstractmethod
    def connect(self, address = None, port = 0):
        pass

    @abstractmethod
    def disconnect(self):
        pass

    @abstractmethod
    def load_mesh(self, url, name):
        pass

    @abstractmethod
    def load_light(self, position, rotation, light_type, color, use_diffuse, use_specular, outer_cone = None, inner_cone = None):
        pass

    @abstractmethod
    def clear_scene(self):
        pass
