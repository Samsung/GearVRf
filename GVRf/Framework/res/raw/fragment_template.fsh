uniform mat4 u_view;
varying vec3 viewspace_position;
varying vec3 viewspace_normal;
varying vec2 diffuse_coord;
varying vec3 view_direction;
uniform vec3 u_eyepos;

struct Radiance
{
   vec3 ambient_intensity;
   vec3 diffuse_intensity;
   vec3 specular_intensity;
   vec3 direction; // view space direction from light to surface
   float attenuation;
};

@FragmentSurface


precision mediump float;

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
	gl_FragColor = vec4(color.x, color.y, color.z, 1.0);
}