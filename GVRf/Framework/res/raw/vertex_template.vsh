uniform mat4 u_mvp;
uniform mat4 u_mv;
uniform mat4 u_mv_it;
uniform vec3 u_eye_pos;

attribute vec3 a_position;
#ifdef HAS_a_normal
    attribute vec3 a_normal;
#endif
#ifdef HAS_a_texcoord
    attribute vec2 a_texcoord;
#endif
#ifdef HAS_a_tangent
	attribute vec3 a_tangent;
	attribute vec3 a_bitangent;
#endif

varying vec2 diffuse_coord;
varying vec3 view_direction;
varying vec3 viewspace_position;
varying vec3 viewspace_normal;

void main() {
@VertexShader
#ifdef HAS_VertexSkinShader
   @VertexSkinShader
#endif
}