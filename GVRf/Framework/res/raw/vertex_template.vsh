uniform mat4 u_mvp;
uniform mat4 u_mv;
uniform mat4 u_mv_it;
attribute vec3 a_position;
#ifdef HAS_a_normal
    attribute vec3 a_normal;
#endif
#ifdef HAS_a_texcoord
    attribute vec2 a_texcoord;
#endif
varying vec4 world_position;
varying vec3 world_normal;
varying vec2 diffuse_coord;
void main() {
@VertexShader
#ifdef HAS_VertexSkinShader
@VertexSkinShader
#endif
}