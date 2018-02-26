from telnetlib import Telnet
from .base_client import BaseClient

class GvrfClient(BaseClient):
    _addr = None
    _port = 0

    _conn = None
    _debug = False

    def __init__(self, address = None, port = 0, do_connect = True, do_debug = False):
        self._debug = do_debug
        if do_connect:
            self.connect(address, port)

    def connect(self, address = None, port = 0):
        if self._addr != address:
            self._addr = address
        if self._port != port:
            self._port = port
        if self._addr != None:
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

    def exec_command(self, command, wait_for = b'js>'):
        self.send_command(command)
        self.wait_completion(wait_for)

    def send_command(self, command):
        if type(command) != bytes:
            command = bytes(command, encoding = 'utf8')
        
        if command[-1] != 10: # CR
            command = command + b'\n'
        
        self._conn.write(command)

    def wait_completion(self, wait_for = b'js>'):
        if type(wait_for) != bytes:
            wait_for = bytes(wait_for, encoding = 'utf8')
        
        r = self._conn.read_until(wait_for)
        self._debug and print(r)

    def prepare_scene(self):
        self.exec_command('importPackage(org.gearvrf);')
        self.exec_command('var mainScene = gvrf.getMainScene();')

    def load_mesh(self, url):
        self.exec_command('var url = new java.net.URL("' + url + '");')
        # self.exec_command('var object = mainScene.getSceneObjectByName("' + name + '");')
        # self.exec_command('if (object != null) { mainScene.removeSceneObject(object); }')
        self.exec_command('object = gvrf.getAssetLoader().loadModel(url, mainScene);')
    
    def load_light(self):
        pass
    
    def clear_scene(self):
        self.exec_command('mainScene.clear();')

    def create_light(gvrf_app):
        gvrf_app.exec_command('var lightNode = new GVRSceneObject(gvrContext);')
        create_direct_ligh()
        gvrf_app.exec_command('lightNode.attachLigh(light);')

    def create_direct_ligh():
        gvrf_app.exec_command('var light = new GVRDirectLight(gvrContext);')

    def create_spot_light():
        gvrf_app.exec_command('var light = new GVRDirectLight(gvrContext);')

    def create_point_light():
        gvrf_app.exec_command('var light = new GVRDirectLight(gvrContext);')