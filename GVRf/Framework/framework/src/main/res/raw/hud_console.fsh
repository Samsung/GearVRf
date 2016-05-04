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
 

precision mediump float;

uniform sampler2D u_texture;
uniform sampler2D u_overlay;

varying vec2 v_scene_coord;
varying vec2 v_overlay_coord;

void main() {
  vec4 rendered = texture2D(u_texture, v_scene_coord);
  vec4 overlay = texture2D(u_overlay, v_overlay_coord); 

  vec3 sum = rendered.rgb + overlay.rgb;

  gl_FragColor = vec4(sum, rendered.a);
}
