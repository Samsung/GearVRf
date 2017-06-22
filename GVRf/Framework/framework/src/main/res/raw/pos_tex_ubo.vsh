in vec3 a_position;
in vec2 a_texcoord;

@MATRIX_UNIFORMS

out vec2 diffuse_coord;

void main()
{
    diffuse_coord = a_texcoord;
    gl_Position = u_mvp * vec4(a_position, 1.0);
}