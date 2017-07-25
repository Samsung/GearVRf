precision highp float;

layout (location = 0) in vec2 diffuse_coord;
layout (set = 0, binding = 4) uniform sampler2D y_texture;
layout (set = 0, binding = 5) uniform sampler2D uv_texture;
layout (location = 0) out vec4 fragColor;

void main (void) {
  float r, g, b, y, u, v;
  y = texture(y_texture, v_texCoord).r;
  vec4 texColor = texture(uv_texture,v_texCoord);
  u = texColor.a - 0.5;
  v = texColor.r - 0.5;
  r = y + 1.13983*v;
  g = y - 0.39465*u - 0.58060*v;
  b = y + 2.03211*u;
  fragColor = vec4(r, g, b, 1.0);
}
