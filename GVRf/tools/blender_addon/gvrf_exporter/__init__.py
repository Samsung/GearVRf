#!/usr/bin/python3

__version__ = '0.0.1'
__all__ = [ 'gvrf_client', 'file_server', ]

__author__ = 'Sidia'

from .gvrf_client import gvrf_client 
from .fileserver import file_server 

def simple_test(addr = "192.168.0.118", port = 1645):
    a = gvrf_client(addr, port, do_debug = True)
    a.exec_command("importPackage(org.gearvrf)")
    a.exec_command("importPackage(org.gearvrf.scene_objects)")
    a.exec_command("var mainScene = gvrf.getMainScene();")
    a.exec_command("var mat = new GVRMaterial(gvrf, GVRMaterial.GVRShaderType.Phong.ID);")
    a.exec_command("mat.setDiffuseColor(1.0, 0.0, 0.0, 1.0);")
    a.exec_command("var cube = new GVRCubeSceneObject(gvrf, true, mat);")
    a.exec_command("cube.getTransform().setPosition(1.0, 1.0, -9.0);")
    a.exec_command("mainScene.addSceneObject(cube);")
    a.disconnect()

def test1():
    fs = file_server()
    a = gvrf_client("192.168.0.118", 1645, do_debug = True)
    a.exec_command("importPackage(org.gearvrf);")
    a.exec_command("var mainScene = gvrf.getMainScene();")
    a.exec_command("var mat = new GVRMaterial(gvrf, GVRMaterial.GVRShaderType.Phong.ID);")
    a.exec_command("mat.setDiffuseColor(1.0, 0.0, 0.0, 1.0);")
    url = "http://" + fs.get_current_ipv4() + ":" + str(fs.get_port())
    url = url + "/SM_SuperGun.FBX"
    a.exec_command('var url = new java.net.URL("' + url + '");')
    a.send_command('var gun = gvrf.getAssetLoader().loadModel(url);')
    fs.handle_request()
    fs.handle_request()
    a.wait_completion()
    a.exec_command("gun.getTransform().setPosition(1.0, 1.0, -9.0);")
    #a.exec_command("mainScene.addSceneObject(gun);")
    #a.exec_command("gun.getRenderData().setMaterial(mat);")
    return a, fs, url

