precision highp float;

layout (std140) uniform Material_ubo{
    vec4 u_opacity;
    vec4 u_color;
    vec4 ambient_color;
    vec4 diffuse_color;
    vec4 specular_color;
    vec4 emissive_color;
    vec4 specular_exponent;
    vec4 line_width;
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

//uniform mat4 u_model;
in vec3 viewspace_position;
in vec3 viewspace_normal;
in vec4 local_position;
in vec4 proj_position;
in vec3 view_direction;
in vec2 diffuse_coord;
out vec4 fragColor;

#ifdef HAS_ambientTexture
out vec2 ambient_coord;
#endif

#ifdef HAS_specularTexture
out vec2 specular_coord;
#endif

#ifdef HAS_emissiveTexture
out vec2 emissive_coord;
#endif

#ifdef HAS_normalTexture
out vec2 normal_coord;
#endif

#ifdef HAS_SHADOWS
uniform sampler2DArray u_shadow_maps;

float unpackFloatFromVec4i(const vec4 value)
{
    const vec4 bitSh = vec4(1.0 / (256.0 * 256.0 * 256.0), 1.0 / (256.0 * 256.0), 1.0 / 256.0, 1.0);
    const vec4 unpackFactors = vec4( 1.0 / (256.0 * 256.0 * 256.0), 1.0 / (256.0 * 256.0), 1.0 / 256.0, 1.0 );
    return dot(value,unpackFactors);
}

#endif

struct Radiance
{
   vec3 ambient_intensity;
   vec3 diffuse_intensity;
   vec3 specular_intensity;
   vec3 direction; // view space direction from light to surface
   float attenuation;
};

@FragmentSurface

@FragmentAddLight

@LIGHTSOURCES

void main()
{
	Surface s = @ShaderName();
#if defined(HAS_LIGHTSOURCES)
    vec4 color = LightPixel(s);
	color = clamp(color, vec4(0), vec4(1));
	fragColor = color;
#else
	fragColor = s.diffuse;
	//fragColor = vec4(1,0,1,1);
#endif
}
