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
uniform vec3 u_color;
varying vec2 v_tex_coord;
void main() {
  vec4 tex = texture2D(u_texture, v_tex_coord);
  float r = tex.r * u_color.r; 
  float g = tex.g * u_color.g;
  float b = tex.b * u_color.b;
  vec3 color = vec3(r, g, b);
  gl_FragColor = vec4(color, tex.a);
}
