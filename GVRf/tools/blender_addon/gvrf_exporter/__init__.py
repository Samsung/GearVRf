#!/usr/bin/python3

__version__ = '0.0.1'
__all__ = [ 'gvrf_client', 'file_server', 'scene_exporter']

__author__ = 'Sidia'

bl_info = {
    "name": "Export to GVRf",
    "category": "Import-Export",
}

if "bpy" in locals():
    import imp
    imp.reload(gvrf_client)
    imp.reload(file_server)
    imp.reload(scene_exporter)
    print("Reloaded multifiles")
else:
    from . import gvrf_client, file_server, scene_exporter
    print("Imported multifiles")

import bpy, os, threading
from pathlib import Path

dirpath = str(Path.home()) + "\\GvrfExporterWorkspace"
if not os.path.exists(dirpath):
    os.makedirs(dirpath)
os.chdir(dirpath)


class SceneExporter(bpy.types.Operator):
    """Export Scene to GVRf"""
    bl_idname = "export.gvrf"
    bl_label = "Export Scene to GVRf"
    bl_options = {'REGISTER', 'UNDO'}

    def execute(self, context):
        fs = file_server.file_server()
        t = threading.Thread(target = fs.start_server)
        t.start()
        

        # IP and port of gvrf client device
        _addr = "192.168.0.99"
        _port = 1645
        scene_exporter.export(gvrf_client.gvrf_client(_addr, _port, do_debug = True))

        fs.stop_server()
        t.join()
        
        
        return {'FINISHED'}


def register():
    bpy.utils.register_module(__name__)


def unregister():
    bpy.utils.unregister_module(__name__)


if __name__ == "__main__":
    register()