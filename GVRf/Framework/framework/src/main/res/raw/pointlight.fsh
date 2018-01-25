Radiance @LightType(Surface s, in U@LightType data, int index)
{
#ifdef HAS_MULTIVIEW
	vec4 lightpos = u_view_[gl_ViewID_OVR] * data.world_position;
#else
    vec4 lightpos = u_view * data.world_position;
#endif

	vec3 lightdir = lightpos.xyz - viewspace_position;
	// Attenuation
    float distance    = length(lightdir);
    float attenuation = 1.0 / (data.attenuation_constant + data.attenuation_linear * distance + 
    					data.attenuation_quadratic * (distance * distance));  
	
	return Radiance(clamp(data.ambient_intensity.xyz, 0.0, 1.0),
				    clamp(data.diffuse_intensity.xyz, 0.0, 1.0),
				    clamp(data.specular_intensity.xyz, 0.0, 1.0),
				    normalize(lightdir),
					attenuation);

}
