from abc import ABC, abstractmethod

class BaseClient(ABC):
    @abstractmethod
    def connect(self, address = None, port = 0):
        pass

    @abstractmethod
    def disconnect(self):
        pass

    @abstractmethod
    def load_mesh(self, url):
        pass

    @abstractmethod
    def load_light(self):
        pass

    @abstractmethod
    def clear_scene(self):
        pass
