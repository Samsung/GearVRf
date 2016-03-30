Radiance @LightType(Surface s, in Struct@LightType data)
{
	vec4 L = u_view * vec4(data.world_direction.xyz, 0.0);
	vec4 lightpos = u_view * vec4(data.world_position.xyz, 1.0);
	
		// Attenuation
    float distance    = length(lightpos - viewspace_position);
    float attenuation = 1.0 / (data.attenuation_constant + data.attenuation_linear * distance + 
    data.attenuation_quadratic * (distance * distance));
    
	return Radiance(data.ambient_intensity.xyz,
					data.diffuse_intensity.xyz,
					data.specular_intensity.xyz,
					normalize(-L.xyz),
					attenuation);
}
