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
uniform vec3 u_ratio_r;
uniform vec3 u_ratio_g;
uniform vec3 u_ratio_b;
varying vec2 v_tex_coord;
void main() {
  vec4 tex = texture2D(u_texture, v_tex_coord);
  float r = tex.r * u_ratio_r.r + tex.g * u_ratio_r.g + tex.b * u_ratio_r.b;
  float g = tex.r * u_ratio_g.r + tex.g * u_ratio_g.g + tex.b * u_ratio_g.b;
  float b = tex.r * u_ratio_b.r + tex.g * u_ratio_b.g + tex.b * u_ratio_b.b;
  vec3 color = vec3(r, g, b);
  gl_FragColor = vec4(color, tex.a);
}
