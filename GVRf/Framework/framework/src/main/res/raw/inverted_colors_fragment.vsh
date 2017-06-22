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
in vec2 diffuse_coord;
out vec4 outColor;

void main() {
  vec4 tex = texture(u_texture, diffuse_coord);
  tex = vec4(1.0-tex.x,1.0-tex.y,1.0-tex.z,1.0-tex.w);
  outColor = tex;
}
