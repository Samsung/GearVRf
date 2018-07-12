#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
#extension GL_OES_EGL_image_external : enable
#extension GL_OES_EGL_image_external_essl3 : enable
precision highp float;
uniform samplerExternalOES u_texture;

@MATERIAL_UNIFORMS

out vec4 outColor;

layout ( location = 0 ) in vec2 textureCoordinate;
layout ( location = 1 ) in vec2 leftTextureCoordinate;
layout ( location = 2 ) in vec2 rightTextureCoordinate;

layout ( location = 3 ) in vec2 topTextureCoordinate;
layout ( location = 4 ) in vec2 topLeftTextureCoordinate;
layout ( location = 5 ) in vec2 topRightTextureCoordinate;

layout ( location = 6 ) in vec2 bottomTextureCoordinate;
layout ( location = 7 ) in vec2 bottomLeftTextureCoordinate;
layout ( location = 8 ) in vec2 bottomRightTextureCoordinate;

mediump mat3 matrix = mat3(
            0.25, 0.5, 0.25,
            0.5,  1.0, 0.5,
            0.25, 0.5, 0.25
        );

void main()
{
    mediump vec4 bottomColor = texture(u_texture, bottomTextureCoordinate);
    mediump vec4 bottomLeftColor = texture(u_texture, bottomLeftTextureCoordinate);
    mediump vec4 bottomRightColor = texture(u_texture, bottomRightTextureCoordinate);
    mediump vec4 centerColor = texture(u_texture, textureCoordinate);
    mediump vec4 leftColor = texture(u_texture, leftTextureCoordinate);
    mediump vec4 rightColor = texture(u_texture, rightTextureCoordinate);
    mediump vec4 topColor = texture(u_texture, topTextureCoordinate);
    mediump vec4 topRightColor = texture(u_texture, topRightTextureCoordinate);
    mediump vec4 topLeftColor = texture(u_texture, topLeftTextureCoordinate);

    mediump vec4 color = topLeftColor * matrix[0][0] + topColor * matrix[0][1] + topRightColor * matrix[0][2];
    color += leftColor * matrix[1][0] + centerColor * matrix[1][1] + rightColor * matrix[1][2];
    color += bottomLeftColor * matrix[2][0] + bottomColor * matrix[2][1] + bottomRightColor * matrix[2][2];
    color /= 4.0; // 4.0 is the sum of matrix values

    outColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);
}
