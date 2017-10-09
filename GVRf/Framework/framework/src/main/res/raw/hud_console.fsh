// Copyright 2015 Samsung Electronics Co., LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
 
#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
#endif
precision mediump float;
#ifdef HAS_MULTIVIEW
uniform sampler2DArray u_texture;
#else
uniform sampler2D u_texture;
#endif
uniform sampler2D u_overlay;

in vec2 diffuse_coord;
in vec2 v_overlay_coord;
out vec4 OutColor;

void main() {

#ifdef HAS_MULTIVIEW
  vec3 tex_cord = vec3(diffuse_coord, float(gl_ViewID_OVR));
  vec4 rendered = texture(u_texture, tex_cord);
#else
    vec4 rendered = texture(u_texture, diffuse_coord);
#endif

  vec4 overlay = texture(u_overlay, v_overlay_coord);
  OutColor = mix(rendered, overlay, overlay.a);
}
