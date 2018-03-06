#!/usr/bin/python3

__version__ = '0.0.1'
__all__ = [ 'gvrf_client', 'file_server', 'scene_exporter']

__author__ = 'Sidia'

bl_info = {
    'name': 'Export to GVRf',
    'location': 'Toolshelf > Import-Exportt > Export Scene to GVRf',
    'category': 'Import-Export',
}

if 'bpy' in locals():
    import imp
    imp.reload(gvrf_client.GvrfClient)
    imp.reload(file_server.FileServer)
    imp.reload(scene_exporter)
else:
    from .gvrf_client import GvrfClient
    from .file_server import FileServer
    from . import scene_exporter

import bpy, os
from pathlib import Path


fs = FileServer()
default_dirpath = os.path.join(str(Path.home()), 'GvrfExporterWorkspace')
if not os.path.exists(default_dirpath):
    os.makedirs(default_dirpath)
os.chdir(default_dirpath)

class SceneExporterPanel(bpy.types.Panel):
    bl_space_type = 'VIEW_3D'
    bl_region_type = 'TOOLS'
    bl_context = 'objectmode'
    bl_category = 'Import-Export'
    bl_label = 'Export Scene to GVRf'

    def draw(self, context):
        column = self.layout.column(align = True)
        column.prop(context.scene, 'dirpath')
        column.prop(context.scene, 'clientip')
        column.prop(context.scene, 'globalscale')
        column.prop(context.scene, 'selectedobjects')
        column.operator('export.gvrf', text='Export')
        column.operator('clearscene.gvrf', text='Clear Scene')


class SceneExporter(bpy.types.Operator):
    """Export Scene to GVRf"""
    bl_idname = 'export.gvrf'
    bl_label = 'Export Scene to GVRf'
    bl_options = { 'UNDO' }

    def invoke(self, context, event):
        # IP and port of gvrf client device
        fs.start_server()
        _addr = context.scene.clientip
        _port = 1645
        _selected_objects = context.scene.selectedobjects
        _global_scale = context.scene.globalscale
        scene_exporter.export(GvrfClient(_addr, _port, do_debug = True), selected_objects = _selected_objects, globalscale = _global_scale)

        return { 'FINISHED' }

class SceneClear(bpy.types.Operator):
    """Clear Scene on GVRf"""
    bl_idname = 'clearscene.gvrf'
    bl_label = 'Clear Scene on GVRf'
    bl_options = { 'UNDO' }

    def invoke(self, context, event):
        # IP and port of gvrf client device
        _addr = context.scene.clientip
        _port = 1645

        scene_exporter.clear_scene(GvrfClient(_addr, _port, do_debug = True))

        return { 'FINISHED' }


def change_dir(self, context):
    fs.stop_server()
    os.chdir(context.scene.dirpath)

def register():
    bpy.utils.register_class(SceneExporter)
    bpy.utils.register_class(SceneClear)
    bpy.utils.register_class(SceneExporterPanel)
    bpy.types.Scene.dirpath = bpy.props.StringProperty(name = 'Export Dir', description = '', default = default_dirpath, subtype = 'DIR_PATH', update = change_dir)
    bpy.types.Scene.clientip = bpy.props.StringProperty(name = 'Client\'s IP', description = '', default = '0.0.0.0', subtype = 'NONE')
    bpy.types.Scene.globalscale = bpy.props.FloatProperty(name = 'Global scale', description = '', default = 0.01, min = 0.001, subtype = 'FACTOR')
    bpy.types.Scene.selectedobjects = bpy.props.BoolProperty(name = 'Selected Objects', description = '', default = False)

def unregister():
    bpy.utils.unregister_class(SceneExporter)
    bpy.utils.unregister_class(SceneClear)
    bpy.utils.unregister_class(SceneExporterPanel)
    del bpy.types.Scene.dirpath
    del bpy.types.Scene.clientip
    del bpy.types.Scene.selectedobjects


if __name__ == '__main__':
    register()
