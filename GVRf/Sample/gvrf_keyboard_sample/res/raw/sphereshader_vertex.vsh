attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_tex_coord;
uniform mat4 u_mvp;
uniform vec3 eye;
uniform vec3 u_light;
varying vec3 normal;
varying vec3 view;
varying vec3 light;
varying vec2 coord;
varying vec3  n;
varying vec3  v;
varying vec3  l;
varying vec3  p;

void main() {

	vec4 pos = u_mvp * a_position;
    normal = a_normal;
	view  = eye - pos.xyz;
	light = u_light;
	coord = a_tex_coord;
	n = normalize(normal);
	v = normalize(view);
    l = normalize(light);
    p = pos.xyz;
    gl_Position = pos;
    
}
