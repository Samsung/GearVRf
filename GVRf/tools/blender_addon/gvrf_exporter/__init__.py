#!/usr/bin/python3

__version__ = '0.0.1'
__all__ = [ 'gvrf_client', 'file_server', 'scene_exporter']

__author__ = 'Sidia'

bl_info = {
    'name': 'Export to GVRf',
    'location': 'File > Export > GVRf Client',
    'category': 'Import-Export',
}

if 'bpy' in locals():
    import imp
    imp.reload(gvrf_client)
    imp.reload(file_server)
    imp.reload(scene_exporter)
else:
    from . import gvrf_client, file_server, scene_exporter

import bpy, os, socket, requests
from pathlib import Path

default_dirpath = str(Path.home()) + '\\GvrfExporterWorkspace'
if not os.path.exists(default_dirpath):
    os.makedirs(default_dirpath)
os.chdir(default_dirpath)

fs = None

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
        column.operator('export.gvrf', text='Export')
        column.operator('clearscene.gvrf', text='Clear Scene')


class SceneExporter(bpy.types.Operator):
    """Export Scene to GVRf"""
    bl_idname = 'export.gvrf'
    bl_label = 'Export Scene to GVRf'
    bl_options = { 'UNDO' }

    def invoke(self, context, event):
        # IP and port of gvrf client device
        create_server()
        _addr = context.scene.clientip
        _port = 1645
        scene_exporter.export(gvrf_client.GvrfClient(_addr, _port, do_debug = True))

        
        return {'FINISHED'}

class SceneClear(bpy.types.Operator):
    """Clear Scene on GVRf"""
    bl_idname = 'clearscene.gvrf'
    bl_label = 'Clear Scene on GVRf'
    bl_options = { 'UNDO' }

    def invoke(self, context, event):
        # IP and port of gvrf client device
        _addr = context.scene.clientip
        _port = 1645
        scene_exporter.clear_scene(gvrf_client.GvrfClient(_addr, _port, do_debug = True))

        
        return {'FINISHED'}

def check_running_server():
    url = 'http://' + socket.gethostbyname_ex(socket.getfqdn(''))[2][0] + ':8000/'
    try:
        requests.get(url)
        return True
    except:
        return False

def create_server():
    global fs
    if (not check_running_server()):
        fs = file_server.FileServer()
        fs.start_server()

def end_server():
    global fs
    if (check_running_server()):
        try:
            fs.stop_server()
        except:
            print('Can\'t close the server')

def change_dir(self, context):
    end_server()
    os.chdir(context.scene.dirpath)

def register():
    bpy.utils.register_class(SceneExporter)
    bpy.utils.register_class(SceneClear)
    bpy.utils.register_class(SceneExporterPanel)
    bpy.types.Scene.dirpath = bpy.props.StringProperty(name='Export Dir', description='', default=default_dirpath, subtype='DIR_PATH', update=change_dir)
    bpy.types.Scene.clientip = bpy.props.StringProperty(name='Client\'s IP', description='', default='0.0.0.0', subtype='NONE')


def unregister():
    bpy.utils.unregister_class(SceneExporter)
    bpy.utils.unregister_class(SceneClear)
    bpy.utils.unregister_class(SceneExporterPanel)
    del bpy.types.Scene.dirpath
    del bpy.types.Scene.clientip


if __name__ == '__main__':
    register()