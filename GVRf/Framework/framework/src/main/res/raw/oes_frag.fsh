            #version 300 es
            #extension GL_OES_EGL_image_external : require
            precision highp float;
            uniform samplerExternalOES u_texture;
 layout (std140) uniform Material_ubo{
     vec4 u_opacity;
     vec4 u_color;
 };
             in vec2 diffuse_coord;
             out vec4 outColor;
            void main()
            {
              vec4 color = texture(u_texture, diffuse_coord);
              outColor = vec4(color.r * u_color.r * u_opacity.x, color.g * u_color.g * u_opacity.x, color.b * u_color.b * u_opacity.x, color.a * u_opacity.x);
            }
