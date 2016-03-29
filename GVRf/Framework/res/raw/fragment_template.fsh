uniform mat4 u_view;
varying vec4 viewspace_position;
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

vec3 AddLight(Surface s, Radiance r)
{
	vec3 L = r.direction.xyz;	// From surface to light, unit length, view-space
	float nDotL = max(dot(s.viewspaceNormal, L), 0.0);
	
	vec3 kA = s.ambient.xyz * r.ambient_intensity.xyz + s.emission.xyz;
	vec3 kS = vec3(0, 0, 0);	
	vec3 kD = nDotL * s.diffuse.xyz * r.diffuse_intensity.xyz;
	if (nDotL > 0.0)
	{
		vec3 reflect = normalize(reflect(-L, s.viewspaceNormal));
    	float cosAngle = dot(view_direction, reflect);
    	if (cosAngle > 0.0)
    	{
			kS = s.specular.xyz * r.specular_intensity;
			kS *= pow(cosAngle, specularExponent);
		}
	}
//	return kD;
	return r.attenuation * (kA + kD + kS);
}

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