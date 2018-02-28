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

    def clear_scene(self):
        self.exec_command('mainScene.clear();')

    def load_mesh(self, url, name):
        self.exec_command('var url = new java.net.URL("%s");' % url)
        #self.exec_command('var object = mainScene.getSceneObjectByName("%s");' % name)
        #self.exec_command('if ("undefined" != typeof object) { mainScene.clear(); }')
        self.exec_command('object = gvrf.getAssetLoader().loadModel(url, mainScene);')
        #self.exec_command('object.setName("%s");' % name)
    
    def load_light(self, position, rotation, light_type, color, use_diffuse, use_specular, outer_cone = None, inner_cone = None):
        self.create_light_node(position, rotation)
        
        if (light_type == 'POINT'):
            self.create_point_light(color, use_diffuse, use_specular)

        elif (light_type == 'SUN'):
            self.create_direct_light(color, use_diffuse, use_specular)

        elif (light_type == 'SPOT'):
            self.create_spot_light(color, use_diffuse, use_specular, outer_cone, inner_cone)

        else:
            print(light_type + ' light type is not supported in GVRf.')


    def create_light_node(self, position, rotation):
        self.exec_command('var lightNode = new GVRSceneObject(gvrf);')
        self.exec_command('lightNode.getTransform().setPosition(%s, %s, %s);' % (position[0], position[2], position[1] * -1))
        self.exec_command('lightNode.getTransform().setRotation(%s, %s, %s, %s);' % (-rotation[1], rotation[2], -rotation[3],   rotation[0]))
        self.exec_command('mainScene.addSceneObject(lightNode);')

    def create_point_light(self, color, use_diffuse, use_specular):
        self.exec_command('var pointLight = new GVRPointLight(gvrf);')
        if (use_diffuse):
            self.exec_command('pointLight.setDiffuseIntensity(%s, %s, %s, %s);' % (color[0], color[1], color[2], 1.0))

        if (use_specular):
            self.exec_command('pointLight.setSpecularIntensity(%s, %s, %s, %s);' % (color[0], color[1], color[2], 1.0))

        self.exec_command('pointLight.setAmbientIntensity(%s, %s, %s, %s);' % (color[0], color[1], color[2], 1.0))
        self.exec_command('lightNode.attachLight(pointLight);')

    def create_direct_light(self, color, use_diffuse, use_specular):
        self.exec_command('var directLight = new GVRDirectLight(gvrf);')
        if (use_diffuse):
            self.exec_command('directLight.setDiffuseIntensity(%s, %s, %s, %s);' % (color[0], color[1], color[2], 1.0))

        if (use_specular):
            self.exec_command('directLight.setSpecularIntensity(%s, %s, %s, %s);' % (color[0], color[1], color[2], 1.0))

        self.exec_command('directLight.setAmbientIntensity(%s, %s, %s, %s);' % (color[0], color[1], color[2], 1.0))
        self.exec_command('lightNode.attachLight(directLight);')

    def create_spot_light(self, color, use_diffuse, use_specular, outer_cone, inner_cone):
        self.exec_command('var spotLight = new GVRSpotLight(gvrf);')
        if (use_diffuse):
            self.exec_command('spotLight.setDiffuseIntensity(%s, %s, %s, %s);' % (color[0], color[1], color[2], 1.0))

        if (use_specular):
            self.exec_command('spotLight.setSpecularIntensity(%s, %s, %s, %s);' % (color[0], color[1], color[2], 1.0))

        self.exec_command('spotLight.setAmbientIntensity(%s, %s, %s, %s);' % (color[0], color[1], color[2], 1.0))
        self.exec_command('spotLight.setInnerConeAngle(%s);' % inner_cone)
        self.exec_command('spotLight.setOuterConeAngle(%s);' % outer_cone)
        self.exec_command('lightNode.attachLight(spotLight);')
