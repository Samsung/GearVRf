precision mediump float;

uniform sampler2D u_texture;
uniform sampler2D u_overlay;

in vec2 v_scene_coord;
in vec2 v_overlay_coord;
out vec4 outColor;

void main() {
  vec4 rendered = texture(u_texture, v_scene_coord);
  vec4 overlay = texture(u_overlay, v_overlay_coord);

  outColor = mix(rendered, overlay, overlay.a);
}
