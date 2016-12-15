            #version 300 es
            #extension GL_OES_EGL_image_external : require
            precision highp float;
            uniform samplerExternalOES u_texture;
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
            in vec2 diffuse_coord;
            void main()
            {
              vec2 tex_coord = vec2(diffuse_coord.x, 0.5 * (diffuse_coord.y + float(u_right.x)));
              vec4 color = texture2D(u_texture, tex_coord);
              gl_FragColor = vec4(color.r * u_color.r * u_opacity.x, color.g * u_color.g * u_opacity.x, color.b * u_color.b * u_opacity.x, color.a * u_opacity.x);
            }
