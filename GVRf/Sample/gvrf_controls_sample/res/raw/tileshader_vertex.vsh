attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_tex_coord;
uniform mat4 u_mvp;
varying vec2 coord;

void main() {

	vec4 pos = u_mvp * a_position;
	coord = a_tex_coord;
    gl_Position = pos;
    
}
