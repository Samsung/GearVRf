precision mediump float;

uniform mat4 u_mvp;
in vec3 a_position;
in vec4 a_color;
out vec4 v_color;

void main() {
  gl_Position = u_mvp * vec4(a_position, 1.0);
  v_color = a_color;
}
