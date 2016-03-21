
varying vec4 world_position;
varying vec3 world_normal;
varying vec2 diffuse_coord;
uniform vec3 u_eyepos;

struct Radiance
{
   vec3 diffuse_intensity;
   vec3 specular_intensity;
   vec3 world_direction;
};

@FragmentSurface

vec3 AddLight(Surface s, Radiance r)
{
	vec3 L = r.world_direction.xyz;	// From surface towards light, unit length, world-space

	// Vector from surface location to the camera's eye
	vec3 E = normalize(u_eyepos - world_position.xyz);
	vec3 H = normalize(L + E);
	vec3 kD = s.diffuse.xyz * r.diffuse_intensity.xyz;
	vec3 kS = s.specular.xyz * r.specular_intensity.xyz;
	if (specularExponent > 0.0f)
		kS *= pow(max(dot(world_normal, H), 0.0), specularExponent);
	return kD + kS;
}

@LIGHTSOURCES

void main()
{
	vec3 color = vec3(0, 0, 0);
	Surface s = @ShaderName();
#ifdef HAS_LIGHTSOURCES
	color = LightPixel(s);
#else
	color = s.diffuse.xyz;
#endif
	gl_FragColor = vec4(color, 1.0);
}