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

_var_context = 'gvrf'
_var_scene = 'scene'
_var_camera = 'camera'
_var_animator = 'animator'


def import_package():
    return 'importPackage(org.gearvrf);'


def import_package_animation():
    return 'importPackage(org.gearvrf.animation);'


def get_scene():
    return 'var %s = %s.getMainScene();' % (_var_scene, _var_context)


def get_camera():
    return 'var %s = %s.getMainCameraRig();' % (_var_camera, _var_scene)


def clear_scene():
    return '%s.clear();' % _var_scene


def create_url(var_url, url):
    return 'var %s = new java.net.URL("%s");' % (var_url, url)


def load_model_from_url(var_obj, var_url):
    return 'var %s = %s.getAssetLoader().loadModel(%s, %s);' % (var_obj, _var_context, var_url, _var_scene)


def create_scene_obj(var_obj):
    return 'var %s = new GVRSceneObject(%s);' % (var_obj, _var_context)


def remove_scene_obj(obj_name):
    return '%s.removeSceneObjectByName("%s");' % (_var_scene, obj_name)


def set_obj_name(var_obj, obj_name):
    return '%s.setName("%s");' % (var_obj, obj_name)


def set_position(var_obj, x, y, z):
    return '%s.getTransform().setPosition(%s, %s, %s);' % (var_obj, x, y, z)


def set_rotation(var_obj, w, x, y, z):
    return '%s.getTransform().setRotation(%s, %s, %s, %s);' % (var_obj, w, x, y, z)


def add_on_scene(var_obj):
    return '%s.addSceneObject(%s);' % (_var_scene, var_obj)


def create_point_light(var_light):
    return 'var %s = new GVRPointLight(%s);' % (var_light, _var_context)


def create_direct_light(var_light):
    return 'var %s = new GVRDirectLight(%s);' % (var_light, _var_context)


def create_spot_light(var_light):
    return 'var %s = new GVRSpotLight(%s);' % (var_light, _var_context)


def set_diffuse(var_light, r, g, b, a):
    return '%s.setDiffuseIntensity(%s, %s, %s, %s);' % (var_light, r, g, b, a)


def set_specular(var_light, r, g, b, a):
    return '%s.setSpecularIntensity(%s, %s, %s, %s);' % (var_light, r, g, b, a)


def set_ambient(var_light, r, g, b, a):
    return '%s.setAmbientIntensity(%s, %s, %s, %s);' % (var_light, r, g, b, a)


def set_inner_cone(var_light, angle):
    return '%s.setInnerConeAngle(%s);' % (var_light, angle)


def set_outer_cone(var_light, angle):
    return '%s.setOuterConeAngle(%s);' % (var_light, angle)


def attach_light(var_obj, var_light):
    return '%s.attachLight(%s);' % (var_obj, var_light)


def set_near_clipping(near_clipping):
    return '%s.setNearClippingDistance(%s);' % (_var_camera, near_clipping)


def set_far_clipping(far_clipping):
    return '%s.setFarClippingDistance(%s);' % (_var_camera, far_clipping)


def get_animator(var_obj):
    return 'var %s = %s.getComponent(GVRAnimator.getComponentType());' % (_var_animator, var_obj)


def animator_start():
    return '%s.start();' % _var_animator


def animator_set_repeat_mode():
    return '%s.setRepeatMode(GVRRepeatMode.REPEATED);' % _var_animator


def animator_set_repeat_count(repeat_count):
    return '%s.setRepeatCount(%s);' % (_var_animator, repeat_count)
