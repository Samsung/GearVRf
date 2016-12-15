
precision highp float;
uniform samplerCube u_texture;
layout (std140) uniform Material_ubo{
    vec4 u_opacity;
    vec4 u_color;
};
layout (std140) uniform Transform_ubo{
 #ifdef HAS_MULTIVIEW
     mat4 u_view_[2];
     mat4 u_mvp_[2];
     mat4 u_mv_[2];
     mat4 u_mv_it_[2];
 #else
     mat4 u_view;
     mat4 u_mvp;
     mat4 u_mv;
     mat4 u_mv_it;
 #endif
     mat4 u_model;
     mat4 u_view_i;
     vec4 u_right;
};

        in vec3 v_viewspace_position;
        in vec3 v_viewspace_normal;
        void main()
        {
          vec3 v_reflected_position = reflect(v_viewspace_position, normalize(v_viewspace_normal));
          vec3 v_tex_coord = (u_view_i * vec4(v_reflected_position, 1.0)).xyz;
          v_tex_coord.z = -v_tex_coord.z;
          vec4 color = texture(u_texture, v_tex_coord.xyz);
          gl_FragColor = vec4(color.r * u_color.r * u_opacity.x, color.g * u_color.g * u_opacity.x, color.b * u_color.b * u_opacity.x, color.a * u_opacity.x);
        }
