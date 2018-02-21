#!/usr/bin/python3

__version__ = '0.0.1'
__all__ = [ 'gvrf_client', 'file_server', 'scene_exporter']

__author__ = 'Sidia'

bl_info = {
    "name": "Export to GVRf",
    "location": "File > Export > GVRf Client",
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

default_dirpath = str(Path.home()) + "\\GvrfExporterWorkspace"
if not os.path.exists(default_dirpath):
    os.makedirs(default_dirpath)
os.chdir(default_dirpath)

class SceneExporterPanel(bpy.types.Panel):
    bl_space_type = "VIEW_3D"
    bl_region_type = "TOOLS"
    bl_context = "objectmode"
    bl_category = "Import-Export"
    bl_label = "Export Scene to GVRf"

    def draw(self, context):
        TheCol = self.layout.column(align = True)
        TheCol.prop(context.scene, "dirpath")
        TheCol.prop(context.scene, "clientip")
        TheCol.operator("export.gvrf", text="Export")


class SceneExporter(bpy.types.Operator):
    """Export Scene to GVRf"""
    bl_idname = "export.gvrf"
    bl_label = "Export Scene to GVRf"
    bl_options = {'UNDO'}

    def invoke(self, context, event):
        fs = file_server.file_server()
        t = threading.Thread(target = fs.start_server)
        t.start()

        # IP and port of gvrf client device
        _addr = context.scene.clientip
        _port = 1645
        scene_exporter.export(gvrf_client.gvrf_client(_addr, _port, do_debug = True))

        fs.stop_server()
        t.join()
        
        
        return {'FINISHED'}


def change_dir(self, context):
    print(context.scene.dirpath)
    os.chdir(context.scene.dirpath)

def register():
    bpy.utils.register_class(SceneExporter)
    bpy.utils.register_class(SceneExporterPanel)
    bpy.types.Scene.dirpath = bpy.props.StringProperty(name="Export Dir", description="", default=default_dirpath, subtype='DIR_PATH', update=change_dir)
    bpy.types.Scene.clientip = bpy.props.StringProperty(name="Client's IP", description="", default="0.0.0.0", subtype='NONE')


def unregister():
    bpy.utils.unregister_class(SceneExporter)
    bpy.utils.unregister_class(SceneExporterPanel)
    del bpy.types.Scene.dirpath
    del bpy.types.Scene.clientip


if __name__ == "__main__":
    register()