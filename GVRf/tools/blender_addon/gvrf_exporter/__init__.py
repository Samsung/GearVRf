#!/usr/bin/python3

__version__ = '0.0.1'
__all__ = [ 'gvrf_client', ]

__author__ = 'Sidia'

from .gvrf_client import gvrf_client 

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

