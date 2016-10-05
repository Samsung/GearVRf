precision highp float;
precision highp sampler2DArray;

#ifdef HAS_MULTIVIEW
	flat in int view_id;
	uniform mat4 u_view_[2];
#else
    uniform mat4 u_view; 
#endif

out vec4 fragColor;

uniform mat4 u_model;

in vec3 viewspace_position;
in vec3 viewspace_normal;
in vec4 local_position;
in vec4 proj_position;
in vec3 view_direction;

in vec2 diffuse_coord;

#ifdef HAS_ambientTexture
in vec2 ambient_coord;
#endif

#ifdef HAS_opacityTexture
in vec2 opacity_coord;
#endif

#ifdef HAS_specularTexture
in vec2 specular_coord;
#endif

#ifdef HAS_emissiveTexture
in vec2 emissive_coord;
#endif

#ifdef HAS_normalTexture
in vec2 normal_coord;
#endif

#ifdef HAS_lightMapTexture
in vec2 lightmap_coord;
#endif

#ifdef HAS_ambientTexture1
in vec2 ambient_coord1;
#endif

#ifdef HAS_diffuseTexture1
in vec2 diffuse_coord1;
#endif

#ifdef HAS_specularTexture1
in vec2 specular_coord1;
#endif

#ifdef HAS_emissiveTexture1
in vec2 emissive_coord1;
#endif

#ifdef HAS_normalTexture1
in vec2 normal_coord1;
#endif

#ifdef HAS_lightMapTexture1
in vec2 lightmap_coord1;
#endif

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
