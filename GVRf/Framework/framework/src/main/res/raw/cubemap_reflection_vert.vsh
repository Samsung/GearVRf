#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

precision highp float;
layout(location = 0) in vec3 a_position;
layout(location = 1) in vec3 a_normal;

@MATRIX_UNIFORMS

layout(location = 1) out vec3 viewspace_position;
layout(location = 2) out vec3 viewspace_normal;

#ifdef HAS_MULTIVIEW
    #define u_modelview_it u_matrices[u_matrix_offset + gl_ViewID_OVR + uint(3)]
#else
    #define u_modelview_it u_matrices[u_matrix_offset + u_right + uint(3)]
#endif

void main()
{
    mat4 mvp = u_mvp;
    mat4 mv = u_view * u_model;

#ifdef HAS_MULTIVIEW
    bool render_mask = (u_render_mask & (gl_ViewID_OVR + uint(1))) > uint(0) ? true : false;
    mvp[3][0] = mvp[3][0] - (u_proj_offset * float(gl_ViewID_OVR));
    mvp = mvp * float(render_mask);
#else
    mvp = u_mvp;
    //generate right eye mvp from left
    mvp[3][0] = mvp[3][0] - (u_proj_offset * float(u_right));
    mv  = u_mv;
#endif
    vec4 v_viewspace_position_vec4 = mv * vec4(a_position,1.0);
    viewspace_position = v_viewspace_position_vec4.xyz / v_viewspace_position_vec4.w;
    viewspace_normal = (u_modelview_it * vec4(a_normal, 1.0)).xyz;
    gl_Position = mvp * vec4(a_position, 1.0);
}