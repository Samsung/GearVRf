precision mediump float;
uniform sampler2D u_texture;
in vec2 v_tex_coord;
out vec4 outColor;

void main() {
  vec4 tex = texture(u_texture, v_tex_coord);
  tex = vec4(1.0-tex.x,1.0-tex.y,1.0-tex.z,1.0-tex.w);
  outColor = tex;
}