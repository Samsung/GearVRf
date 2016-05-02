precision highp float;
precision highp sampler2DArray;

uniform mat4 u_view;
uniform mat4 u_model;
in vec3 viewspace_position;
in vec3 viewspace_normal;
in vec4 local_position;
in vec4 proj_position;
in vec2 diffuse_coord;
in vec3 view_direction;
out vec4 fragColor;

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
	vec3 color = vec3(0, 0, 0);
	Surface s = @ShaderName();
#if defined(HAS_LIGHTSOURCES)
	color = LightPixel(s);
	color = clamp(color, vec3(0), vec3(1));
#else
	color = s.diffuse.xyz;
#endif
	fragColor = vec4(color.x, color.y, color.z, 1.0);
}
