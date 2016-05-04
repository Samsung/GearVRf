Radiance @LightType(Surface s, in Uniform@LightType data)
{
	vec4 lightpos = u_view * vec4(data.world_position.xyz, 1.0);
	vec3 lightdir = lightpos.xyz - viewspace_position;
	// Attenuation
    float distance    = length(lightdir);
    float attenuation = 1.0 / (data.attenuation_constant + data.attenuation_linear * distance + 
    					data.attenuation_quadratic * (distance * distance));  
	
	return Radiance(clamp(data.ambient_intensity.xyz,0.0,1.0),
				    clamp(data.diffuse_intensity.xyz,0.0,1.0),
				    clamp(data.specular_intensity.xyz,0.0,1.0),
				    normalize(lightdir),
					attenuation);

}
