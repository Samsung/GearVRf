precision highp float;
#ifdef HAS_MULTIVIEW
	flat in int view_id;
#endif

out vec4 fragColor;

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

layout (std140) uniform Material_ubo{
        vec4 u_opacity;
        vec4 u_color;
        vec4 ambient_color;
        vec4 diffuse_color;
        vec4 specular_color;
        vec4 emissive_color;
        vec4 specular_exponent;
};
in vec3 viewspace_position;
in vec3 viewspace_normal;
in vec4 local_position;
in vec4 proj_position;
in vec3 view_direction;

in vec2 diffuse_coord;


#ifdef HAS_SHADOWS
uniform sampler2DArray u_shadow_maps;
#endif

struct Radiance
{
   vec3 ambient_intensity;
   vec3 diffuse_intensity;
   vec3 specular_intensity;
   vec3 direction; // view space direction from light to surface
   float attenuation;
};

float unpackFloatFromVec4i(const vec4 value)
{
    const vec4 unpackFactors = vec4(1.0 / (256.0 * 256.0 * 256.0), 1.0 / (256.0 * 256.0), 1.0 / 256.0, 1.0);
    return dot(value, unpackFactors);
}


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
#endif
}
