# Copyright 2015 Samsung Electronics Co., LTD
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

__version__ = '0.0.1'
__all__ = ['gvrf_client', 'file_server', 'scene_exporter']

__author__ = 'Sidia'

bl_info = {
    'name': 'Export to GVRf',
    'location': 'Toolshelf > Import-Exportt > Export Scene to GVRf',
    'category': 'Import-Export',
}

if 'bpy' in locals():
    import importlib
    importlib.reload(gvrf_client.GvrfClient)
    importlib.reload(file_server.FileServer)
    importlib.reload(scene_exporter)
else:
    from .gvrf_client import GvrfClient
    from .file_server import FileServer
    from . import scene_exporter

import bpy
import os
from pathlib import Path


fs = FileServer()
default_dirpath = os.path.join(str(Path.home()), 'GvrfExporterWorkspace')


class SceneExporterPanel(bpy.types.Panel):
    bl_space_type = 'VIEW_3D'
    bl_region_type = 'TOOLS'
    bl_context = 'objectmode'
    bl_category = 'Import-Export'
    bl_label = 'Export Scene to GVRf'

    def draw(self, context):
        column = self.layout.column(align=True)
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
    bl_options = {'UNDO'}

    def invoke(self, context, event):
        # IP and port of gvrf client device
        create_default_dir(context)
        fs.start_server()
        _addr = context.scene.clientip
        _port = 1645
        _selected_objects = context.scene.selectedobjects
        _global_scale = context.scene.globalscale

        try:
            scene_exporter.export(
                GvrfClient(_addr, _port, do_debug=True), fs.get_url(), selected_objects=_selected_objects,
                globalscale=_global_scale)
        except TimeoutError:
            self.report(
                {'ERROR'}, 'Connection attempt failed. Check the clinet\'s IP address and if the gvr-remote-scripting'
                + 'application is running.')

        return {'FINISHED'}


class SceneClear(bpy.types.Operator):
    """Clear Scene on GVRf"""
    bl_idname = 'clearscene.gvrf'
    bl_label = 'Clear Scene on GVRf'
    bl_options = {'UNDO'}

    def invoke(self, context, event):
        # IP and port of gvrf client device
        _addr = context.scene.clientip
        _port = 1645

        try:
            scene_exporter.clear_scene(GvrfClient(_addr, _port, do_debug=True))
        except TimeoutError:
            self.report(
                {'ERROR'}, 'Connection attempt failed. Check the clinet\'s IP address and if the gvr-remote-scripting'
                + 'application is running.')

        return {'FINISHED'}


def create_default_dir(context):
    if context.scene.dirpath != default_dirpath:
        os.chdir(context.scene.dirpath)
        return

    if not os.path.exists(default_dirpath):
        os.makedirs(default_dirpath)
    os.chdir(default_dirpath)


def change_dir(self, context):
    fs.stop_server()
    os.chdir(context.scene.dirpath)


def register():
    bpy.utils.register_class(SceneExporterPanel)
    bpy.utils.register_class(SceneExporter)
    bpy.utils.register_class(SceneClear)
    bpy.types.Scene.dirpath = bpy.props.StringProperty(
        name='Export Dir', description='', default=default_dirpath, subtype='DIR_PATH', update=change_dir)

    bpy.types.Scene.clientip = bpy.props.StringProperty(
        name='Client\'s IP', description='', default='0.0.0.0', subtype='NONE')

    bpy.types.Scene.globalscale = bpy.props.FloatProperty(
        name='Global scale', description='', default=0.01, min=0.001, subtype='FACTOR')

    bpy.types.Scene.selectedobjects = bpy.props.BoolProperty(
        name='Selected Objects', description='', default=False)


def unregister():
    bpy.utils.unregister_class(SceneExporterPanel)
    bpy.utils.unregister_class(SceneExporter)
    bpy.utils.unregister_class(SceneClear)
    del bpy.types.Scene.dirpath
    del bpy.types.Scene.clientip
    del bpy.types.Scene.selectedobjects


if __name__ == '__main__':
    register()
