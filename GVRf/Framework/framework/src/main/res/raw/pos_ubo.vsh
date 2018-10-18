#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
layout(location = 0) in vec3 a_position;

@MATRIX_UNIFORMS

void main()
{
    mat4 mvp = u_mvp;

    //generate right eye mvp from left
    mvp[3][0] = mvp[3][0] - (u_proj_offset * float(u_right));

    vec4 pos = vec4(a_position, 1.0);
    gl_Position = mvp * pos;
}