
varying vec4 world_position;
varying vec3 world_normal;
varying vec2 diffuse_coord;
varying vec3 view_direction;
uniform vec3 u_eyepos;

struct Radiance
{
   vec3 ambient_intensity;
   vec3 diffuse_intensity;
   vec3 specular_intensity;
   vec3 world_direction;
   vec3 reflection;
};

@FragmentSurface

vec3 AddLight(Surface s, Radiance r)
{
	vec3 L = r.world_direction.xyz;	// From surface towards light, unit length, world-space
    float specular = dot(view_direction, r.reflection);
	vec3 kA = s.ambient.xyz * r.ambient_intensity.xyz;
	vec3 kD = s.diffuse.xyz * r.diffuse_intensity.xyz;
	vec3 kS = vec3(0.0, 0.0, 0.0);
	vec3 kE = s.emission.xyz;
	if ((specular > 0.0) && (specularExponent > 0.0))
	{
		kS = s.specular.xyz * r.specular_intensity.xyz;	
		kS *= pow(specular, specularExponent);
	}
	return kA + kD + kS + kE;
}

@LIGHTSOURCES

void main()
{
	vec3 color = vec3(0, 0, 0);
	Surface s = @ShaderName();
#if defined(HAS_LIGHTSOURCES)
	color = LightPixel(s);
#else
	color = (s.diffuse + s.specular + s.ambient + s.emission).xyz;
#endif
	gl_FragColor = vec4(color.x, color.y, color.z, 1.0);
}